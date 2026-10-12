#version 330

// ARMOR_MATERIAL_REFLECTION_SHADER_V0316

out vec4 diffuseOut;

uniform sampler2D tex0; // diffuse
uniform sampler2D tex1; // normals
uniform sampler2D tex2; // depth
uniform sampler2D tex3; // specular, shininess, emissive/reflection marker
uniform sampler2D tex4; // shadow map
uniform sampler2D tex5; // ssao map
uniform sampler2D tex6; // occlusion map

in vec2 _texcoord;

uniform vec4 sunLight[4];
uniform vec4 playerpos;
uniform mat4 invProjView;
uniform mat4 shadowproj;
uniform mat4 shadowview;
uniform vec4 shadowparams;
uniform mat4 skyProjView;
uniform vec4 armorReflectionTuning0;
uniform vec4 armorReflectionTuning1;
uniform vec4 armorReflectionTuning2;
uniform vec4 armorReflectionTuning3;

vec3 getWorldPosAt(vec2 texcoord, float depth)
{
    vec4 screenSpacePosition = vec4(
            texcoord * 2.0 - 1.0,
            depth * 2.0 - 1.0,
            1.0);
    vec4 worldSpacePosition = invProjView * screenSpacePosition;
    return worldSpacePosition.xyz / worldSpacePosition.w;
}

vec3 getWorldPos(float depth)
{
    return getWorldPosAt(_texcoord, depth);
}

vec3 environmentReflection(vec3 direction)
{
    vec3 ambientLight = max(sunLight[2].xyz, vec3(0.0));
    vec3 directLight = max(sunLight[1].xyz, vec3(0.0));

    vec3 horizon = (ambientLight * 1.8 + directLight * 0.10)
            * armorReflectionTuning2.y;
    vec3 zenith = (ambientLight * 1.25
            + directLight * vec3(0.05, 0.09, 0.16))
            * armorReflectionTuning2.y;
    vec3 ground = ambientLight * 0.35
            * armorReflectionTuning2.z;

    float upward = clamp(direction.y, 0.0, 1.0);
    float downward = clamp(-direction.y, 0.0, 1.0);
    vec3 result = direction.y >= 0.0
            ? mix(horizon, zenith, smoothstep(0.0, 1.0, upward))
            : mix(horizon * 0.45, ground, smoothstep(0.0, 1.0, downward));

    vec3 lightDirection = normalize(sunLight[0].xyz);
    float sunGlint = pow(max(dot(direction, lightDirection), 0.0), 160.0);
    return (result + directLight * sunGlint * armorReflectionTuning3.x)
            * armorReflectionTuning1.z;
}

vec3 sampleReflectedAlbedo(vec2 uv, float armorSurface)
{
    vec3 center = texture2D(tex0, uv).rgb;
    if (armorSurface < 0.5)
        return center;

    // Five taps remove the readable mirror image from rough armour while
    // retaining its environment colour and movement. Weapons/tools keep the
    // original single sharp sample.
    vec2 pixel = 1.0 / vec2(textureSize(tex0, 0));
    vec2 radius = pixel * 5.0;
    return center * 0.40
            + texture2D(tex0, clamp(uv + vec2(radius.x, 0.0),
                    vec2(0.002), vec2(0.998))).rgb * 0.15
            + texture2D(tex0, clamp(uv - vec2(radius.x, 0.0),
                    vec2(0.002), vec2(0.998))).rgb * 0.15
            + texture2D(tex0, clamp(uv + vec2(0.0, radius.y),
                    vec2(0.002), vec2(0.998))).rgb * 0.15
            + texture2D(tex0, clamp(uv - vec2(0.0, radius.y),
                    vec2(0.002), vec2(0.998))).rgb * 0.15;
}

vec3 traceScreenReflection(
        vec3 origin,
        vec3 direction,
        float armorSurface,
        out float confidence)
{
    confidence = 0.0;
    mat4 projView = inverse(invProjView);
    float travel = 0.16;

    // Keep the deferred sunlight pass bounded. Earlier 24+8 sample versions
    // could stall the old Wurm/LWJGL render loop on some driver paths.
    for (int stepIndex = 0; stepIndex < 8; ++stepIndex)
    {
        travel += (0.16 + float(stepIndex) * 0.055)
                * armorReflectionTuning3.y;
        vec3 rayPosition = origin + direction * travel;
        vec4 clipPosition = projView * vec4(rayPosition, 1.0);
        if (clipPosition.w <= 0.0)
            break;

        vec3 ndc = clipPosition.xyz / clipPosition.w;
        vec2 sampleUv = ndc.xy * 0.5 + 0.5;
        if (sampleUv.x <= 0.0 || sampleUv.x >= 1.0
                || sampleUv.y <= 0.0 || sampleUv.y >= 1.0
                || ndc.z >= 1.0)
            break;

        float sceneDepth = texture2D(tex2, sampleUv).x;
        if (sceneDepth >= 0.9999)
            continue;

        float rayDepth = ndc.z * 0.5 + 0.5;
        float depthDelta = rayDepth - sceneDepth;
        float thickness = (0.0025 + travel * 0.00045)
                * armorReflectionTuning3.z;

        if (depthDelta >= 0.0 && depthDelta <= thickness
                && distance(sampleUv, _texcoord) > 0.006)
        {
            float edge = min(
                    min(sampleUv.x, 1.0 - sampleUv.x),
                    min(sampleUv.y, 1.0 - sampleUv.y));
            confidence = smoothstep(0.0, 0.08, edge);

            vec3 sceneAlbedo = sampleReflectedAlbedo(
                    sampleUv,
                    armorSurface);
            // tex0 is albedo, not the already-lit scene. Never inject a
            // minimum brightness here: a local-light pass will add the real
            // lamp/torch contribution later.
            vec3 approximateLighting = max(
                    sunLight[2].xyz + sunLight[1].xyz * 0.45,
                    vec3(0.0));
            return sceneAlbedo * approximateLighting;
        }
    }

    return vec3(0.0);
}

vec3 sampleScreenMirrorFallback(
        vec3 normal,
        float armorSurface,
        out float confidence)
{
    // One bounded lookup gives view-facing mirrors visible scene detail when
    // a physically correct SSR ray points outside the current frame.
    vec2 mirrorUv = vec2(1.0 - _texcoord.x, 1.0 - _texcoord.y);
    mirrorUv += clamp(normal.xz, vec2(-1.0), vec2(1.0)) * 0.025;
    mirrorUv = clamp(mirrorUv, vec2(0.002), vec2(0.998));

    float mirrorDepth = texture2D(tex2, mirrorUv).x;
    confidence = mirrorDepth < 0.9999 ? 0.78 : 0.0;
    vec3 mirrorColor = sampleReflectedAlbedo(mirrorUv, armorSurface);
    // A power curve preserves black. The old midpoint contrast formula
    // turned black into grey whenever contrast was below 1.0.
    return clamp(pow(
            max(mirrorColor, vec3(0.0)),
            vec3(max(armorReflectionTuning3.w, 0.001))),
            0.0,
            1.35);
}

float chebyshevUpperBound(float distanceToLight, vec4 moments)
{
    if (distanceToLight <= moments.x)
        return 1.0;

    float variance = moments.y - moments.x * moments.x;
    variance = max(variance, 0.00002);
    float difference = distanceToLight - moments.x;
    return variance / (variance + difference * difference);
}

void main()
{
    vec4 tex = texture2D(tex0, _texcoord);
    vec3 normal = normalize(texture2D(tex1, _texcoord).xyz);
    vec3 sse = texture2D(tex3, _texcoord).xyz;
    float depth = texture2D(tex2, _texcoord).x;

    float specularValue = sse.r;
    float shininess = sse.g;
    // Values up to 1 are ordinary Gold/Silver items. Reflective Plate/Chain
    // adds 2 to the marker so it can receive a rough profile without changing
    // the reflection amount selected in the tuning window.
    float reflectionMarker = max(-sse.b, 0.0);
    float armorSurface = step(1.5, reflectionMarker);
    float reflectionStrength = max(
            reflectionMarker - armorSurface * 2.0,
            0.0);
    float emissive = max(sse.b, 0.0);
    float ambientocclusion = texture2D(tex5, _texcoord).x;

    vec3 worldpos = getWorldPos(depth);

    vec4 mapcoord = skyProjView * vec4(worldpos, 1.0);
    mapcoord.xyz = (mapcoord.xyz * 0.5 + 0.5) / mapcoord.w;

    float caveOcc = 0.0;
    if (mapcoord.x > 0.02 && mapcoord.x < 0.98
            && mapcoord.y > 0.02 && mapcoord.y < 0.98
            && mapcoord.z < 0.95 && mapcoord.z > 0.05)
    {
        float caveTex = texture2D(tex6, mapcoord.xy).r;
        caveOcc = clamp((mapcoord.z - caveTex) * 80.0, 0.0, 1.0);
    }

    vec4 shadowmapcoord = shadowproj * (shadowview * vec4(worldpos, 1.0));
    vec3 shadowUV = (shadowmapcoord.xyz / shadowmapcoord.w) * 0.5 + 0.5;
    vec4 shadowColor = texture2D(tex4, shadowUV.xy);

    float shadowDepth = min(
            length(shadowparams.xyz - worldpos.xyz) * shadowparams.w,
            1.0);
    float shadow = clamp(
            chebyshevUpperBound(shadowDepth, shadowColor),
            0.0,
            1.0);

    vec3 lightdir = normalize(sunLight[0].xyz);
    vec3 lightcolor = sunLight[1].xyz;
    vec3 eyedir = normalize(playerpos.xyz - worldpos.xyz);
    float ndotl = dot(normal, lightdir);
    vec3 halfvec = normalize(lightdir + eyedir);
    float ndoth = dot(halfvec, normal);
    float facing = ndotl > 0.0 ? 1.0 : 0.0;
    vec3 specular = vec3(
            facing
            * pow(clamp(ndoth, 0.0, 1.0),
                    clamp(shininess * 128.0, 8.0, 64.0))
            * specularValue);

    vec3 ambient = sunLight[2].xyz;
    vec3 caveAmbient = sunLight[3].xyz * tex.rgb;
    vec3 finalColor = max(ndotl, 0.0) * lightcolor * shadow + ambient;
    finalColor *= tex.rgb;
    finalColor += specular * lightcolor * shadow * 0.5;
    finalColor = mix(finalColor, caveAmbient, caveOcc);
    finalColor += vec3(emissive) * tex.rgb;

    if (reflectionStrength > 0.001)
    {
        vec3 reflectionDirection = normalize(reflect(-eyedir, normal));
        float screenConfidence;
        vec3 screenColor = traceScreenReflection(
                worldpos + normal * 0.04,
                reflectionDirection,
                armorSurface,
                screenConfidence);
        screenConfidence = clamp(
                screenConfidence
                        * armorReflectionTuning1.w
                        * mix(1.0, 0.76, armorSurface),
                0.0,
                1.0);
        vec3 environmentColor = environmentReflection(reflectionDirection);
        float backdropConfidence;
        vec3 backdropColor = sampleScreenMirrorFallback(
                normal,
                armorSurface,
                backdropConfidence);

        // SSR and the mirror fallback sample tex0, which is unlit albedo in
        // this deferred pass. In a cave that albedo must be attenuated by the
        // cave irradiance; otherwise a pale cave wall becomes a light source
        // and makes metal brighter in a dark corner. Point lights are added
        // afterwards by pointlight.fragment.shader, so a nearby lantern still
        // brightens the metal and produces its local glint.
        vec3 caveIrradiance = clamp(
                sunLight[3].xyz * armorReflectionTuning2.w,
                vec3(0.0),
                vec3(1.0));
        screenColor = mix(
                screenColor,
                screenColor * caveIrradiance,
                caveOcc);
        backdropColor = mix(
                backdropColor,
                backdropColor * caveIrradiance,
                caveOcc);

        // A flat upward-facing metal surface reflects the sky. The old
        // unconditional fallback sampled the opposite point on the screen,
        // so a sword lying on the ground incorrectly reflected more ground.
        // Inside caves there is no sky environment; retain the bounded
        // screen sample so nearby cave surfaces remain visible instead.
        float upwardSky = smoothstep(
                0.05,
                0.35,
                reflectionDirection.y);
        float backdropVisibility = mix(
                armorReflectionTuning1.y,
                1.0,
                max(caveOcc, 1.0 - upwardSky));
        float backdropWeight = backdropConfidence * backdropVisibility;
        // Keep reflectionStrength intact in darkness. Fading the blend would
        // reveal the brighter diffuse albedo and recreate the inverted effect;
        // the reflected radiance itself must become dark instead.
        vec3 caveEnvironment = caveIrradiance * 0.35;
        environmentColor = mix(
                environmentColor,
                caveEnvironment,
                caveOcc);
        environmentColor = mix(
                environmentColor,
                backdropColor,
                backdropWeight);
        vec3 reflectedColor = mix(
                environmentColor,
                screenColor,
                screenConfidence);
        // Black-preserving contrast cannot manufacture light from a zero
        // reflection, unlike a contrast curve pivoted around 0.5.
        reflectedColor = clamp(pow(
                max(reflectedColor, vec3(0.0)),
                vec3(max(armorReflectionTuning2.x, 0.001))),
                0.0,
                1.5);

        float strongestChannel = max(tex.r, max(tex.g, tex.b));
        vec3 metalTint = strongestChannel > 0.0001
                ? tex.rgb / strongestChannel
                : vec3(1.0);
        // Conductive metals tint their reflected light. Retaining only 18%
        // of the tint caused an 80%-reflective Gold item to turn silver-grey.
        reflectedColor *= mix(
                vec3(1.0),
                metalTint,
                armorReflectionTuning1.x);

        // reflectionStrength defaults to 0.30 for premium-metal pixels.
        finalColor = mix(
                finalColor,
                reflectedColor,
                clamp(reflectionStrength, 0.0, 1.0));

        // Restore a controlled light-source glint after the reflection
        // blend. Without this, the stock specular highlight is mostly mixed
        // away and polished metal looks flat. Keep sunlight out of caves;
        // local cave lights are handled by the point-light pass.
        float metallicGlintExponent = mix(
                44.0,
                104.0,
                clamp(shininess, 0.0, 1.0))
                * mix(0.55, 1.55, armorReflectionTuning0.w)
                * mix(1.0, 0.62, armorSurface);
        float metallicGlint = facing
                * pow(clamp(ndoth, 0.0, 1.0), metallicGlintExponent)
                * mix(0.32, 1.0, clamp(specularValue, 0.0, 1.0));
        finalColor += lightcolor
                * shadow
                * metallicGlint
                * reflectionStrength
                * (1.0 - caveOcc)
                * armorReflectionTuning0.y
                * mix(1.0, 0.68, armorSurface);
    }

    diffuseOut = vec4(finalColor * ambientocclusion, caveOcc);
}

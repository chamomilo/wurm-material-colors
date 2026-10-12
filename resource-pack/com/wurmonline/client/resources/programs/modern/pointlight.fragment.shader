#version 330

// ARMOR_MATERIAL_POINTLIGHT_REFLECTION_SHADER_V0316

out vec4 diffuseOut;

uniform sampler2D tex0; // diffuse
uniform sampler2D tex1; // normals
uniform sampler2D tex2; // depth
uniform sampler2D tex3; // specular, shininess, emissive/reflection marker

#ifdef BATCH_LIGHTS
uniform vec4 lightsInfo[32];
uniform int numLight;
#elif defined USE_INSTANCING
in vec4 lightPos;
in vec4 lightColor;
#else
uniform vec4 lightInfo[2];
#endif

uniform vec4 playerpos;
uniform mat4 invProjView;
uniform vec2 resolution;
uniform vec4 armorReflectionTuning0;
uniform vec4 armorReflectionTuning1;
uniform vec4 armorReflectionTuning4; // cave local-light multiplier, cave layer

vec3 getWorldPos(float depth, vec2 texcoord)
{
    vec4 screenSpacePosition = vec4(
            texcoord * 2.0 - 1.0,
            depth * 2.0 - 1.0,
            1.0);
    vec4 worldSpacePosition = invProjView * screenSpacePosition;
    return worldSpacePosition.xyz / worldSpacePosition.w;
}

// Reuse the bounded mirror fallback from the environment pass, but evaluate
// real lamp irradiance at the sampled wall. No ambient or brightness floor is
// added here: switching the lamp off removes this reflected contribution.
void sampleCaveReflection(
        vec2 texcoord, vec3 normal, float armorSurface,
        out vec3 reflectedPos, out vec3 reflectedNormal,
        out vec3 reflectedAlbedo, out float confidence)
{
    vec2 uv = vec2(1.0) - texcoord;
    uv += clamp(normal.xz, vec2(-1.0), vec2(1.0)) * 0.025;
    uv = clamp(uv, vec2(0.002), vec2(0.998));
    float depth = texture2D(tex2, uv).x;
    reflectedPos = getWorldPos(depth, uv);
    reflectedNormal = normalize(texture2D(tex1, uv).xyz);
    reflectedAlbedo = texture2D(tex0, uv).rgb;
    confidence = depth < 0.9999 ? 0.78 : 0.0;
    // Avoid feeding a reflective surface back into itself.
    if (texture2D(tex3, uv).z < -0.001)
        confidence = 0.0;
    if (armorSurface > 0.5)
    {
        vec2 radius = 5.0 / vec2(textureSize(tex0, 0));
        reflectedAlbedo = reflectedAlbedo * 0.40
                + texture2D(tex0, clamp(uv + vec2(radius.x, 0.0),
                        vec2(0.002), vec2(0.998))).rgb * 0.15
                + texture2D(tex0, clamp(uv - vec2(radius.x, 0.0),
                        vec2(0.002), vec2(0.998))).rgb * 0.15
                + texture2D(tex0, clamp(uv + vec2(0.0, radius.y),
                        vec2(0.002), vec2(0.998))).rgb * 0.15
                + texture2D(tex0, clamp(uv - vec2(0.0, radius.y),
                        vec2(0.002), vec2(0.998))).rgb * 0.15;
        confidence *= 0.76;
    }
}

vec3 calcPointLight(
        vec4 lightPos,
        vec4 lightColor,
        vec3 worldPos,
        vec3 normal,
        float specularValue,
        float shininess,
        float reflectionStrength,
        float armorSurface,
        vec3 tex,
        vec3 reflectedPos,
        vec3 reflectedNormal,
        vec3 reflectedAlbedo,
        float reflectionConfidence)
{
    vec3 lightdir = lightPos.xyz - worldPos.xyz;
    float distanceToLight = length(lightdir);
    lightdir /= max(distanceToLight, 0.0001);
    float attenuation = smoothstep(
            lightColor.a,
            0.0,
            distanceToLight);

    float ndotl = dot(normal, lightdir);
    float diffuse = max(ndotl, 0.0);

    vec3 eyedir = normalize(playerpos.xyz - worldPos.xyz);
    vec3 halfvec = normalize(lightdir + eyedir);
    float ndoth = dot(halfvec, normal);
    float facing = ndotl > 0.0 ? 1.0 : 0.0;
    float stockSpecular = facing
            * pow(clamp(ndoth, 0.0, 1.0),
                    clamp(shininess * 128.0, 8.0, 64.0))
            * specularValue
            * 0.5;

    float cave = clamp(armorReflectionTuning4.y, 0.0, 1.0);
    float localBoost = mix(1.0, armorReflectionTuning4.x, cave);
    // Compensate for the absent solar reflection only underground. The stock
    // diffuse/specular light and the overall 30% reflection amount stay intact.
    float metallicExponent = mix(
            36.0,
            88.0,
            clamp(shininess, 0.0, 1.0))
            * mix(0.55, 1.55, armorReflectionTuning0.w)
            * mix(1.0, 0.62, armorSurface);
    float metallicGlint = facing
            * pow(clamp(ndoth, 0.0, 1.0), metallicExponent)
            * mix(0.30, 0.82, clamp(specularValue, 0.0, 1.0))
            * clamp(reflectionStrength, 0.0, 1.0)
            * armorReflectionTuning0.z
            * localBoost
            * mix(1.0, 0.68, armorSurface);

    vec3 diffuseLight = diffuse
            * lightColor.xyz
            * tex
            * attenuation;
    vec3 specularLight = lightColor.xyz
            * max(stockSpecular + metallicGlint, 0.0)
            * attenuation;
    vec3 reflectedLight = vec3(0.0);
    // Sky/background pixels have no valid normal. Skip both their irradiance
    // and the extra per-light work for ordinary/surface pixels.
    if (reflectionConfidence > 0.001)
    {
        vec3 reflectedLightDir = lightPos.xyz - reflectedPos;
        float reflectedDistance = length(reflectedLightDir);
        reflectedLightDir /= max(reflectedDistance, 0.0001);
        float reflectedAttenuation = smoothstep(lightColor.a, 0.0, reflectedDistance);
        float reflectedDiffuse = max(dot(reflectedNormal, reflectedLightDir), 0.0);
        float strongestChannel = max(tex.r, max(tex.g, tex.b));
        vec3 metalTint = strongestChannel > 0.0001 ? tex / strongestChannel : vec3(1.0);
        reflectedLight = reflectedAlbedo * lightColor.xyz
                * reflectedDiffuse * reflectedAttenuation
                * mix(vec3(1.0), metalTint, armorReflectionTuning1.x)
                * clamp(reflectionStrength, 0.0, 1.0)
                * reflectionConfidence * cave * localBoost;
    }
    return diffuseLight + specularLight + reflectedLight;
}

void main()
{
    vec2 texcoord = gl_FragCoord.xy / resolution;
    vec3 tex = texture2D(tex0, texcoord).xyz;
    vec3 normal = normalize(texture2D(tex1, texcoord).xyz);
    vec3 sse = texture2D(tex3, texcoord).xyz;
    float depth = texture2D(tex2, texcoord).x;
    float reflectionMarker = max(-sse.z, 0.0);
    float armorSurface = step(1.5, reflectionMarker);
    float reflectionStrength = max(
            reflectionMarker - armorSurface * 2.0,
            0.0);
    vec3 worldpos = getWorldPos(depth, texcoord);
    vec3 reflectedPos = worldpos;
    vec3 reflectedNormal = normal;
    vec3 reflectedAlbedo = vec3(0.0);
    float reflectionConfidence = 0.0;
    if (reflectionStrength > 0.001 && armorReflectionTuning4.y > 0.5
            && armorReflectionTuning4.x > 0.0)
        sampleCaveReflection(texcoord, normal, armorSurface,
                reflectedPos, reflectedNormal, reflectedAlbedo, reflectionConfidence);

    #ifdef BATCH_LIGHTS
    vec3 finalColor = vec3(0.0);
    for (int i = 0; i < numLight; ++i)
    {
        finalColor += calcPointLight(
                lightsInfo[i * 2],
                lightsInfo[i * 2 + 1],
                worldpos,
                normal,
                sse.x,
                sse.y,
                reflectionStrength,
                armorSurface,
                tex, reflectedPos, reflectedNormal, reflectedAlbedo, reflectionConfidence);
    }
    #elif defined USE_INSTANCING
    vec3 finalColor = calcPointLight(
            lightPos,
            lightColor,
            worldpos,
            normal,
            sse.x,
            sse.y,
            reflectionStrength,
            armorSurface,
            tex, reflectedPos, reflectedNormal, reflectedAlbedo, reflectionConfidence);
    #else
    vec3 finalColor = calcPointLight(
            lightInfo[0],
            lightInfo[1],
            worldpos,
            normal,
            sse.x,
            sse.y,
            reflectionStrength,
            armorSurface,
            tex, reflectedPos, reflectedNormal, reflectedAlbedo, reflectionConfidence);
    #endif

    diffuseOut = vec4(finalColor, 0.0);
}

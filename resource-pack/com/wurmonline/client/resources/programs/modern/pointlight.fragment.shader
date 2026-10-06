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

vec3 getWorldPos(float depth, vec2 texcoord)
{
    vec4 screenSpacePosition = vec4(
            texcoord * 2.0 - 1.0,
            depth * 2.0 - 1.0,
            1.0);
    vec4 worldSpacePosition = invProjView * screenSpacePosition;
    return worldSpacePosition.xyz / worldSpacePosition.w;
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
        vec3 tex)
{
    vec3 lightdir = lightPos.xyz - worldPos.xyz;
    float distanceToLight = length(lightdir);
    lightdir /= distanceToLight;
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

    // A moderately wider additive glint makes torches, lamps, and other
    // point lights readable on reflective Gold/Silver without blooming.
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
            * mix(1.0, 0.68, armorSurface);

    vec3 diffuseLight = diffuse
            * lightColor.xyz
            * tex
            * attenuation;
    vec3 specularLight = lightColor.xyz
            * max(stockSpecular + metallicGlint, 0.0)
            * attenuation;
    return diffuseLight + specularLight;
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
                tex);
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
            tex);
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
            tex);
    #endif

    diffuseOut = vec4(finalColor, 0.0);
}

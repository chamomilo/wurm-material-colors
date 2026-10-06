#version 330

// ARMOR_MATERIAL_SKIN_GBUFFER_SHADER_V0316
in vec2 _texcoord;
in vec3 _normal;
in vec3 _viewpos;
in vec3 _worldpos;
in vec4 _color;

layout (location = 0) out vec4 diffuseOut;
layout (location = 1) out vec4 normalOut;
layout (location = 2) out vec4 sseOut;

uniform sampler2D tex0;
uniform sampler2D tex3;
uniform vec4 playerpos;
uniform vec4 sse;
uniform vec4 armorReflectionTuning0;

#ifdef USE_RARITY
uniform vec4 rarity;
uniform vec4 time;
#endif

mat3 cotangent_frame(vec3 N, vec3 p, vec2 uv)
{
    vec3 dp1 = dFdx(p);
    vec3 dp2 = dFdy(p);
    vec2 duv1 = dFdx(uv);
    vec2 duv2 = dFdy(uv);
    vec3 dp2perp = cross(dp2, N);
    vec3 dp1perp = cross(N, dp1);
    vec3 T = dp2perp * duv1.x + dp1perp * duv2.x;
    vec3 B = dp2perp * duv1.y + dp1perp * duv2.y;
    float invmax = inversesqrt(max(max(dot(T, T), dot(B, B)), 0.00000001));
    return mat3(T * invmax, B * invmax, N);
}

// A stable UV-space hammered grain breaks up the broad reflection on Wurm's
// very smooth armour meshes. It changes the lighting normal, not the model or
// silhouette, and is evaluated only for reflective Gold/Silver Plate/Chain.
float armorSurfaceHeight(vec2 uv)
{
    vec2 p = uv * vec2(83.0, 97.0);
    float broad = sin(p.x + sin(p.y * 0.73));
    float crossGrain = sin(p.y * 1.31 + p.x * 0.47);
    float fine = sin((p.x - p.y) * 2.17);
    return broad * 0.48 + crossGrain * 0.34 + fine * 0.18;
}

vec3 roughenArmorNormal(vec3 N, vec3 V, vec2 uv)
{
    const float sampleStep = 1.0 / 512.0;
    float left = armorSurfaceHeight(uv - vec2(sampleStep, 0.0));
    float right = armorSurfaceHeight(uv + vec2(sampleStep, 0.0));
    float down = armorSurfaceHeight(uv - vec2(0.0, sampleStep));
    float up = armorSurfaceHeight(uv + vec2(0.0, sampleStep));
    vec2 slope = vec2(right - left, up - down) * 0.42;
    vec3 detailNormal = normalize(vec3(-slope, 1.0));
    return normalize(cotangent_frame(N, -V, uv) * detailNormal);
}

#ifdef USE_NORMAL_MAPS
uniform int normalmap;

vec3 perturb_normal(vec3 N, vec3 V, vec2 texcoord, vec3 nmap)
{
    vec3 map = nmap * 255.0 / 127.0 - 128.0 / 127.0;
    map.z = sqrt(max(1.0 - dot(map.xy, map.xy), 0.0));
    return normalize(cotangent_frame(N, -V, texcoord) * map);
}
#endif

void main()
{
    vec4 tex = texture(tex0, _texcoord);

    // The alpha-only compositor pass writes 254/255: visually opaque in the
    // paperdoll and portraits, but still distinct from an opaque atlas texel.
    // The upper fade also keeps filtered armour edges inside the metal mask.
    float lowerBand = smoothstep(0.992, 0.995, tex.a);
    float upperBand = 1.0 - smoothstep(0.9990, 0.9999, tex.a);
    float bakedMetal = lowerBand * upperBand;

    if (tex.a < 0.7 && bakedMetal < 0.01)
        discard;

    vec3 normal = normalize(_normal);
    vec4 sseResult = sse;
    #ifdef USE_NORMAL_MAPS
    if (normalmap > 0)
    {
        vec4 nmap = texture(tex3, _texcoord);
        sseResult.r = nmap.a;
        normal = perturb_normal(normal, _viewpos, _texcoord, nmap.xyz);
    }
    #endif

    if (bakedMetal > 0.0)
    {
        normal = roughenArmorNormal(normal, _viewpos, _texcoord);
        // A lower shininess produces a wider, softer armour highlight. The
        // reflection amount remains unchanged and is encoded after the flag.
        sseResult.g = min(sseResult.g, 0.38);
        float reflectionStrength = bakedMetal
                * clamp(armorReflectionTuning0.x, 0.0, 1.0);
        sseResult.b = -(2.0 + reflectionStrength);
    }

    vec3 rarityColor = vec3(0.0);
    #ifdef USE_RARITY
    float seed = dot(
            _normal.xyz + vec3(_texcoord, time.z * 0.1),
            vec3(12.9898, 78.233, 37.719));
    float noise = fract(sin(seed + time.z * 0.5) * 43758.5453) * 2.0 - 1.0;
    rarityColor = clamp(rarity.xyz * noise * 0.1 * rarity.w, 0.0, 1.0);
    #endif

    diffuseOut = vec4(tex.rgb * _color.rgb + rarityColor, 1.0);
    normalOut = vec4(normal, 1.0);
    sseOut = sseResult;
}

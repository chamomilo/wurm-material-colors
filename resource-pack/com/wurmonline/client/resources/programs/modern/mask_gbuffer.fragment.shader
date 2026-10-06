#version 330

in vec2 _texcoord;
in vec3 _normal;
in vec3 _viewpos;
in vec3 _worldpos;
in vec4 _color;

layout (location = 0) out vec4 diffuseOut;
layout (location = 1) out vec4 normalOut;
layout (location = 2) out vec4 sseOut;

uniform sampler2D tex0;
uniform sampler2D tex2;
uniform sampler2D tex3;

uniform vec4 playerpos;

#ifdef USE_INSTANCING
in vec4 _materialColor;
in vec4 _secondaryColor;
#else
uniform vec4 secondarycolor;
uniform vec4 materialcolor;
#endif

#ifdef USE_RARITY
uniform vec4 rarity;
uniform vec4 time;
#endif
uniform vec4 sse;
uniform vec4 armorReflectionTuning0;

float snoise(vec4);

// Derivative-based tangent frame; retained from the stock modern shader.
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
    map.z = sqrt(1.0 - dot(map.xy, map.xy));

    mat3 TBN = cotangent_frame(N, -V, texcoord);
    return normalize(TBN * map);
}
#endif

void main()
{
    vec4 tex = texture(tex0, _texcoord);
    vec4 mask = texture(tex2, _texcoord);

    if (tex.a < 0.7)
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

    vec3 rarityColor = vec3(0.0);

    #ifdef USE_RARITY
    float noise = snoise(vec4(
            _normal.xyz + vec3(_texcoord.x, _texcoord.y, time.z * 0.1),
            time.z * 0.5));
    rarityColor = rarity.xyz * (noise * 0.1);
    rarityColor = clamp(rarityColor.xyz * rarity.w, 0.0, 1.0);
    #endif

    #ifdef USE_INSTANCING
    vec4 activeMaterialColor = _materialColor;
    vec4 activeSecondaryColor = _secondaryColor;
    #else
    vec4 activeMaterialColor = materialcolor;
    vec4 activeSecondaryColor = secondarycolor;
    #endif

    // Java uses -1 for ordinary reflective items and -2 for Plate/Chain.
    // Both keep the full configured reflection strength, while the armour
    // range also enables the rough surface profile.
    float materialMarker = max(-activeMaterialColor.a, 0.0);
    float reflectiveMaterial = min(materialMarker, 1.0);
    float armorSurface = step(1.5, materialMarker);
    float reflectionStrength = reflectiveMaterial
            * clamp(armorReflectionTuning0.x, 0.0, 1.0)
            * mask.g;
    if (reflectionStrength > 0.0)
    {
        if (armorSurface > 0.5)
        {
            normal = roughenArmorNormal(normal, _viewpos, _texcoord);
            sseResult.g = min(sseResult.g, 0.38);
        }
        sseResult.b = -(reflectionStrength + armorSurface * 2.0);
    }

    vec4 recolored = vec4(tex.rgb * activeMaterialColor.rgb, tex.a);
    vec4 finalTex = mix(tex, recolored, mask.g);

    vec4 actualColor = mix(vec4(1.0), _color, mask.r);
    actualColor = mix(actualColor, activeSecondaryColor, mask.b);
    vec4 finalColor = finalTex * actualColor;

    diffuseOut = finalColor + vec4(rarityColor, 1.0);
    normalOut = vec4(normalize(normal), 1.0);
    sseOut = sseResult;
}

// Stock 4D simplex noise used by Wurm's rarity effect.
vec4 mod289(vec4 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

float mod289(float x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 permute(vec4 x) {
    return mod289(((x * 34.0) + 1.0) * x);
}

float permute(float x) {
    return mod289(((x * 34.0) + 1.0) * x);
}

vec4 taylorInvSqrt(vec4 r) {
    return 1.79284291400159 - 0.85373472095314 * r;
}

float taylorInvSqrt(float r) {
    return 1.79284291400159 - 0.85373472095314 * r;
}

vec4 grad4(float j, vec4 ip)
{
    const vec4 ones = vec4(1.0, 1.0, 1.0, -1.0);
    vec4 p;
    vec4 signs;

    p.xyz = floor(fract(vec3(j) * ip.xyz) * 7.0) * ip.z - 1.0;
    p.w = 1.5 - dot(abs(p.xyz), ones.xyz);
    signs = vec4(lessThan(p, vec4(0.0)));
    p.xyz = p.xyz + (signs.xyz * 2.0 - 1.0) * signs.www;
    return p;
}

#define F4 0.309016994374947451

float snoise(vec4 v)
{
    const vec4 C = vec4(
            0.138196601125011,
            0.276393202250021,
            0.414589803375032,
           -0.447213595499958);

    vec4 i = floor(v + dot(v, vec4(F4)));
    vec4 x0 = v - i + dot(i, C.xxxx);

    vec4 i0;
    vec3 isX = step(x0.yzw, x0.xxx);
    vec3 isYZ = step(x0.zww, x0.yyz);
    i0.x = isX.x + isX.y + isX.z;
    i0.yzw = 1.0 - isX;
    i0.y += isYZ.x + isYZ.y;
    i0.zw += 1.0 - isYZ.xy;
    i0.z += isYZ.z;
    i0.w += 1.0 - isYZ.z;

    vec4 i3 = clamp(i0, 0.0, 1.0);
    vec4 i2 = clamp(i0 - 1.0, 0.0, 1.0);
    vec4 i1 = clamp(i0 - 2.0, 0.0, 1.0);

    vec4 x1 = x0 - i1 + C.xxxx;
    vec4 x2 = x0 - i2 + C.yyyy;
    vec4 x3 = x0 - i3 + C.zzzz;
    vec4 x4 = x0 + C.wwww;

    i = mod289(i);
    float j0 = permute(permute(permute(permute(i.w) + i.z) + i.y) + i.x);
    vec4 j1 = permute(permute(permute(permute(
            i.w + vec4(i1.w, i2.w, i3.w, 1.0))
            + i.z + vec4(i1.z, i2.z, i3.z, 1.0))
            + i.y + vec4(i1.y, i2.y, i3.y, 1.0))
            + i.x + vec4(i1.x, i2.x, i3.x, 1.0));

    vec4 ip = vec4(1.0 / 294.0, 1.0 / 49.0, 1.0 / 7.0, 0.0);
    vec4 p0 = grad4(j0, ip);
    vec4 p1 = grad4(j1.x, ip);
    vec4 p2 = grad4(j1.y, ip);
    vec4 p3 = grad4(j1.z, ip);
    vec4 p4 = grad4(j1.w, ip);

    vec4 norm = taylorInvSqrt(vec4(
            dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;
    p4 *= taylorInvSqrt(dot(p4, p4));

    vec3 m0 = max(0.6 - vec3(
            dot(x0, x0), dot(x1, x1), dot(x2, x2)), 0.0);
    vec2 m1 = max(0.6 - vec2(dot(x3, x3), dot(x4, x4)), 0.0);
    m0 *= m0;
    m1 *= m1;
    return 49.0 * (
            dot(m0 * m0, vec3(dot(p0, x0), dot(p1, x1), dot(p2, x2)))
            + dot(m1 * m1, vec2(dot(p3, x3), dot(p4, x4))));
}

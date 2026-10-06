// ARMOR_MATERIAL_PLAYER_MASK_SHADER_V0316
uniform sampler2D tex0;
uniform sampler2D tex2;

varying vec2 _texcoord;
varying vec4 _color;

void main()
{
    float sourceAlpha = texture2D(tex0, _texcoord).a;
    float materialAmount = texture2D(tex2, _texcoord).g;
    if (sourceAlpha * materialAmount < 0.5)
        discard;

    // RGB writes are disabled by PlayerArmorReflection. Only this sentinel
    // reaches the alpha channel of Wurm's composited player atlas.
    gl_FragColor = vec4(0.0, 0.0, 0.0, _color.a);
}

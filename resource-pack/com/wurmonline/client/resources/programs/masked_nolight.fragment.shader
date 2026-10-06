// ARMOR_MATERIAL_PLAYER_COMPOSITOR_SHADER_V0316
uniform sampler2D tex0;
uniform sampler2D tex1;
uniform sampler2D tex2;

varying vec2 _texcoord;
varying vec4 _color;

uniform vec4 secondarycolor;
uniform vec4 materialcolor;

void main()
{
    vec4 texC = texture2D(tex0, _texcoord);
    vec4 mask = texture2D(tex2, _texcoord);

    // materialcolor.a is private metadata for the Java hook. It must never
    // change the opacity used by Wurm's normal armour-compositing pass.
    vec4 recolored = vec4(texC.rgb * materialcolor.rgb, texC.a);
    vec4 finalTex = mix(texC, recolored, mask.g);

    vec4 actualColor = mix(vec4(1.0), _color, mask.r);
    actualColor = mix(actualColor, secondarycolor, mask.b);

    gl_FragColor = vec4((finalTex * actualColor).rgb, texC.a);
}

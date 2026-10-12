package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.jar.JarFile;

/** Renders stock shaders in a hidden offscreen context, without starting Wurm. */
public final class MaterialOpacityGpuProbe {
    private static final String ROOT = "com/wurmonline/client/resources/programs/";
    private static final String VERTEX = "#version 120\n"
            + "varying vec2 _texcoord; varying vec4 _worldpos, _shadowmapcoord, _fog, _color;"
            + "varying vec3 _normal, _eyedir; varying vec4 _lightvec[1];"
            + "void main() { gl_Position=gl_Vertex; _texcoord=vec2(0.5);"
            + "_worldpos=vec4(0,0,0,1); _shadowmapcoord=vec4(0,0,0,1);"
            + "_fog=vec4(0,0,0,1); _color=vec4(1); _normal=vec3(0,0,1);"
            + "_eyedir=vec3(0,0,1); _lightvec[0]=vec4(0,0,1,1); }";
    private static final String[] METALS = MetalPaletteTest.METALS;
    private static final String MODERN_VERTEX = "#version 330 compatibility\n"
            + "out vec2 _texcoord; out vec3 _normal, _viewpos, _worldpos; out vec4 _color;"
            + "\n#ifdef USE_INSTANCING\n"
            + "out vec4 _materialColor, _secondaryColor; uniform vec4 materialcolor, secondarycolor;"
            + "\n#endif\n"
            + "void main() { gl_Position=gl_Vertex; _texcoord=gl_Vertex.xy*0.5+0.5;"
            + "_normal=vec3(0,0,1); _viewpos=vec3(gl_Vertex.xy,-1);"
            + "_worldpos=_viewpos; _color=vec4(1);"
            + "\n#ifdef USE_INSTANCING\n"
            + "_materialColor=materialcolor; _secondaryColor=secondarycolor;"
            + "\n#endif\n }";

    public static void main(String[] args) throws Exception {
        Pbuffer context = new Pbuffer(8, 8, new PixelFormat().withAlphaBits(8),
                null, null, new ContextAttribs(3, 3).withProfileCompatibility(true));
        try (JarFile client = new JarFile(args[0])) {
            context.makeCurrent();
            System.out.println("GPU: " + GL11.glGetString(GL11.GL_RENDERER));
            GL11.glViewport(0, 0, 8, 8);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT,
                    vector(1,1,1,1));
            GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT,
                    vector(1,1,1,1));
            GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE,
                    vector(0,0,0,1));
            GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR,
                    vector(0,0,0,1));
            GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT,
                    vector(0,0,0,1));
            int checks = 0;
            for (String name : new String[]{"masked.fragment.shader",
                    "masked_rare.fragment.shader", "masked.multipass.fragment.shader",
                    "masked_nolight.fragment.shader"}) {
                String fragment = read(client, ROOT + name);
                int program = link(VERTEX, "#version 120\n#define MAX_LIGHTS 1\n" + fragment);
                GL20.glUseProgram(program);
                uniform1i(program, "tex0", 0);
                uniform1i(program, "tex1", 1);
                uniform1i(program, "tex2", 2);
                uniform1i(program, "numlights", 1);
                uniform4f(program, "secondarycolor", 1,1,1,1);
                texture(1, 255,255,255,255);
                for (String metal : METALS) {
                    for (String model : new String[]{"model.weapon.halberd.", "model.armour.torso.plate."}) {
                        Color marked = materialColor(model, metal);
                        Color safe = MaterialColorOpacity.forReflectionSupport(marked, false);
                        for (int diffuseAlpha : new int[]{0, 128, 255}) {
                            texture(0, 180,180,180,diffuseAlpha);
                            texture(2, 0,255,0,255);
                            int[] oldMetal = render(program, marked);
                            int[] fixedMetal = render(program, safe);
                            int[] stockOpaque = render(program,
                                    new Color(marked.r, marked.g, marked.b, 1.0f));
                            if (marked.a < 0) require(oldMetal[3] == 0, "old bug did not reproduce: " + name);
                            require(java.util.Arrays.equals(fixedMetal, stockOpaque),
                                    "stock metal opacity changed: " + name);
                            if (diffuseAlpha == 0) require(fixedMetal[3] == 0, "cutout filled");
                            require(fixedMetal[0] == oldMetal[0]
                                    && fixedMetal[1] == oldMetal[1]
                                    && fixedMetal[2] == oldMetal[2], "metal RGB changed");
                            texture(2, 0,0,0,255);
                            require(java.util.Arrays.equals(render(program, marked), render(program, safe)),
                                    "wood/handle changed: " + name);
                            checks += 3;
                        }
                    }
                    texture(2, 0,255,0,255);
                    Color safe = MaterialColorOpacity.forReflectionSupport(materialColor("model.tool.shovel.", metal), false);
                    for (int base : new int[]{32,100,180}) {
                        texture(0, base,base,base,255);
                        int[] pixel = render(program, safe);
                        for (int channel=0;channel<3;channel++) {
                            require(Math.abs(pixel[channel] - MetalPaletteTest.expectedMaterialPixel(metal, channel, base)) <= 1,
                                    "icon colour-space mismatch " + name + " " + metal + " base=" + base + " channel=" + channel);
                            checks++;
                        }
                    }
                }
                System.out.println("PASS " + name + ": 14 metal palettes, opaque equipment,"
                        + " transparent cutouts and unchanged handles");
                GL20.glUseProgram(0);
                GL20.glDeleteProgram(program);
            }
            System.out.println("MATERIAL_OPACITY_GPU_OK checks=" + checks);
            verifyModernMasks();
            verifyWornCompositor();
            CaveLightingGpuProbe.verify();
        } finally {
            context.destroy();
        }
    }

    private static Color materialColor(String model, String metal) {
        Color stock = ArmorMaterialColorsMod.materialColorForModel("model.armour.torso.plate." + metal);
        return ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(model + metal,
                new Color(stock.r, stock.g, stock.b));
    }

    private static void verifyModernMasks() throws Exception {
        File project = new File(System.getProperty("armorMaterialColorsProjectDir"));
        String fragment = new String(Files.readAllBytes(new File(project,
                "resource-pack/" + ROOT + "modern/mask_gbuffer.fragment.shader").toPath()),
                StandardCharsets.UTF_8);
        int framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        int[] attachments = new int[3];
        java.nio.IntBuffer drawBuffers = BufferUtils.createIntBuffer(3);
        for (int index = 0; index < attachments.length; index++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 4);
            attachments[index] = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, attachments[index]);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, 8, 8, 0,
                    GL11.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer) null);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + index,
                    GL11.GL_TEXTURE_2D, attachments[index], 0);
            drawBuffers.put(GL30.GL_COLOR_ATTACHMENT0 + index);
        }
        drawBuffers.flip();
        GL20.glDrawBuffers(drawBuffers);
        require(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE,
                "Modern shader framebuffer is incomplete");
        int checks = 0;
        try {
            for (boolean instanced : new boolean[]{false, true}) {
                String define = instanced ? "\n#define USE_INSTANCING\n" : "\n";
                int program = link(MODERN_VERTEX.replace("#version 330 compatibility\n",
                                "#version 330 compatibility" + define),
                        fragment.replace("#version 330", "#version 330" + define));
                GL20.glUseProgram(program);
                uniform1i(program, "tex0", 0);
                uniform1i(program, "tex2", 2);
                uniform4f(program, "secondarycolor", 1, 1, 1, 1);
                uniform4f(program, "sse", 0.2f, 0.9f, 0, 0);
                for (String metal : METALS) {
                    for (String model : new String[]{"model.weapon.halberd.",
                            "model.armour.torso.plate.", "model.armour.torso.chain."}) {
                        Color marked = materialColor(model, metal);
                        Color deferred = MaterialColorOpacity.forReflectionSupport(marked, true);
                        boolean armor = model.contains(".armour.");
                        String label = metal + " " + model + " instanced=" + instanced;
                        boolean reflective = metal.equals("gold") || metal.equals("silver")
                                || metal.equals("adamantine") || metal.equals("seryll")
                                || metal.equals("glimmersteel");
                        require(reflective == (marked.a < 0), "reflection eligibility " + label);
                        checks++;
                        uniform4f(program, "armorReflectionTuning0",
                                ReflectionTuning.DEFAULT_REFLECTION_STRENGTH, 0.28f, 0.32f, 0.45f);
                        texture(0, 180, 180, 180, 255);
                        texture(2, 0, 255, 0, 255);
                        float[][] colored = renderModern(program, deferred);
                        for (int channel = 0; channel < 3; channel++) {
                            float tint = new float[]{marked.r, marked.g, marked.b}[channel];
                            close(180f / 255f * tint, colored[0][channel], "reference palette " + label);
                        }
                        close(reflective ? (armor ? -2.3f : -0.3f) : 0,
                                colored[2][2], "premium-only reflection profile " + label);
                        close(reflective && armor ? 0.38f : 0.9f,
                                colored[2][1], "surface roughness " + label);
                        require(colored[0][3] >= 1, "Metal became transparent: " + label);
                        checks += 6;
                        texture(2, 0, 0, 0, 255);
                        float[][] handle = renderModern(program, deferred);
                        for (int channel = 0; channel < 3; channel++) {
                            close(180f / 255f, handle[0][channel], "handle recolored " + label);
                        }
                        close(0, handle[2][2], "handle became reflective " + label);
                        checks += 4;
                        texture(2, 0, 255, 0, 255);
                        uniform4f(program, "armorReflectionTuning0", 0, 0.28f, 0.32f, 0.45f);
                        close(0, renderModern(program, deferred)[2][2], "strength zero ignored " + label);
                        texture(0, 180, 180, 180, 0);
                        float[][] cutout = renderModern(program, deferred);
                        close(0, cutout[0][3], "cutout filled " + label);
                        close(0, cutout[2][2], "cutout became reflective " + label);
                        checks += 3;
                    }
                }
                GL20.glUseProgram(0);
                GL20.glDeleteProgram(program);
                System.out.println("PASS modern mask shader instanced=" + instanced
                        + ": 14 reference palettes, only five reflective metals, handles and cutouts");
            }
        } finally {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            for (int attachment : attachments) GL11.glDeleteTextures(attachment);
            GL30.glDeleteFramebuffers(framebuffer);
        }
        System.out.println("MATERIAL_REFLECTION_GPU_OK checks=" + checks);
    }

    private static void verifyWornCompositor() throws Exception {
        File project = new File(System.getProperty("armorMaterialColorsProjectDir"));
        String fragment = new String(Files.readAllBytes(new File(project,
                "resource-pack/" + ROOT + "masked_nolight.fragment.shader").toPath()), StandardCharsets.UTF_8);
        int program = link(VERTEX, "#version 120\n" + fragment);
        GL20.glUseProgram(program);
        uniform1i(program, "tex0", 0);
        uniform1i(program, "tex2", 2);
        uniform4f(program, "secondarycolor", 1, 1, 1, 1);
        int checks = 0;
        for (String metal : METALS) {
            Color color = ArmorMaterialColorsMod.materialColorForModel("model.armour.torso.plate." + metal);
            for (int alpha : new int[]{0, 128, 255}) {
                texture(0, 100, 100, 100, alpha);
                texture(2, 0, 255, 0, 255);
                int[] pixel = render(program, color);
                for (int channel = 0; channel < 3; channel++) {
                    require(Math.abs(pixel[channel] - MetalPaletteTest.expectedMaterialPixel(metal, channel, 100)) <= 1,
                            "worn reference palette " + metal + " channel=" + channel);
                }
                require(pixel[3] == alpha, "worn texture alpha changed: " + metal);
                texture(2, 0, 0, 0, 255);
                int[] leather = render(program, color);
                require(java.util.Arrays.equals(leather, new int[]{100, 100, 100, alpha}),
                        "worn unmasked leather recolored: " + metal);
                checks += 5;
            }
        }
        GL20.glUseProgram(0);
        GL20.glDeleteProgram(program);
        System.out.println("MATERIAL_WORN_COMPOSITOR_GPU_OK metals=14 checks=" + checks);
    }

    private static float[][] renderModern(int program, Color color) {
        draw(program, color);
        float[][] result = new float[3][4];
        for (int index = 0; index < result.length; index++) {
            GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0 + index);
            FloatBuffer pixel = BufferUtils.createFloatBuffer(4);
            GL11.glReadPixels(4, 4, 1, 1, GL11.GL_RGBA, GL11.GL_FLOAT, pixel);
            pixel.get(result[index]);
        }
        require(GL11.glGetError() == GL11.GL_NO_ERROR, "Modern OpenGL error");
        return result;
    }

    private static void close(float expected, float actual, String label) {
        require(Math.abs(expected - actual) < 0.001f,
                label + ": expected=" + expected + " actual=" + actual);
    }

    private static int[] render(int program, Color color) {
        draw(program, color);
        ByteBuffer pixel = BufferUtils.createByteBuffer(4);
        GL11.glReadPixels(4,4,1,1,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixel);
        require(GL11.glGetError() == GL11.GL_NO_ERROR, "OpenGL error");
        return new int[]{pixel.get(0)&255,pixel.get(1)&255,pixel.get(2)&255,pixel.get(3)&255};
    }

    private static void draw(int program, Color color) {
        uniform4f(program, "materialcolor", color.r, color.g, color.b, color.a);
        GL11.glClearColor(0,0,0,0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1,-1); GL11.glVertex2f(1,-1);
        GL11.glVertex2f(1,1); GL11.glVertex2f(-1,1);
        GL11.glEnd();
    }

    private static void texture(int unit, int r, int g, int b, int a) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
        ByteBuffer pixel = BufferUtils.createByteBuffer(4);
        pixel.put(new byte[]{(byte)r,(byte)g,(byte)b,(byte)a}).flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,1,1,0,
                GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixel);
    }

    static int link(String vertex, String fragment) {
        int program = GL20.glCreateProgram();
        int vs = shader(GL20.GL_VERTEX_SHADER,vertex);
        int fs = shader(GL20.GL_FRAGMENT_SHADER,fragment);
        GL20.glAttachShader(program,vs); GL20.glAttachShader(program,fs);
        GL20.glLinkProgram(program);
        require(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS) != 0,
                GL20.glGetProgramInfoLog(program,16384));
        GL20.glDeleteShader(vs); GL20.glDeleteShader(fs);
        return program;
    }

    private static int shader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader,source); GL20.glCompileShader(shader);
        require(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS) != 0,
                GL20.glGetShaderInfoLog(shader,16384));
        return shader;
    }

    static void uniform1i(int program, String name, int value) {
        GL20.glUniform1i(GL20.glGetUniformLocation(program,name),value);
    }

    static void uniform4f(int program, String name, float x, float y, float z, float w) {
        GL20.glUniform4f(GL20.glGetUniformLocation(program,name),x,y,z,w);
    }

    private static String read(JarFile jar, String path) throws IOException {
        try (InputStream input = jar.getInputStream(jar.getJarEntry(path));
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            for (int count; (count=input.read(buffer)) >= 0;) output.write(buffer,0,count);
            return new String(output.toByteArray(),StandardCharsets.UTF_8);
        }
    }

    private static java.nio.FloatBuffer vector(float x, float y, float z, float w) {
        java.nio.FloatBuffer result = BufferUtils.createFloatBuffer(4);
        result.put(new float[]{x,y,z,w}).flip();
        return result;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

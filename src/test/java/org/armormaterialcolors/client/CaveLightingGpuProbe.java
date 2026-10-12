package org.armormaterialcolors.client;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.armormaterialcolors.client.MaterialOpacityGpuProbe.*;

/** Exercises actual local-light shader pixels in the caller's hidden context. */
final class CaveLightingGpuProbe {
    private static final String VERTEX = "#version 330 compatibility\n"
            + "\n#ifdef USE_INSTANCING\n"
            + "out vec4 lightPos, lightColor; uniform vec4 testLightPos, testLightColor;"
            + "\n#endif\n"
            + "void main() { gl_Position=gl_Vertex;"
            + "\n#ifdef USE_INSTANCING\n"
            + "lightPos=testLightPos; lightColor=testLightColor;"
            + "\n#endif\n }";
    private static final int[] INPUTS = new int[4];
    private static int checks;

    static void verify() throws Exception {
        File project = new File(System.getProperty("armorMaterialColorsProjectDir"));
        String fragment = new String(Files.readAllBytes(new File(project,
                "resource-pack/com/wurmonline/client/resources/programs/modern/pointlight.fragment.shader")
                .toPath()), StandardCharsets.UTF_8);
        int framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + 7);
        int output = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, output);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, 8, 8,
                0, GL11.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer) null);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D, output, 0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        check(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER)
                == GL30.GL_FRAMEBUFFER_COMPLETE, "Cave probe framebuffer incomplete");
        for (int i = 0; i < INPUTS.length; i++) INPUTS[i] = GL11.glGenTextures();
        checks = 0;
        try {
            for (String mode : new String[]{"single", "BATCH_LIGHTS", "USE_INSTANCING"}) {
                String define = mode.equals("single") ? "\n" : "\n#define " + mode + "\n";
                int program = link(VERTEX.replace("#version 330 compatibility\n",
                                "#version 330 compatibility" + define),
                        fragment.replace("#version 330", "#version 330" + define));
                try {
                    GL20.glUseProgram(program);
                    for (int unit = 0; unit < 4; unit++) uniform1i(program, "tex" + unit, unit);
                    uniform4f(program, "playerpos", 0, 0, 2, 1);
                    GL20.glUniform2f(GL20.glGetUniformLocation(program, "resolution"), 8, 8);
                    FloatBuffer identity = BufferUtils.createFloatBuffer(16);
                    identity.put(new float[]{1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1}).flip();
                    GL20.glUniformMatrix4(GL20.glGetUniformLocation(program, "invProjView"), false, identity);
                    uniform4f(program, "armorReflectionTuning1", 0.72f, 0, 1, 1);
                    for (float marker : new float[]{-0.30f, -2.30f}) {
                        fixture(marker, 1, 0.5f);
                        lamp(program, 0, 0, 1, 1, 0.75f, 0.4f, 6);
                        tuning(program, 1, false, 0.32f);
                        float[] surface = render();
                        tuning(program, 2.5f, false, 0.32f);
                        same(surface, render(), "surface compensation " + mode);

                        tuning(program, 0, true, 0.32f);
                        float[] base = render();
                        tuning(program, 1, true, 0.32f);
                        float[] cave = render();
                        tuning(program, 2.5f, true, 0.32f);
                        float[] boosted = render();
                        for (int channel = 0; channel < 3; channel++) {
                            near(2.5f * (cave[channel] - base[channel]),
                                    boosted[channel] - base[channel], "2.5x cave contribution " + mode);
                            check(boosted[channel] > cave[channel] + 0.001f, "cave did not brighten " + mode);
                            checks++;
                        }

                        // Isolate the reflected-wall component from the lamp glint.
                        tuning(program, 0, true, 0);
                        float[] withoutReflection = render();
                        tuning(program, 2.5f, true, 0);
                        float[] wallReflection = render();
                        for (int channel = 0; channel < 3; channel++) {
                            check(wallReflection[channel] > withoutReflection[channel] + 0.001f,
                                    "lit wall missing from reflection " + mode);
                            checks++;
                        }
                        fixture(marker, -1, 0.5f);
                        same(withoutReflection, render(), "unlit wall glows " + mode);
                        fixture(marker, 0, 1);
                        same(withoutReflection, render(), "background glows " + mode);
                        fixture(marker, 1, 0.5f);
                        tuning(program, 2.5f, true, 0.32f);
                        lamp(program, 0, 0, 1, 0, 0, 0, 6);
                        same(new float[4], render(), "lamp off still glows " + mode);
                        lamp(program, 100, 100, 100, 1, 0.75f, 0.4f, 1);
                        same(new float[4], render(), "distant lamp still glows " + mode);

                        // Plain materials/handles must keep stock lighting underground.
                        fixture(0, 1, 0.5f);
                        lamp(program, 0, 0, 1, 1, 0.75f, 0.4f, 6);
                        tuning(program, 1, false, 0.32f);
                        float[] plain = render();
                        tuning(program, 2.5f, true, 0.32f);
                        same(plain, render(), "plain material changed " + mode);
                    }
                    if (mode.equals("BATCH_LIGHTS")) {
                        fixture(-0.3f, 1, 0.5f);
                        lamp(program, 0, 0, 1, 1, 0.75f, 0.4f, 6);
                        tuning(program, 2.5f, true, 0.32f);
                        float[] oneLamp = render();
                        uniform4f(program, "lightsInfo[2]", 0, 0, 1, 1);
                        uniform4f(program, "lightsInfo[3]", 1, 0.75f, 0.4f, 6);
                        uniform1i(program, "numLight", 2);
                        float[] twoLamps = render();
                        for (int channel = 0; channel < 3; channel++)
                            near(oneLamp[channel] * 2, twoLamps[channel], "batched lamp addition");
                    }
                    System.out.println("PASS cave local lighting " + mode
                            + ": 2.5x reflections/glints, lit walls, darkness, plain materials and surface lighting");
                } finally {
                    GL20.glUseProgram(0);
                    GL20.glDeleteProgram(program);
                }
            }
        } finally {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            for (int texture : INPUTS) GL11.glDeleteTextures(texture);
            GL11.glDeleteTextures(output);
            GL30.glDeleteFramebuffers(framebuffer);
        }
        System.out.println("MATERIAL_CAVE_LIGHTING_GPU_OK variants=3 checks=" + checks);
    }

    private static void tuning(int program, float boost, boolean cave, float glint) {
        uniform4f(program, "armorReflectionTuning0", 0.30f, 0.28f, glint, 0.45f);
        uniform4f(program, "armorReflectionTuning4", boost, cave ? 1 : 0, 0, 0);
    }

    private static void lamp(int program, float x, float y, float z,
                             float r, float g, float b, float radius) {
        uniform4f(program, "lightInfo[0]", x, y, z, 1);
        uniform4f(program, "lightInfo[1]", r, g, b, radius);
        uniform4f(program, "lightsInfo[0]", x, y, z, 1);
        uniform4f(program, "lightsInfo[1]", r, g, b, radius);
        uniform1i(program, "numLight", 1);
        uniform4f(program, "testLightPos", x, y, z, 1);
        uniform4f(program, "testLightColor", r, g, b, radius);
    }

    private static void fixture(float marker, float wallNormal, float wallDepth) {
        for (int unit = 0; unit < 4; unit++) {
            FloatBuffer pixels = BufferUtils.createFloatBuffer(8 * 8 * 4);
            for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++) {
                boolean metal = x == 4 && y == 4;
                boolean wall = x == 3 && y == 3;
                if (unit == 0) pixels.put(metal ? new float[]{0.4f,0.3f,0.12f,1}
                        : new float[]{0.5f,0.45f,0.35f,1});
                if (unit == 1) pixels.put(new float[]{0,0,wall ? wallNormal : 1,1});
                if (unit == 2) pixels.put(new float[]{wall ? wallDepth : 0.5f,0,0,1});
                if (unit == 3) pixels.put(new float[]{0.2f,0.7f,metal ? marker : 0,1});
            }
            pixels.flip();
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, INPUTS[unit]);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, 8, 8, 0,
                    GL11.GL_RGBA, GL11.GL_FLOAT, pixels);
        }
    }

    private static float[] render() {
        GL11.glClearColor(0,0,0,0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1,-1); GL11.glVertex2f(1,-1);
        GL11.glVertex2f(1,1); GL11.glVertex2f(-1,1);
        GL11.glEnd();
        FloatBuffer pixel = BufferUtils.createFloatBuffer(4);
        GL11.glReadPixels(4,4,1,1,GL11.GL_RGBA,GL11.GL_FLOAT,pixel);
        float[] rgba = new float[4];
        pixel.get(rgba);
        check(GL11.glGetError() == GL11.GL_NO_ERROR, "Cave probe OpenGL error");
        for (float value : rgba) check(!Float.isNaN(value) && !Float.isInfinite(value), "Non-finite cave light");
        return rgba;
    }

    private static void same(float[] expected, float[] actual, String label) {
        for (int channel = 0; channel < 3; channel++) near(expected[channel], actual[channel], label);
    }

    private static void near(float expected, float actual, String label) {
        check(Math.abs(expected - actual) < 0.0001f,
                label + ": expected=" + expected + " actual=" + actual);
        checks++;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

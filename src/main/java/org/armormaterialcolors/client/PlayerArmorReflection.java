package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.Matrix;
import com.wurmonline.client.renderer.backend.Primitive;
import com.wurmonline.client.renderer.backend.Queue;
import com.wurmonline.client.renderer.backend.RenderState;
import com.wurmonline.client.renderer.shaders.Program;
import com.wurmonline.client.renderer.shaders.ProgramBindings;
import com.wurmonline.client.resources.textures.PlayerTextureBuilderGL;
import com.wurmonline.client.resources.textures.Texture;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Carries the Plate/Chain reflection mask through Wurm's player atlas. */
public final class PlayerArmorReflection {
    private static final Logger LOG = Logger.getLogger("ArmorMaterialColors");
    // Keep the paperdoll/portrait texture visually opaque. The 3D skin shader
    // can still distinguish this exact RGBA8 value from ordinary alpha 1.0.
    static final float BAKED_METAL_ALPHA = 254.0f / 255.0f;
    private static final Primitive.CustomPrimitive ALPHA_ONLY =
            new Primitive.CustomPrimitive() {
                @Override
                public void preRender(Primitive primitive) {
                    GL11.glColorMask(false, false, false, true);
                }

                @Override
                public void postRender(Primitive primitive) {
                    GL11.glColorMask(true, true, true, true);
                }
            };

    private static final Field QUEUE_FIELD;
    private static Program maskProgram;
    private static ProgramBindings maskBindings;
    private static boolean programAttempted;

    public static String redirectProgramName(String name) {
        if ("program.skin_gbuffer".equals(name)) {
            return "program.player_skin_reflection";
        }
        if ("program.masked.nolight".equals(name)) {
            return "program.player_armor_compositor";
        }
        return name;
    }

    static {
        Field field = null;
        try {
            field = PlayerTextureBuilderGL.class.getDeclaredField("queue");
            field.setAccessible(true);
        } catch (ReflectiveOperationException error) {
            LOG.log(Level.SEVERE,
                    "Unable to access the player texture compositor queue", error);
        }
        QUEUE_FIELD = field;
    }

    private PlayerArmorReflection() {
    }

    public static void queueMetalMask(
            PlayerTextureBuilderGL builder,
            Texture diffuse,
            int x,
            int y,
            int width,
            int height,
            Texture materialMask,
            Color materialColor) {
        if (!isReflective(materialColor) || materialMask == null
                || diffuse == null || QUEUE_FIELD == null) {
            return;
        }

        try {
            ensureProgram();
            if (maskProgram == null) {
                return;
            }

            Queue queue = (Queue) QUEUE_FIELD.get(builder);
            if (queue == null) {
                return;
            }

            Primitive marker = queue.reservePrimitive();
            marker.copyStateFrom(RenderState.RENDERSTATE_DEFAULT);
            marker.texture[0] = diffuse;
            marker.texture[2] = materialMask;
            marker.program = maskProgram;
            marker.bindings = maskBindings;
            marker.vertex = Primitive.staticVertexSquare2D;
            marker.type = Primitive.Type.TRIANGLESTRIP;
            marker.num = 2;
            marker.blendmode = Primitive.BlendMode.OPAQUE;
            marker.twosided = true;
            marker.custom = ALPHA_ONLY;
            marker.setColor(1.0f, 1.0f, 1.0f, BAKED_METAL_ALPHA);

            Matrix transform = new Matrix();
            transform.fromTranslationAndNonUniformScale(
                    (float) x,
                    (float) y,
                    0.0f,
                    (float) width,
                    (float) height,
                    0.0f);
            queue.queue(marker, transform);

        } catch (Throwable error) {
            LOG.log(Level.SEVERE,
                    "Unable to bake the worn-armour reflection mask", error);
        }
    }

    static boolean isReflective(Color materialColor) {
        return materialColor != null && materialColor.a < 0.0f;
    }

    private static synchronized void ensureProgram() {
        if (programAttempted) {
            return;
        }
        programAttempted = true;
        maskProgram = Program.load("program.player_metal_mask");
        if (maskProgram == null) {
            LOG.severe("Unable to load program.player_metal_mask");
            return;
        }

        maskBindings = new ProgramBindings();
        maskBindings.bindUniformSampler(
                maskProgram.getUniformByName("tex2").getLocation(), 2);
    }

}

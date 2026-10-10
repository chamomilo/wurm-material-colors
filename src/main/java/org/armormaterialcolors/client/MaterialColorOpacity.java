package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.shaders.Program;

/** Keeps reflection metadata out of shaders that interpret alpha as opacity. */
public final class MaterialColorOpacity {
    private MaterialColorOpacity() {
    }

    public static Color forProgram(Color color, Program program) {
        if (color == null || color.a >= 0.0f) {
            return color;
        }
        return forReflectionSupport(color, program != null
                && program.getUniformByName("armorReflectionTuning0") != null);
    }

    static Color forReflectionSupport(Color color, boolean understandsMarker) {
        if (color == null || color.a >= 0.0f || understandsMarker) {
            return color;
        }
        // Preserve the original shared colour: the same item can also render
        // through the deferred, shadow, paperdoll and reflection passes.
        return new Color(color.r, color.g, color.b, 1.0f);
    }
}

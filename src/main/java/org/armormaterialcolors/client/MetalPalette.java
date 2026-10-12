package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;

/** One High Res Icons palette adapted to Wurm's texture colour space. */
final class MetalPalette {
    private MetalPalette() {
    }

    static Color color(String material, float alpha) {
        Color reference = referenceColor(material, alpha);
        if (reference == null) return null;
        // High Res Icons multiplies decoded pixels in linear light, then
        // encodes them with gamma 2.2. Wurm uploads diffuse DDS textures as
        // RGB8/RGBA8 (not sRGB) and its mask shaders multiply encoded pixels.
        // Raising each multiplier to 1/2.2 gives the same fully masked colour
        // without changing stock shaders, non-metal pixels or opacity.
        // Darken lead by 15% for the requested adjustment, retaining its hue.
        float brightness = "lead".equals(material) ? 0.85f : 1.0f;
        return new Color(encodedMultiplier(reference.r) * brightness,
                encodedMultiplier(reference.g) * brightness,
                encodedMultiplier(reference.b) * brightness, alpha);
    }

    private static float encodedMultiplier(float linearMultiplier) {
        return (float) Math.pow(linearMultiplier, 1.0 / 2.2);
    }

    static Color referenceColor(String material, float alpha) {
        // High Res Icons: ToolMaterialPalette.referenceColour, calibrated to
        // the user's saved metal references on 2026-10-11. Values above one
        // brighten the neutral diffuse texture; normalising/clamping them here
        // would lose the white zinc/light steel/dark iron distinctions.
        if ("gold".equals(material)) return new Color(1.45f, 1.10f, 0.12f, alpha);
        if ("silver".equals(material)) return new Color(1.65f, 1.70f, 1.72f, alpha);
        if ("zinc".equals(material)) return new Color(2.10f, 2.12f, 2.08f, alpha);
        if ("copper".equals(material)) return new Color(1.30f, 0.37f, 0.16f, alpha);
        if ("steel".equals(material)) return new Color(1.12f, 1.17f, 1.24f, alpha);
        if ("iron".equals(material)) return new Color(0.62f, 0.66f, 0.70f, alpha);
        if ("brass".equals(material)) return new Color(1.12f, 0.84f, 0.27f, alpha);
        if ("tin".equals(material)) return new Color(1.30f, 1.38f, 1.50f, alpha);
        if ("lead".equals(material)) return new Color(0.48f, 0.50f, 0.54f, alpha);
        if ("electrum".equals(material)) return new Color(1.30f, 1.36f, 0.25f, alpha);

        // The remaining High Res Icons metals use its game-XML fallbacks.
        if ("bronze".equals(material)) return new Color(0.588f, 0.384f, 0.215f, alpha);
        if ("glimmersteel".equals(material)) return new Color(0.86f, 0.82f, 0.59f, alpha);
        if ("adamantine".equals(material)) return new Color(0.55f, 0.67f, 0.86f, alpha);
        if ("seryll".equals(material)) return new Color(0.65f, 0.17f, 0.06f, alpha);
        return null;
    }

    static boolean isMetal(String material) {
        if (material == null) return false;
        switch (material) {
            case "gold": case "silver": case "zinc": case "copper":
            case "steel": case "iron": case "brass": case "tin":
            case "lead": case "electrum": case "bronze": case "glimmersteel":
            case "adamantine": case "seryll":
                return true;
            default:
                return false;
        }
    }

    static boolean isReflective(String material) {
        return "gold".equals(material) || "silver".equals(material)
                || "adamantine".equals(material) || "seryll".equals(material)
                || "glimmersteel".equals(material);
    }
}

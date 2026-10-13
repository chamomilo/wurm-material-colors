package org.armormaterialcolors.client;

import org.chamomilo.wurm.update.ModSetting;
import java.util.*;

/** Product metadata for the shared native Settings editor. */
final class ModSettingsSchema {
    private ModSettingsSchema() { }
    static List<ModSetting> specs() {
        return Arrays.asList(
            ModSetting.number("Reflections", "reflectionStrength", "Reflection strength", "Opacity of the environment reflection on reflective metal.", 0.3, 0, 1, false, true),
            ModSetting.number("Reflections", "sunGlintStrength", "Sun glint strength", "Intensity of direct sunlight highlights.", 0.28, 0, 1, false, true),
            ModSetting.number("Reflections", "localGlintStrength", "Local glint strength", "Intensity of highlights from local light sources.", 0.32, 0, 1, false, true),
            ModSetting.number("Reflections", "caveLocalReflectionBoost", "Cave local reflection boost", "Multiplier for local-light reflections while underground.", 2.5, 0, 4, false, true),
            ModSetting.number("Reflections", "glintSharpness", "Glint sharpness", "Concentration of metal highlights.", 0.45, 0, 1, false, true),
            ModSetting.number("Reflections", "reflectionTint", "Reflection tint", "Amount of metal color in reflected light.", 0.72, 0, 1, false, true),
            ModSetting.number("Reflections", "frontReflectionDetail", "Front reflection detail", "Additional detail in forward-facing metal reflections.", 0, 0, 1, false, true),
            ModSetting.number("Reflections", "environmentBrightness", "Environment brightness", "Brightness multiplier for reflected surroundings.", 1, 0.25, 2, false, true),
            ModSetting.number("Reflections", "ssrDetail", "Screen reflection detail", "Detail strength in reflections of visible objects.", 1, 0, 1.5, false, true),
            ModSetting.number("Reflections", "reflectionContrast", "Reflection contrast", "Contrast multiplier applied to reflected light.", 1, 0.5, 2, false, true),
            ModSetting.number("Reflections", "skyBrightness", "Sky brightness", "Brightness of the sky portion of reflections.", 1, 0.25, 2, false, true),
            ModSetting.number("Reflections", "groundBrightness", "Ground brightness", "Brightness of the ground portion of reflections.", 1, 0.25, 2, false, true),
            ModSetting.number("Reflections", "caveBrightness", "Cave brightness", "Brightness of underground reflection surroundings.", 1, 0.25, 2, false, true),
            ModSetting.number("Reflections", "sunDiscStrength", "Sun disc strength", "Strength of the reflected sun disc.", 1.5, 0, 3, false, true),
            ModSetting.number("Reflections", "ssrRayDistance", "Reflection ray distance", "Multiplier for the screen reflection ray distance.", 1, 0.5, 2, false, true),
            ModSetting.number("Reflections", "ssrThickness", "Reflection thickness", "Thickness tolerance used to find screen reflection surfaces.", 1, 0.5, 3, false, true),
            ModSetting.number("Reflections", "backdropContrast", "Backdrop contrast", "Contrast of the environment reflection backdrop.", 1.12, 0.5, 2, false, true),
            ModSetting.text("Resources", "resourcePack", "Material resource pack", "Resource pack supplying metal and wood masks and reflection shaders, loaded at startup.", "mods/armor-material-colors/armor-material-colors-resources-" + ArmorMaterialColorsMod.VERSION + ".jar", false)
        );
    }
}

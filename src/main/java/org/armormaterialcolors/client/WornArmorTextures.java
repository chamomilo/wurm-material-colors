package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.model.collada.material.ColladaMaterial;
import com.wurmonline.client.resources.ResourceUrl;

import java.util.Locale;

/** Uses the untinted stock atlas before applying the shared metal palette. */
public final class WornArmorTextures {
    private static final String[] CHAIN_TEXTURES = {
            "chain_boots_LL_hasAlpha.dds", "chain_boot_foot.dds",
            "Chain_Coif_lower_hasAlpha.dds", "chain_glove_hand.dds",
            "chain_glove_LA_hasAlpha.dds", "chain_jacket_tasset.dds",
            "chain_jacket_torso_hasAlpha.dds", "chain_jacket_UA.dds",
            "chain_pants_legs.dds", "chain_pants_LL.dds",
            "chain_sleeve_LA.dds", "chain_sleeve_UA.dds"
    };
    private static final String[] PLATE_TEXTURES = {
            "plate_boots_foot.dds", "plate_boots_LL.dds",
            "plate_gloves_hand.dds", "plate_gloves_LA.dds",
            "plate_jacket_tasset.dds", "plate_jacket_torso.dds",
            "plate_jacket_UA.dds", "plate_pants_legs.dds",
            "plate_sleeve_LA_hasAlpha.dds", "plate_sleeve_UA.dds"
    };

    private WornArmorTextures() {
    }

    public static String maskTexturePath(String texturePath) {
        // The stock neutral diffuse filenames omit hasAlpha for these two
        // layers, but the matching masks retain it. Both the preload set and
        // compositor lookup must use this same key.
        if ("textures/Masks/Chain/chain_jacket_UA.dds".equals(texturePath)) {
            return "textures/Masks/Chain/chain_jacket_UA_hasAlpha.dds";
        }
        if ("textures/Masks/Chain/chain_sleeve_LA.dds".equals(texturePath)) {
            return "textures/Masks/Chain/chain_sleeve_LA_hasAlpha.dds";
        }
        return texturePath;
    }

    public static void neutralizeWorldTexture(ColladaMaterial material, ResourceUrl modelUrl) {
        if (material != null && modelUrl != null) {
            material.setTextureName(neutralWorldTexture(
                    modelUrl.getFilePath(), material.getTextureName()));
        }
    }

    static String neutralWorldTexture(String modelPath, String textureName) {
        if (modelPath == null || textureName == null) return textureName;
        String path = modelPath.replace('\\', '/').toLowerCase(Locale.ROOT);
        String texture = textureName.toLowerCase(Locale.ROOT);
        if ("player/helms/chain_coif_head.wom".equals(path)
                && ("chain_coif_upper_ada.dds".equals(texture)
                || "chain_coif_upper_gl.dds".equals(texture))) {
            return "Chain_Coif_upper.dds";
        }
        return textureName;
    }

    public static String neutralTexture(String modelName, String textureName) {
        if (textureName == null
                || ArmorMaterialColorsMod.materialForSupportedArmor(modelName) == null) {
            return textureName;
        }
        String[] path = textureName.replace('\\', '/').split("/");
        if (path.length != 4 || !"textures".equals(path[0])) return textureName;
        String folder = path[2].toLowerCase(Locale.ROOT);
        if (!MetalPalette.isMetal(folder) && !"glimmer".equals(folder)) return textureName;
        String[] textures = "Chain".equals(path[1]) ? CHAIN_TEXTURES
                : "Plate".equals(path[1]) ? PLATE_TEXTURES : null;
        if (textures == null) return textureName;
        for (String base : textures) {
            // Chain's coloured UA and Copper LA use a hasAlpha filename,
            // whereas their neutral counterparts omit that filename marker.
            if (path[3].equals(base) || path[3].equals(base.replace(".dds", "_hasAlpha.dds"))) {
                return "textures/" + path[1] + "/" + base;
            }
        }
        return textureName;
    }
}

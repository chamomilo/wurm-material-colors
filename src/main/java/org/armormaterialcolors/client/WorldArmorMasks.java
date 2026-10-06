package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.model.collada.importer.ColladaModel;
import com.wurmonline.client.resources.ResourceUrl;

import java.util.Locale;
import java.util.logging.Logger;

/** Bridges stock WOM URLs to the full-metal mask stored in the mod pack. */
public final class WorldArmorMasks {
    static final String MASK_MARKER = "armor_material_colors_world_mm.dds";
    private static final String MASK_RESOURCE = "armor.material.world.mask";
    private static final Logger LOG = Logger.getLogger("ArmorMaterialColors");
    private static volatile ResourceUrl resolvedMask;

    private WorldArmorMasks() {
    }

    /** Called immediately after the game's own properties.xml lookup. */
    public static void attachMask(ColladaModel model, ResourceUrl modelUrl) {
        if (model == null || modelUrl == null || model.isMasked()
                || !ReflectionResources.isLoaded()
                || !isStandaloneArmorResource(modelUrl.getFilePath())) {
            return;
        }

        model.mask = MASK_MARKER;
    }

    /** Called only around ResourceUrl.derive inside model texture preloading. */
    public static ResourceUrl resolveDerivedMask(String derivedFileName) {
        if (!MASK_MARKER.equals(derivedFileName)) {
            return null;
        }

        if (resolvedMask == null) {
            LOG.severe("Preloaded world armour material mask is unavailable: "
                    + MASK_RESOURCE);
        }
        return resolvedMask;
    }

    public static void registerResolvedMask(ResourceUrl mask) {
        resolvedMask = mask;
    }

    static boolean isStandaloneArmorResource(String resourcePath) {
        if (resourcePath == null) {
            return false;
        }
        String normalized = resourcePath.replace('\\', '/')
                .toLowerCase(Locale.ROOT);
        return normalized.endsWith(".wom")
                && (normalized.startsWith("clothing/armor/plate/")
                || normalized.startsWith("clothing/armor/chain/"));
    }
}

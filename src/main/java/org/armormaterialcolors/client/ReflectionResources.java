package org.armormaterialcolors.client;

import com.wurmonline.client.WurmClientBase;
import com.wurmonline.client.resources.ResourceUrl;
import org.gotti.wurmunlimited.modsupport.packs.ModPacks;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReflectionResources {
    private static final String SHADER_RESOURCE = "shader.dirlight.fragment";
    private static final String SHADER_SIGNATURE =
            "ARMOR_MATERIAL_REFLECTION_SHADER_V0316";
    private static final String POINTLIGHT_SHADER_RESOURCE =
            "shader.pointlight.fragment";
    private static final String POINTLIGHT_SHADER_SIGNATURE =
            "ARMOR_MATERIAL_POINTLIGHT_REFLECTION_SHADER_V0316";
    private static final String PLAYER_GBUFFER_RESOURCE =
            "shader.skin_reflection_gbuffer.fragment";
    private static final String PLAYER_GBUFFER_SIGNATURE =
            "ARMOR_MATERIAL_SKIN_GBUFFER_SHADER_V0316";
    private static final String PLAYER_MASK_RESOURCE =
            "shader.player_metal_mask.fragment";
    private static final String PLAYER_MASK_SIGNATURE =
            "ARMOR_MATERIAL_PLAYER_MASK_SHADER_V0316";
    private static final String PLAYER_COMPOSITOR_RESOURCE =
            "shader.player_armor_compositor.fragment";
    private static final String PLAYER_COMPOSITOR_SIGNATURE =
            "ARMOR_MATERIAL_PLAYER_COMPOSITOR_SHADER_V0316";
    private static final Logger LOG = Logger.getLogger("ArmorMaterialColors");
    private static boolean attempted;
    private static boolean loaded;

    private ReflectionResources() {
    }

    public static synchronized void ensurePackLoaded() {
        if (attempted) {
            return;
        }
        attempted = true;

        File pack = new File(ArmorMaterialColorsMod.resourcePack());
        if (!pack.isAbsolute()) {
            pack = new File(System.getProperty("user.dir"), pack.getPath());
        }
        pack = pack.getAbsoluteFile();

        if (!pack.isFile()) {
            LOG.severe("Reflection shader resource pack is missing: " + pack);
            return;
        }

        try {
            loaded = ModPacks.addPack(
                    pack,
                    ModPacks.Options.PREPEND,
                    ModPacks.Options.NORELOAD,
                    ModPacks.Options.NOMAPS,
                    ModPacks.Options.NOARMOR);
            if (loaded) {
                String source = WurmClientBase.getResourceManager()
                        .getResourceAsString(SHADER_RESOURCE);
                String pointLightSource = WurmClientBase.getResourceManager()
                        .getResourceAsString(POINTLIGHT_SHADER_RESOURCE);
                String playerGbufferSource = WurmClientBase.getResourceManager()
                        .getResourceAsString(PLAYER_GBUFFER_RESOURCE);
                String playerMaskSource = WurmClientBase.getResourceManager()
                        .getResourceAsString(PLAYER_MASK_RESOURCE);
                String playerCompositorSource = WurmClientBase.getResourceManager()
                        .getResourceAsString(PLAYER_COMPOSITOR_RESOURCE);
                ResourceUrl worldArmorMask = WurmClientBase.getResourceManager()
                        .getResource("armor.material.world.mask");
                if (source != null && source.contains(SHADER_SIGNATURE)
                        && pointLightSource != null
                        && pointLightSource.contains(POINTLIGHT_SHADER_SIGNATURE)
                        && playerGbufferSource != null
                        && playerGbufferSource.contains(PLAYER_GBUFFER_SIGNATURE)
                        && playerMaskSource != null
                        && playerMaskSource.contains(PLAYER_MASK_SIGNATURE)
                        && playerCompositorSource != null
                        && playerCompositorSource.contains(
                                PLAYER_COMPOSITOR_SIGNATURE)
                        && worldArmorMask != null) {
                    WorldArmorMasks.registerResolvedMask(worldArmorMask);
                } else {
                    loaded = false;
                    logResourceMismatch(SHADER_RESOURCE, source,
                            SHADER_SIGNATURE);
                    logResourceMismatch(POINTLIGHT_SHADER_RESOURCE,
                            pointLightSource, POINTLIGHT_SHADER_SIGNATURE);
                    logResourceMismatch(PLAYER_GBUFFER_RESOURCE,
                            playerGbufferSource, PLAYER_GBUFFER_SIGNATURE);
                    logResourceMismatch(PLAYER_MASK_RESOURCE,
                            playerMaskSource, PLAYER_MASK_SIGNATURE);
                    logResourceMismatch(PLAYER_COMPOSITOR_RESOURCE,
                            playerCompositorSource, PLAYER_COMPOSITOR_SIGNATURE);
                    if (worldArmorMask == null) {
                        LOG.severe("Inactive reflection resource: "
                                + "armor.material.world.mask is missing");
                    }
                    LOG.severe("Reflection pack was registered, but one or more "
                            + "required resources are inactive: " + pack);
                }
            } else {
                LOG.warning("ModLoader did not add reflection shader pack: " + pack);
            }
        } catch (RuntimeException error) {
            LOG.log(Level.SEVERE,
                    "Unable to load reflection shader pack " + pack,
                    error);
        }
    }

    static synchronized boolean isLoaded() {
        return loaded;
    }

    private static void logResourceMismatch(
            String name, String source, String expectedSignature) {
        if (source == null) {
            LOG.severe("Inactive reflection resource: " + name
                    + " is missing");
        } else if (!source.contains(expectedSignature)) {
            LOG.severe("Inactive reflection resource: " + name
                    + " does not contain " + expectedSignature);
        }
    }
}

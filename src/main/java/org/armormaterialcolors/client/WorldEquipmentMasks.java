package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.model.collada.importer.ColladaGeometry;
import com.wurmonline.client.renderer.model.collada.importer.ColladaModel;
import com.wurmonline.client.renderer.model.collada.importer.TriangleMesh;
import com.wurmonline.client.renderer.model.collada.material.ColladaMaterial;
import com.wurmonline.client.resources.ResourceUrl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Restores stock semantic masks on pile meshes sharing the original UV atlas. */
public final class WorldEquipmentMasks {
    private static final Map<String, String> TEXTURE_MASKS = textureMasks();

    private WorldEquipmentMasks() {
    }

    public static void attachMasks(ColladaModel model, ResourceUrl modelUrl) {
        if (model != null && modelUrl != null) {
            attachMasks(model, modelUrl.getFilePath());
        }
    }

    static void attachMasks(ColladaModel model, String modelPath) {
        if (model == null || model.mask != null || modelPath == null
                || model.getGeometryArray() == null) {
            return;
        }
        // Keep custom and stock masks authoritative. Add only absent mesh
        // entries, using the matching diffuse atlas rather than the pile name.
        for (ColladaGeometry geometry : model.getGeometryArray()) {
            if (geometry == null || geometry.getTriangleMeshArray() == null) continue;
            for (TriangleMesh mesh : geometry.getTriangleMeshArray()) {
                if (mesh == null) continue;
                ColladaMaterial material = mesh.getColladaMaterial();
                if (material == null) continue;
                String mask = maskForTexture(modelPath, material.getTextureName());
                if (mask == null) continue;
                if (model.meshMasks == null) model.meshMasks = new HashMap<String, String>();
                if (!model.meshMasks.containsKey(mesh.getName())) {
                    model.meshMasks.put(mesh.getName(), mask);
                }
            }
        }
    }

    static String maskForTexture(String modelPath, String textureName) {
        if (modelPath == null || textureName == null) return null;
        String path = modelPath.replace('\\', '/').toLowerCase(Locale.ROOT);
        if (!path.endsWith(".wom")) return null;
        // These share an atlas but the stock item uses the separate wood
        // material mask. A metal mask must not replace its wood treatment.
        if (path.equals("tools/clayshaperpile.wom") || path.equals("tools/spindlepile.wom")
                || path.equals("tools/spatulapile.wom") || path.equals("tools/malletpile.wom")
                || path.equals("tools/malletheadpile.wom") || path.equals("tools/handlepile.wom")
                || path.equals("weapon/polearms/staffpile.wom")) return null;
        int slash = path.lastIndexOf('/');
        if (slash < 0) return null;
        String texture = textureName.replace('\\', '/').toLowerCase(Locale.ROOT);
        return TEXTURE_MASKS.get(path.substring(0, slash + 1) + texture);
    }

    private static Map<String, String> textureMasks() {
        Map<String, String> masks = new HashMap<String, String>();
        // Each entry was checked against graphics.jar properties.xml and the
        // original WOM diffuse atlas. Never fill a compound item with green:
        // these masks retain the stock wood/leather/dye/secondary channels.
        masks.put("tools/setone.dds", "s1_mm.dds");
        masks.put("tools/settwo.dds", "settwo_mm.dds");
        masks.put("tools/setthree.dds", "setthree_mm.dds");
        masks.put("tools/setfour.dds", "gm_mm.dds");
        masks.put("tools/setfive.dds", "setfive_mm.dds");
        masks.put("tools/shovel.dds", "shovel_mm.dds");
        masks.put("tools/ironhandel.dds", "gm_mm.dds");
        masks.put("tools/branding_iron_cold.dds", "gm_mm.dds");
        masks.put("tools/lead_anchor.dds", "gm_mm.dds");
        masks.put("tools/forkspoonknifetexture.dds", "gm_mm.dds");
        masks.put("tools/hatchet/hatchet.dds", "hatchet_mm.dds");
        masks.put("tools/butcherknife/burcherknife.dds", "burcherknife_mm.dds");
        masks.put("tools/crowbar/crowbar.dds", "gm_mm.dds");
        masks.put("weapon/axes/axes.dds", "axes_pm.dds");
        masks.put("weapon/swords.dds", "swords_pm.dds");
        masks.put("weapon/polearms/spears.dds", "spears_pm.dds");
        masks.put("weapon/mauls/large_maul_diff_512.dds", "lmaul_pm.dds");
        masks.put("weapon/mauls/maul_diff_512.dds", "maul_pm.dds");
        masks.put("weapon/mauls/smallmaul_512.dds", "smaul_pm.dds");
        masks.put("weapon/shields/smallmetal.dds", "SmallMetal_mm.dds");
        masks.put("weapon/shields/mediummetal.dds", "MediumMetal_mm.dds");
        masks.put("weapon/shields/largemetal.dds", "LargeMetal_mm.dds");
        masks.put("player/helms/openhelmet01.dds", "ohelm_mm.dds");
        masks.put("player/helms/greate_helmet01.dds", "ghelm_mm.dds");
        masks.put("player/helms/basinet_helmet01.dds", "bhelm_mm.dds");
        return Collections.unmodifiableMap(masks);
    }
}

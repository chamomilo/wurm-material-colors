package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.model.collada.importer.*;
import com.wurmonline.client.renderer.model.collada.material.ColladaMaterial;
import org.junit.Test;
import java.util.HashMap;
import static org.junit.Assert.*;

public class WorldEquipmentMasksTest {
    @Test
    public void restoresPileMasksUsingTheirDiffuseAtlas() {
        assertEquals("shovel_mm.dds", WorldEquipmentMasks.maskForTexture("tools/shovelpile.wom", "shovel.dds"));
        assertEquals("setfive_mm.dds", WorldEquipmentMasks.maskForTexture("tools/scythepile.wom", "setfive.dds"));
        assertEquals("spears_pm.dds", WorldEquipmentMasks.maskForTexture("weapon/Polearms/HalberdPile.wom", "spears.dds"));
        assertEquals("SmallMetal_mm.dds", WorldEquipmentMasks.maskForTexture("weapon/Shields/MetalShieldPile.wom", "SmallMetal.dds"));
        assertNull(WorldEquipmentMasks.maskForTexture("custom/HalberdPile.wom", "spears.dds"));
        assertNull(WorldEquipmentMasks.maskForTexture("tools/brushpile.wom", "unknown.dds"));
        assertNull(WorldEquipmentMasks.maskForTexture("tools/malletpile.wom", "setthree.dds"));
        assertNull(WorldEquipmentMasks.maskForTexture("weapon/Polearms/StaffPile.wom", "spears.dds"));
    }

    @Test
    public void eachMeshInAHelmetPileReceivesItsOwnMask() {
        ColladaModel model = model(mesh("open", "OpenHelmet01.dds"), mesh("great", "greate_helmet01.dds"),
                mesh("extra", "custom.dds"));
        WorldEquipmentMasks.attachMasks(model, "player/helms/helmPile.wom");
        assertEquals("ohelm_mm.dds", model.meshMasks.get("open"));
        assertEquals("ghelm_mm.dds", model.meshMasks.get("great"));
        assertFalse(model.meshMasks.containsKey("extra"));
    }

    @Test
    public void existingStockOrCustomMasksRemainAuthoritative() {
        ColladaModel model = model(mesh("open", "OpenHelmet01.dds"), mesh("great", "greate_helmet01.dds"));
        model.mask = "custom-all.dds";
        WorldEquipmentMasks.attachMasks(model, "player/helms/helmPile.wom");
        assertTrue(model.meshMasks == null || model.meshMasks.isEmpty());
        model.mask = null;
        model.meshMasks = new HashMap<String, String>();
        model.meshMasks.put("open", "custom-open.dds");
        WorldEquipmentMasks.attachMasks(model, "player/helms/helmPile.wom");
        assertEquals("custom-open.dds", model.meshMasks.get("open"));
        assertEquals("ghelm_mm.dds", model.meshMasks.get("great"));
    }

    private static TriangleMesh mesh(String name, String texture) {
        TriangleMesh mesh = new TriangleMesh(name);
        ColladaMaterial material = new ColladaMaterial();
        material.setTextureName(texture);
        mesh.setColladaMaterial(material);
        return mesh;
    }

    private static ColladaModel model(TriangleMesh... meshes) {
        ColladaGeometry geometry = new ColladaGeometry("pile");
        geometry.setTriangleMeshArray(meshes);
        ColladaModel model = new ColladaModel("test");
        model.setGeometryArray(new ColladaGeometry[]{geometry});
        return model;
    }
}

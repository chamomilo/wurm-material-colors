package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.model.collada.importer.*;
import com.wurmonline.client.renderer.model.collada.material.ColladaMaterial;
import org.junit.Test;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import static org.junit.Assert.*;

public class WorldWoodMasksTest {
    @Test public void upgradesSavedDefaultPackPathsAndKeepsCustomPaths() {
        assertEquals("mods/armor-material-colors/armor-material-colors-resources-" + ArmorMaterialColorsMod.VERSION + ".jar",
                ArmorMaterialColorsMod.currentResourcePack("mods/armor-material-colors/armor-material-colors-resources-0.4.0.jar"));
        assertEquals("custom/wood-and-metal.jar", ArmorMaterialColorsMod.currentResourcePack("custom/wood-and-metal.jar"));
    }
    @Test public void restoresPmkWagonWithoutColouringFabricAndCratesWithoutColouringSeals() {
        ColladaModel wagon = model(mesh("WagonMesh", "cart_D.dds"), mesh("TentMesh", "wagon_cloth_free.dds"));
        WorldWoodMasks.attachMasks(wagon, "models/pmk/wagons/Wagon_main.wom");
        assertNotNull(wagon.meshMasks.get("WagonMesh"));
        assertFalse(wagon.meshMasks.containsKey("TentMesh"));
        ColladaModel crate = model(mesh("crate", "crate.dds"), mesh("wax", "seal.dds"));
        WorldWoodMasks.attachMasks(crate, "models/Crates/crateLarge_sealed.wom");
        assertNotNull(crate.meshMasks.get("crate"));
        assertFalse(crate.meshMasks.containsKey("wax"));
    }

    @Test public void keepsSubstringMasksAndGlobalCustomMasksAuthoritative() {
        ColladaModel wagon = model(mesh("WagonLod2Mesh", "cart_D.dds"));
        wagon.meshMasks = new HashMap<String, String>();
        wagon.meshMasks.put("wagon", "custom.dds");
        WorldWoodMasks.attachMasks(wagon, "models/pmk/wagons/Wagon_main.wom");
        assertEquals(1, wagon.meshMasks.size());
        assertEquals("custom.dds", wagon.meshMasks.get("wagon"));
        wagon.mask = "global-custom.dds";
        wagon.meshMasks.clear();
        WorldWoodMasks.attachMasks(wagon, "models/pmk/wagons/Wagon_main.wom");
        assertTrue(wagon.meshMasks.isEmpty());
    }

    @Test public void usesTheActualWoodMaskOnPilesAndNeverClaimsSteelStaffsOrUnknownPacks() {
        assertNotNull(WorldWoodMasks.maskForTexture("tools/malletpile.wom", "setthree.dds"));
        assertNotNull(WorldWoodMasks.maskForTexture("weapon/Polearms/StaffPile.wom", "spears.dds"));
        assertNull(WorldWoodMasks.maskForTexture("weapon/Polearms/SteelStaffPile.wom", "spears.dds"));
        assertNull(WorldWoodMasks.maskForTexture("custom/Wagon_main.wom", "cart_D.dds"));
        assertNull(WorldWoodMasks.maskForTexture("models/Cages/creatureCage_cow.wom", "Cow.dds"));
    }

    @Test public void everyGeneratedRuleResolvesToAPackagedMaskAndMapping() throws Exception {
        File project = new File(System.getProperty("armorMaterialColorsProjectDir"));
        File resources = new File(project, "resource-pack/com/wurmonline/client/resources");
        String mappings = new String(Files.readAllBytes(new File(resources, "mappings.txt").toPath()), StandardCharsets.UTF_8);
        assertTrue(WorldWoodMasks.rules().size() >= 400);
        for (String marker : new HashSet<String>(WorldWoodMasks.rules().values())) {
            assertTrue(marker, new File(resources, "wood-masks/" + marker).isFile());
            assertTrue(marker, mappings.contains("wood.material.mask." + marker + " = wood-masks/" + marker));
        }
    }

    private static TriangleMesh mesh(String name, String texture) {
        TriangleMesh mesh = new TriangleMesh(name);
        ColladaMaterial material = new ColladaMaterial();
        material.setTextureName(texture);
        mesh.setColladaMaterial(material);
        return mesh;
    }
    private static ColladaModel model(TriangleMesh... meshes) {
        ColladaGeometry geometry = new ColladaGeometry("test");
        geometry.setTriangleMeshArray(meshes);
        ColladaModel model = new ColladaModel("test");
        model.setGeometryArray(new ColladaGeometry[]{geometry});
        return model;
    }
}

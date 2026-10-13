package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.util.MaterialUtilities;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.*;

public class WoodPaletteTest {
    @Test public void matchesEveryClientWoodMaterialAndIndependentStockXml() throws Exception {
        Set<String> materials = new HashSet<String>();
        for (Field field : ItemMaterials.class.getFields()) {
            if (field.getName().startsWith("MATERIAL_WOOD_")) {
                materials.add(MaterialUtilities.getMaterialString(field.getByte(null)));
            }
        }
        assertEquals(26, materials.size());
        assertEquals(materials, WoodPalette.references().keySet());
        Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(getClass().getResourceAsStream("/wood-itemcolors.xml"));
        for (String material : materials) {
            Color color = WoodPalette.color(material, 1.0f);
            Element node = (Element) xml.getElementsByTagName(material).item(0);
            assertNotNull(material, node);
            String[] axes = {"r", "g", "b"};
            float[] rgb = {color.r, color.g, color.b};
            for (int i = 0; i < 3; i++) assertEquals(material + ":" + axes[i],
                    Float.parseFloat(node.getElementsByTagName(axes[i]).item(0).getTextContent()), rgb[i], 0.00001f);
        }
    }

    @Test public void suppliesDistinctTintsToWorldHeldPileAndVehicleNames() {
        for (String prefix : new String[]{"model.tool.hammer.", "model.pile.mallet.",
                "model.furniture.chair.", "model.transports.medium.wagon.free.",
                "model.transports.medium.wagon.wagoner.loaded."}) {
            Set<String> colours = new HashSet<String>();
            for (String wood : WoodPalette.references().keySet()) {
                Color color = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(prefix + wood + ".equipped", null);
                assertNotNull(wood, color);
                assertEquals(1.0f, color.a, 0);
                assertNull(ArmorMaterialColorsMod.reflectiveMaterialForModel(prefix + wood));
                colours.add(color.r + ":" + color.g + ":" + color.b);
            }
            assertEquals(prefix, 26, colours.size());
        }
    }

    @Test public void usesLastActualMaterialIncludingWoodNamesWithoutWoodSuffix() {
        for (String wood : new String[]{"willow", "walnut", "chestnut", "thorn", "ivy"}) {
            assertEquals(wood, WoodPalette.materialForModel("model.test.gold." + wood));
            assertNull(ArmorMaterialColorsMod.materialForSupportedEquipment("model.test.gold." + wood));
            assertNull(WoodPalette.materialForModel("model.test." + wood + ".gold"));
        }
        assertNull(WoodPalette.materialForModel("model.creature.humanoid.walnut"));
        assertNull(WoodPalette.materialForModel("model.tree.birch.birchwood"));
        assertNull(WoodPalette.materialForModel("model.furniture.chair.wood"));
    }
}

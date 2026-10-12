package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.cell.PlayerArmorBuilderComponent;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MetalPaletteTest {
    static final String[] METALS = {"adamantine", "seryll", "gold", "silver",
            "zinc", "copper", "steel", "iron", "brass", "tin", "lead",
            "electrum", "bronze", "glimmersteel"};
    // Source values from High Res Icons, independent of the mod's conversion.
    static final float[][] ICON_REFERENCE_RGB = {
            {0.55f, 0.67f, 0.86f}, {0.65f, 0.17f, 0.06f},
            {1.45f, 1.10f, 0.12f}, {1.65f, 1.70f, 1.72f},
            {2.10f, 2.12f, 2.08f}, {1.30f, 0.37f, 0.16f},
            {1.12f, 1.17f, 1.24f}, {0.62f, 0.66f, 0.70f},
            {1.12f, 0.84f, 0.27f}, {1.30f, 1.38f, 1.50f},
            {0.48f, 0.50f, 0.54f}, {1.30f, 1.36f, 0.25f},
            {0.588f, 0.384f, 0.215f}, {0.86f, 0.82f, 0.59f}
    };

    static int expectedMaterialPixel(String material, int channel, int value) {
        int index = java.util.Arrays.asList(METALS).indexOf(material);
        double linear = Math.pow(value / 255.0, 2.2) * ICON_REFERENCE_RGB[index][channel];
        // The only explicit departure from the icon palette is darker lead.
        if (material.equals("lead")) linear *= Math.pow(0.85, 2.2);
        return (int) Math.round(Math.pow(Math.min(1.0, linear), 1.0 / 2.2) * 255.0);
    }

    @Test
    public void stockShaderPixelsMatchTheReferenceAndLeadAdjustmentForEveryMetal() {
        for (String metal : METALS) {
            Color color = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor("model.tool.shovel." + metal, null);
            float[] tint = {color.r, color.g, color.b};
            for (int base : new int[]{32, 100, 180, 240}) {
                for (int channel = 0; channel < 3; channel++) {
                    int actual = Math.round(Math.min(255, base * tint[channel]));
                    assertEquals(metal + " channel=" + channel + " base=" + base,
                            expectedMaterialPixel(metal, channel, base), actual);
                }
            }
        }
        // A neutral 180-pixel must become orange copper, not the old red
        // (234, 67, 29) caused by multiplying encoded pixels by linear values.
        assertEquals(203, expectedMaterialPixel("copper", 0, 180));
        assertEquals(115, expectedMaterialPixel("copper", 1, 180));
        assertEquals(78, expectedMaterialPixel("copper", 2, 180));
        assertEquals(110, expectedMaterialPixel("lead", 0, 180));
        assertEquals(112, expectedMaterialPixel("lead", 1, 180));
        assertEquals(116, expectedMaterialPixel("lead", 2, 180));
    }

    @Test
    public void allMaterialsUseOnePaletteAcrossHeldWornDroppedAndPileModels() {
        for (String metal : METALS) {
            Color worn = ArmorMaterialColorsMod.materialColorForModel("model.armour.torso.plate." + metal);
            assertNotNull(metal, worn);
            for (String prefix : new String[]{"model.weapon.halberd.", "model.tool.scythe.",
                    "model.shield.large.", "model.armour.head.openhelm.",
                    "model.armour.torso.plate.", "model.armour.torso.chain.",
                    "model.pile.shovel.", "model.pile.platejacket.", "model.pile.chainjacket.",
                    "model.container.bowl."}) {
                for (String suffix : new String[]{"", ".equipped", ".equipped.female", ".rare"}) {
                    String model = prefix + metal + suffix;
                    // Include absent XML colours (iron) and arbitrary fallback
                    // colours: neither may alter the reference selection.
                    for (Color stock : new Color[]{null, new Color(0.2f, 0.5f, 0.4f)}) {
                        Color actual = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(model, stock);
                        assertEquals(model, worn.r, actual.r, 0.0001f);
                        assertEquals(model, worn.g, actual.g, 0.0001f);
                        assertEquals(model, worn.b, actual.b, 0.0001f);
                        boolean premium = metal.equals("gold") || metal.equals("silver")
                                || metal.equals("seryll") || metal.equals("adamantine")
                                || metal.equals("glimmersteel");
                        assertEquals(model, premium, actual.a < 0.0f);
                        assertEquals(model, premium, PlayerArmorReflection.isReflective(actual));
                        if (!premium) assertEquals(model, 1.0f, actual.a, 0.0f);
                    }
                }
            }
        }
    }

    @Test
    public void materialNamesCannotRecolorCreaturesOrWoodOrMatchSubstrings() {
        Color stock = new Color(0.3f, 0.4f, 0.5f, 0.4f);
        for (String model : new String[]{null, "model.creature.quadraped.horse.gold",
                "model.corpse.horse.gold", "model.tool.hammer.seryllwood",
                "model.tool.hammer.adamantinewood", "model.tool.hammer.gold.oakenwood",
                "model.weapon.club.birchwood", "img.icon.shovel.gold"}) {
            assertSame(model, stock, ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(model, stock));
            assertNull(model, ArmorMaterialColorsMod.reflectiveMaterialForModel(model));
        }
        assertEquals("zinc", ArmorMaterialColorsMod.materialForSupportedEquipment("MODEL.PILE.SHOVEL.ZINC"));
        assertEquals(0.4f, ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                "model.tool.shovel.zinc", stock).a, 0.0f);
        assertEquals(0.3f, stock.r, 0.0f);
        assertEquals(1.0f, ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                "model.tool.shovel.steel", new Color(1, 1, 1, -1)).a, 0.0f);
    }

    @Test
    public void brightAndDarkReferenceMetalsRemainDistinct() {
        Color zinc = MetalPalette.referenceColor("zinc", 1), silver = MetalPalette.referenceColor("silver", 1);
        Color steel = MetalPalette.referenceColor("steel", 1), iron = MetalPalette.referenceColor("iron", 1);
        Color lead = MetalPalette.referenceColor("lead", 1), copper = MetalPalette.referenceColor("copper", 1);
        Color gold = MetalPalette.referenceColor("gold", 1), electrum = MetalPalette.referenceColor("electrum", 1);
        assertTrue(zinc.r > silver.r && zinc.g > silver.g && zinc.b > silver.b);
        assertTrue(steel.r > iron.r && steel.g > iron.g && steel.b > iron.b);
        assertTrue(iron.r > lead.r && iron.g > lead.g && iron.b > lead.b);
        assertTrue(copper.r > copper.g * 3 && copper.g > copper.b * 2);
        assertTrue(gold.r > gold.g && gold.g > gold.b * 8);
        assertTrue(electrum.g > electrum.r && electrum.r > electrum.b * 5);
    }

    @Test
    public void wornAtlasStoresTheSharedColorUnderTheActualNeutralTextureKey() {
        for (String metal : METALS) {
            Map<String, PlayerArmorBuilderComponent> layers = new HashMap<String, PlayerArmorBuilderComponent>();
            PlayerArmorBuilderComponent torso = new PlayerArmorBuilderComponent();
            layers.put("torso", torso);
            String texture = "textures/Plate/plate_jacket_torso.dds";
            ArmorMaterialColorsMod.applyMaterialColor("model.armour.torso.plate." + metal,
                    "torso", texture, layers);
            Color color = torso.getMaterialMap().get(texture);
            Color world = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                    "model.armour.torso.plate." + metal, null);
            assertEquals(metal, world.r, color.r, 0.0f);
            assertEquals(metal, world.g, color.g, 0.0f);
            assertEquals(metal, world.b, color.b, 0.0f);
            assertEquals(metal, world.a, color.a, 0.0f);
        }
    }
}

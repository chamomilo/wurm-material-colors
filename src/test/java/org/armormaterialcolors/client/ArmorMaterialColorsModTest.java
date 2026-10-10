package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ArmorMaterialColorsModTest {
    private static final float EPSILON = 0.0001f;

    @Test
    public void findsMaterialsOnPlateAndChainModels() {
        assertEquals("gold", ArmorMaterialColorsMod.materialForSupportedArmor(
                "model.armour.torso.plate.gold"));
        assertEquals("copper", ArmorMaterialColorsMod.materialForSupportedArmor(
                "model.armour.sleeve.chain.copper"));
        assertEquals("steel", ArmorMaterialColorsMod.materialForSupportedArmor(
                "model.armour.head.chain.steel.equipped.male"));
    }

    @Test
    public void ignoresUnrelatedModelsAndIron() {
        assertNull(ArmorMaterialColorsMod.materialForSupportedArmor(
                "model.armour.torso.leather.gold"));
        assertNull(ArmorMaterialColorsMod.materialColorForModel(
                "model.armour.torso.plate.iron"));
        assertNull(ArmorMaterialColorsMod.materialColorForModel(
                "model.weapon.sword.gold"));
    }

    @Test
    public void findsMetalsOnlyOnWeaponAndToolModels() {
        assertEquals("gold", ArmorMaterialColorsMod.materialForSupportedEquipment(
                "model.weapon.swordlong.gold"));
        assertEquals("copper", ArmorMaterialColorsMod.materialForSupportedEquipment(
                "model.tool.hammer.copper"));
        assertEquals("adamantine", ArmorMaterialColorsMod.materialForSupportedEquipment(
                "model.weapon.axe.medium.adamantine.equipped"));
        assertNull(ArmorMaterialColorsMod.materialForSupportedEquipment(
                "model.armour.torso.plate.gold"));
        assertNull(ArmorMaterialColorsMod.materialForSupportedEquipment(
                "model.tool.hammer.oakenwood"));
    }

    @Test
    public void brightensEquipmentColorWithoutChangingStockHueRatios() {
        Color stockGold = new Color(0.968f, 0.721f, 0.208f);
        Color enhancedGold = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                "model.weapon.swordlong.gold", stockGold);

        assertColor(enhancedGold,
                1.0f, 0.721f / 0.968f, 0.208f / 0.968f);
        assertEquals(-1.0f, enhancedGold.a, EPSILON);
    }

    @Test
    public void leavesIronAndWoodColorsUntouched() {
        Color fallback = new Color(0.7f, 0.7f, 0.7f);
        assertSame(fallback, ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                "model.tool.hammer.iron", fallback));
        assertSame(fallback, ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                "model.weapon.bow.oakenwood", fallback));
    }

    @Test
    public void distinguishesReflectiveArmorFromOtherGoldAndSilverItems() {
        Color fallback = new Color(0.7f, 0.7f, 0.7f);

        Color gold = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                "model.container.bowl.gold", fallback);
        Color silver = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                "model.armour.torso.plate.silver", fallback);

        assertColor(gold, 0.7f, 0.7f, 0.7f);
        assertColor(silver, 0.7f, 0.7f, 0.7f);
        assertEquals(ArmorMaterialColorsMod.REFLECTIVE_ITEM_MARKER,
                gold.a, EPSILON);
        assertEquals(ArmorMaterialColorsMod.REFLECTIVE_ARMOR_MARKER,
                silver.a, EPSILON);
        assertEquals("gold", ArmorMaterialColorsMod.reflectiveMaterialForModel(
                "model.container.bowl.gold"));
        assertEquals("silver", ArmorMaterialColorsMod.reflectiveMaterialForModel(
                "model.weapon.swordlong.silver.equipped"));
    }

    @Test
    public void reflectionStrengthIsConfigurableAndClamped() {
        try {
            Properties settings = new Properties();
            settings.setProperty("reflectionStrength", "1.5");
            new ArmorMaterialColorsMod().configure(settings);

            Color silver = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                    "model.weapon.swordlong.silver",
                    new Color(0.68f, 0.756f, 0.776f));
            assertEquals(-1.0f, silver.a, EPSILON);
            assertEquals(1.0f,
                    ArmorMaterialColorsMod.reflectionStrength(), EPSILON);
        } finally {
            ArmorMaterialColorsMod.resetSettingsForTests();
        }
    }

    @Test
    public void shippedConfigCarriesTheCurrentReflectionDefaults()
            throws Exception {
        File project = new File(System.getProperty(
                "armorMaterialColorsProjectDir"));
        File config = new File(project,
                "mods/armor-material-colors.properties");
        Properties settings = new Properties();
        FileInputStream input = new FileInputStream(config);
        try {
            settings.load(input);
        } finally {
            input.close();
        }

        assertSetting(settings, "reflectionStrength",
                ReflectionTuning.DEFAULT_REFLECTION_STRENGTH);
        assertSetting(settings, "sunGlintStrength",
                ReflectionTuning.DEFAULT_SUN_GLINT_STRENGTH);
        assertSetting(settings, "localGlintStrength",
                ReflectionTuning.DEFAULT_LOCAL_GLINT_STRENGTH);
        assertSetting(settings, "glintSharpness",
                ReflectionTuning.DEFAULT_GLINT_SHARPNESS);
        assertSetting(settings, "reflectionTint",
                ReflectionTuning.DEFAULT_REFLECTION_TINT);
        assertSetting(settings, "frontReflectionDetail",
                ReflectionTuning.DEFAULT_FRONT_REFLECTION_DETAIL);
        assertSetting(settings, "environmentBrightness",
                ReflectionTuning.DEFAULT_ENVIRONMENT_BRIGHTNESS);
        assertSetting(settings, "ssrDetail",
                ReflectionTuning.DEFAULT_SSR_DETAIL);
        assertSetting(settings, "reflectionContrast",
                ReflectionTuning.DEFAULT_REFLECTION_CONTRAST);
        assertSetting(settings, "skyBrightness",
                ReflectionTuning.DEFAULT_SKY_BRIGHTNESS);
        assertSetting(settings, "groundBrightness",
                ReflectionTuning.DEFAULT_GROUND_BRIGHTNESS);
        assertSetting(settings, "caveBrightness",
                ReflectionTuning.DEFAULT_CAVE_BRIGHTNESS);
        assertSetting(settings, "sunDiscStrength",
                ReflectionTuning.DEFAULT_SUN_DISC_STRENGTH);
        assertSetting(settings, "ssrRayDistance",
                ReflectionTuning.DEFAULT_SSR_RAY_DISTANCE);
        assertSetting(settings, "ssrThickness",
                ReflectionTuning.DEFAULT_SSR_THICKNESS);
        assertSetting(settings, "backdropContrast",
                ReflectionTuning.DEFAULT_BACKDROP_CONTRAST);
    }

    @Test
    public void usesVanillaMetalRgbValues() {
        Color gold = ArmorMaterialColorsMod.materialColorForModel(
                "model.armour.torso.plate.gold");
        assertColor(gold, 0.968f, 0.721f, 0.208f);
        assertEquals(ArmorMaterialColorsMod.REFLECTIVE_ARMOR_MARKER,
                gold.a, EPSILON);

        Color silver = ArmorMaterialColorsMod.materialColorForModel(
                "model.armour.torso.chain.silver");
        assertEquals(ArmorMaterialColorsMod.REFLECTIVE_ARMOR_MARKER,
                silver.a, EPSILON);

        assertColor(
                ArmorMaterialColorsMod.materialColorForModel(
                        "model.armour.torso.chain.copper"),
                0.720f, 0.450f, 0.218f);
        assertColor(
                ArmorMaterialColorsMod.materialColorForModel(
                        "model.armour.leg.plate.adamantine"),
                0.550f, 0.670f, 0.860f);
        assertEquals(1.0f,
                ArmorMaterialColorsMod.materialColorForModel(
                        "model.armour.torso.chain.copper").a,
                EPSILON);
    }

    @Test
    public void seryllAndAdamantineUseTheirStockHueAndExistingReflectionProfiles() {
        for (String material : new String[]{"seryll", "adamantine"}) {
            float[] rgb = "seryll".equals(material)
                    ? new float[]{0.650f, 0.170f, 0.060f}
                    : new float[]{0.550f, 0.670f, 0.860f};
            for (String family : new String[]{"plate", "chain"}) {
                for (String part : new String[]{"torso", "leg", "sleeve", "hand", "foot", "head"}) {
                    String model = "model.armour." + part + "." + family + "."
                            + material + ".equipped.male";
                    Color color = ArmorMaterialColorsMod.materialColorForModel(model);
                    assertColor(color, rgb[0], rgb[1], rgb[2]);
                    assertEquals(model, ArmorMaterialColorsMod.REFLECTIVE_ARMOR_MARKER,
                            color.a, EPSILON);
                }
            }
            for (String model : new String[]{"model.weapon.halberd.", "model.weapon.swordlong.",
                    "model.tool.scythe.", "model.tool.shovel.", "model.tool.hammer.",
                    "model.shield.large.", "model.armour.head.openhelm.",
                    "model.armour.head.greathelm.", "model.armour.head.basinet."}) {
                String equipped = model + material + ".equipped";
                Color color = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                        equipped, new Color(rgb[0], rgb[1], rgb[2]));
                assertEquals(equipped, ArmorMaterialColorsMod.REFLECTIVE_ITEM_MARKER,
                        color.a, EPSILON);
                // Equipment brightening preserves the stock hue ratios.
                assertEquals(rgb[0] / rgb[2], color.r / color.b, EPSILON);
                assertEquals(rgb[1] / rgb[2], color.g / color.b, EPSILON);
                assertEquals(material, ArmorMaterialColorsMod.reflectiveMaterialForModel(equipped));
            }
        }
        assertNull(ArmorMaterialColorsMod.reflectiveMaterialForModel("model.tool.hammer.steel"));
        assertNull(ArmorMaterialColorsMod.reflectiveMaterialForModel("model.tool.hammer.seryllwood"));
    }

    @Test
    public void redirectsOnlyTheTwoPlayerPrograms() {
        assertEquals("program.player_skin_reflection",
                PlayerArmorReflection.redirectProgramName(
                        "program.skin_gbuffer"));
        assertEquals("program.player_armor_compositor",
                PlayerArmorReflection.redirectProgramName(
                        "program.masked.nolight"));
        assertEquals("program.dirlight",
                PlayerArmorReflection.redirectProgramName("program.dirlight"));
    }

    @Test
    public void bakedMetalMarkerRemainsVisuallyOpaqueInPaperdolls() {
        assertEquals(254.0f / 255.0f,
                PlayerArmorReflection.BAKED_METAL_ALPHA, EPSILON);
        org.junit.Assert.assertTrue(
                PlayerArmorReflection.BAKED_METAL_ALPHA > 0.99f);
        org.junit.Assert.assertTrue(
                PlayerArmorReflection.BAKED_METAL_ALPHA < 1.0f);
    }

    private static void assertColor(Color color, float r, float g, float b) {
        assertEquals(r, color.r, EPSILON);
        assertEquals(g, color.g, EPSILON);
        assertEquals(b, color.b, EPSILON);
    }

    private static void assertSetting(Properties settings, String name,
                                      float expected) {
        assertEquals(expected,
                Float.parseFloat(settings.getProperty(name)), EPSILON);
    }
}

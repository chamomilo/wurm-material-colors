package org.armormaterialcolors.client;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorldArmorMasksTest {
    @Test
    public void recognizesStandalonePlateAndChainWomModels() {
        assertTrue(WorldArmorMasks.isStandaloneArmorResource(
                "clothing/Armor/Plate/plateJacket.wom"));
        assertTrue(WorldArmorMasks.isStandaloneArmorResource(
                "clothing\\Armor\\Chain\\chainJacketPile.wom"));
    }

    @Test
    public void ignoresOtherModelsAndTextures() {
        assertFalse(WorldArmorMasks.isStandaloneArmorResource(
                "weapon/Swords/longSword.wom"));
        assertFalse(WorldArmorMasks.isStandaloneArmorResource(
                "clothing/Armor/Plate/plate_jacket.dds"));
        assertFalse(WorldArmorMasks.isStandaloneArmorResource(null));
    }
}

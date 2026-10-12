package org.armormaterialcolors.client;

import org.junit.Test;
import static org.junit.Assert.*;

public class WornArmorTexturesTest {
    @Test
    public void neutralizesColoredAtlasLayersWithoutChangingTheirMasksOrCutouts() {
        for (String material : MetalPaletteTest.METALS) {
            String chain = "model.armour.torso.chain." + material;
            String plate = "model.armour.torso.plate." + material;
            String folder = material.equals("glimmersteel") ? "Glimmer" : material;
            assertEquals("textures/Chain/chain_jacket_torso_hasAlpha.dds",
                    WornArmorTextures.neutralTexture(chain, "textures/Chain/" + folder
                            + "/chain_jacket_torso_hasAlpha.dds"));
            assertEquals("textures/Plate/plate_jacket_torso.dds",
                    WornArmorTextures.neutralTexture(plate, "textures/Plate/" + material
                            + "/plate_jacket_torso.dds"));
        }
        assertEquals("textures/Chain/chain_jacket_UA.dds", WornArmorTextures.neutralTexture(
                "model.armour.torso.chain.gold", "textures/Chain/Gold/chain_jacket_UA_hasAlpha.dds"));
        assertEquals("textures/Chain/chain_sleeve_LA.dds", WornArmorTextures.neutralTexture(
                "model.armour.sleeve.chain.copper", "textures/Chain/Copper/chain_sleeve_LA_hasAlpha.dds"));
    }

    @Test
    public void retainsRarityOverlaysAndUnrelatedOrCustomTextures() {
        String model = "model.armour.torso.plate.gold";
        for (String texture : new String[]{null, "textures/Rare/model.armour.torso.plate.gold.png",
                "textures/Plate/plate_jacket_torso.dds", "textures/Leather/Gold/jacket.dds",
                "textures/Plate/Custom/plate_jacket_torso.dds", "textures/Plate/Gold/custom.dds"}) {
            assertEquals(texture, WornArmorTextures.neutralTexture(model, texture));
        }
        String colored = "textures/Plate/Gold/plate_jacket_torso.dds";
        assertEquals(colored, WornArmorTextures.neutralTexture("model.armour.torso.leather.gold", colored));
    }

    @Test
    public void equippedFantasyCoifUsesTheSameUntintedBaseAsOtherMetals() {
        assertEquals("Chain_Coif_upper.dds", WornArmorTextures.neutralWorldTexture(
                "player/helms/chain_coif_head.wom", "Chain_Coif_upper_Ada.dds"));
        assertEquals("Chain_Coif_upper.dds", WornArmorTextures.neutralWorldTexture(
                "player/helms/chain_coif_head.wom", "Chain_Coif_upper_Gl.dds"));
        assertEquals("Chain_Coif_upper_Ada.dds", WornArmorTextures.neutralWorldTexture(
                "custom/chain_coif_head.wom", "Chain_Coif_upper_Ada.dds"));
    }

    @Test
    public void preloaderAndCompositorResolveTheSameExistingChainMasks() {
        for (String texture : new String[]{"chain_jacket_UA.dds", "chain_sleeve_LA.dds"}) {
            String neutral = "textures/Chain/" + texture;
            String preloaded = WornArmorTextures.maskTexturePath(neutral.replace("textures", "textures/Masks"));
            String lookedUp = WornArmorTextures.maskTexturePath(
                    neutral.replace("textures/", "textures/1024/").replace("1024", "Masks"));
            assertEquals(preloaded, lookedUp);
            assertTrue(preloaded.endsWith("_hasAlpha.dds"));
            assertEquals(neutral, WornArmorTextures.maskTexturePath(neutral));
        }
    }
}

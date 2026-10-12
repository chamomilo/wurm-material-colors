package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import org.junit.Test;

import static org.junit.Assert.*;

public class MaterialColorOpacityTest {
    @Test
    public void allReflectiveMetalsStayOpaqueInStockShaders() {
        for (String material : new String[]{"gold", "silver", "seryll", "adamantine", "glimmersteel"}) {
            for (String modelPrefix : new String[]{"model.weapon.halberd.",
                    "model.tool.scythe.", "model.armour.head.openhelm.",
                    "model.shield.large.", "model.tool.hammer.",
                    "model.armour.torso.plate."}) {
                String model = modelPrefix + material;
                Color marked = ArmorMaterialColorsMod.enhanceEquipmentMaterialColor(
                        model, ArmorMaterialColorsMod.materialColorForModel(
                                "model.armour.torso.plate." + material));
                assertTrue(model, marked.a < 0.0f);
                Color forward = MaterialColorOpacity.forProgram(marked, null);
                assertEquals(model, 1.0f, forward.a, 0.0f);
                assertEquals(marked.r, forward.r, 0.0f);
                assertEquals(marked.g, forward.g, 0.0f);
                assertEquals(marked.b, forward.b, 0.0f);
                assertTrue("Do not corrupt the shared deferred marker", marked.a < 0.0f);
            }
        }
    }

    @Test
    public void deferredPassRetainsBothReflectionProfiles() {
        for (float marker : new float[]{-1.0f, -2.0f}) {
            Color marked = new Color(1.0f, 0.7f, 0.2f, marker);
            assertSame(marked, MaterialColorOpacity.forReflectionSupport(marked, true));
        }
    }

    @Test
    public void ordinaryOpacityAndNullRemainUntouched() {
        assertNull(MaterialColorOpacity.forProgram(null, null));
        for (float alpha : new float[]{0.0f, 0.25f, 0.5f, 1.0f}) {
            Color ordinary = new Color(0.3f, 0.4f, 0.5f, alpha);
            assertSame(ordinary, MaterialColorOpacity.forProgram(ordinary, null));
        }
    }
}

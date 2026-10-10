package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.backend.Primitive;
import javassist.*;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class MaterialOpacityHookTest {
    @Test
    public void patchedReadsPreserveThePrimitiveOnSuccessAndFailure() throws Exception {
        ClassPool pool = new ClassPool(true);
        File libs = new File(System.getProperty("wurmClientLibDir"));
        pool.appendClassPath(new File(libs, "client-patched.jar").getAbsolutePath());
        CtClass fixture = pool.makeClass("org.armormaterialcolors.client.OpacityReadFixture");
        CtMethod read = CtNewMethod.make(
                "public static com.wurmonline.client.renderer.Color read("
                        + "com.wurmonline.client.renderer.backend.Primitive p, boolean fail) {"
                        + "com.wurmonline.client.renderer.Color result = p.materialColor;"
                        + "if (fail) throw new IllegalStateException(); return result; }", fixture);
        fixture.addMethod(read);
        ArmorMaterialColorsMod.installMaterialOpacityHook(read);
        Class<?> patched = new ClassLoader(getClass().getClassLoader()) {
            Class<?> define(byte[] bytes) { return defineClass(null, bytes, 0, bytes.length); }
        }.define(fixture.toBytecode());
        Primitive primitive = new Primitive();
        Color marker = new Color(1.0f, 0.7f, 0.2f, -1.0f);
        primitive.materialColor = marker;
        Color bound = (Color) patched.getMethod("read", Primitive.class, boolean.class)
                .invoke(null, primitive, false);
        assertEquals(1.0f, bound.a, 0.0f);
        assertSame(marker, primitive.materialColor);
        try {
            patched.getMethod("read", Primitive.class, boolean.class)
                    .invoke(null, primitive, true);
            fail("Failure must propagate");
        } catch (InvocationTargetException error) {
            assertTrue(error.getCause() instanceof IllegalStateException);
        }
        assertSame("A later deferred pass needs the original marker",
                marker, primitive.materialColor);
    }
}

package org.armormaterialcolors.client;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ClientContractTest {
    @Test
    public void pinnedClientContainsHookTarget() throws Exception {
        ClassPool pool = clientClassPool();
        CtClass builder = pool.get(
                "com.wurmonline.client.renderer.cell.PlayerArmorBuilder");

        CtMethod method = builder.getDeclaredMethod(
                "addToTextureList",
                hookParameterTypes(pool));
        assertNotNull(method);

        CtClass playerTextureBuilder = pool.get(
                "com.wurmonline.client.resources.textures.PlayerTextureBuilderGL");
        assertNotNull(playerTextureBuilder.getDeclaredMethod(
                "drawImage",
                new CtClass[]{
                        pool.get("com.wurmonline.client.resources.textures.Texture"),
                        CtClass.intType,
                        CtClass.intType,
                        CtClass.intType,
                        CtClass.intType,
                        pool.get("com.wurmonline.client.resources.textures.Texture"),
                        pool.get("com.wurmonline.client.renderer.Color"),
                        pool.get("com.wurmonline.client.renderer.Color"),
                        pool.get("com.wurmonline.client.renderer.Color")
                }));

        CtClass itemColors = pool.get(
                "com.wurmonline.client.renderer.ItemColorsXml");
        assertNotNull(itemColors.getDeclaredMethod(
                "findItemColor",
                new CtClass[]{pool.get("java.lang.String")}));

        CtClass material = pool.get("com.wurmonline.client.renderer.Material");
        assertNotNull(material.getDeclaredMethod("preload"));

        CtClass propertiesXml = pool.get(
                "com.wurmonline.client.renderer.model.collada.ColladaPropertiesXml");
        assertNotNull(propertiesXml.getDeclaredMethod(
                "load",
                new CtClass[]{
                        pool.get("com.wurmonline.client.renderer.model.collada.importer.ColladaModel"),
                        pool.get("com.wurmonline.client.resources.ResourceUrl"),
                        CtClass.booleanType
                }));

        CtClass colladaModelData = pool.get(
                "com.wurmonline.client.renderer.model.collada.AbstractColladaModelData");
        assertNotNull(colladaModelData.getDeclaredMethod(
                "preloadTextures", new CtClass[]{CtClass.booleanType}));

        CtClass program = pool.get(
                "com.wurmonline.client.renderer.shaders.Program");
        assertNotNull(program.getDeclaredMethod("bind"));

    }

    @Test
    public void hookCompilesAgainstPinnedClientBytecode() throws Exception {
        ClassPool pool = clientClassPool();
        ArmorMaterialColorsMod.installHooks(pool);

        assertTrue(pool.get(
                "com.wurmonline.client.renderer.cell.PlayerArmorBuilder")
                .toBytecode().length > 0);
        assertTrue(pool.get(
                "com.wurmonline.client.resources.textures.PlayerTextureBuilderGL")
                .toBytecode().length > 0);
        assertTrue(pool.get(
                "com.wurmonline.client.renderer.ItemColorsXml")
                .toBytecode().length > 0);
        assertTrue(pool.get(
                "com.wurmonline.client.renderer.Material")
                .toBytecode().length > 0);
        assertTrue(pool.get(
                "com.wurmonline.client.renderer.model.collada.ColladaPropertiesXml")
                .toBytecode().length > 0);
        assertTrue(pool.get(
                "com.wurmonline.client.renderer.model.collada.AbstractColladaModelData")
                .toBytecode().length > 0);
        assertTrue(pool.get(
                "com.wurmonline.client.renderer.shaders.Program")
                .toBytecode().length > 0);
        assertTrue(pool.get(
                "com.wurmonline.client.renderer.shaders.ProgramBindings")
                .toBytecode().length > 0);
    }

    @Test
    public void reflectionShadersCarryAndConsumeMaterialMarker() throws Exception {
        File project = new File(System.getProperty(
                "armorMaterialColorsProjectDir"));
        File shaders = new File(project,
                "resource-pack/com/wurmonline/client/resources/programs/modern");
        File mappingsFile = new File(project,
                "resource-pack/com/wurmonline/client/resources/mappings.txt");
        File armorResources = new File(project,
                "resource-pack/com/wurmonline/client/resources/clothing/Armor");

        String gbuffer = read(new File(shaders,
                "mask_gbuffer.fragment.shader"));
        String lighting = read(new File(shaders,
                "dirlight.fragment.shader"));
        String pointLighting = read(new File(shaders,
                "pointlight.fragment.shader"));
        String playerGbuffer = read(new File(shaders,
                "skin_reflection_gbuffer.fragment.shader"));
        String playerGbufferProgram = read(new File(shaders,
                "skin_gbuffer.program"));
        File programs = new File(project,
                "resource-pack/com/wurmonline/client/resources/programs");
        String playerMask = read(new File(programs,
                "playerMetalMask.fragment.shader"));
        String playerCompositor = read(new File(programs,
                "masked_nolight.fragment.shader"));
        String mappings = read(mappingsFile);

        assertTrue(gbuffer.contains("uniform vec4 armorReflectionTuning0"));
        assertTrue(gbuffer.contains("clamp(armorReflectionTuning0.x"));
        assertTrue(gbuffer.contains("float armorSurface = step(1.5, materialMarker)"));
        assertTrue(gbuffer.contains("roughenArmorNormal"));
        assertTrue(gbuffer.contains(
                "sseResult.b = -(reflectionStrength + armorSurface * 2.0)"));
        assertTrue(lighting.contains("ARMOR_MATERIAL_REFLECTION_SHADER_V0316"));
        assertTrue(lighting.contains("float armorSurface = step(1.5, reflectionMarker)"));
        assertTrue(lighting.contains(
                "reflectionMarker - armorSurface * 2.0"));
        assertTrue(lighting.contains("sampleReflectedAlbedo"));
        assertTrue(lighting.contains("traceScreenReflection"));
        assertTrue(lighting.contains("sampleScreenMirrorFallback"));
        assertTrue(lighting.contains("environmentReflection"));
        assertTrue(lighting.contains("float upwardSky = smoothstep("));
        assertTrue(lighting.contains("float backdropVisibility = mix("));
        assertTrue(lighting.contains("armorReflectionTuning1.x"));
        assertTrue(lighting.contains("armorReflectionTuning2.x"));
        assertTrue(lighting.contains("armorReflectionTuning3.y"));
        assertTrue(lighting.contains("vec3 caveIrradiance = clamp("));
        assertTrue(lighting.contains("screenColor * caveIrradiance"));
        assertTrue(lighting.contains("backdropColor * caveIrradiance"));
        assertTrue(lighting.contains("vec3 caveEnvironment = caveIrradiance"));
        assertFalse(lighting.contains("vec3(0.006)"));
        assertFalse(lighting.contains("(mirrorColor - 0.5)"));
        assertFalse(lighting.contains("(reflectedColor - 0.5)"));
        assertFalse(lighting.contains(
                "reflectedColor = mix(reflectedColor, caveAmbient, caveOcc)"));
        assertTrue(lighting.contains("float metallicGlintExponent = mix("));
        assertTrue(lighting.contains("finalColor = mix("));
        assertTrue(pointLighting.contains(
                "ARMOR_MATERIAL_POINTLIGHT_REFLECTION_SHADER_V0316"));
        assertTrue(pointLighting.contains(
                "reflectionMarker - armorSurface * 2.0"));
        assertTrue(pointLighting.contains("float metallicGlint = facing"));
        assertTrue(pointLighting.contains("armorReflectionTuning0.z"));
        assertTrue(mappings.contains(
                "shader.dirlight.fragment = programs/modern/dirlight.fragment.shader"));
        assertTrue(mappings.contains(
                "shader.mask_gbuffer.fragment = programs/modern/mask_gbuffer.fragment.shader"));
        assertTrue(mappings.contains(
                "shader.pointlight.fragment = programs/modern/pointlight.fragment.shader"));
        assertTrue(playerGbuffer.contains(
                "ARMOR_MATERIAL_SKIN_GBUFFER_SHADER_V0316"));
        assertTrue(playerGbuffer.contains("float bakedMetal"));
        assertTrue(playerGbuffer.contains(
                "float lowerBand = smoothstep(0.992, 0.995, tex.a)"));
        assertTrue(playerGbuffer.contains(
                "float upperBand = 1.0 - smoothstep(0.9990, 0.9999, tex.a)"));
        assertTrue(playerGbuffer.contains("roughenArmorNormal"));
        assertTrue(playerGbuffer.contains(
                "sseResult.b = -(2.0 + reflectionStrength)"));
        assertTrue(playerGbufferProgram.contains(
                "pixelshader shader.skin_reflection_gbuffer.fragment"));
        assertTrue(playerMask.contains(
                "ARMOR_MATERIAL_PLAYER_MASK_SHADER_V0316"));
        assertTrue(playerMask.contains("materialAmount"));
        assertTrue(playerCompositor.contains(
                "ARMOR_MATERIAL_PLAYER_COMPOSITOR_SHADER_V0316"));
        assertTrue(playerCompositor.contains(
                "vec4(texC.rgb * materialcolor.rgb, texC.a)"));
        assertTrue(mappings.contains(
                "program.player_metal_mask = programs/playerMetalMask.program"));
        assertTrue(mappings.contains(
                "program.player_skin_reflection = programs/modern/skin_gbuffer.program"));
        assertTrue(mappings.contains(
                "program.player_armor_compositor = programs/static_masked_nolight.program"));
        assertTrue(playerCompositor.contains("materialcolor.a is private metadata"));
        assertTrue(mappings.contains(
                "shader.skin_reflection_gbuffer.fragment = programs/modern/skin_reflection_gbuffer.fragment.shader"));
        assertTrue(mappings.contains(
                "armor.material.world.mask = clothing/Armor/Plate/material_all_mm.dds"));

        String plateProperties = read(new File(
                armorResources, "Plate/properties.xml"));
        String chainProperties = read(new File(
                armorResources, "Chain/properties.xml"));
        assertTrue(plateProperties.contains("<plateJacket.wom>"));
        assertTrue(plateProperties.contains("<plateJacketPile.wom>"));
        assertTrue(plateProperties.contains("<mask>material_all_mm.dds</mask>"));
        assertTrue(chainProperties.contains("<chainJacket.wom>"));
        assertTrue(chainProperties.contains("<chainJacketPile.wom>"));
        assertTrue(chainProperties.contains("<mask>material_all_mm.dds</mask>"));
    }

    private static ClassPool clientClassPool() throws Exception {
        File libs = new File(System.getProperty("wurmClientLibDir"));
        ClassPool pool = new ClassPool(true);
        pool.appendClassPath(new File(libs, "client-patched.jar").getAbsolutePath());
        pool.appendClassPath(new File(libs, "common.jar").getAbsolutePath());
        return pool;
    }

    private static CtClass[] hookParameterTypes(ClassPool pool) throws Exception {
        return new CtClass[]{
                pool.get("java.lang.String"),
                pool.get("java.lang.String"),
                pool.get("java.lang.String"),
                CtClass.booleanType,
                pool.get("java.util.Map"),
                pool.get("com.wurmonline.client.renderer.Color"),
                pool.get("com.wurmonline.client.renderer.Color")
        };
    }

    private static String read(File file) throws Exception {
        return new String(Files.readAllBytes(file.toPath()),
                StandardCharsets.UTF_8);
    }
}

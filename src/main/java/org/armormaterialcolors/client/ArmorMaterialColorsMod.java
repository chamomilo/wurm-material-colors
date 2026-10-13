package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import com.wurmonline.client.renderer.cell.PlayerArmorBuilderComponent;
import javassist.ClassPool;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.ModEntry;
import org.gotti.wurmunlimited.modloader.interfaces.ModListener;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;
import org.chamomilo.wurm.update.SharedUpdateCoordinator;
import org.chamomilo.wurm.update.SharedUpdateHooks;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArmorMaterialColorsMod
        implements WurmClientMod, Configurable, PreInitable, Initable,
        ModListener {
    public static final String VERSION = "0.5.3";
    static final float DEFAULT_REFLECTION_STRENGTH = ReflectionTuning.DEFAULT_REFLECTION_STRENGTH;
    static final float REFLECTIVE_ITEM_MARKER = -1.0f;
    static final float REFLECTIVE_ARMOR_MARKER = -2.0f;

    private static volatile String resourcePack =
            "mods/armor-material-colors/armor-material-colors-resources-" + VERSION + ".jar";

    private static final Logger LOG = Logger.getLogger("ArmorMaterialColors");

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void configure(Properties properties) {
        properties.setProperty("resourcePack", currentResourcePack(properties.getProperty("resourcePack", resourcePack)));
        properties = org.chamomilo.wurm.update.SharedModSettings.configure("armor-material-colors",properties,
                ModSettingsSchema.specs(),(key,values) -> ReflectionTuning.configure(values));
        ReflectionTuning.configure(properties);
        resourcePack = currentResourcePack(properties.getProperty("resourcePack", resourcePack));
        properties.setProperty("resourcePack", resourcePack);
        org.chamomilo.wurm.update.SharedModSettings.Provider editor =
                org.chamomilo.wurm.update.SharedModSettings.provider("armor-material-colors");
        if (!resourcePack.equals(editor.value("resourcePack"))) {
            try {
                editor.change("resourcePack", resourcePack);
            } catch (Exception failure) {
                LOG.log(java.util.logging.Level.WARNING, "Could not save the upgraded resource-pack path", failure);
            }
        }
    }

    static String currentResourcePack(String configured) {
        String value = configured.trim();
        String normalized = value.replace('\\', '/');
        if (normalized.matches("mods/armor-material-colors/armor-material-colors-resources-\\d+\\.\\d+\\.\\d+\\.jar")) {
            return "mods/armor-material-colors/armor-material-colors-resources-" + VERSION + ".jar";
        }
        return value;
    }

    @Override
    public void preInit() {
        SharedUpdateHooks.install();
        try {
            installHooks(HookManager.getInstance().getClassPool());
        } catch (Throwable error) {
            LOG.log(Level.SEVERE, "Unable to install armour material-colour hook", error);
            throw new RuntimeException(
                    "Unable to install armour material-colour hook", error);
        }
    }

    static void installHooks(ClassPool pool) throws Exception {
        CtClass stringType = pool.get("java.lang.String");
        CtClass mapType = pool.get("java.util.Map");
        CtClass colorType = pool.get("com.wurmonline.client.renderer.Color");
        CtClass resourceUrlType = pool.get(
                "com.wurmonline.client.resources.ResourceUrl");
        CtClass colladaModelType = pool.get(
                "com.wurmonline.client.renderer.model.collada.importer.ColladaModel");

        CtClass itemColors = pool.getCtClass(
                "com.wurmonline.client.renderer.ItemColorsXml");
        CtMethod findItemColor = itemColors.getDeclaredMethod(
                "findItemColor", new CtClass[]{stringType});
        findItemColor.insertAfter(
                "{ $_ = org.armormaterialcolors.client.ArmorMaterialColorsMod"
                        + ".enhanceEquipmentMaterialColor($1, $_); }");

        // ColladaPropertiesXml derives properties.xml from the pack that owns
        // the WOM. The stock Plate/Chain WOM therefore cannot see a properties
        // file supplied by a separate mod pack. Mark those all-metal models
        // after stock property loading, then redirect only our unique mask
        // filename inside the model texture-preload method only.
        CtClass colladaProperties = pool.getCtClass(
                "com.wurmonline.client.renderer.model.collada.ColladaPropertiesXml");
        colladaProperties.getDeclaredMethod(
                "load",
                new CtClass[]{
                        colladaModelType,
                        resourceUrlType,
                        CtClass.booleanType
                }).insertAfter(
                "{ org.armormaterialcolors.client.WorldArmorMasks"
                        + ".attachMask($1, $2);"
                        + " org.armormaterialcolors.client.WorldWoodMasks"
                        + ".attachMasks($1, $2);"
                        + " org.armormaterialcolors.client.WorldEquipmentMasks"
                        + ".attachMasks($1, $2); }");

        CtClass colladaModelData = pool.getCtClass(
                "com.wurmonline.client.renderer.model.collada.AbstractColladaModelData");
        colladaModelData.getDeclaredMethod(
                "preloadTextures", new CtClass[]{CtClass.booleanType})
                .instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall call)
                            throws CannotCompileException {
                        if ("com.wurmonline.client.resources.ResourceUrl"
                                .equals(call.getClassName())
                                && "derive".equals(call.getMethodName())) {
                            call.replace(
                                    "{ com.wurmonline.client.resources.ResourceUrl"
                                            + " armorMask = org.armormaterialcolors"
                                            + ".client.WorldArmorMasks"
                                            + ".resolveDerivedMask($1);"
                                            + " if (armorMask == null) armorMask ="
                                            + " org.armormaterialcolors.client.WorldWoodMasks"
                                            + ".resolveDerivedMask($1);"
                                            + " $_ = armorMask == null"
                                            + " ? $proceed($$) : armorMask; }");
                        }
                    }
                });

        CtClass material = pool.getCtClass(
                "com.wurmonline.client.renderer.Material");
        material.getDeclaredMethod("preload").insertBefore(
                "org.armormaterialcolors.client.ReflectionResources"
                        + ".ensurePackLoaded();");

        pool.getCtClass("com.wurmonline.client.renderer.model.collada.material.ColladaMaterial")
                .getDeclaredMethod("processOverrides", new CtClass[]{resourceUrlType})
                .insertAfter("{ org.armormaterialcolors.client.WornArmorTextures"
                        + ".neutralizeWorldTexture($0, $1);"
                        + " org.armormaterialcolors.client.WorldWoodMasks"
                        + ".neutralizeWorldTexture($0, $1); }");

        CtClass program = pool.getCtClass(
                "com.wurmonline.client.renderer.shaders.Program");
        program.getDeclaredMethod(
                "load", new CtClass[]{stringType, stringType})
                .insertBefore(
                        "{ $1 = org.armormaterialcolors.client"
                                + ".PlayerArmorReflection"
                                + ".redirectProgramName($1); }");
        program.getDeclaredMethod("bind").insertAfter(
                "org.armormaterialcolors.client.ReflectionTuning"
                        + ".applyToBoundProgram($0);");

        pool.getCtClass("com.wurmonline.client.renderer.DeferredRenderer")
                .getDeclaredMethod("prepareLightingPass").insertBefore(
                        "org.armormaterialcolors.client.ReflectionTuning"
                                + ".updatePlayerLayer(world.getPlayerLayer());");

        // Forward rendering (including paperdolls and some world passes) uses
        // stock masked shaders. Their material alpha is opacity, so -1/-2
        // makes only the green-mask metal disappear. Keep the private marker
        // only for our deferred shader. Substitute reads while binding rather
        // than mutating a primitive that other passes can reuse.
        CtClass bindings = pool.getCtClass(
                "com.wurmonline.client.renderer.shaders.ProgramBindings");
        CtMethod bind = bindings.getDeclaredMethod("bind", new CtClass[]{
                pool.get("com.wurmonline.client.renderer.backend.Primitive"),
                CtClass.intType
        });
        installMaterialOpacityHook(bind);

        CtClass builder = pool.getCtClass(
                "com.wurmonline.client.renderer.cell.PlayerArmorBuilder");

        CtMethod addToTextureList = builder.getDeclaredMethod(
                "addToTextureList",
                new CtClass[]{
                        stringType,
                        stringType,
                        stringType,
                        CtClass.booleanType,
                        mapType,
                        colorType,
                        colorType
                });

        addToTextureList.insertBefore(
                "{ $3 = org.armormaterialcolors.client.WornArmorTextures"
                        + ".neutralTexture($1, $3); }");
        addToTextureList.insertAfter(
                "{ org.armormaterialcolors.client.ArmorMaterialColorsMod"
                        + ".applyMaterialColor($1, $2, $3, $5); }");

        // Worn armour is flattened into the player's single RGBA texture.
        // Add a second, alpha-only compositor pass for reflective armour so
        // the skin G-buffer can recover the per-pixel metal mask afterwards.
        CtClass textureType = pool.get(
                "com.wurmonline.client.resources.textures.Texture");
        CtClass playerTextureBuilder = pool.getCtClass(
                "com.wurmonline.client.resources.textures.PlayerTextureBuilderGL");
        installWornMaskPathHook(pool.getCtClass(
                "com.wurmonline.client.renderer.cell.PlayerTexture")
                .getDeclaredMethod("loadPlayerBody"));
        installWornMaskPathHook(playerTextureBuilder.getDeclaredMethod("generateTexture"));

        // Stock passes wantedAlpha=false to Offscreen.init, so its player
        // atlas is RGB8 and silently discards our alpha-only material mask.
        // Change only this constructor call to RGBA8; other offscreen targets
        // and the normal armour RGB composition are left untouched.
        playerTextureBuilder.getDeclaredConstructors()[0]
                .instrument(new ExprEditor() {
                    @Override
                    public void edit(MethodCall call)
                            throws CannotCompileException {
                        if ("com.wurmonline.client.renderer.backend.Offscreen"
                                .equals(call.getClassName())
                                && "init".equals(call.getMethodName())) {
                            call.replace(
                                    "{ $proceed($1, $2, $3, $4, $5, true, $7); }");
                        }
                    }
                });

        CtMethod drawImage = playerTextureBuilder.getDeclaredMethod(
                "drawImage",
                new CtClass[]{
                        textureType,
                        CtClass.intType,
                        CtClass.intType,
                        CtClass.intType,
                        CtClass.intType,
                        textureType,
                        colorType,
                        colorType,
                        colorType
                });
        drawImage.insertAfter(
                "{ org.armormaterialcolors.client.PlayerArmorReflection"
                        + ".queueMetalMask($0, $1, $2, $3, $4, $5, $6, $9); }");
    }

    static void installMaterialOpacityHook(CtMethod method)
            throws CannotCompileException {
        method.instrument(new ExprEditor() {
            @Override
            public void edit(FieldAccess access) throws CannotCompileException {
                if (access.isReader()
                        && "com.wurmonline.client.renderer.backend.Primitive"
                        .equals(access.getClassName())
                        && "materialColor".equals(access.getFieldName())) {
                    access.replace("{ $_ = org.armormaterialcolors.client"
                            + ".MaterialColorOpacity.forProgram($proceed(), $0.program); }");
                }
            }
        });
    }

    static void installWornMaskPathHook(CtMethod method) throws CannotCompileException {
        method.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall call) throws CannotCompileException {
                if ("java.lang.String".equals(call.getClassName())
                        && "replace".equals(call.getMethodName())) {
                    call.replace("{ $_ = org.armormaterialcolors.client.WornArmorTextures"
                            + ".maskTexturePath($proceed($$)); }");
                }
            }
        });
    }

    /** Supplies the same reference palette for held, worn and dropped items. */
    public static Color enhanceEquipmentMaterialColor(
            String modelName, Color vanillaColor) {
        String wood = WoodPalette.materialForModel(modelName);
        if (wood != null) {
            return WoodPalette.color(wood, vanillaColor != null && vanillaColor.a >= 0.0f
                    ? vanillaColor.a : 1.0f);
        }
        String material = materialForSupportedEquipment(modelName);
        if (material == null) {
            return vanillaColor;
        }
        float alpha = vanillaColor == null || vanillaColor.a < 0.0f ? 1.0f : vanillaColor.a;
        if (MetalPalette.isReflective(material)) {
            alpha = materialForSupportedArmor(modelName) != null
                    ? REFLECTIVE_ARMOR_MARKER : REFLECTIVE_ITEM_MARKER;
        }
        return MetalPalette.color(material, alpha);
    }

    static String reflectiveMaterialForModel(String modelName) {
        String material = materialForSupportedEquipment(modelName);
        return MetalPalette.isReflective(material) ? material : null;
    }

    static String materialForSupportedEquipment(String modelName) {
        if (modelName == null) {
            return null;
        }

        String normalized = modelName.toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("model.")
                || normalized.startsWith("model.creature.")
                || normalized.startsWith("model.corpse.")) {
            return null;
        }

        String[] tokens = normalized.split("\\.");
        for (int index = tokens.length - 1; index >= 0; index--) {
            if (MetalPalette.isMetal(tokens[index])) {
                return tokens[index];
            }
            // A later wood suffix is the actual material, even if the base
            // item's name happens to contain a metal word.
            if (WoodPalette.isWood(tokens[index]) || tokens[index].endsWith("wood")) {
                return null;
            }
        }
        return null;
    }

    static float reflectionStrength() {
        return ReflectionTuning.reflectionStrength();
    }

    static String resourcePack() {
        return resourcePack;
    }

    static void resetSettingsForTests() {
        ReflectionTuning.resetDefaults();
        resourcePack =
                "mods/armor-material-colors/armor-material-colors-resources-" + VERSION + ".jar";
    }

    /**
     * Called from the patched PlayerArmorBuilder after it has registered one
     * texture layer. The original model name still contains the material
     * suffix even when armor.xml fell back to the generic Plate/Chain entry.
     */
    @SuppressWarnings("unchecked")
    public static void applyMaterialColor(
            String modelName,
            String texturePlace,
            String textureName,
            Map<String, PlayerArmorBuilderComponent> armorTextures) {
        Color materialColor = materialColorForModel(modelName);
        if (materialColor == null || texturePlace == null || textureName == null
                || armorTextures == null) {
            return;
        }

        PlayerArmorBuilderComponent component = armorTextures.get(texturePlace);
        if (component == null) {
            return;
        }

        component.getMaterialMap().put(textureName, materialColor);
    }

    static Color materialColorForModel(String modelName) {
        String material = materialForSupportedArmor(modelName);
        if (material == null) {
            return null;
        }

        return MetalPalette.color(material, MetalPalette.isReflective(material)
                ? REFLECTIVE_ARMOR_MARKER : 1.0f);
    }

    static String materialForSupportedArmor(String modelName) {
        if (modelName == null) {
            return null;
        }

        String normalized = modelName.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("model.pile.")) {
            String[] parts = normalized.split("\\.");
            if (parts.length > 2 && ("platejacket".equals(parts[2])
                    || "platepants".equals(parts[2]) || "plateboot".equals(parts[2])
                    || "platesleeve".equals(parts[2]) || "plategauntlet".equals(parts[2])
                    || "chainjacket".equals(parts[2]) || "chainpants".equals(parts[2])
                    || "chainboot".equals(parts[2]) || "chainsleeve".equals(parts[2])
                    || "chaingauntlet".equals(parts[2]) || "chaincoif".equals(parts[2]))) {
                return materialForSupportedEquipment(modelName);
            }
            return null;
        }
        if (!normalized.contains(".armour.")) {
            return null;
        }

        int armorTypeEnd = tokenEnd(normalized, ".plate");
        if (armorTypeEnd < 0) {
            armorTypeEnd = tokenEnd(normalized, ".chain");
        }
        if (armorTypeEnd < 0 || armorTypeEnd >= normalized.length()) {
            return null;
        }

        return materialForSupportedEquipment(modelName);
    }

    private static int tokenEnd(String modelName, String armorToken) {
        int start = modelName.indexOf(armorToken);
        if (start < 0) {
            return -1;
        }

        int end = start + armorToken.length();
        return end == modelName.length() || modelName.charAt(end) == '.' ? end : -1;
    }

    @Override
    public void init() {
        SharedUpdateHooks.registerHost("armor-material-colors");
    }

    @Override
    public void modInitialized(ModEntry<?> entry) {
        SharedUpdateCoordinator.modInitialized(entry);
    }
}

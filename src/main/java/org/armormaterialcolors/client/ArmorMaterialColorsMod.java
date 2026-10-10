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
    public static final String VERSION = "0.3.23";
    static final float DEFAULT_REFLECTION_STRENGTH = 0.80f;
    static final float REFLECTIVE_ITEM_MARKER = -1.0f;
    static final float REFLECTIVE_ARMOR_MARKER = -2.0f;

    private static volatile String resourcePack =
            "mods/armor-material-colors/armor-material-colors-resources-0.3.23.jar";

    private static final Logger LOG = Logger.getLogger("ArmorMaterialColors");

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void configure(Properties properties) {
        ReflectionTuning.configure(properties);
        resourcePack = properties.getProperty("resourcePack", resourcePack).trim();
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
                        + ".attachMask($1, $2); }");

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

    /**
     * Makes the stock material filters visible on weapons and tools. Wurm's
     * shader multiplies a dark grey diffuse texture by the values from
     * itemcolors.xml. Normalising the strongest channel to 1 keeps the stock
     * hue and channel ratios while avoiding an unnecessary overall darkening.
     */
    public static Color enhanceEquipmentMaterialColor(
            String modelName, Color vanillaColor) {
        if (vanillaColor == null) {
            return vanillaColor;
        }

        String material = materialForSupportedEquipment(modelName);
        String reflectiveMaterial = reflectiveMaterialForModel(modelName);
        if (material == null && reflectiveMaterial == null) {
            return vanillaColor;
        }

        float red = vanillaColor.r;
        float green = vanillaColor.g;
        float blue = vanillaColor.b;
        if (material != null) {
            float strongest = Math.max(red, Math.max(green, blue));
            if (strongest > 0.0f) {
                red = clamp01(red / strongest);
                green = clamp01(green / strongest);
                blue = clamp01(blue / strongest);
            }
        }

        boolean reflectiveArmor = reflectiveMaterial != null
                && materialForSupportedArmor(modelName) != null;
        Color enhanced = new Color(
                red,
                green,
                blue,
                reflectiveMaterial == null
                        ? vanillaColor.a
                        : reflectiveArmor
                                ? REFLECTIVE_ARMOR_MARKER
                                : REFLECTIVE_ITEM_MARKER);
        return enhanced;
    }

    static String reflectiveMaterialForModel(String modelName) {
        if (modelName == null) {
            return null;
        }

        String[] tokens = modelName.toLowerCase(Locale.ROOT).split("\\.");
        for (int index = tokens.length - 1; index >= 0; index--) {
            if (isReflectiveMetal(tokens[index])) {
                return tokens[index];
            }
        }
        return null;
    }

    static String materialForSupportedEquipment(String modelName) {
        if (modelName == null) {
            return null;
        }

        String normalized = modelName.toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("model.weapon.")
                && !normalized.startsWith("model.tool.")) {
            return null;
        }

        String[] tokens = normalized.split("\\.");
        for (int index = tokens.length - 1; index >= 0; index--) {
            if (isTintedMetal(tokens[index])) {
                return tokens[index];
            }
        }
        return null;
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
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
                "mods/armor-material-colors/armor-material-colors-resources-0.3.23.jar";
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

        // These are the stock values from graphics.jar/xmls/itemcolors.xml.
        if ("brass".equals(material)) {
            return armorColor(0.823f, 0.690f, 0.553f, material);
        }
        if ("bronze".equals(material)) {
            return armorColor(0.588f, 0.384f, 0.215f, material);
        }
        if ("copper".equals(material)) {
            return armorColor(0.720f, 0.450f, 0.218f, material);
        }
        if ("gold".equals(material)) {
            return armorColor(0.968f, 0.721f, 0.208f, material);
        }
        if ("lead".equals(material)) {
            return armorColor(0.510f, 0.540f, 0.590f, material);
        }
        if ("silver".equals(material)) {
            return armorColor(0.680f, 0.756f, 0.776f, material);
        }
        if ("steel".equals(material)) {
            return armorColor(0.570f, 0.600f, 0.650f, material);
        }
        if ("seryll".equals(material)) {
            return armorColor(0.650f, 0.170f, 0.060f, material);
        }
        if ("tin".equals(material)) {
            return armorColor(0.680f, 0.660f, 0.600f, material);
        }
        if ("zinc".equals(material)) {
            return armorColor(0.910f, 0.870f, 0.770f, material);
        }
        if ("glimmersteel".equals(material)) {
            return armorColor(0.860f, 0.820f, 0.590f, material);
        }
        if ("adamantine".equals(material)) {
            return armorColor(0.550f, 0.670f, 0.860f, material);
        }
        if ("electrum".equals(material)) {
            return armorColor(0.886f, 0.862f, 0.425f, material);
        }

        // Iron deliberately has no multiplier in vanilla. Its dedicated/base
        // grey texture must be used without an extra tint.
        return null;
    }

    private static Color armorColor(
            float red, float green, float blue, String material) {
        // The negative alpha is metadata, never opacity. The replacement
        // player compositor shader preserves the source texture's alpha;
        // PlayerArmorReflection consumes this flag to add the baked mask.
        float marker = isReflectiveMetal(material)
                ? REFLECTIVE_ARMOR_MARKER
                : 1.0f;
        return new Color(red, green, blue, marker);
    }

    static String materialForSupportedArmor(String modelName) {
        if (modelName == null) {
            return null;
        }

        String normalized = modelName.toLowerCase(Locale.ROOT);
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

        String[] suffixTokens = normalized.substring(armorTypeEnd).split("\\.");
        for (String token : suffixTokens) {
            if (isTintedMetal(token)) {
                return token;
            }
        }
        return null;
    }

    private static int tokenEnd(String modelName, String armorToken) {
        int start = modelName.indexOf(armorToken);
        if (start < 0) {
            return -1;
        }

        int end = start + armorToken.length();
        return end == modelName.length() || modelName.charAt(end) == '.' ? end : -1;
    }

    private static boolean isReflectiveMetal(String material) {
        return "gold".equals(material)
                || "silver".equals(material)
                || "seryll".equals(material)
                || "adamantine".equals(material);
    }

    private static boolean isTintedMetal(String material) {
        return "brass".equals(material)
                || "bronze".equals(material)
                || "copper".equals(material)
                || "gold".equals(material)
                || "lead".equals(material)
                || "silver".equals(material)
                || "steel".equals(material)
                || "seryll".equals(material)
                || "tin".equals(material)
                || "zinc".equals(material)
                || "glimmersteel".equals(material)
                || "adamantine".equals(material)
                || "electrum".equals(material);
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

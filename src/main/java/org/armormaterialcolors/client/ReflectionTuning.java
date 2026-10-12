package org.armormaterialcolors.client;

import com.wurmonline.client.options.Options;
import com.wurmonline.client.renderer.shaders.Program;
import com.wurmonline.client.renderer.shaders.Uniform;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL20;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Configured values supplied to the bound reflection shaders. */
public final class ReflectionTuning {
    public static final float DEFAULT_REFLECTION_STRENGTH = 0.30f;
    public static final float DEFAULT_SUN_GLINT_STRENGTH = 0.28f;
    public static final float DEFAULT_LOCAL_GLINT_STRENGTH = 0.32f;
    public static final float DEFAULT_CAVE_LOCAL_REFLECTION_BOOST = 2.50f;
    public static final float DEFAULT_GLINT_SHARPNESS = 0.45f;
    public static final float DEFAULT_REFLECTION_TINT = 0.72f;
    public static final float DEFAULT_FRONT_REFLECTION_DETAIL = 0.00f;
    public static final float DEFAULT_ENVIRONMENT_BRIGHTNESS = 1.00f;
    public static final float DEFAULT_SSR_DETAIL = 1.00f;
    public static final float DEFAULT_REFLECTION_CONTRAST = 1.00f;
    public static final float DEFAULT_SKY_BRIGHTNESS = 1.00f;
    public static final float DEFAULT_GROUND_BRIGHTNESS = 1.00f;
    public static final float DEFAULT_CAVE_BRIGHTNESS = 1.00f;
    public static final float DEFAULT_SUN_DISC_STRENGTH = 1.50f;
    public static final float DEFAULT_SSR_RAY_DISTANCE = 1.00f;
    public static final float DEFAULT_SSR_THICKNESS = 1.00f;
    public static final float DEFAULT_BACKDROP_CONTRAST = 1.12f;

    private static final Logger LOG = Logger.getLogger("ArmorMaterialColors");
    private static final AtomicInteger REVISION = new AtomicInteger(1);
    private static final ConcurrentHashMap<Integer, Integer> APPLIED =
            new ConcurrentHashMap<Integer, Integer>();

    private static volatile float reflectionStrength =
            DEFAULT_REFLECTION_STRENGTH;
    private static volatile float sunGlintStrength =
            DEFAULT_SUN_GLINT_STRENGTH;
    private static volatile float localGlintStrength =
            DEFAULT_LOCAL_GLINT_STRENGTH;
    private static volatile float caveLocalReflectionBoost =
            DEFAULT_CAVE_LOCAL_REFLECTION_BOOST;
    private static volatile boolean inCave;
    private static volatile float glintSharpness =
            DEFAULT_GLINT_SHARPNESS;
    private static volatile float reflectionTint = DEFAULT_REFLECTION_TINT;
    private static volatile float frontReflectionDetail =
            DEFAULT_FRONT_REFLECTION_DETAIL;
    private static volatile float environmentBrightness =
            DEFAULT_ENVIRONMENT_BRIGHTNESS;
    private static volatile float ssrDetail = DEFAULT_SSR_DETAIL;
    private static volatile float reflectionContrast =
            DEFAULT_REFLECTION_CONTRAST;
    private static volatile float skyBrightness = DEFAULT_SKY_BRIGHTNESS;
    private static volatile float groundBrightness = DEFAULT_GROUND_BRIGHTNESS;
    private static volatile float caveBrightness = DEFAULT_CAVE_BRIGHTNESS;
    private static volatile float sunDiscStrength = DEFAULT_SUN_DISC_STRENGTH;
    private static volatile float ssrRayDistance = DEFAULT_SSR_RAY_DISTANCE;
    private static volatile float ssrThickness = DEFAULT_SSR_THICKNESS;
    private static volatile float backdropContrast = DEFAULT_BACKDROP_CONTRAST;

    private ReflectionTuning() {
    }

    public static void configure(Properties properties) {
        reflectionStrength = number(properties, "reflectionStrength",
                DEFAULT_REFLECTION_STRENGTH, 0.0f, 1.0f);
        sunGlintStrength = number(properties, "sunGlintStrength",
                DEFAULT_SUN_GLINT_STRENGTH, 0.0f, 1.0f);
        localGlintStrength = number(properties, "localGlintStrength",
                DEFAULT_LOCAL_GLINT_STRENGTH, 0.0f, 1.0f);
        caveLocalReflectionBoost = number(properties, "caveLocalReflectionBoost",
                DEFAULT_CAVE_LOCAL_REFLECTION_BOOST, 0.0f, 4.0f);
        glintSharpness = number(properties, "glintSharpness",
                DEFAULT_GLINT_SHARPNESS, 0.0f, 1.0f);
        reflectionTint = number(properties, "reflectionTint",
                DEFAULT_REFLECTION_TINT, 0.0f, 1.0f);
        frontReflectionDetail = number(properties, "frontReflectionDetail",
                DEFAULT_FRONT_REFLECTION_DETAIL, 0.0f, 1.0f);
        environmentBrightness = number(properties, "environmentBrightness",
                DEFAULT_ENVIRONMENT_BRIGHTNESS, 0.25f, 2.0f);
        ssrDetail = number(properties, "ssrDetail",
                DEFAULT_SSR_DETAIL, 0.0f, 1.5f);
        reflectionContrast = number(properties, "reflectionContrast",
                DEFAULT_REFLECTION_CONTRAST, 0.5f, 2.0f);
        skyBrightness = number(properties, "skyBrightness",
                DEFAULT_SKY_BRIGHTNESS, 0.25f, 2.0f);
        groundBrightness = number(properties, "groundBrightness",
                DEFAULT_GROUND_BRIGHTNESS, 0.25f, 2.0f);
        caveBrightness = number(properties, "caveBrightness",
                DEFAULT_CAVE_BRIGHTNESS, 0.25f, 2.0f);
        sunDiscStrength = number(properties, "sunDiscStrength",
                DEFAULT_SUN_DISC_STRENGTH, 0.0f, 3.0f);
        ssrRayDistance = number(properties, "ssrRayDistance",
                DEFAULT_SSR_RAY_DISTANCE, 0.5f, 2.0f);
        ssrThickness = number(properties, "ssrThickness",
                DEFAULT_SSR_THICKNESS, 0.5f, 3.0f);
        backdropContrast = number(properties, "backdropContrast",
                DEFAULT_BACKDROP_CONTRAST, 0.5f, 2.0f);
        changed();
    }

    public static void resetDefaults() {
        reflectionStrength = DEFAULT_REFLECTION_STRENGTH;
        sunGlintStrength = DEFAULT_SUN_GLINT_STRENGTH;
        localGlintStrength = DEFAULT_LOCAL_GLINT_STRENGTH;
        caveLocalReflectionBoost = DEFAULT_CAVE_LOCAL_REFLECTION_BOOST;
        inCave = false;
        glintSharpness = DEFAULT_GLINT_SHARPNESS;
        reflectionTint = DEFAULT_REFLECTION_TINT;
        frontReflectionDetail = DEFAULT_FRONT_REFLECTION_DETAIL;
        environmentBrightness = DEFAULT_ENVIRONMENT_BRIGHTNESS;
        ssrDetail = DEFAULT_SSR_DETAIL;
        reflectionContrast = DEFAULT_REFLECTION_CONTRAST;
        skyBrightness = DEFAULT_SKY_BRIGHTNESS;
        groundBrightness = DEFAULT_GROUND_BRIGHTNESS;
        caveBrightness = DEFAULT_CAVE_BRIGHTNESS;
        sunDiscStrength = DEFAULT_SUN_DISC_STRENGTH;
        ssrRayDistance = DEFAULT_SSR_RAY_DISTANCE;
        ssrThickness = DEFAULT_SSR_THICKNESS;
        backdropContrast = DEFAULT_BACKDROP_CONTRAST;
        changed();
    }

    public static float reflectionStrength() {
        return reflectionStrength;
    }

    /** Updated before each world lighting pass, including cave entrance/exit. */
    public static void updatePlayerLayer(int layer) {
        boolean cave = layer < 0;
        if (inCave != cave) {
            inCave = cave;
            changed();
        }
    }

    static float caveLocalReflectionBoost() {
        return caveLocalReflectionBoost;
    }

    static boolean isInCave() {
        return inCave;
    }

    /** Called immediately after Wurm binds any GLSL program. */
    public static void applyToBoundProgram(Program program) {
        if (program == null) return;
        int revision = REVISION.get();
        Integer appliedRevision = APPLIED.get(program.getId());
        if (appliedRevision != null && appliedRevision == revision) return;

        set(program, "armorReflectionTuning0",
                reflectionStrength,
                sunGlintStrength,
                localGlintStrength,
                glintSharpness);
        set(program, "armorReflectionTuning1",
                reflectionTint,
                frontReflectionDetail,
                environmentBrightness,
                ssrDetail);
        set(program, "armorReflectionTuning2",
                reflectionContrast,
                skyBrightness,
                groundBrightness,
                caveBrightness);
        set(program, "armorReflectionTuning3",
                sunDiscStrength,
                ssrRayDistance,
                ssrThickness,
                backdropContrast);
        set(program, "armorReflectionTuning4",
                caveLocalReflectionBoost, inCave ? 1.0f : 0.0f, 0.0f, 0.0f);
        APPLIED.put(program.getId(), revision);
    }

    private static void set(Program program, String name,
                            float x, float y, float z, float w) {
        Uniform uniform = program.getUniformByName(name);
        if (uniform == null) return;
        if (Options.useGLSL.inCore()) {
            GL20.glUniform4f(uniform.getLocation(), x, y, z, w);
        } else {
            ARBShaderObjects.glUniform4fARB(
                    uniform.getLocation(), x, y, z, w);
        }
    }

    private static void changed() {
        REVISION.incrementAndGet();
    }

    private static float number(Properties properties, String key,
                                float fallback, float minimum, float maximum) {
        String text = properties.getProperty(key);
        if (text == null || text.trim().isEmpty()) return fallback;
        try {
            return clamp(Float.parseFloat(text.trim()), minimum, maximum);
        } catch (NumberFormatException error) {
            LOG.log(Level.WARNING,
                    "Invalid " + key + " '" + text + "'; using " + fallback,
                    error);
            return fallback;
        }
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

}

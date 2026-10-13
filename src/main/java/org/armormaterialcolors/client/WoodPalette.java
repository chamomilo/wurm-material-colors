package org.armormaterialcolors.client;

import com.wurmonline.client.renderer.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** All 26 stock item wood materials, in the client's encoded DDS colour space. */
final class WoodPalette {
    private static final Map<String, float[]> COLORS = colors();
    private WoodPalette() { }

    static boolean isWood(String name) { return COLORS.containsKey(name); }

    static String materialForModel(String name) {
        if (name == null) return null;
        String value = name.toLowerCase(Locale.ROOT);
        if (!value.startsWith("model.") && !isWood(value)) return null;
        if (value.startsWith("model.creature.") || value.startsWith("model.corpse.")
                || value.startsWith("model.tree.") || value.startsWith("model.bush.")) return null;
        String[] tokens = value.split("\\.");
        for (int i = tokens.length - 1; i >= 0; i--) {
            if (isWood(tokens[i])) return tokens[i];
            if (MetalPalette.isMetal(tokens[i])) return null;
        }
        return null;
    }

    static Color color(String name, float alpha) {
        float[] rgb = COLORS.get(name);
        return rgb == null ? null : new Color(rgb[0], rgb[1], rgb[2], alpha);
    }

    static Map<String, float[]> references() { return COLORS; }

    private static Map<String, float[]> colors() {
        Map<String, float[]> values = new LinkedHashMap<String, float[]>();
        values.put("birchwood", new float[]{1.20f, 1.24f, 1.30f});
        values.put("pinewood", new float[]{1.50f, 1.53f, 1.57f});
        values.put("oakenwood", new float[]{0.77f, 0.75f, 0.68f});
        values.put("cedarwood", new float[]{1.228f, 1.141f, 1.005f});
        values.put("willow", new float[]{1.00f, 1.15f, 1.28f});
        values.put("maplewood", new float[]{1.15f, 1.09f, 0.87f});
        values.put("applewood", new float[]{1.10f, 0.99f, 0.67f});
        values.put("lemonwood", new float[]{1.24f, 1.04f, 0.876f});
        values.put("olivewood", new float[]{1.40f, 1.29f, 1.03f});
        values.put("cherrywood", new float[]{1.25f, 1.00f, 1.02f});
        values.put("lavenderwood", new float[]{0.84f, 0.67f, 0.96f});
        values.put("rosewood", new float[]{0.98f, 0.65f, 0.47f});
        values.put("thorn", new float[]{0.33f, 0.43f, 0.33f});
        values.put("grapewood", new float[]{0.64f, 0.80f, 0.50f});
        values.put("camelliawood", new float[]{0.69f, 0.63f, 0.48f});
        values.put("oleanderwood", new float[]{0.59f, 0.63f, 0.67f});
        values.put("chestnut", new float[]{0.97f, 0.85f, 0.90f});
        values.put("walnut", new float[]{0.70f, 0.65f, 0.70f});
        values.put("firwood", new float[]{1.37f, 1.34f, 1.43f});
        values.put("lindenwood", new float[]{1.11f, 1.15f, 1.03f});
        values.put("ivy", new float[]{0.81f, 0.984f, 0.67f});
        values.put("hazelnutwood", new float[]{0.86f, 0.67f, 0.36f});
        values.put("orangewood", new float[]{1.44f, 1.14f, 0.976f});
        values.put("raspberrywood", new float[]{0.861f, 0.476f, 0.578f});
        values.put("blueberrywood", new float[]{0.51f, 0.56f, 0.85f});
        values.put("lingonberrywood", new float[]{0.79f, 0.29f, 0.27f});
        return Collections.unmodifiableMap(values);
    }
}

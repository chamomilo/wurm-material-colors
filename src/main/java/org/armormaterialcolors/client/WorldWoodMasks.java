package org.armormaterialcolors.client;

import com.wurmonline.client.WurmClientBase;
import com.wurmonline.client.renderer.model.collada.importer.ColladaGeometry;
import com.wurmonline.client.renderer.model.collada.importer.ColladaModel;
import com.wurmonline.client.renderer.model.collada.importer.TriangleMesh;
import com.wurmonline.client.renderer.model.collada.material.ColladaMaterial;
import com.wurmonline.client.resources.ResourceUrl;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/** Restores reviewed wood regions on stock and audited replacement models. */
public final class WorldWoodMasks {
    private static final String MARKER_PREFIX = "material_colors_wood_";
    private static final Map<String, String> RULES = readRules();
    private static final Map<String, ResourceUrl> RESOLVED = new ConcurrentHashMap<String, ResourceUrl>();
    private static final Logger LOG = Logger.getLogger("ArmorMaterialColors");
    private WorldWoodMasks() { }

    public static void attachMasks(ColladaModel model, ResourceUrl url) {
        if (url != null) attachMasks(model, url.getFilePath());
    }

    static void attachMasks(ColladaModel model, String path) {
        if (model == null || path == null || model.mask != null || model.getGeometryArray() == null) return;
        for (ColladaGeometry geometry : model.getGeometryArray()) {
            if (geometry == null || geometry.getTriangleMeshArray() == null) continue;
            for (TriangleMesh mesh : geometry.getTriangleMeshArray()) {
                if (mesh == null || hasMask(model, mesh.getName())) continue;
                ColladaMaterial material = mesh.getColladaMaterial();
                if (material == null) continue;
                String marker = maskForTexture(path, material.getTextureName());
                if (marker == null) continue;
                if (model.meshMasks == null) model.meshMasks = new HashMap<String, String>();
                model.meshMasks.put(mesh.getName(), marker);
            }
        }
    }

    static boolean hasMask(ColladaModel model, String meshName) {
        if (model.mask != null) return true;
        if (model.meshMasks == null || meshName == null) return false;
        String value = meshName.toLowerCase(Locale.ROOT);
        // Match the client's own case-insensitive substring rule. Exact-key
        // checks would overwrite a stock "wagon" mask on "WagonMesh".
        for (String key : model.meshMasks.keySet()) {
            if (key != null && value.contains(key.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    static String maskForTexture(String path, String texture) {
        if (path == null || texture == null) return null;
        return RULES.get(normalize(path) + "|" + normalize(texture));
    }

    public static ResourceUrl resolveDerivedMask(String marker) {
        if (marker == null || !marker.startsWith(MARKER_PREFIX) || !marker.endsWith(".dds")) return null;
        ResourceUrl result = RESOLVED.get(marker);
        if (result != null) return result;
        ReflectionResources.ensurePackLoaded();
        if (!ReflectionResources.isLoaded()) return null;
        result = WurmClientBase.getResourceManager().getResource("wood.material.mask." + marker);
        if (result != null) RESOLVED.put(marker, result);
        else LOG.severe("Missing wood material mask: " + marker);
        return result;
    }

    public static void neutralizeWorldTexture(ColladaMaterial material, ResourceUrl url) {
        if (material == null || url == null) return;
        String path = normalize(url.getFilePath());
        String texture = material.getTextureName();
        if (texture == null) return;
        String lower = texture.toLowerCase(Locale.ROOT);
        // Stock has ten baked plank colours; all other woods fall back to the
        // birch atlas. Use one baseline before applying all 26 material tints.
        if (path.startsWith("resources/plank_shafts/") && lower.startsWith("plank_") && lower.endsWith(".dds")) {
            material.setTextureName("Plank_birch.dds");
        }
    }

    static Map<String, String> rules() { return RULES; }

    private static String normalize(String value) {
        String path = value.replace('\\', '/').toLowerCase(Locale.ROOT);
        int query = path.indexOf('?');
        if (query >= 0) path = path.substring(0, query);
        return path;
    }

    private static Map<String, String> readRules() {
        Map<String, String> result = new HashMap<String, String>();
        InputStream source = WorldWoodMasks.class.getResourceAsStream("wood-model-masks.tsv");
        if (source == null) throw new IllegalStateException("Missing wood model mask rules");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                String[] fields = line.split("\t");
                if (fields.length != 2 || !fields[1].startsWith(MARKER_PREFIX)) {
                    throw new IllegalStateException("Invalid wood mask rule: " + line);
                }
                result.put(fields[0], fields[1]);
            }
        } catch (Exception error) {
            throw new IllegalStateException("Unable to read wood mask rules", error);
        }
        return Collections.unmodifiableMap(result);
    }
}

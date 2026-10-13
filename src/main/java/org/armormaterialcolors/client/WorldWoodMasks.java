package org.armormaterialcolors.client;

import com.wurmonline.client.WurmClientBase;
import com.wurmonline.client.renderer.model.collada.importer.ColladaGeometry;
import com.wurmonline.client.renderer.model.collada.importer.ColladaModel;
import com.wurmonline.client.renderer.model.collada.importer.TriangleMesh;
import com.wurmonline.client.renderer.model.collada.material.ColladaMaterial;
import com.wurmonline.client.resources.ResourceUrl;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Restores reviewed wood regions on stock and audited replacement models. */
public final class WorldWoodMasks {
    private static final String MARKER_PREFIX = "material_colors_wood_";
    private static final Logger LOG = Logger.getLogger("ArmorMaterialColors");
    private static final Map<String, String> RULES = readRules();
    private static final Map<String, ResourceUrl> RESOLVED = new ConcurrentHashMap<String, ResourceUrl>();
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
        try (InputStream source = openRules()) {
            if (source == null) throw new IOException("Missing wood model mask rules");
            BufferedReader reader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8));
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
            // Optional masks must never poison this class and terminate every
            // Collada/WOM model-loader thread if an installation is incomplete.
            LOG.log(Level.WARNING, "Wood mask rules unavailable; keeping original model masks", error);
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(result);
    }

    private static InputStream openRules() throws IOException {
        InputStream source = WorldWoodMasks.class.getResourceAsStream("wood-model-masks.tsv");
        if (source != null) return source;
        // Wurm loads shared mod bytecode from its ClassPool, but ordinary
        // ClassLoader resource lookups cannot see those JARs. Resolve the table
        // beside the actual class selected by that same pool, including renamed
        // JARs, without searching obsolete versions left in the mods directory.
        URL owner = HookManager.getInstance().getClassPool().find(WorldWoodMasks.class.getName());
        if (owner == null) return null;
        URLConnection resource = new URL(owner, "wood-model-masks.tsv").openConnection();
        resource.setUseCaches(false);
        return resource.getInputStream();
    }
}

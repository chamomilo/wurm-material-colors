import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/** Reproduce model loading with no product resources on the parent classpath. */
public final class WorldWoodSharedLoaderProbe {
    private static final String TYPE = "org.armormaterialcolors.client.WorldWoodMasks";
    private static final String RESOURCE = "org/armormaterialcolors/client/wood-model-masks.tsv";

    public static void main(String[] args) throws Exception {
        Path jar = Paths.get(args[0]);
        String mode = args[1];
        Map<String, String> expected = new HashMap<String, String>();
        if ("healthy".equals(mode)) {
            for (String line : Files.readAllLines(Paths.get(args[2]), StandardCharsets.UTF_8)) {
                if (line.startsWith("#") || line.trim().isEmpty()) continue;
                String[] fields = line.split("\t");
                expected.put(fields[0], fields[1]);
            }
            check(expected.size() >= 400, "Complete independent reference table");
        } else {
            Path directory = Paths.get(args[3]);
            Files.createDirectories(directory);
            Path fixture = directory.resolve("renamed-" + mode + ".jar");
            try (ZipFile original = new ZipFile(jar.toFile());
                 ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(fixture))) {
                java.util.Enumeration<? extends ZipEntry> entries = original.entries();
                byte[] buffer = new byte[8192];
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (RESOURCE.equals(entry.getName()) && "missing".equals(mode)) continue;
                    output.putNextEntry(new ZipEntry(entry.getName()));
                    if (RESOURCE.equals(entry.getName())) {
                        output.write("invalid rule\n".getBytes(StandardCharsets.UTF_8));
                    } else if (!entry.isDirectory()) {
                        try (InputStream input = original.getInputStream(entry)) {
                            int count;
                            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                        }
                    }
                    output.closeEntry();
                }
            }
            jar = fixture;
        }

        HookManager manager = HookManager.getInstance();
        // ClientLauncher adds these delegations before any game/mod classes.
        manager.getLoader().delegateLoadingOf("org.gotti.wurmunlimited.modloader.classhooks.");
        manager.getLoader().delegateLoadingOf("javassist.");
        manager.getClassPool().insertClassPath(jar.toString());
        Class<?> wood = manager.getLoader().loadClass(TYPE);
        check(wood.getClassLoader() == manager.getLoader(), "Use Wurm's real shared loader");
        check(wood.getResourceAsStream("wood-model-masks.tsv") == null,
                "Parent classpath must reproduce the missing-resource lookup");
        Method rules = wood.getDeclaredMethod("rules");
        rules.setAccessible(true);
        check(expected.equals(rules.invoke(null)), "Complete table or safe empty fallback: " + mode);
        check(expected.equals(rules.invoke(null)), "Later model loads must remain usable: " + mode);

        Class<?> model = manager.getLoader().loadClass(
                "com.wurmonline.client.renderer.model.collada.importer.ColladaModel");
        Object item = model.getConstructor(String.class).newInstance("test-item");
        Method attach = wood.getDeclaredMethod("attachMasks", model, String.class);
        attach.setAccessible(true);
        attach.invoke(null, item, "models/pmk/wagons/Wagon_main.wom");
        System.out.println("WOOD_SHARED_LOADER_OK mode=" + mode + " rules=" + expected.size());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

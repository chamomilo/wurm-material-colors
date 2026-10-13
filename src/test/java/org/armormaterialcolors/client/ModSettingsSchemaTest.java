package org.armormaterialcolors.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.chamomilo.wurm.update.ModSetting;
import org.chamomilo.wurm.update.SharedModSettings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class ModSettingsSchemaTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void sharedEditorExposesProductSettingsAndPersistsLiveChanges() throws Exception {
        String originalUserHome = System.getProperty("user.home");
        File settingsHome = temporary.newFolder("settings-home");
        try {
            System.setProperty("user.home", settingsHome.getAbsolutePath());
            Properties descriptor = new Properties();
            File project = new File(System.getProperty("armorMaterialColorsProjectDir"));
            try (InputStream input = new FileInputStream(new File(project, "mods/armor-material-colors.properties"))) {
                descriptor.load(input);
            }
            new ArmorMaterialColorsMod().configure(descriptor);
            SharedModSettings.Provider provider = SharedModSettings.provider("armor-material-colors");
            assertNotNull(provider);
            Set<String> actual = new HashSet<String>();
            for (ModSetting setting : provider.settings()) {
                assertTrue("Duplicate control: " + setting.key, actual.add(setting.key));
                assertTrue("Unknown product property: " + setting.key, descriptor.containsKey(setting.key));
                assertEquals("Only the resource pack needs a restart", !setting.key.equals("resourcePack"), setting.live);
            }
            Set<String> expected = new HashSet<String>(descriptor.stringPropertyNames());
            expected.removeIf(key -> key.startsWith("update") || key.equals("version") || key.equals("classname")
                    || key.equals("classpath") || key.equals("sharedClassLoader"));
            assertEquals(expected, actual);
            assertEquals(18, actual.size());
            assertEquals(ArmorMaterialColorsMod.resourcePack(), provider.value("resourcePack"));

            provider.change("reflectionStrength", "0.61");
            assertEquals(0.61f, ReflectionTuning.reflectionStrength(), 0.00001f);
            File savedFile = new File(settingsHome, ".chamomilo/mod-settings/armor-material-colors.properties");
            Properties saved = new Properties();
            try (InputStream input = new FileInputStream(savedFile)) { saved.load(input); }
            assertEquals("0.61", saved.getProperty("reflectionStrength"));
            try {
                provider.change("reflectionStrength", "NaN");
                fail("Invalid editor input must be rejected");
            } catch (IllegalArgumentException expectedInvalidValue) {
                assertEquals(0.61f, ReflectionTuning.reflectionStrength(), 0.00001f);
            }

            Properties restartedDescriptor = new Properties();
            restartedDescriptor.putAll(descriptor);
            restartedDescriptor.setProperty("reflectionStrength", "0.30");
            provider.change("resourcePack", "mods/armor-material-colors/armor-material-colors-resources-0.4.0.jar");
            new ArmorMaterialColorsMod().configure(restartedDescriptor);
            assertEquals("Saved editor values override installation defaults", 0.61f,
                    ReflectionTuning.reflectionStrength(), 0.00001f);
            assertEquals("The editor and active loader share the upgraded pack path",
                    ArmorMaterialColorsMod.resourcePack(),
                    SharedModSettings.provider("armor-material-colors").value("resourcePack"));
            try (InputStream input = new FileInputStream(savedFile)) { saved.load(input); }
            assertEquals(ArmorMaterialColorsMod.resourcePack(), saved.getProperty("resourcePack"));
            SharedModSettings.provider("armor-material-colors").change("resourcePack", "custom/wood.jar");
            new ArmorMaterialColorsMod().configure(restartedDescriptor);
            assertEquals("Custom resource packs remain selected", "custom/wood.jar", ArmorMaterialColorsMod.resourcePack());
        } finally {
            System.setProperty("user.home", originalUserHome);
            ArmorMaterialColorsMod.resetSettingsForTests();
        }
    }
}

package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class FallbackConfigUtil {
    private FallbackConfigUtil() {

    }
    public static FileConfiguration getConfig(File originalFile) {
        FileConfiguration config = new YamlConfiguration();

        try {
            File defaultFile = new File(FallbackConfigUtil.class.getResource(originalFile.getName()).getFile());
            if (defaultFile.exists()) {
                config.load(originalFile);
            }
            if (originalFile.exists()) {
                FileConfiguration configOverride = new YamlConfiguration();
                configOverride.load(originalFile);
                for (String key : configOverride.getKeys(false)) {
                    config.set(key, configOverride.get(key));
                }
            }
        } catch (Exception e) {

        }

        return config;
    }
}

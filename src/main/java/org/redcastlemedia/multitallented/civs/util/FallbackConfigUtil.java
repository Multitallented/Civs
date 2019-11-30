package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public final class FallbackConfigUtil {
    private FallbackConfigUtil() {

    }

    public static FileConfiguration getConfig(File originalFile, String filePath) {
        String url = "/resources/" + ConfigManager.getInstance().getDefaultConfigSet() + "/" + filePath;
        return getConfigFullPath(originalFile, url);
    }

    public static FileConfiguration getConfigFullPath(File originalFile, String url) {
        FileConfiguration config = new YamlConfiguration();
        try {
            InputStream inputStream = FallbackConfigUtil.class.getResourceAsStream(url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            config.load(reader);
            if (originalFile != null && originalFile.exists()) {
                FileConfiguration configOverride = new YamlConfiguration();
                configOverride.load(originalFile);
                for (String key : configOverride.getKeys(true)) {
                    if (configOverride.get(key) instanceof ConfigurationSection) {
                        continue;
                    }
                    config.set(key, configOverride.get(key));
                }
            }
        } catch (Exception e) {
            Civs.logger.warning(Arrays.toString(e.getStackTrace()));
        }

        return config;
    }
}

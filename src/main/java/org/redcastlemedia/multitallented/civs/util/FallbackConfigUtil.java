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
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

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
            if (Civs.getInstance() != null || !url.contains("config.yml")) {
                InputStream inputStream = FallbackConfigUtil.class.getResourceAsStream(url);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                config.load(reader);
            }
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
            if (originalFile != null) {
                Civs.logger.log(Level.SEVERE, "File name: {0}", originalFile.getName());
            }
            if (url != null) {
                Civs.logger.log(Level.SEVERE, "Resource path: {0}", url);
            }
            Civs.logger.log(Level.SEVERE, "Unable to load config", e);
        }

        return config;
    }
}

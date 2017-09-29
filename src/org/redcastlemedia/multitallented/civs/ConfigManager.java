package org.redcastlemedia.multitallented.civs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class ConfigManager {


    public static ConfigManager configManager;
    List<String> blackListWorlds;

    public List<String> getBlackListWorlds() {
        return blackListWorlds;
    }

    public ConfigManager(File configFile) {
        configManager = this;
        loadFile(configFile);
    }

    private void loadFile(File configFile) {
        FileConfiguration config = new YamlConfiguration();
        try {
            if (!configFile.exists()) {
                Civs.logger.severe(Civs.getPrefix() + "No config.yml found");
                return;
            }
            config.load(configFile);

            blackListWorlds = config.getStringList("blacklist-worlds");

        } catch (Exception e) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to read from config.yml");
        }
    }

    public static ConfigManager getInstance() {
        if (configManager == null) {
            configManager = new ConfigManager(new File(Civs.getInstance().getDataFolder(), "config.yml"));
            return configManager;
        } else {
            return configManager;
        }
    }
}

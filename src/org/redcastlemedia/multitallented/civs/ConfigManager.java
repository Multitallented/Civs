package org.redcastlemedia.multitallented.civs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigManager {


    public static ConfigManager configManager;
    List<String> blackListWorlds = new ArrayList<>();
    String defaultLanguage;
    boolean allowCivItemDropping;
    boolean explosionOverride;
    double priceMultiplier;
    HashMap<String, String> itemGroups;
    String defaultClass;
    HashMap<String, Integer> groups;

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
    public List<String> getBlackListWorlds() {
        return blackListWorlds;
    }
    public boolean getAllowSharingCivsItems() { return allowCivItemDropping; }
    public boolean getExplosionOverride() { return explosionOverride; }
    public double getPriceMultiplier() { return priceMultiplier; }
    public String getDefaultClass() { return defaultClass; }
    public HashMap<String, String> getItemGroups() { return itemGroups; }
    public HashMap<String, Integer> getGroups() { return groups; }

    public ConfigManager(File configFile) {
        configManager = this;
        loadFile(configFile);
    }

    private void loadFile(File configFile) {
        FileConfiguration config = new YamlConfiguration();
        try {
            if (!configFile.exists()) {
                Civs.logger.severe("No config.yml found");
                loadDefaults();
                return;
            }
            config.load(configFile);

            blackListWorlds = config.getStringList("blacklist-worlds");
            defaultLanguage = config.getString("default-language", "en");
            allowCivItemDropping = config.getBoolean("allow-civ-item-sharing", false);
            allowCivItemDropping = config.getBoolean("explosion-override", false);
            priceMultiplier = config.getDouble("price-multiplier", 1);
            defaultClass = config.getString("default-class", "default");
            itemGroups = new HashMap<>();
            ConfigurationSection section1 = config.getConfigurationSection("item-groups");
            if (section1 != null) {
                for (String key : section1.getKeys(false)) {
                    itemGroups.put(key, config.getString("item-groups." + key));
                }
            }
            groups = new HashMap<>();
            ConfigurationSection section = config.getConfigurationSection("groups");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    groups.put(key, config.getInt("groups." + key, -1));
                }
            }

        } catch (Exception e) {
            Civs.logger.severe("Unable to read from config.yml");
            e.printStackTrace();
        }
    }
    private void loadDefaults() {
        defaultLanguage = "en";
        allowCivItemDropping = false;
        explosionOverride = false;
        priceMultiplier = 1;
        itemGroups = new HashMap<>();
        defaultClass = "default";
        groups = new HashMap<>();
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

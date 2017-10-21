package org.redcastlemedia.multitallented.civs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.util.CVItem;

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
    double expModifier;
    int expBase;
    HashMap<String, String> itemGroups;
    String defaultClass;
    HashMap<String, Integer> groups;
    HashMap<String, CVItem> folderIcons;
    boolean useStarterBook;

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
    public List<String> getBlackListWorlds() {
        return blackListWorlds;
    }
    public boolean getAllowSharingCivsItems() { return allowCivItemDropping; }
    public boolean getExplosionOverride() { return explosionOverride; }
    public boolean getUseStarterBook() { return useStarterBook; }
    public double getPriceMultiplier() { return priceMultiplier; }
    public double getExpModifier() { return expModifier; }
    public int getExpBase() { return expBase; }
    public String getDefaultClass() { return defaultClass; }
    public HashMap<String, String> getItemGroups() { return itemGroups; }
    public HashMap<String, Integer> getGroups() { return groups; }
    public CVItem getFolderIcon(String folderName) {
        CVItem cvItem = folderIcons.get(folderName);
        if (cvItem == null) {
            cvItem = CVItem.createCVItemFromString("CHEST");
        }
        return cvItem;
    }

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
            useStarterBook = config.getBoolean("use-starter-book", true);
            priceMultiplier = config.getDouble("price-multiplier", 1);
            expModifier = config.getDouble("exp-modifier", 0.2);
            expBase = config.getInt("exp-base", 100);
            defaultClass = config.getString("default-class", "default");
            folderIcons = new HashMap<>();
            ConfigurationSection section2 = config.getConfigurationSection("folders");
            if (section2 != null) {
                for (String key : section2.getKeys(false)) {
                    folderIcons.put(key, CVItem.createCVItemFromString(config.getString("folders." + key, "CHEST")));
                }
            }
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
        useStarterBook = true;
        priceMultiplier = 1;
        expModifier = 0.2;
        expBase = 100;
        itemGroups = new HashMap<>();
        defaultClass = "default";
        groups = new HashMap<>();
        folderIcons = new HashMap<>();
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

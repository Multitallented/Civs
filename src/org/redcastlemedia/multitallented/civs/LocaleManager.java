package org.redcastlemedia.multitallented.civs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;

public class LocaleManager {

    private static LocaleManager localeManager;
    HashMap<String, HashMap<String, String>> languageMap = new HashMap<>();

    public String getTranslation(String language, String key) {
        if (!languageMap.containsKey(language) ||
                !languageMap.get(language).containsKey(key)) {
            return languageMap.get(ConfigManager.getInstance().getDefaultLanguage()).get(key);
        }
        return languageMap.get(language).get(key);
    }

    public LocaleManager(File localeFile) {
        localeManager = this;
        loadFile(localeFile);
    }

    private void loadFile(File localeFile) {
        FileConfiguration localeConfig = new YamlConfiguration();
        try {
            if (!localeFile.exists()) {
                Civs.logger.severe(Civs.getPrefix() + "No locale.yml found");
                return;
            }
            localeConfig.load(localeFile);
            if (!localeConfig.getKeys(false).contains("en")) {
                localeConfig.set("en", "Please fix your locale file.");
            }

            for (String langKey : localeConfig.getKeys(false)) {
                HashMap<String, String> currentLanguage = new HashMap<>();
                currentLanguage.put("no-region-type-found",
                        localeConfig.getString(langKey + ".no-region-type-found", "No region type found for $1"));
                currentLanguage.put("building-too-big",
                        localeConfig.getString(langKey + ".building-too-big", "You're building is too big to be a $1"));
                currentLanguage.put("no-required-blocks",
                        localeConfig.getString(langKey + ".no-required-blocks", "You haven't placed the required blocks to make a $1"));
                languageMap.put(langKey, currentLanguage);
            }


        } catch (Exception e) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to read from locale.yml");
        }
    }

    public static LocaleManager getInstance() {
        return localeManager;
    }
}

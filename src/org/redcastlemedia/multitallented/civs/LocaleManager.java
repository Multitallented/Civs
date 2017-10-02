package org.redcastlemedia.multitallented.civs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

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
    public Set<String> getAllLanguages() {
        return languageMap.keySet();
    }

    public LocaleManager(File localeFile) {
        localeManager = this;
        loadFile(localeFile);
    }

    private void loadFile(File localeFile) {
        FileConfiguration localeConfig = new YamlConfiguration();
        loadDefaults();
        try {
            if (!localeFile.exists()) {
                Civs.logger.severe(Civs.getPrefix() + "No locale.yml found");
                return;
            }
            localeConfig.load(localeFile);

            for (String langKey : localeConfig.getKeys(false)) {
                HashMap<String, String> currentLanguage = new HashMap<>();
                currentLanguage.put("language-menu",
                        localeConfig.getString(langKey + ".language-menu", "Select Language"));
                currentLanguage.put("shop",
                        localeConfig.getString(langKey + ".shop", "Shop"));
                currentLanguage.put("items",
                        localeConfig.getString(langKey + ".items", "Items"));
                currentLanguage.put("community",
                        localeConfig.getString(langKey + ".community", "Community"));

                currentLanguage.put("no-region-type-found",
                        localeConfig.getString(langKey + ".no-region-type-found", "No region type found for $1"));
                currentLanguage.put("building-too-big",
                        localeConfig.getString(langKey + ".building-too-big", "You're building is too big to be a $1"));
                currentLanguage.put("no-required-blocks",
                        localeConfig.getString(langKey + ".no-required-blocks", "You haven't placed the required blocks to make a $1"));
                currentLanguage.put("cant-build-on-region",
                        localeConfig.getString(langKey + ".cant-build-on-region", "You can't build a $1 on top of a $2"));
                currentLanguage.put("rebuild-required",
                        localeConfig.getString(langKey + ".rebuild-required", "You need to build this $1 on top of a $2"));
                languageMap.put(langKey, currentLanguage);
            }


        } catch (Exception e) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to read from locale.yml");
        }
    }
    private void loadDefaults() {
        HashMap<String, String> englishMap = new HashMap<>();
        englishMap.put("language-menu", "Select Language");
        englishMap.put("shop", "Shop");
        englishMap.put("items", "Items");
        englishMap.put("community", "Community");
        englishMap.put("no-region-type-found", "No region type found for $1");
        englishMap.put("building-too-big", "You're building is too big to be a $1");
        englishMap.put("no-required-blocks", "You haven't placed the required blocks to make a $1");
        englishMap.put("cant-build-on-region", "You can't build a $1 on top of a $2");
        englishMap.put("rebuild-required", "You need to build this $1 on top of a $2");

        languageMap.put("en", englishMap);
    }

    public static LocaleManager getInstance() {
        return localeManager;
    }
}

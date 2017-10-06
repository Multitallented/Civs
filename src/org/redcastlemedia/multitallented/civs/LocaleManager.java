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
                Civs.logger.severe("No locale.yml found");
                return;
            }
            localeConfig.load(localeFile);

            for (String langKey : localeConfig.getKeys(false)) {
                HashMap<String, String> currentLanguage = new HashMap<>();
                currentLanguage.put("name",
                        localeConfig.getString(langKey + ".name", "Unnamed"));
                currentLanguage.put("icon",
                        localeConfig.getString(langKey + ".icon", "WOOL.14"));
                currentLanguage.put("language-menu",
                        localeConfig.getString(langKey + ".language-menu", "Select Language"));
                currentLanguage.put("language-set",
                        localeConfig.getString(langKey + ".language-set", "Your language has been set to $1"));
                currentLanguage.put("shop",
                        localeConfig.getString(langKey + ".shop", "Shop"));
                currentLanguage.put("items",
                        localeConfig.getString(langKey + ".items", "Items"));
                currentLanguage.put("community",
                        localeConfig.getString(langKey + ".community", "Community"));
                currentLanguage.put("size",
                        localeConfig.getString(langKey + ".size", "Size"));
                currentLanguage.put("range",
                        localeConfig.getString(langKey + ".range", "Range"));
                currentLanguage.put("build-reqs",
                        localeConfig.getString(langKey + ".build-reqs", "All the blocks you need to place to build a $1"));
                currentLanguage.put("too-close-region",
                        localeConfig.getString(langKey + ".too-close-region", "Your $1 would be too close to a $2"));
                currentLanguage.put("region-built",
                        localeConfig.getString(langKey + ".region-built", "You have successfully built a $1"));

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
                currentLanguage.put("prevent-civs-item-share",
                        localeConfig.getString(langKey + ".prevent-civs-item-share", "You are not allowed to share Civ items"));
                currentLanguage.put("region-protected",
                        localeConfig.getString(langKey + ".region-protected", "This region is protected"));
                currentLanguage.put("region-destroyed",
                        localeConfig.getString(langKey + ".region-destroyed", "Region $1 has been destroyed!"));
                languageMap.put(langKey, currentLanguage);
            }


        } catch (Exception e) {
            Civs.logger.severe("Unable to read from locale.yml");
        }
    }
    private void loadDefaults() {
        HashMap<String, String> englishMap = new HashMap<>();
        englishMap.put("name", "English");
        englishMap.put("icon", "WOOL.14");
        englishMap.put("language-menu", "Select Language");
        englishMap.put("language-set", "Your language has been set to $1");
        englishMap.put("shop", "Shop");
        englishMap.put("items", "Items");
        englishMap.put("community", "Community");
        englishMap.put("size", "Size");
        englishMap.put("range", "Range");
        englishMap.put("build-reqs", "All the blocks you need to place to build a $1");
        englishMap.put("too-close-region", "Your $1 would be too close to a $2");
        englishMap.put("region-built", "You have successfully built a $1");
        englishMap.put("no-region-type-found", "No region type found for $1");
        englishMap.put("building-too-big", "You're building is too big to be a $1");
        englishMap.put("no-required-blocks", "You haven't placed the required blocks to make a $1");
        englishMap.put("cant-build-on-region", "You can't build a $1 on top of a $2");
        englishMap.put("rebuild-required", "You need to build this $1 on top of a $2");
        englishMap.put("prevent-civs-item-share", "You are not allowed to share Civ items");
        englishMap.put("region-protected", "This region is protected");
        englishMap.put("region-destroyed", "Region $1 has been destroyed!");

        languageMap.put("en", englishMap);
    }

    public static LocaleManager getInstance() {
        return localeManager;
    }
}

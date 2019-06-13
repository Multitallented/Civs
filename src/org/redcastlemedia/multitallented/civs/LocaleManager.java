package org.redcastlemedia.multitallented.civs;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.util.Util;

public class LocaleManager {

    private static LocaleManager localeManager;
    HashMap<String, HashMap<String, String>> languageMap = new HashMap<>();

    public String getTranslation(String language, String key) {
        String textPrefix = ConfigManager.getInstance().getPrefixAllText();
        if (!languageMap.containsKey(language) ||
                !languageMap.get(language).containsKey(key)) {
            HashMap<String, String> map = languageMap.get(ConfigManager.getInstance().getDefaultLanguage());
            if (map == null) {
                Civs.logger.severe("Unable to find default language for " + ConfigManager.getInstance().getDefaultLanguage());
                return "";
            }
            String translation = map.get(key);
            if (translation == null) {
                Civs.logger.severe("Unable to find any translation for " + key);
                return "";
            }
            return Util.parseColors(textPrefix + translation);
        }
        return Util.parseColors(textPrefix + languageMap.get(language).get(key));
    }
    public String getRawTranslation(String language, String key) {
        String textPrefix = ConfigManager.getInstance().getPrefixAllText();
        if (!languageMap.containsKey(language) ||
                !languageMap.get(language).containsKey(key)) {
            return textPrefix + languageMap.get(ConfigManager.getInstance().getDefaultLanguage()).get(key);
        }
        return textPrefix + languageMap.get(language).get(key);
    }
    public Set<String> getAllLanguages() {
        return languageMap.keySet();
    }

    public LocaleManager() {
        if (Civs.getInstance() != null) {
            loadAllConfigs();
        }
        localeManager = this;
    }

    private void loadAllConfigs() {
        File translationFolder = new File(Civs.getInstance().getDataFolder(), "translations");
        if (!translationFolder.exists()) {
            if (!translationFolder.mkdir()) {
                Civs.logger.severe("Unable to load any translations!");
                return;
            }
        }
        for (File currentTranslationFile : translationFolder.listFiles()) {
            FileConfiguration config = new YamlConfiguration();
            HashMap<String, String> currentLanguage = new HashMap<>();

            for (String translationKey : config.getKeys(false)) {
                currentLanguage.put(translationKey, config.getString(translationKey));
            }

            String langKey = currentTranslationFile.getName().replace(".yml", "");
            languageMap.put(langKey, currentLanguage);
        }
    }

    public static LocaleManager getInstance() {
        if (localeManager == null) {
            localeManager = new LocaleManager();
        }
        return localeManager;
    }
}

package org.redcastlemedia.multitallented.civs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Predicate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.util.FallbackConfigUtil;
import org.redcastlemedia.multitallented.civs.util.Util;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import javax.annotation.Nullable;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGHEST)
public class LocaleManager {

    private static LocaleManager localeManager;
    HashMap<String, HashMap<String, String>> languageMap = new HashMap<>();

    public String getTranslation(String language, String key) {
        String textPrefix = ConfigManager.getInstance().getPrefixAllText();
        if (!languageMap.containsKey(language) ||
                !languageMap.get(language).containsKey(key) ||
                languageMap.get(language).get(key).isEmpty()) {
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
                !languageMap.get(language).containsKey(key) ||
                languageMap.get(language).get(key).isEmpty()) {
            return textPrefix + languageMap.get(ConfigManager.getInstance().getDefaultLanguage()).get(key);
        }
        return textPrefix + languageMap.get(language).get(key);
    }
    public Set<String> getAllLanguages() {
        return languageMap.keySet();
    }

    private void loadAllConfigs() {
        final String TRANSLATION_FOLDER_NAME = "translations";
        File translationFolder = new File(Civs.dataLocation, TRANSLATION_FOLDER_NAME);
        boolean translationFolderExists = translationFolder.exists();
        String path = "resources." + ConfigManager.getInstance().getDefaultConfigSet() + "." + TRANSLATION_FOLDER_NAME;
        Reflections reflections = new Reflections(path , new ResourcesScanner());
        for (String fileName : reflections.getResources(Pattern.compile(".*\\.yml"))) {
            try {
                FileConfiguration config;
                String name = fileName.substring(fileName.lastIndexOf("/") + 1);
                String filePath = TRANSLATION_FOLDER_NAME + "/" + name;
                if (translationFolderExists) {
                    config = FallbackConfigUtil.getConfig(
                            new File(translationFolder, name), filePath);
                } else {
                    config = FallbackConfigUtil.getConfig(null, filePath);
                }
                loadLanguageFromConfig(config, name.replace(".yml", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (translationFolderExists) {
            for (File file : translationFolder.listFiles()) {
                String langKey = file.getName().replace(".yml", "");
                if (languageMap.containsKey(langKey)) {
                    continue;
                }
                FileConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);
                } catch (Exception e) {
                    e.printStackTrace();
                    Civs.logger.severe("Unable to load " + file.getName());
                    continue;
                }
                loadLanguageFromConfig(config, langKey);
            }
        }
    }

    private void loadLanguageFromConfig(FileConfiguration config, String name) {
        HashMap<String, String> currentLanguage = new HashMap<>();

        for (String translationKey : config.getKeys(false)) {
            currentLanguage.put(translationKey, config.getString(translationKey));
        }

        languageMap.put(name, currentLanguage);
    }

    public static LocaleManager getInstance() {
        if (localeManager == null) {
            localeManager = new LocaleManager();
            localeManager.loadAllConfigs();
        }
        return localeManager;
    }

    public boolean hasTranslation(String language, String key) {
        return languageMap.get(language) != null &&
                languageMap.get(language).get(key) != null;
    }
}

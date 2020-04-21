package org.redcastlemedia.multitallented.civs.localization;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.FallbackConfigUtil;
import org.redcastlemedia.multitallented.civs.util.Util;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import me.clip.placeholderapi.PlaceholderAPI;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGHEST)
public class LocaleManager {

    private static LocaleManager localeManager;
    HashMap<String, HashMap<String, String>> languageMap = new HashMap<>();

    public String getTranslationWithPlaceholders(OfflinePlayer player, String key) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String messageWithPlaceholders = getTranslation(civilian.getLocale(), key);
        return replacePlaceholders(player, messageWithPlaceholders);
    }

    public String getRawTranslationWithPlaceholders(OfflinePlayer player, String key) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String messageWithPlaceholders = getRawTranslation(civilian.getLocale(), key);
        return replacePlaceholders(player, messageWithPlaceholders);
    }

    @Deprecated
    public String getTranslation(String language, String key) {
        String textPrefix = ConfigManager.getInstance().getPrefixAllText();
        String[] variables = getVariables(key);
        key = key.split("\\{")[0];
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
                Civs.logger.log(Level.SEVERE, "Unable to find any translation for {0}", key);
                return "";
            }
            return Util.parseColors(textPrefix + replaceVariables(translation, variables));
        }
        return Util.parseColors(textPrefix + replaceVariables(languageMap.get(language).get(key), variables));
    }

    private String[] getVariables(String key) {
        if (!key.contains("{")) {
            return new String[0];
        }
        String[] keySplit = key.split("\\{");
        if (keySplit.length < 2) {
            return new String[0];
        }
        return key.split("\\{")[1].split(",,");
    }

    private String replaceVariables(String translation, String[] vars) {
        for (int i = 0; i < vars.length; i++) {
            translation = translation.replace("$" + (i+1), vars[i]);
        }
        return translation;
    }

    @Deprecated
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

    public String replacePlaceholders(OfflinePlayer player, String input) {
        if (Civs.placeholderAPI == null) {
            return input;
        }
        return PlaceholderAPI.setPlaceholders(player, input);
    }

    public void reload() {
        languageMap.clear();
        loadAllConfigs();
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
                Civs.logger.log(Level.SEVERE, "Error loading " + fileName, e);
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
                    Civs.logger.log(Level.SEVERE, "Unable to load " + file.getName(), e);
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

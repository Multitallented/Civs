package org.redcastlemedia.multitallented.civs.update;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;

public final class UpdateUtil {
    private UpdateUtil() {

    }

    public static void checkUpdate() {
        String currentVersion = Civs.getInstance().getDescription().getVersion();
        File configFile = new File(Civs.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }
        String lastVersion = "1.5.5";
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            lastVersion = config.getString("last-version", "1.5.5");
            if (currentVersion.equals(lastVersion)) {
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentVersion = upgrade(lastVersion);
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            config.set("last-version", currentVersion);
            config.save(configFile);
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    private static String upgrade(String lastVersion) {
        String newVersion = lastVersion;
        switch (lastVersion) {
            case "1.5.5":
                Civs.logger.info("Updating configs from " + newVersion + " to 1.5.6");
                try {
                    newVersion = Update1d5d6.update();
                } catch (Exception e) {
                    Civs.logger.severe("[Error] Update to 1.5.6 interrupted");
                    e.printStackTrace();
                    return newVersion;
                }
            case "1.5.6":
                Civs.logger.info("Updating configs from " + newVersion + " to 1.5.7");
                try {
                    newVersion = Update1d5d7.update();
                } catch (Exception e) {
                    Civs.logger.severe("[Error] Update to 1.5.7 interrupted");
                    e.printStackTrace();
                    return newVersion;
                }
            case "1.5.7":
                Civs.logger.info("Updating configs from " + newVersion + " to 1.5.8");
                try {
                    newVersion = Update1d5d8.update();
                } catch (Exception e) {
                    Civs.logger.severe("[Error] Update to 1.5.8 interrupted");
                    e.printStackTrace();
                    return newVersion;
                }
            case "1.5.8":
                Civs.logger.info("Updating configs from " + newVersion + " to 1.6.0");
                try {
                    newVersion = Update1d6d0.update();
                } catch (Exception e) {
                    Civs.logger.severe("[Error] Update to 1.6.0 interrupted");
                    e.printStackTrace();
                    return newVersion;
                }
            default:
                newVersion = Civs.getInstance().getDescription().getVersion();
        }
        return newVersion;
    }
}

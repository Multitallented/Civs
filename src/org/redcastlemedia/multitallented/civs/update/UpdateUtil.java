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
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            String lastVersion = config.getString("last-version", "1.5.5");
            if (currentVersion.equals(lastVersion)) {
                return;
            }
            upgrade(lastVersion);
            config.load(configFile);
            config.set("last-version", currentVersion);
        } catch (Exception exception) {
            return;
        }
    }

    private static void upgrade(String lastVersion) {
        switch (lastVersion) {
            case "1.5.5":
                Civs.logger.info("Updating configs from 1.5.5 to 1.5.6");
                try {
                    Update1d5d6.update();
                } catch (Exception e) {
                    Civs.logger.severe("[Error] Update to 1.5.6 interrupted");
                    e.printStackTrace();
                    return;
                }
        }
    }
}

package org.redcastlemedia.multitallented.civs.update;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;

public final class Update1d6d0 {
    private Update1d6d0() {

    }
    public static String update() {
        updateTranslations();
        return "1.6.0";
    }
    private static void updateTranslations() {
        File translationsFolder = new File(Civs.getInstance().getDataFolder(), "translations");
        if (!translationsFolder.exists()) {
            return;
        }
        File enFile = new File(translationsFolder, "en.yml");
        if (enFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(enFile);
                config.set("region-evolve", "$1 evolves into $2");
                config.set("income", "Income");
                config.set("income-desc", "Your $1 has earned $2 in the past day and $2 past week");
                config.set("item-limit", "You have $1 of $2 $3");
                config.set("warehouse-toggle-on", "Warehouse Supplies On");
                config.set("warehouse-toggle-off", "Warehouse Supplies Off");
                config.set("warehouse-toggle-desc", "Click to toggle warehouse supplies for this region");
                config.save(enFile);
            } catch (Exception e) {

            }
        }
    }
}

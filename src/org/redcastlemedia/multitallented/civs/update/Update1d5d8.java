package org.redcastlemedia.multitallented.civs.update;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;

public final class Update1d5d8 {
    private Update1d5d8() {

    }

    public static String update() {
        updateConfig();
        updateItemTypes();
        return "1.5.8";
    }

    private static void updateConfig() {
        File configFile = new File(Civs.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            config.set("use-delayed-region-upkeep-in-unloaded-chunks", true);
            config.save(configFile);
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    private static void updateItemTypes() {
        File itemTypesFolder = new File(Civs.getInstance().getDataFolder(), "item-types");
        if (!itemTypesFolder.exists()) {
            return;
        }
        upgradeTowns(itemTypesFolder);
    }

    private static void upgradeTowns(File itemTypesFolder) {
        File townsFolder = new File(itemTypesFolder, "towns");
        if (!townsFolder.exists()) {
            return;
        }
        File settlementFile = new File(townsFolder, "settlement.yml");
        if (settlementFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(settlementFile);
                List<String> limitList = config.getStringList("limits");
                if (limitList.contains("allshack:4")) {
                    limitList.remove("allshack:4");
                    limitList.add("allshack:5");
                }
                if (limitList.contains("allhovel:1")) {
                    limitList.remove("allhovel:1");
                    limitList.add("allhovel:2");
                }
                config.set("limits", limitList);
                config.save(settlementFile);
            } catch (Exception e) {

            }
        }

        File hamletFile = new File(townsFolder, "hamlet.yml");
        if (hamletFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(hamletFile);
                List<String> limitList = config.getStringList("limits");
                if (limitList.contains("allshack:5")) {
                    limitList.remove("allshack:5");
                    limitList.add("allshack:6");
                }
                config.set("limits", limitList);
                config.save(hamletFile);
            } catch (Exception e) {

            }
        }

        File villageFile = new File(townsFolder, "village.yml");
        if (villageFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(villageFile);
                List<String> limitList = config.getStringList("limits");
                if (limitList.contains("npchousing:17")) {
                    limitList.remove("npchousing:17");
                    limitList.add("npchousing:19");
                }
                config.set("limits", limitList);
                config.save(villageFile);
            } catch (Exception e) {

            }
        }

        File townFile = new File(townsFolder, "town.yml");
        if (townFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(townFile);
                List<String> limitList = config.getStringList("limits");
                if (limitList.contains("npchousing:25")) {
                    limitList.remove("npchousing:25");
                    limitList.add("npchousing:29");
                }
                config.set("limits", limitList);
                config.save(townFile);
            } catch (Exception e) {

            }
        }

        File cityFile = new File(townsFolder, "city.yml");
        if (cityFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(cityFile);
                List<String> limitList = config.getStringList("limits");
                if (limitList.contains("npchousing:43")) {
                    limitList.remove("npchousing:43");
                    limitList.add("npchousing:49");
                }
                config.set("limits", limitList);
                config.save(cityFile);
            } catch (Exception e) {

            }
        }
    }
}

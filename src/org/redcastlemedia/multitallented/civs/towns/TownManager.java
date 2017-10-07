package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.HashMap;

public class TownManager {

    private static TownManager townManager = null;
    private HashMap<String, TownType> towns = new HashMap<>();

    public TownManager() {
        townManager = this;
    }

    public void loadAllTowns() {
        File townFolder = new File(Civs.getInstance().getDataFolder(), "towns");
        if (!townFolder.exists()) {
            townFolder.mkdir();
        }
        try {
            for (File file : townFolder.listFiles()) {
                FileConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);

                    loadTown(config, file.getName().replace(".yml", ""));
                } catch (Exception e) {
                    Civs.logger.warning("Unable to read from towns/" + file.getName());
                }
            }
        } catch (NullPointerException npe) {
            Civs.logger.severe("Unable to read from town folder!");
        }
    }

    private void loadTown(FileConfiguration config, String name) {

    }

    public static TownManager getInstance() {
        if (townManager == null) {
            new TownManager();
        }
        return townManager;
    }
}

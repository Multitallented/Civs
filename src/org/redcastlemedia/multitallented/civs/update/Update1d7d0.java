package org.redcastlemedia.multitallented.civs.update;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;

public final class Update1d7d0 {
    private Update1d7d0() {

    }

    public static String update() {
        updateConfig();
        return "1.7.0";
    }

    private static void updateConfig() {
        File configFile = new File(Civs.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            ArrayList<String> claimEffects = new ArrayList<>();
            claimEffects.add("block_build");
            claimEffects.add("block_break");
            claimEffects.add("block_fire");
            config.set("nation-claim-effects", claimEffects);
            config.set("power-per-nation-claim", 1);
            config.save(configFile);
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }
}

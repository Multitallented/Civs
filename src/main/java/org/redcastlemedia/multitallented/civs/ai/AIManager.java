package org.redcastlemedia.multitallented.civs.ai;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;

import lombok.Getter;

public class AIManager {
    private HashMap<String, AI> ais = new HashMap<>();
    @Getter
    private HashMap<Player, AI> chatHandler = new HashMap<>();

    private static AIManager instance = null;
    public static AIManager getInstance() {
        if (instance == null) {
            new AIManager();
        }
        return instance;
    }

    public AIManager() {
        instance = this;
        loadAIs();
    }

    private void loadAIs() {
        File aiFolder = new File(Civs.dataLocation, "ai");
        if (!aiFolder.exists()) {
            if (!aiFolder.mkdir()) {
                Civs.logger.severe("Unable to create ai folder");
                return;
            }
        }
        try {
            for (File aiFile : aiFolder.listFiles()) {
                loadAI(aiFile);
            }
        } catch (NullPointerException npe) {
            Civs.logger.severe("Unable to create ai folder");
        }
    }

    private void loadAI(File aiFile) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(aiFile);
            // TODO load persistent data

            String townName = aiFile.getName().replace(".yml", "");
            AI ai = new AI(townName);
            ais.put(townName, ai);
        } catch (Exception e) {
            Civs.logger.severe("Unable load ai file " + aiFile.getName());
        }
    }

    public void saveAI(AI ai) {
        File aiFolder = new File(Civs.dataLocation, "ai");
        if (!aiFolder.exists()) {
            Civs.logger.severe("Unable to save to non-existent ai folder");
            return;
        }
        File aiFile = new File(aiFolder, ai.getTownName() + ".yml");
        if (!aiFile.exists()) {
            try {
                aiFile.createNewFile();
            } catch (Exception e) {
                Civs.logger.severe("Unable to create new ai file " + ai.getTownName() + ".yml");
                return;
            }
        }

        FileConfiguration config = new YamlConfiguration();
        try {
            config.set("name", ai.getTownName());
            config.save(aiFile);
        } catch (Exception e) {
            Civs.logger.severe("Unable to save ai file " + ai.getTownName() + ".yml");
        }
    }

    public AI getAI(String townName) {
        return ais.get(townName);
    }

    public ArrayList<AI> getRandomAIs() {
        ArrayList<AI> aiList = new ArrayList<>(ais.values());
        Collections.shuffle(aiList);

        return aiList;
    }
}

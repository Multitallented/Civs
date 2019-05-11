package org.redcastlemedia.multitallented.civs.alliances;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.towns.Town;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class AllianceManager implements Listener {
    private static AllianceManager instance = null;
    private final HashMap<String, Alliance> alliances = new HashMap<>();
    public static AllianceManager getInstance() {
        if (instance == null) {
            new AllianceManager();
        }
        return instance;
    }
    public AllianceManager() {
        instance = this;
        if (Civs.getInstance() != null) {
            Bukkit.getPluginManager().registerEvents(this, Civs.getInstance());
        }
    }

    public void loadAllAlliances() {
        File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
        if (allianceFolder.exists()) {
            allianceFolder.mkdir();
            return;
        }
        for (File allianceFile : allianceFolder.listFiles()) {
            loadAlliance(allianceFile);
        }
    }

    private void loadAlliance(File allianceFile) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(allianceFile);
            Alliance alliance = new Alliance();
            alliance.setName(allianceFile.getName().replace(".yml", ""));
            alliance.setMembers(new HashSet<String>(config.getStringList("members")));
            String uuidString = config.getString("last-rename", null);
            if (uuidString != null) {
                alliance.setLastRenamedBy(UUID.fromString(uuidString));
            }
            alliances.put(alliance.getName(), alliance);
        } catch (Exception e) {
            Civs.logger.severe("Unable to load alliance " + allianceFile.getName());
        }
    }

    public boolean renameAlliance(String oldName, String newName) {
        Alliance alliance = alliances.get(oldName);
        File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
        File allianceFile = new File(allianceFolder, oldName + ".yml");
        if (!allianceFile.delete()) {
            return false;
        }
        alliances.remove(oldName);
        alliance.setName(newName);
        alliances.put(newName, alliance);
        saveAlliance(alliance);
        return true;
    }

    private void saveAlliance(Alliance alliance) {
        try {
            File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
            File allianceFile = new File(allianceFolder, alliance.getName() + ".yml");
            if (!allianceFile.exists()) {
                allianceFile.createNewFile();
            }
            FileConfiguration config = new YamlConfiguration();
            config.set("members", new ArrayList<String>(alliance.getMembers()));
            if (alliance.getLastRenamedBy() != null) {
                config.set("last-rename", alliance.getLastRenamedBy().toString());
            }
            config.save(allianceFile);
        } catch (Exception e) {
            Civs.logger.severe("Unable to save alliance " + alliance.getName());
        }
    }

    public Alliance getAlliance(String name) {
        return alliances.get(name);
    }

    public HashSet<Alliance> getAlliances(Town town) {
        HashSet<Alliance> returnAlliances = new HashSet<>();
        for (Alliance alliance : alliances.values()) {
            if (alliance.getMembers().contains(town.getName())) {
                returnAlliances.add(alliance);
            }
        }
        return returnAlliances;
    }

    public boolean isAllied(Town town1, Town town2) {
        for (Alliance alliance : alliances.values()) {
            if (alliance.getMembers().contains(town1.getName()) &&
                    alliance.getMembers().contains(town2.getName())) {
                return true;
            }
        }
        return false;
    }

    public void allyTheseTowns(Town town1, Town town2) {
        for (Alliance alliance : getAlliances(town1)) {

        }
    }

    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        HashSet<Alliance> saveThese = new HashSet<>();
        for (Alliance alliance : alliances.values()) {
            if (alliance.getMembers().contains(event.getOldName())) {
                alliance.getMembers().remove(event.getOldName());
                alliance.getMembers().add(event.getNewName());
                saveThese.add(alliance);
            }
        }
        for (Alliance alliance : saveThese) {
            saveAlliance(alliance);
        }
    }

    public ArrayList<Alliance> getAllAlliances() {
        return new ArrayList<>(alliances.values());
    }
}

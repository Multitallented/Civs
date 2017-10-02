package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class CivilianManager {

    private HashMap<UUID, Civilian> onlineCivilians = new HashMap<>();

    private static CivilianManager civilianManager = null;

    public CivilianManager() {
        civilianManager = this;
    }

    public static CivilianManager getInstance() {
        if (civilianManager == null) {
            civilianManager = new CivilianManager();
            return civilianManager;
        } else {
            return civilianManager;
        }
    }

    public void loadCivilian(Player player) {
        ConfigManager configManager = ConfigManager.getInstance();
        Civilian civilian = new Civilian(player.getUniqueId(),
                configManager.getDefaultLanguage());
        onlineCivilians.put(player.getUniqueId(), civilian);
    }
    public void unloadCivilian(Player player) {
        onlineCivilians.remove(player.getUniqueId());
    }
    public Civilian getCivilian(UUID uuid) {
        return onlineCivilians.get(uuid);
    }
    public void saveCivilian(Civilian civilian) {
        Civs civs = Civs.getInstance();
        File civilianFolder = new File(civs.getDataFolder(), "players");
        if (!civilianFolder.exists()) {
            if (civilianFolder.mkdir()) {
                Civs.logger.severe(Civs.getPrefix() + "Unable to create players folder");
                return;
            }
        }
        File civilianFile = new File(civilianFolder, civilian.getUuid() + ".yml");
        if (!civilianFile.exists()) {
            try {
                civilianFile.createNewFile();
            } catch (IOException ioexception) {
                Civs.logger.severe(Civs.getPrefix() + "Unable to create " + civilian.getUuid() + ".yml");
                return;
            }
        }
        FileConfiguration civConfig = new YamlConfiguration();
        try {
            civConfig.load(civilianFile);

            civConfig.set("locale", civilian.getLocale());
            //TODO save other civilian file properties

            civConfig.save(civilianFile);
        } catch (Exception ex) {
            Civs.logger.severe(Civs.getPrefix() + "Unable to read/write " + civilian.getUuid() + ".yml");
            return;
        }
    }
}

package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.entity.Player;

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
        onlineCivilians.put(player.getUniqueId(), new Civilian());
    }
    public void unloadCivilian(Player player) {
        onlineCivilians.remove(player.getUniqueId());
    }
    public Civilian getCivilian(UUID uuid) {
        return onlineCivilians.get(uuid);
    }
}

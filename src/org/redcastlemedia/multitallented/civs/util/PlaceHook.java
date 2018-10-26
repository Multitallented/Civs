package org.redcastlemedia.multitallented.civs.util;


import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class PlaceHook extends PlaceholderHook {
    public PlaceHook() {
        PlaceholderAPI.registerPlaceholderHook("civtownname", this);
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        if (offlinePlayer == null) {
            return "";
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(offlinePlayer.getUniqueId());
        return getReplacement(civilian);
    }

    private String getReplacement(Civilian civilian) {
        Town town = TownManager.getInstance().isOwnerOfATown(civilian);
        if (town != null) {
            return town.getName();
        } else {
            int highestPopulation = 0;
            Town highestTown = null;
            for (Town to : TownManager.getInstance().getTowns()) {
                if (!to.getPeople().containsKey(civilian.getUuid())) {
                    continue;
                }
                int pop = to.getPopulation();
                if (pop > highestPopulation) {
                    highestTown = to;
                    highestPopulation = pop;
                }
            }
            return highestTown == null ? "" : highestTown.getName();
        }
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        if (p == null) { return ""; }
        Civilian civilian = CivilianManager.getInstance().getCivilian(p.getUniqueId());
        return getReplacement(civilian);
    }
}

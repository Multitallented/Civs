package org.redcastlemedia.multitallented.civs.util;


import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class PlaceHook extends PlaceholderExpansion {
    public PlaceHook() {

    }

    @Override
    public String onPlaceholderRequest(Player p, String identifier) {
        if (p == null) { return ""; }
        if (identifier.equals("town_name")) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(p.getUniqueId());
            Town town = TownManager.getInstance().isOwnerOfATown(civilian);
            if (town != null) {
                return town.getName();
            } else {

            }
            return null; //TODO fix this
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "Civs";
    }

    @Override
    public String getAuthor() {
        return "Multitallented";
    }

    @Override
    public String getVersion() {
        return "v1.0.0-b1";
    }
}

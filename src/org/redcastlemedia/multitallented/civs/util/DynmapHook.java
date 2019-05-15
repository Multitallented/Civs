package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;
import org.redcastlemedia.multitallented.civs.events.TownCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDevolveEvent;
import org.redcastlemedia.multitallented.civs.events.TownEvolveEvent;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class DynmapHook implements Listener {
    public static DynmapCommonAPI dynmapCommonAPI = null;

    public static boolean isMarkerAPIReady() {
        if (dynmapCommonAPI == null) {
            return true;
        }
        return dynmapCommonAPI.markerAPIInitialized();
    }

    public static void createMarker(Town town, TownType townType) {
        if (!isMarkerAPIReady()) {
            return;
        }
        MarkerSet markerSet = dynmapCommonAPI.getMarkerAPI().createMarkerSet("islandearth.markerset", "Dungeons",
                dynmapCommonAPI.getMarkerAPI().getMarkerIcons(), false);
        String markerId = "globe_town" + town.getName();
        int radius = townType.getBuildRadius();
        int centerX = town.getLocation().getBlockX();
        int centerZ = town.getLocation().getBlockZ();
        double x1 = centerX + radius;
        double x2 = centerX - radius;
        double z1 = centerZ + radius;
        double z2 = centerZ - radius;
        double[] x = { x1, x2 };
        double[] z = { z1, z2 };
        AreaMarker areaMarker = markerSet.createAreaMarker(markerId, "Dungeon1", false,
                "WorldName", x, z, false);
        areaMarker.setDescription("example test");
    }

    @EventHandler
    public void onTownCreation(TownCreatedEvent event) {
        createMarker(event.getTown(), event.getTownType());
    }

    @EventHandler
    public void onTownEvolve(TownEvolveEvent event) {

    }

    @EventHandler
    public void onTownDevolve(TownDevolveEvent event) {

    }

    @EventHandler
    public void onTownDestroyedEvent(TownDestroyedEvent event) {}

}

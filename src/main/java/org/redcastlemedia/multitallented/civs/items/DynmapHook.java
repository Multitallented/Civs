package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.events.TownCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDevolveEvent;
import org.redcastlemedia.multitallented.civs.events.TownEvolveEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class DynmapHook implements Listener {
    public static DynmapCommonAPI dynmapCommonAPI = null;
    private static MarkerSet markerSet = null;

    public static boolean isMarkerAPIReady() {
        if (dynmapCommonAPI == null) {
            return true;
        }
        return dynmapCommonAPI.markerAPIInitialized();
    }

    private static void initMarkerSet() {
        if (markerSet == null) {
            markerSet = dynmapCommonAPI.getMarkerAPI().createMarkerSet("islandearth.markerset", "Dungeons",
                    dynmapCommonAPI.getMarkerAPI().getMarkerIcons(), false);
        }
    }

    public static void createMarker(Town town, TownType townType) {
        if (!isMarkerAPIReady()) {
            return;
        }
        initMarkerSet();
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
                town.getLocation().getWorld().getName(), x, z, false);

        String defaultLocale = ConfigManager.getInstance().getDefaultLanguage();
        areaMarker.setDescription(townType.getDescription(defaultLocale));
    }
    public static void deleteMarker(String townName) {
        if (!isMarkerAPIReady()) {
            return;
        }
        initMarkerSet();
        String markerId = "globe_town" + townName;
        AreaMarker marker = null;
        for (AreaMarker cMarker : markerSet.getAreaMarkers()) {
            if (cMarker.getMarkerID().equals(markerId)) {
                marker = cMarker;
                break;
            }
        }
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    @EventHandler
    public void onTownCreation(TownCreatedEvent event) {
        createMarker(event.getTown(), event.getTownType());
    }

    @EventHandler
    public void onTownEvolve(TownEvolveEvent event) {
        deleteMarker(event.getTown().getName());
        createMarker(event.getTown(), event.getNewTownType());
    }

    @EventHandler
    public void onTownDevolve(TownDevolveEvent event) {
        deleteMarker(event.getTown().getName());
        createMarker(event.getTown(), event.getTownType());
    }

    @EventHandler
    public void onTownDestroyedEvent(TownDestroyedEvent event) {
        deleteMarker(event.getTown().getName());
    }

    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        deleteMarker(event.getOldName());
        TownType townType = (TownType) ItemManager.getInstance().getItemType(event.getTown().getType());
        createMarker(event.getTown(), townType);
    }

}

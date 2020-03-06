package org.redcastlemedia.multitallented.civs.dynmaphook;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RegionCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.events.TownCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDevolveEvent;
import org.redcastlemedia.multitallented.civs.events.TownEvolveEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

//https://github.com/webbukkit/dynmap/wiki/Using-markers
public class DynmapHook implements Listener {
    public static DynmapCommonAPI dynmapCommonAPI = null;
    private static MarkerSet markerSet = null;

    public static boolean isMarkerAPIReady() {
        if (dynmapCommonAPI == null) {
            return false;
        }
        return dynmapCommonAPI.markerAPIInitialized();
    }

    private static void initMarkerSet() {
        if (markerSet == null) {
            markerSet = dynmapCommonAPI.getMarkerAPI().createMarkerSet("islandearth.markerset", "Dungeons",
                    dynmapCommonAPI.getMarkerAPI().getMarkerIcons(), false);
        }
    }

    private static void createAreaMarker(Town town, TownType townType) {
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
        AreaMarker areaMarker = markerSet.createAreaMarker(markerId, town.getName(), false,
                town.getLocation().getWorld().getName(), x, z, true);

        areaMarker.setDescription(town.getName());
    }

    private static void createMarker(Location location, String label, String iconName) {
        if (!isMarkerAPIReady()) {
            return;
        }
        initMarkerSet();
        MarkerIcon markerIcon = dynmapCommonAPI.getMarkerAPI().getMarkerIcon(iconName);
        markerSet.createMarker(Region.locationToString(location), label, false,
                location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), markerIcon, true);
    }

    private static void deleteMarker(String townName) {
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
        createAreaMarker(event.getTown(), event.getTownType());
    }

    @EventHandler
    public void onTownEvolve(TownEvolveEvent event) {
        deleteMarker(event.getTown().getName());
        createAreaMarker(event.getTown(), event.getNewTownType());
    }

    @EventHandler
    public void onTownDevolve(TownDevolveEvent event) {
        deleteMarker(event.getTown().getName());
        createAreaMarker(event.getTown(), event.getTownType());
    }

    @EventHandler
    public void onTownDestroyedEvent(TownDestroyedEvent event) {
        deleteMarker(event.getTown().getName());
    }

    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        deleteMarker(event.getOldName());
        TownType townType = (TownType) ItemManager.getInstance().getItemType(event.getTown().getType());
        createAreaMarker(event.getTown(), townType);
    }

    @EventHandler
    public void onRegionCreated(RegionCreatedEvent event) {

    }
    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {

    }
}

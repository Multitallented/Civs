package org.redcastlemedia.multitallented.civs.dynmaphook;

import java.util.HashSet;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.RegionCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.events.TownCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDevolveEvent;
import org.redcastlemedia.multitallented.civs.events.TownEvolveEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

//https://github.com/webbukkit/dynmap/wiki/Using-markers
public class DynmapHook implements Listener {
    public static DynmapCommonAPI dynmapCommonAPI = null;
    private static MarkerSet markerSet = null;

    public static boolean isMarkerAPIReady() {
        if (dynmapCommonAPI == null) {
            return false;
        }
        boolean initialized = false;
        try {
            initialized = dynmapCommonAPI.markerAPIInitialized();
        } catch (NullPointerException npe) {
            return false;
        }
        return initialized;
    }

    public static void initMarkerSet() {
        if (isMarkerAPIReady() && markerSet == null) {
            markerSet = dynmapCommonAPI.getMarkerAPI().createMarkerSet("islandearth.markerset", "Civs",
                    dynmapCommonAPI.getMarkerAPI().getMarkerIcons(), false);
            for (Region region : RegionManager.getInstance().getAllRegions()) {
                RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                if (!"".equals(regionType.getDynmapMarkerKey())) {
                    createMarker(region.getLocation(),
                            regionType.getDisplayName(),
                            regionType.getDynmapMarkerKey());
                }
            }
            for (Town town : TownManager.getInstance().getTowns()) {
                createAreaMarker(town, (TownType) ItemManager.getInstance().getItemType(town.getType()));
            }
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
        try {
            AreaMarker areaMarker = markerSet.createAreaMarker(markerId, town.getName(), false,
                    town.getLocation().getWorld().getName(), x, z, true);
            areaMarker.setDescription(town.getName());
        } catch (NullPointerException npe) {
            Civs.logger.log(Level.SEVERE, "Unable to create area marker " + town.getName(), npe);
        }
    }

    private static void createMarker(Location location, String label, String iconName) {
        if (!isMarkerAPIReady()) {
            return;
        }
        initMarkerSet();
        MarkerIcon markerIcon = dynmapCommonAPI.getMarkerAPI().getMarkerIcon(iconName);
        try {
            markerSet.createMarker(Region.locationToString(location), label, false,
                    location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), markerIcon, true);
        } catch (NullPointerException nullPointerException) {
            Civs.logger.log(Level.SEVERE, "Unable to create marker " + label, nullPointerException);
        }
    }

    private static void deleteRegionMarker(Location location) {
        if (!isMarkerAPIReady()) {
            return;
        }
        initMarkerSet();
        String key = Region.locationToString(location);
        Marker marker = null;
        for (Marker cMarker : markerSet.getMarkers()) {
            if (cMarker.getMarkerID().equals(key)) {
                marker = cMarker;
            }
        }
        if (marker != null) {
            marker.deleteMarker();
        }
    }

    private static void deleteTownMarker(String townName) {
        if (!isMarkerAPIReady()) {
            return;
        }
        initMarkerSet();
        String markerId = "globe_town" + townName;
        for (AreaMarker cMarker : new HashSet<>(markerSet.getAreaMarkers())) {
            if (cMarker.getMarkerID().equals(markerId)) {
                cMarker.deleteMarker();
                break;
            }
        }
    }

    @EventHandler
    public void onTownCreation(TownCreatedEvent event) {
        createAreaMarker(event.getTown(), event.getTownType());
    }

    @EventHandler
    public void onTownEvolve(TownEvolveEvent event) {
        deleteTownMarker(event.getTown().getName());
        createAreaMarker(event.getTown(), event.getNewTownType());
    }

    @EventHandler
    public void onTownDevolve(TownDevolveEvent event) {
        deleteTownMarker(event.getTown().getName());
        createAreaMarker(event.getTown(), event.getTownType());
    }

    @EventHandler
    public void onTownDestroyedEvent(TownDestroyedEvent event) {
        deleteTownMarker(event.getTown().getName());
    }

    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        deleteTownMarker(event.getOldName());
        TownType townType = (TownType) ItemManager.getInstance().getItemType(event.getTown().getType());
        createAreaMarker(event.getTown(), townType);
    }

    @EventHandler
    public void onRegionCreated(RegionCreatedEvent event) {
        if (!"".equals(event.getRegionType().getDynmapMarkerKey())) {
            createMarker(event.getRegion().getLocation(),
                    event.getRegionType().getDisplayName(),
                    event.getRegionType().getDynmapMarkerKey());
        }
    }
    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        deleteRegionMarker(event.getRegion().getLocation());
    }
}

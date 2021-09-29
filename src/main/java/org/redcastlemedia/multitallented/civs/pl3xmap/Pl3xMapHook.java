package org.redcastlemedia.multitallented.civs.pl3xmap;

import net.pl3x.map.api.*;
import net.pl3x.map.api.Point;
import net.pl3x.map.api.marker.Marker;
import net.pl3x.map.api.marker.MarkerOptions;
import net.pl3x.map.api.marker.Rectangle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.*;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Pl3xMapHook implements Listener {
    public static Pl3xMap pl3xMap = null;
    private static Map<UUID, SimpleLayerProvider> map = new HashMap<>();
    public static void initMarkerSet() {
        if (pl3xMap == null) {
            pl3xMap = Pl3xMapProvider.get();
            pl3xMap.mapWorlds().forEach(a->{
                SimpleLayerProvider layerProvider = SimpleLayerProvider.builder("Civs-" +a.name())
                        .defaultHidden(false)
                        .showControls(true)
                        .zIndex(5)
                        .build();
                a.layerRegistry().register(Key.of("Civs"), layerProvider);
                map.put(a.uuid(), layerProvider);
            });

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
        initMarkerSet();
        String markerId = "town" + town.getName();
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
            World world = town.getLocation().getWorld();
            if (map.containsKey(world.getUID())) {
                Rectangle rectangle = Marker.rectangle(Point.point(x1, z1), Point.point(x2, z2));
                rectangle.markerOptions(MarkerOptions.builder()
                        .clickTooltip(town.getName())
                        .fill(true)
                        .fillColor(Color.RED)
                        .fillOpacity(50)
                        .build());
                map.get(world.getUID()).addMarker(Key.key(markerId), rectangle);
            }

        } catch (NullPointerException npe) {
            Civs.logger.log(Level.SEVERE, "Unable to create area marker " + town.getName(), npe);
        }
    }

    private static void createMarker(Location location, String label, String iconName) {
        initMarkerSet();
            Town townAt = TownManager.getInstance().getTownAt(location);
            World world = townAt.getLocation().getWorld();

            if (map.containsKey(world.getUID())) {
                SimpleLayerProvider layerProvider = map.get(world.getUID());
                String key = Region.locationToString(location);
                layerProvider.addMarker(Key.of(key),
                        Marker.icon(Point.fromLocation(location), Key.key(iconName), 5));
            }

    }

    private static void deleteRegionMarker(Location location) {
        initMarkerSet();
        Town townAt = TownManager.getInstance().getTownAt(location);
        World world = townAt.getLocation().getWorld();

        if (map.containsKey(world.getUID())) {
            SimpleLayerProvider layerProvider = map.get(world.getUID());
            String key = Region.locationToString(location);
            layerProvider.removeMarker(Key.of(key));
        }

    }

    private static void deleteTownMarker(String townName) {
        initMarkerSet();
        String markerId = "town" + townName;
        Town town = TownManager.getInstance().getTown(townName);
        World world = town.getLocation().getWorld();
        if (map.containsKey(world.getUID())) {
            SimpleLayerProvider layerProvider = map.get(world.getUID());
            layerProvider.removeMarker(Key.of(markerId));
        }
    }

    @EventHandler(ignoreCancelled = true)
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

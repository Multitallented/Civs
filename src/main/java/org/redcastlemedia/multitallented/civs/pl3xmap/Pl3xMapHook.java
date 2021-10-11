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
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.events.*;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.*;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Pl3xMapHook implements Listener {
    public static Pl3xMap pl3xMap = null;
    private static Map<UUID, Providers> map = new HashMap<>();

    private static class Providers {
        SimpleLayerProvider towns;
        SimpleLayerProvider regions;

        public Providers(SimpleLayerProvider towns, SimpleLayerProvider regions) {
            this.towns = towns;
            this.regions = regions;
        }
    }

    public static void initMarkerSet() {
        if (pl3xMap == null) {
            pl3xMap = Pl3xMapProvider.get();
            pl3xMap.mapWorlds().forEach(a -> {
                SimpleLayerProvider layerProviderTowns = SimpleLayerProvider.builder("Towns")
                        .defaultHidden(false)
                        .showControls(true)
                        .zIndex(90)
                        .build();
                a.layerRegistry().register(Key.of("Civs-Towns"), layerProviderTowns);

                SimpleLayerProvider layerProviderRegions = SimpleLayerProvider.builder("Regions")
                        .defaultHidden(false)
                        .showControls(true)
                        .zIndex(91)
                        .build();
                a.layerRegistry().register(Key.of("Civs-Regions"), layerProviderRegions);
                map.put(a.uuid(), new Providers(layerProviderTowns, layerProviderRegions));
            });

            for (Region region : RegionManager.getInstance().getAllRegions()) {
                RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                if (!"".equals(regionType.getDynmapMarkerKey())) {
                    createMarker(region.getLocation(),
                            regionType.getDisplayName(),
                            regionType.getDynmapMarkerKey(), region);
                }
            }
            for (Town town : TownManager.getInstance().getTowns()) {
                createAreaMarker(town, (TownType) ItemManager.getInstance().getItemType(town.getType()));
            }
        }
    }

    private static void createAreaMarker(Town town, TownType townType) {
        String markerId = "town" + town.getName().replaceAll("[\\W_]", "");
        int radius = townType.getBuildRadius();
        int centerX = town.getLocation().getBlockX();
        int centerZ = town.getLocation().getBlockZ();
        double x1 = centerX + radius;
        double x2 = centerX - radius;
        double z1 = centerZ + radius;
        double z2 = centerZ - radius;

        try {
            World world = town.getLocation().getWorld();
            if (map.containsKey(world.getUID())) {
                Rectangle rectangle = Marker.rectangle(Point.point(x1, z1), Point.point(x2, z2));

                HashSet<Alliance> alliances = AllianceManager.getInstance().getAlliances(town);
                String ally = alliances.stream().map(Alliance::getName).collect(Collectors.joining(", "));

                Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());

                rectangle.markerOptions(MarkerOptions.builder()
                        .hoverTooltip(town.getName())
                        .fill(true)
                        .fillColor(Color.RED)
                        .fillOpacity(0.2)
                        .clickTooltip(
                                ConfigManager.getInstance().getPl3xMapTownMarkerDesc()
                                        .replaceAll("%town%", town.getName())
                                        .replaceAll("%town_type%", townType.getDisplayName())
                                        .replaceAll("%player_count%", String.valueOf(town.getPeople().size()))
                                        .replaceAll("%government%", government.getName())
                                        .replaceAll("%alliance%", ally))
                        .build());
                map.get(world.getUID()).towns.addMarker(Key.key(markerId), rectangle);
            }

        } catch (NullPointerException npe) {
            Civs.logger.log(Level.SEVERE, "Unable to create area marker " + town.getName(), npe);
        }
    }

    private static void createMarker(Location location, String label, String iconName, Region region) {
        World world = location.getWorld();

        if (map.containsKey(world.getUID())) {
            SimpleLayerProvider layerProvider = map.get(world.getUID()).regions;
            String key = Region.locationToString(location).replaceAll("[\\W_]", "");
            layerProvider.addMarker(Key.of(key + "icon"),
                    Marker.icon(Point.fromLocation(location), Key.key(iconName), 5));

            int centerX = region.getLocation().getBlockX();
            int centerZ = region.getLocation().getBlockZ();
            double x1 = centerX + region.getRadiusXP();
            double x2 = centerX - region.getRadiusXN();
            double z1 = centerZ + region.getRadiusZP();
            double z2 = centerZ - region.getRadiusZN();

            Rectangle rectangle = Marker.rectangle(Point.point(x1, z1), Point.point(x2, z2));
            rectangle.markerOptions(MarkerOptions.builder()
                            .fillOpacity(0.001)
                            .fill(true)
                            .fillColor(Color.getHSBColor(204, 153, 255))
                            .strokeColor(Color.getHSBColor(153, 102, 255))
                            .hoverTooltip(region.getType())
                    .build());
            layerProvider.addMarker(Key.of(key), rectangle);

        }

    }

    private static void deleteRegionMarker(Location location) {
        Town townAt = TownManager.getInstance().getTownAt(location);
        World world = townAt.getLocation().getWorld();

        if (map.containsKey(world.getUID())) {
            SimpleLayerProvider layerProvider = map.get(world.getUID()).regions;
            String key = Region.locationToString(location).replaceAll("[\\W_]", "");;;
            layerProvider.removeMarker(Key.of(key));
        }

    }

    private static void deleteTownMarker(String townName) {
        String markerId = "town" + townName.replaceAll("[\\W_]", "");;;
        Town town = TownManager.getInstance().getTown(townName);
        World world = town.getLocation().getWorld();
        if (map.containsKey(world.getUID())) {
            SimpleLayerProvider layerProvider = map.get(world.getUID()).towns;
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
                    event.getRegionType().getDynmapMarkerKey(),
                    event.getRegion());
        }
    }

    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        deleteRegionMarker(event.getRegion().getLocation());
    }

    @EventHandler
    public void onChangeGoverment(TownChangedGovermnentEvent event) {
        deleteTownMarker(event.getTown().getName());
        TownType townType = (TownType) ItemManager.getInstance().getItemType(event.getTown().getType());
        createAreaMarker(event.getTown(), townType);
    }

    @EventHandler
    public void onPlayerJoinTown(PlayerAcceptsTownInviteEvent event) {
        deleteTownMarker(event.getTown().getName());
        TownType townType = (TownType) ItemManager.getInstance().getItemType(event.getTown().getType());
        createAreaMarker(event.getTown(), townType);
    }
}

package org.redcastlemedia.multitallented.civs.pl3xmap;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Rectangle;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
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
import java.util.HashSet;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Pl3xMapHook implements Listener {
    public static Pl3xMap pl3xMap = null;

    private static String CIVS_TOWNS = "CivsTowns";
    private static String CIVS_REGIONS = "CivsRegions";

    public void initMarkerSet() {
        if (pl3xMap == null) {
            pl3xMap = Pl3xMap.api();
        }
        pl3xMap.getWorldRegistry().forEach(world -> {
            if (!world.getLayerRegistry().has(CIVS_TOWNS)) {
                WorldLayer townLayer = new WorldLayer(CIVS_TOWNS, world, () -> "Civs Towns") {};
                townLayer.setZIndex(90);
                townLayer.setDefaultHidden(false);
                townLayer.setShowControls(true);
                if (!world.getLayerRegistry().has(CIVS_TOWNS)) {
                    world.getLayerRegistry().register(townLayer);
                }
            }
            if (!world.getLayerRegistry().has(CIVS_REGIONS)) {
                WorldLayer regionLayer = new WorldLayer(CIVS_REGIONS, world, () -> "Civs Regions") {};
                regionLayer.setZIndex(91);
                regionLayer.setDefaultHidden(false);
                regionLayer.setShowControls(true);
                if (!world.getLayerRegistry().has(CIVS_REGIONS)) {
                    world.getLayerRegistry().register(regionLayer);
                }
            }
        });

        for (Region region : RegionManager.getInstance().getAllRegions()) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (!"".equals(regionType.getDynmapMarkerKey())) {
                createMarker(region);
            }
        }
        for (Town town : TownManager.getInstance().getTowns()) {
            createAreaMarker(town);
        }
    }

    private static void createAreaMarker(Town town) {
        String markerId = "town" + town.getName().replaceAll("[\\W_]", "");
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        int radius = townType.getBuildRadius();
        int centerX = town.getLocation().getBlockX();
        int centerZ = town.getLocation().getBlockZ();
        double x1 = centerX + radius;
        double x2 = centerX - radius;
        double z1 = centerZ + radius;
        double z2 = centerZ - radius;

        try {
            World world = town.getLocation().getWorld();
            if (pl3xMap != null &&
                    world != null &&
                    pl3xMap.getWorldRegistry().has(world.getName()) &&
                    pl3xMap.getWorldRegistry().get(world.getName()).getLayerRegistry().has(CIVS_TOWNS)) {
                WorldLayer townLayer = (WorldLayer) pl3xMap.getWorldRegistry().get(world.getName()).getLayerRegistry().get(CIVS_TOWNS);
                Rectangle rectangle = Marker.rectangle(markerId, Point.of(x1, z1), Point.of(x2, z2));
                Tooltip tooltip = new Tooltip();
                tooltip.setContent(town.getName());

                HashSet<Alliance> alliances = AllianceManager.getInstance().getAlliances(town);
                String ally = alliances == null ? "" : alliances.stream().map(Alliance::getName).collect(Collectors.joining(", "));

                Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
                rectangle.setOptions(Options.builder()
                        .tooltip(tooltip)
                        .fill(true)
                        .fillColor(Color.RED.getRGB())
                        .popupContent(ConfigManager.getInstance().getPl3xMapTownMarkerDesc()
                                .replaceAll("%town%", town.getName())
                                .replaceAll("%town_type%", townType.getDisplayName())
                                .replaceAll("%player_count%", String.valueOf(town.getPeople().size()))
                                .replaceAll("%government%", government.getName())
                                .replaceAll("%alliance%", ally))
                        .build());
                townLayer.addMarker(rectangle);
            }

        } catch (NullPointerException npe) {
            Civs.logger.log(Level.SEVERE, "Unable to create area marker " + town.getName(), npe);
        }
    }

    private static void createMarker(Region region) {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        String iconName = regionType.getDisplayName();
        Location location = region.getLocation();
        World world = location.getWorld();

        if (pl3xMap != null &&
                world != null &&
                pl3xMap.getWorldRegistry().has(world.getName()) &&
                pl3xMap.getWorldRegistry().get(world.getName()).getLayerRegistry().has(CIVS_REGIONS)) {
            WorldLayer regionLayer = (WorldLayer) pl3xMap.getWorldRegistry().get(world.getName()).getLayerRegistry().get(CIVS_REGIONS);
            String key = Region.locationToString(location).replaceAll("[\\W_]", "");
            regionLayer.addMarker(Marker.icon(key + "icon", Point.of(location.getX(), location.getZ()), iconName, 5));

            int centerX = region.getLocation().getBlockX();
            int centerZ = region.getLocation().getBlockZ();
            double x1 = centerX + region.getRadiusXP();
            double x2 = centerX - region.getRadiusXN();
            double z1 = centerZ + region.getRadiusZP();
            double z2 = centerZ - region.getRadiusZN();

            Rectangle rectangle = Marker.rectangle(key, Point.of(x1, z1), Point.of(x2, z2));
            rectangle.setOptions(Options.builder()
                    .fillColor(Color.getHSBColor(204, 153, 255).getRGB())
                    .strokeColor(Color.getHSBColor(153, 102, 255).getRGB())
                    .fill(true)
                    .tooltipContent(regionType.getDisplayName())
                    .build());
            regionLayer.addMarker(rectangle);
        }
    }

    private static void deleteRegionMarker(Location location) {
        Town townAt = TownManager.getInstance().getTownAt(location);
        World world = townAt.getLocation().getWorld();

        if (pl3xMap != null &&
                world != null &&
                pl3xMap.getWorldRegistry().has(world.getName()) &&
                pl3xMap.getWorldRegistry().get(world.getName()).getLayerRegistry().has(CIVS_REGIONS)) {
            WorldLayer worldLayer = (WorldLayer) pl3xMap.getWorldRegistry().get(world.getName()).getLayerRegistry().get(CIVS_REGIONS);
            String key = Region.locationToString(location).replaceAll("[\\W_]", "");
            worldLayer.removeMarker(key);
            worldLayer.removeMarker(key + "icon");
        }

    }

    private static void deleteTownMarker(String townName) {
        Town town = TownManager.getInstance().getTown(townName);
        deleteTownMarker(town);
    }

    private static void deleteTownMarker(Town town) {
        String townName = town.getName();
        String markerId = "town" + townName.replaceAll("[\\W_]", "");
        World world = town.getLocation().getWorld();
        if (pl3xMap != null &&
                world != null &&
                pl3xMap.getWorldRegistry().has(world.getName()) &&
                pl3xMap.getWorldRegistry().get(world.getName()).getLayerRegistry().has(CIVS_TOWNS)) {
            WorldLayer worldLayer = (WorldLayer) pl3xMap.getWorldRegistry().get(world.getName()).getLayerRegistry().get(CIVS_TOWNS);
            worldLayer.removeMarker(markerId);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTownCreation(TownCreatedEvent event) {
        createAreaMarker(event.getTown());
    }

    @EventHandler
    public void onTownEvolve(TownEvolveEvent event) {
        deleteTownMarker(event.getTown());
        createAreaMarker(event.getTown());
    }

    @EventHandler
    public void onTownDevolve(TownDevolveEvent event) {
        deleteTownMarker(event.getTown());
        createAreaMarker(event.getTown());
    }

    @EventHandler
    public void onTownDestroyedEvent(TownDestroyedEvent event) {
        deleteTownMarker(event.getTown().getName());
    }

    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        deleteTownMarker(event.getOldName());
        createAreaMarker(event.getTown());
    }

    @EventHandler
    public void onRegionCreated(RegionCreatedEvent event) {
        if (!"".equals(event.getRegionType().getDynmapMarkerKey())) {
            createMarker(event.getRegion());
        }
    }

    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        deleteRegionMarker(event.getRegion().getLocation());
    }

    @EventHandler
    public void onChangeGovernment(TownChangedGovermnentEvent event) {
        deleteTownMarker(event.getTown());
        createAreaMarker(event.getTown());
    }

    @EventHandler
    public void onPlayerJoinTown(PlayerAcceptsTownInviteEvent event) {
        deleteTownMarker(event.getTown());
        createAreaMarker(event.getTown());
    }
}

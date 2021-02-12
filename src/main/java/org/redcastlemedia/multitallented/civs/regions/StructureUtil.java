package org.redcastlemedia.multitallented.civs.regions;

import lombok.Setter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public final class StructureUtil {
    private static final long DURATION = 20000;
    private static final long COOLDOWN = 5000;
    private static final HashMap<UUID, StructureUtil.BoundingBox> boundingBoxes = new HashMap<>();

    private StructureUtil() {
        // Exists so that you can't instantiate this util class
    }

    public static boolean hasBoundingBoxShown(UUID uuid) {
        if (!boundingBoxes.containsKey(uuid)) {
            return false;
        }
        StructureUtil.BoundingBox boundingBox = boundingBoxes.get(uuid);
        if (boundingBox.getCreationTime() == -1) {
            return true;
        }
        return boundingBox.getCreationTime() + DURATION >= System.currentTimeMillis();
    }

    public static void cleanUpExpiredBoundingBoxes() {
        HashSet<UUID> removeThese = new HashSet<>();
        for (UUID uuid : boundingBoxes.keySet()) {
            long createdTime = boundingBoxes.get(uuid).getCreationTime();
            if (createdTime == -1) {
                continue;
            }
            if (createdTime + DURATION < System.currentTimeMillis()) {
                removeThese.add(uuid);
            }
        }
        for (UUID uuid : removeThese) {
            removeBoundingBox(uuid);
        }
    }
    public static void showGuideBoundingBox(Player player, Location location, Region region) {
        if (!ConfigManager.getInstance().isUseBoundingBox()) {
            return;
        }
        int[] radii = new int[6];
        radii[0] = region.getRadiusXP();
        radii[1] = region.getRadiusZP();
        radii[2] = region.getRadiusXN();
        radii[3] = region.getRadiusZN();
        radii[4] = region.getRadiusYP();
        radii[5] = region.getRadiusYN();
        showGuideBoundingBox(player, location, radii, false);
    }

    public static void showGuideBoundingBox(Player player,
                                            Location location,
                                            RegionType regionType,
                                            boolean isInfinite) {
        if (!ConfigManager.getInstance().isUseBoundingBox()) {
            return;
        }
        int[] radii = new int[6];
        radii[0] = regionType.getBuildRadiusX();
        radii[1] = regionType.getBuildRadiusZ();
        radii[2] = regionType.getBuildRadiusX();
        radii[3] = regionType.getBuildRadiusZ();
        radii[4] = regionType.getBuildRadiusY();
        radii[5] = regionType.getBuildRadiusY();
        showGuideBoundingBox(player, location, radii, isInfinite);
    }

    public static void showGuideBoundingBox(Player player, Location location, int[] radii, boolean isInfinite) {
        if (!ConfigManager.getInstance().isUseBoundingBox()) {
            return;
        }
        if (location.getWorld() == null || Civs.getInstance() == null) {
            return;
        }
        boolean showParticles = ConfigManager.getInstance().isUseParticleBoundingBoxes();
        if (!showParticles && boundingBoxes.containsKey(player.getUniqueId())) {
            StructureUtil.BoundingBox boundingBox = boundingBoxes.get(player.getUniqueId());
            if (boundingBox.getCreationTime() > -1 &&
                    boundingBox.getCreationTime() + COOLDOWN > System.currentTimeMillis()) {
                return;
            } else {
                removeBoundingBox(player.getUniqueId());
            }
        }

        double maxX = location.getX() + radii[0] + 1;
        double minX = location.getX() - radii[2] - 1;
        double maxY = location.getY() + radii[4] + 1;
        double minY = location.getY() - radii[5] - 1;
        double maxZ = location.getZ() + radii[1] + 1;
        double minZ = location.getZ() - radii[3] - 1;

        StructureUtil.BoundingBox boundingBox = new StructureUtil.BoundingBox();
        if (isInfinite) {
            boundingBox.setCreationTime(-1);
        }

        for (double x = minX; x <= maxX; x++) {
            if (showParticles) {
                spawnParticle(location.getWorld(), x, minY, minZ, Color.RED, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), x, maxY, maxZ, Color.RED, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), x, minY, maxZ, Color.RED, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), x, maxY, minZ, Color.RED, player, boundingBox.getLocations());
            } else {
                setGlass(location.getWorld(), x, minY, minZ, boundingBox.getLocations(), Material.RED_STAINED_GLASS, player);
                setGlass(location.getWorld(), x, maxY, maxZ, boundingBox.getLocations(), Material.RED_STAINED_GLASS, player);
                setGlass(location.getWorld(), x, minY, maxZ, boundingBox.getLocations(), Material.RED_STAINED_GLASS, player);
                setGlass(location.getWorld(), x, maxY, minZ, boundingBox.getLocations(), Material.RED_STAINED_GLASS, player);
            }
        }
        for (double y = minY; y <= maxY; y++) {
            if (showParticles) {
                spawnParticle(location.getWorld(), minX, y, minZ, Color.GREEN, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), maxX, y, maxZ, Color.GREEN, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), minX, y, maxZ, Color.GREEN, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), maxX, y, minZ, Color.GREEN, player, boundingBox.getLocations());
            } else {
                setGlass(location.getWorld(), minX, y, minZ, boundingBox.getLocations(), Material.LIME_STAINED_GLASS, player);
                setGlass(location.getWorld(), maxX, y, maxZ, boundingBox.getLocations(), Material.LIME_STAINED_GLASS, player);
                setGlass(location.getWorld(), minX, y, maxZ, boundingBox.getLocations(), Material.LIME_STAINED_GLASS, player);
                setGlass(location.getWorld(), maxX, y, minZ, boundingBox.getLocations(), Material.LIME_STAINED_GLASS, player);
            }
        }
        for (double z = minZ; z <= maxZ; z++) {
            if (showParticles) {
                spawnParticle(location.getWorld(), minX, minY, z, Color.BLUE, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), maxX, maxY, z, Color.BLUE, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), minX, maxY, z, Color.BLUE, player, boundingBox.getLocations());
                spawnParticle(location.getWorld(), maxX, minY, z, Color.BLUE, player, boundingBox.getLocations());
            } else {
                setGlass(location.getWorld(), minX, minY, z, boundingBox.getLocations(), Material.BLUE_STAINED_GLASS, player);
                setGlass(location.getWorld(), maxX, maxY, z, boundingBox.getLocations(), Material.BLUE_STAINED_GLASS, player);
                setGlass(location.getWorld(), minX, maxY, z, boundingBox.getLocations(), Material.BLUE_STAINED_GLASS, player);
                setGlass(location.getWorld(), maxX, minY, z, boundingBox.getLocations(), Material.BLUE_STAINED_GLASS, player);
            }
        }
        boundingBoxes.put(player.getUniqueId(), boundingBox);
    }

    public static void refreshBoundingBox(StructureUtil.BoundingBox boundingBox, Player player) {
        for (Location location : boundingBox.getLocations().keySet()) {
            spawnParticle(location.getWorld(), location.getX(), location.getY(), location.getZ(),
                    boundingBox.getLocations().get(location), player, boundingBox.getLocations());
        }
    }

    public static void refreshAllBoundingBoxes() {
        if (!ConfigManager.getInstance().isUseParticleBoundingBoxes()) {
            return;
        }
        for (UUID uuid : new HashSet<>(boundingBoxes.keySet())) {
            StructureUtil.BoundingBox boundingBox = boundingBoxes.get(uuid);
            Player player = Bukkit.getPlayer(uuid);
            refreshBoundingBox(boundingBox, player);
        }
    }

    public static void removeAllBoundingBoxes() {
        for (UUID uuid : boundingBoxes.keySet()) {
            removeBoundingBox(uuid);
        }
    }

    public static void removeBoundingBox(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }
        StructureUtil.BoundingBox boundingBox = boundingBoxes.get(uuid);
        if (boundingBox == null) {
            return;
        }
        Map<Location, Color> locations = boundingBoxes.get(uuid).getLocations();
        if (locations == null) {
            return;
        }
        if (!ConfigManager.getInstance().isUseParticleBoundingBoxes()) {
            for (Location location : locations.keySet()) {
                if (!Util.isLocationWithinSightOfPlayer(location)) {
                    continue;
                }
                player.sendBlockChange(location, Material.AIR.createBlockData());
            }
        }
        boundingBoxes.remove(uuid);
    }

    private static void spawnParticle(World world, double x, double y, double z, Color color, Player player,
                                      Map<Location, Color> boundingBox) {
        Location location = new Location(world, x, y, z);
        if (location.getBlock().getType() != Material.AIR) {
            return;
        }
        player.spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 1,
                new Particle.DustOptions(color, 1));

        boundingBox.put(location, color);
    }

    private static void setGlass(World world, double x, double y, double z, Map<Location, Color> boundingBox, Material mat, Player player) {
        if (y < 1 || y >= world.getMaxHeight()) {
            return;
        }

        Location location = new Location(world, x, y, z);
        Block block = location.getBlock();
        if (block.getType() != Material.AIR ||
                block.getRelative(BlockFace.DOWN).getType() == Material.GRASS_PATH ||
                block.getRelative(BlockFace.DOWN).getType() == Material.FARMLAND) {
            return;
        }
        Color color = Color.RED;
        if (mat == Material.BLUE_STAINED_GLASS) {
            color = Color.BLUE;
        } else if (mat == Material.LIME_STAINED_GLASS) {
            color = Color.GREEN;
        }
        BlockData blockData = mat.createBlockData();
        boundingBox.put(new Location(world, x, y, z), color);
        player.sendBlockChange(location, blockData);
    }

    public static class BoundingBox {
        @Setter
        private long creationTime;
        private final HashMap<Location, Color> locations;
        public BoundingBox() {
            creationTime = System.currentTimeMillis();
            locations = new HashMap<>();
        }
        public Map<Location, Color> getLocations() {
            return locations;
        }
        public long getCreationTime() {
            return creationTime;
        }
    }
}

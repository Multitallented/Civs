package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class StructureUtil {
    private final static long DURATION = 20000;
    private final static long COOLDOWN = 5000;
    private final static HashMap<UUID, BoundingBox> boundingBoxes = new HashMap<>();

    private StructureUtil() {
        // Exists so that you can't instantiate this
    }

    public static void cleanUpExpiredBoundingBoxes() {
        HashSet<UUID> removeThese = new HashSet<>();
        for (UUID uuid : boundingBoxes.keySet()) {
            long createdTime = boundingBoxes.get(uuid).getCreationTime();
            if (createdTime + DURATION < System.currentTimeMillis()) {
                removeThese.add(uuid);
            }
        }
        for (UUID uuid : removeThese) {
            removeBoundingBox(uuid);
        }
    }
    public static void showGuideBoundingBox(Player player, Location location, Region region) {
        int[] radii = new int[6];
        radii[0] = region.getRadiusXP();
        radii[1] = region.getRadiusZP();
        radii[2] = region.getRadiusXN();
        radii[3] = region.getRadiusZN();
        radii[4] = region.getRadiusYP();
        radii[5] = region.getRadiusYN();
        showGuideBoundingBox(player, location, radii);
    }

    public static void showGuideBoundingBox(Player player, Location location, RegionType regionType) {
        int[] radii = new int[6];
        radii[0] = regionType.getBuildRadiusX();
        radii[1] = regionType.getBuildRadiusZ();
        radii[2] = regionType.getBuildRadiusX();
        radii[3] = regionType.getBuildRadiusZ();
        radii[4] = regionType.getBuildRadiusY();
        radii[5] = regionType.getBuildRadiusY();
        showGuideBoundingBox(player, location, radii);
    }

    public static void showGuideBoundingBox(Player player, Location location, int[] radii) {
        if (location.getWorld() == null || Civs.getInstance() == null) {
            return;
        }
        if (boundingBoxes.containsKey(player.getUniqueId())) {
            BoundingBox boundingBox = boundingBoxes.get(player.getUniqueId());
            if (boundingBox.getCreationTime() + COOLDOWN > System.currentTimeMillis()) {
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

        BoundingBox boundingBox = new BoundingBox();

        for (double x = minX; x <= maxX; x++) {
            setGlass(location.getWorld(), x, minY, minZ, boundingBox.getLocations(), Material.RED_STAINED_GLASS);
            setGlass(location.getWorld(), x, maxY, maxZ, boundingBox.getLocations(), Material.RED_STAINED_GLASS);
            setGlass(location.getWorld(), x, minY, maxZ, boundingBox.getLocations(), Material.RED_STAINED_GLASS);
            setGlass(location.getWorld(), x, maxY, minZ, boundingBox.getLocations(), Material.RED_STAINED_GLASS);
        }
        for (double y = minY; y <= maxY; y++) {
            setGlass(location.getWorld(), minX, y, minZ, boundingBox.getLocations(), Material.LIME_STAINED_GLASS);
            setGlass(location.getWorld(), maxX, y, maxZ, boundingBox.getLocations(), Material.LIME_STAINED_GLASS);
            setGlass(location.getWorld(), minX, y, maxZ, boundingBox.getLocations(), Material.LIME_STAINED_GLASS);
            setGlass(location.getWorld(), maxX, y, minZ, boundingBox.getLocations(), Material.LIME_STAINED_GLASS);
        }
        for (double z = minZ; z <= maxZ; z++) {
            setGlass(location.getWorld(), minX, minY, z, boundingBox.getLocations(), Material.BLUE_STAINED_GLASS);
            setGlass(location.getWorld(), maxX, maxY, z, boundingBox.getLocations(), Material.BLUE_STAINED_GLASS);
            setGlass(location.getWorld(), minX, maxY, z, boundingBox.getLocations(), Material.BLUE_STAINED_GLASS);
            setGlass(location.getWorld(), maxX, minY, z, boundingBox.getLocations(), Material.BLUE_STAINED_GLASS);
        }
        boundingBoxes.put(player.getUniqueId(), boundingBox);
    }

    public static void removeAllBoundingBoxes() {
        for (UUID uuid : boundingBoxes.keySet()) {
            removeBoundingBox(uuid);
        }
    }

    public static void removeBoundingBox(UUID uuid) {
        BoundingBox boundingBox = boundingBoxes.get(uuid);
        if (boundingBox == null) {
            return;
        }
        HashSet<Location> locations = boundingBoxes.get(uuid).getLocations();
        if (locations == null) {
            return;
        }
        for (Location location : locations) {
            Block block = location.getBlock();
            if (block.getType() != Material.RED_STAINED_GLASS &&
                    block.getType() != Material.LIME_STAINED_GLASS &&
                    block.getType() != Material.BLUE_STAINED_GLASS) {
                continue;
            }
            block.setType(Material.AIR);
        }
        boundingBoxes.remove(uuid);
    }

    private static void spawnParticle(World world, int x, int y, int z, HashSet<Location> boundingBox, Color color) {
        Location location = new Location(world, x, y, z);
        world.spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 1,
                new Particle.DustOptions(color, 1), true);
    }

    private static void setGlass(World world, double x, double y, double z, HashSet<Location> boundingBox, Material mat) {
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
        boundingBox.add(new Location(world, x, y, z));
        block.setType(mat);
    }

    private static class BoundingBox {
        private long creationTime;
        private HashSet<Location> locations;
        public BoundingBox() {
            creationTime = System.currentTimeMillis();
            locations = new HashSet<>();
        }
        public HashSet<Location> getLocations() {
            return locations;
        }
        public long getCreationTime() {
            return creationTime;
        }
    }
}

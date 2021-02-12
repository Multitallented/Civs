package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RingBuilder {
    private int x = 0;
    private int z = 0;
    private static final int Y_LEVEL = 80;
    private final Town town;
    private final Set<String> locations = new HashSet<>();

    public RingBuilder(Town town) {
        this.town = town;
    }

    public void createRing() {
        if (Civs.getInstance() == null) {
            return;
        }

        //Check if super-region has the effect
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        if (townType == null) {
            return;
        }

        final Location l = town.getLocation();
        int baseY = Math.max(l.getWorld().getHighestBlockAt(l).getY(), 64);
        baseY = baseY + Y_LEVEL > l.getWorld().getMaxHeight() ? l.getWorld().getMaxHeight() - 1 : baseY + Y_LEVEL;
        final int yL = baseY;
        final int radius = townType.getBuildRadius();
        final World world = l.getWorld();
        x = 0;
        z = 0;
        final Material material = ConfigManager.getInstance().getTownRingMat();

        do {
            if (x <= radius) {
                int xp = (int) l.getX() + x;
                int xn = (int) l.getX() - x;
                int asdf = (int) Math.sqrt(radius * radius - (x * x));
                int zp = asdf + (int) l.getZ();
                int zn = (int) l.getZ() - asdf;
                setBlockAt(world, xp, yL, zp, material);
                setBlockAt(world, xn, yL, zp, material);
                setBlockAt(world, xp, yL, zn, material);
                setBlockAt(world, xn, yL, zn, material);

            }
            x++;

            if (z <= radius) {
                int zp = (int) l.getZ() + z;
                int zn = (int) l.getZ() - z;
                int asdf = (int) Math.sqrt(radius * radius - (z * z));
                int xp = asdf + (int) l.getX();
                int xn = (int) l.getX() - asdf;
                setBlockAt(world, xp, yL, zp, material);
                setBlockAt(world, xn, yL, zp, material);
                setBlockAt(world, xp, yL, zn, material);
                setBlockAt(world, xn, yL, zn, material);

            }
            z++;
        } while (x <= radius && z <= radius);
    }

    private void setBlockAt(World world, int x, int y, int z, Material material) {
        String locationString = x + ":" + y + ":" + z;
        if (locations.contains(locationString)) {
            return;
        }
        locations.add(locationString);
        world.getBlockAt(x, y, z).setType(material);
    }

    public void destroyRing(boolean destroyAll, boolean useGravel) {
        if (Civs.getInstance() == null) {
            return;
        }
        if (!ConfigManager.getInstance().isTownRingsCrumbleToGravel()) {
            useGravel = false;
        }
        final boolean finalUseGravel = useGravel;
        removeOuterRing(useGravel);

        if (!destroyAll) {
            return;
        }

        List<Location> childLocations = town.getChildLocations();
        if (childLocations == null || childLocations.isEmpty()) {
            return;
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        long delay = (long) townType.getBuildRadius() * 2;
        for (Location l : childLocations) {

            final Location loc = l;
            if (townType.getChild() == null) {
                return;
            }

            townType = (TownType) ItemManager.getInstance().getItemType(townType.getChild());
            final TownType srType = townType;
            if (townType == null) {
                return;
            }

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(),
                    () -> removeRing(loc, srType.getBuildRadius(), finalUseGravel), delay);
            delay += townType.getBuildRadius() * 2L;
        }
    }

    private void removeOuterRing(boolean useGravel) {
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        final int radius = townType.getBuildRadius();
        removeRing(town.getLocation(), radius, useGravel);
    }

    private void removeRing(final Location l, final int radius, boolean setGravel) {
        if (Civs.getInstance() == null) {
            return;
        }
        final World world = l.getWorld();
        if (world == null) {
            return;
        }
        x = 0;
        z = 0;
        int baseY = l.getWorld().getHighestBlockAt(l).getY();
        baseY = Math.max(baseY, 64);
        baseY = baseY + Y_LEVEL > l.getWorld().getMaxHeight() ? l.getWorld().getMaxHeight() - 1 : baseY + Y_LEVEL;
        final int yL = baseY;
        final Material material = ConfigManager.getInstance().getTownRingMat();

        do {
            if (x <= radius) {
                int xp = (int) l.getX() + x;
                int xn = (int) l.getX() - x;
                int asdf = (int) Math.sqrt(radius * radius - (x * x));
                int zp = asdf + (int) l.getZ();
                int zn = (int) l.getZ() - asdf;
                if (setGravel) {
                    setBlockAt(world, xp, yL, zp, Material.GRAVEL);
                    setBlockAt(world, xn, yL, zp, Material.GRAVEL);
                    setBlockAt(world, xp, yL, zn, Material.GRAVEL);
                    setBlockAt(world, xn, yL, zn, Material.GRAVEL);
                } else {
                    setBlockAt(world, xp, yL, zp, Material.AIR);
                    setBlockAt(world, xn, yL, zp, Material.AIR);
                    setBlockAt(world, xp, yL, zn, Material.AIR);
                    setBlockAt(world, xn, yL, zn, Material.AIR);
                }

            }
            x++;
            if (z <= radius) {
                int zp = (int) l.getZ() + z;
                int zn = (int) l.getZ() - z;
                int asdf = (int) Math.sqrt(radius*radius - (z * z));
                int xp = asdf + (int) l.getX();
                int xn = (int) l.getX() - asdf;
                if (setGravel) {
                    setBlockAt(world, xp, yL, zp, Material.GRAVEL);
                    setBlockAt(world, xn, yL, zp, Material.GRAVEL);
                    setBlockAt(world, xp, yL, zn, Material.GRAVEL);
                    setBlockAt(world, xn, yL, zn, Material.GRAVEL);
                } else {
                    setBlockAt(world, xp, yL, zp, Material.AIR);
                    setBlockAt(world, xn, yL, zp, Material.AIR);
                    setBlockAt(world, xp, yL, zn, Material.AIR);
                    setBlockAt(world, xn, yL, zn, Material.AIR);
                }


            }
            z++;
        } while (x <= radius && z <= radius);
    }
}

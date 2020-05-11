package org.redcastlemedia.multitallented.civs.towns;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

public class RingBuilder {
    private int x = 0;
    private int z = 0;
    private static final int Y_LEVEL = 80;
    private final Town town;

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
                world.getBlockAt(xp, yL, zp).setType(material);
                world.getBlockAt(xn, yL, zp).setType(material);
                world.getBlockAt(xp, yL, zn).setType(material);
                world.getBlockAt(xn, yL, zn).setType(material);

            }
            x++;

            if (z <= radius) {
                int zp = (int) l.getZ() + z;
                int zn = (int) l.getZ() - z;
                int asdf = (int) Math.sqrt(radius * radius - (z * z));
                int xp = asdf + (int) l.getX();
                int xn = (int) l.getX() - asdf;
                world.getBlockAt(xp, yL, zp).setType(material);
                world.getBlockAt(xn, yL, zp).setType(material);
                world.getBlockAt(xp, yL, zn).setType(material);
                world.getBlockAt(xn, yL, zn).setType(material);

            }
            z++;
        } while (x <= radius && z <= radius);
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
                new Runnable() {

                    @Override
                    public void run() {
                        removeRing(loc, srType.getBuildRadius(), finalUseGravel);
                    }

                }, delay);
            delay += townType.getBuildRadius() * 2;
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
        baseY = baseY < 64 ? 64 : baseY;
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
                    Block block1 = world.getBlockAt(xp, yL, zp);
                    if (block1.getType() == material) {
                        block1.setType(Material.GRAVEL);
                    }
                    Block block2 = world.getBlockAt(xn, yL, zp);
                    if (block2.getType() == material) {
                        block2.setType(Material.GRAVEL);
                    }
                    Block block3 = world.getBlockAt(xp, yL, zn);
                    if (block3.getType() == material) {
                        block3.setType(Material.GRAVEL);
                    }
                    Block block4 = world.getBlockAt(xn, yL, zn);
                    if (block4.getType() == material) {
                        block4.setType(Material.GRAVEL);
                    }
                } else {
                    Block block1 = world.getBlockAt(xp, yL, zp);
                    if (block1.getType() == material) {
                        block1.setType(Material.AIR);
                    }
                    Block block2 = world.getBlockAt(xn, yL, zp);
                    if (block2.getType() == material) {
                        block2.setType(Material.AIR);
                    }
                    Block block3 = world.getBlockAt(xp, yL, zn);
                    if (block3.getType() == material) {
                        block3.setType(Material.AIR);
                    }
                    Block block4 = world.getBlockAt(xn, yL, zn);
                    if (block4.getType() == material) {
                        block4.setType(Material.AIR);
                    }
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
                    world.getBlockAt(xp, yL, zp).setType(Material.GRAVEL);
                    world.getBlockAt(xn, yL, zp).setType(Material.GRAVEL);
                    world.getBlockAt(xp, yL, zn).setType(Material.GRAVEL);
                    world.getBlockAt(xn, yL, zn).setType(Material.GRAVEL);
                } else {
                    world.getBlockAt(xp, yL, zp).setType(Material.AIR);
                    world.getBlockAt(xn, yL, zp).setType(Material.AIR);
                    world.getBlockAt(xp, yL, zn).setType(Material.AIR);
                    world.getBlockAt(xn, yL, zn).setType(Material.AIR);
                }


            }
            z++;
        } while (x <= radius && z <= radius);
    }
}

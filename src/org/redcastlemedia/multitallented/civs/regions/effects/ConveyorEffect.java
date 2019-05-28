package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import java.util.*;

public class ConveyorEffect implements Listener {
    private HashMap<Region, StorageMinecart> carts = new HashMap<>();
    private HashMap<Region, Location> cachePoints = new HashMap<>();
    private HashMap<Region, Region> cacheRegions = new HashMap<>();
    public static String KEY = "conveyor";

    @EventHandler
    public void onPoweredRailBreak(BlockBreakEvent event) {
        if (event.isCancelled() || event.getBlock().getType() != Material.POWERED_RAIL) {
            return;
        }
        Location location = Region.idToLocation(Region.blockLocationToString(event.getBlock().getLocation()));
        ArrayList<Region> regions = new ArrayList<>(RegionManager.getInstance()
                .getRegions(location, 0, false));
        if (regions.isEmpty()) {
            return;
        }
        cacheRegions.remove(regions.get(0));
    }

    @EventHandler
    public void onCustomEvent(RegionTickEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }
        Region r = event.getRegion();
        Location l = r.getLocation();

        //Check if has effect conveyor
        RegionType rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());
        if (rt == null) {
            return;
        }
        String conveyorString = r.getEffects().get(KEY);
        Material conveyor = Material.valueOf(conveyorString);

        handleExistingCarts(r);

        //Check if has reagents
        if (!r.hasUpkeepItems()) {
            cachePoints.remove(r);
            cacheRegions.remove(r);
            return;
        }

        Location loc = null;
        if (cachePoints.containsKey(r)) {
            loc = cachePoints.get(r);
        } else {
            double radius = rt.getBuildRadius();
            double x0 = l.getX();
            double y0 = l.getY();
            double z0 = l.getZ();
            outer: for (int x = (int) (x0 - radius); x < x0 + radius; x++) {
                for (int y = (int) (y0 - radius); y < y0 + radius; y++) {
                    for (int z = (int) (z0 - radius); z < z0 + radius; z++) {
                        Block b = l.getWorld().getBlockAt(x, y, z);
                        if (b.getType() == Material.POWERED_RAIL) {
                            Location location = b.getRelative(BlockFace.UP).getLocation();
                            location = Region.idToLocation(Region.blockLocationToString(location));
                            cachePoints.put(r, location);
                            break outer;
                        }
                    }
                }
            }
            if (loc == null) {
                return;
            }
            loc = cachePoints.get(r);
        }

        Chest chest = null;
        try {
            chest = (Chest) l.getBlock().getState();
        } catch (Exception e) {
            return;
        }
        Inventory cInv = chest.getInventory();
        HashSet<ItemStack> iss = new HashSet<>();
        if (!cInv.contains(Material.CHEST_MINECART) || !cInv.contains(conveyor)) {
            return;
        }
        for (ItemStack is : cInv.getContents()) {
            if (is != null && is.getType() == conveyor) {
                iss.add(is);
            }
        }
        if (iss.isEmpty()) {
            return;
        }

        //If chunk not loaded try using region cache to move directly
        if (!loc.getChunk().isLoaded()) {
            if (cacheRegions.containsKey(r)) {
                Chest tempChest = (Chest) cacheRegions.get(r).getLocation().getBlock().getState();
                if (tempChest.getInventory().firstEmpty() < 0) {
                    return;
                }
                for (ItemStack is : iss) {
                    cInv.removeItem(is);
                }
                try {
                    for (ItemStack is : iss) {
                        tempChest.getInventory().addItem(is);
                    }
                } catch (Exception e) {

                }
            }
            return;
        } else {
            for (ItemStack is : iss) {
                cInv.removeItem(is);
            }

            ItemStack tempCart = new ItemStack(Material.CHEST_MINECART, 1);
            cInv.removeItem(tempCart);

            StorageMinecart cart = loc.getWorld().spawn(loc, StorageMinecart.class);

            for (ItemStack is : iss) {
                cart.getInventory().addItem(is);
            }
            carts.put(r, cart);
        }
    }

    private void handleExistingCarts(Region r) {
        HashSet<Region> removeMe = new HashSet<>();

        if (carts.containsKey(r)) {
            StorageMinecart sm = carts.get(r);
            if (!sm.isValid() || sm.isDead()) {
                carts.remove(r);
                return;
            }
            if (!sm.getLocation().getChunk().isLoaded()) {
                try {
                    Chest returnChest = (Chest) r.getLocation().getBlock().getState();
                    returnChest.getInventory().addItem(new ItemStack(Material.CHEST_MINECART, 1));
                } catch (Exception e) {
                    //don't care
                }
                sm.remove();
                carts.remove(r);
                return;
            }
            Region region = RegionManager.getInstance().getRegionAt(sm.getLocation());
            if (region != null && !r.equals(region)) {
                Chest currentChest = null;
                try {
                    currentChest = (Chest) region.getLocation().getBlock().getState();
                } catch (Exception e) {
                    return;
                }
                HashSet<ItemStack> cartInventory = new HashSet<>();

                Inventory originInv = null;
                try {
                    originInv = ((Chest) carts.get(sm).getLocation().getBlock().getState()).getInventory();
                    originInv.addItem(new ItemStack(Material.CHEST_MINECART, 1));
                } catch (Exception e) {

                }
                boolean isFull = false;
                cartInventory.addAll(Arrays.asList(sm.getInventory().getContents()));
                for (ItemStack is : cartInventory) {
                    try {
                        if (!isFull) {
                            if (currentChest.getBlockInventory().firstEmpty() < 0) {
                                isFull = true;
                                if (originInv == null || originInv.firstEmpty() < 0) {
                                    break;
                                } else {
                                    originInv.addItem(is);
                                    sm.getInventory().removeItem(is);
                                }
                            }
                            sm.getInventory().removeItem(is);
                            currentChest.getInventory().addItem(is);
                        } else {
                            sm.getInventory().removeItem(is);
                            originInv.addItem(is);
                        }
                    } catch (NullPointerException npe) {

                    }
                }
                try {
                    Chest chest = (Chest) r.getLocation().getBlock().getState();
                    if (chest.getBlockInventory().firstEmpty() > -1) {
                        chest.getBlockInventory().addItem(new ItemStack(Material.CHEST_MINECART));
                    }
                } catch (Exception e) {

                }
                removeMe.add(r);
                if (!cacheRegions.containsKey(r)) {
                    cacheRegions.put(r, region);
                }
            }
        }

        for (Region rr : removeMe) {
            carts.get(rr).remove();
            carts.remove(rr);
        }
    }
}
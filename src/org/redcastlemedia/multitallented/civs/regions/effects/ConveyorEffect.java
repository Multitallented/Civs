package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.*;

import static org.redcastlemedia.multitallented.civs.util.Util.isLocationWithinSightOfPlayer;

public class ConveyorEffect implements Listener, RegionCreatedListener {
    private static ConveyorEffect instance = null;
    private HashMap<Region, StorageMinecart> carts = new HashMap<>();
    private HashMap<Region, Location> cacheSpawnPoints = new HashMap<>();
    private HashMap<Region, Region> cacheDestinationRegions = new HashMap<>();
    private boolean disabled = false;
    public static String KEY = "conveyor";

    public ConveyorEffect() {
        instance = this;
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
    }

    public static ConveyorEffect getInstance() {
        if (instance == null) {
            new ConveyorEffect();
        }
        return instance;
    }

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
        cacheSpawnPoints.remove(regions.get(0));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPoweredRailPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.POWERED_RAIL) {
            return;
        }
        checkForPoweredRail(event.getBlockPlaced().getLocation());
    }

    @Override
    public void regionCreatedHandler(Region region) {
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        checkForPoweredRail(region);
    }

    private void checkForPoweredRail(Location location) {
        Region region = RegionManager.getInstance().getRegionAt(location);
        if (region == null) {
            return;
        }
        checkForPoweredRail(region);
    }

    private void checkForPoweredRail(Region region) {
        Location l = region.getLocation();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        double radius = regionType.getBuildRadius();
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
                        cacheSpawnPoints.put(region, location);
                        break outer;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCustomEvent(RegionTickEvent event) {
        if (disabled || !event.getRegion().getEffects().containsKey(KEY)) {
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
            cacheSpawnPoints.remove(r);
            cacheDestinationRegions.remove(r);
            returnCart(r, true);
            return;
        }

        Location loc = null;
        if (cacheSpawnPoints.containsKey(r)) {
            loc = cacheSpawnPoints.get(r);
        } else {
            return;
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

        if (isDestinationChestFull(r)) {
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
        if (!isLocationWithinSightOfPlayer(loc)) {
            if (cacheDestinationRegions.containsKey(r)) {
                Chest tempChest = (Chest) cacheDestinationRegions.get(r).getLocation().getBlock().getState();
                if (tempChest.getBlockInventory().firstEmpty() < 0 ||
                        tempChest.getBlockInventory().firstEmpty() > tempChest.getBlockInventory().getSize() - 3) {
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

    private void returnCart(Region region, boolean removeFromCarts) {
        if (!carts.containsKey(region)) {
            return;
        }
        StorageMinecart sm = carts.get(region);
        sm.remove();
        try {
            Chest chest = (Chest) region.getLocation().getBlock().getState();
            if (chest.getBlockInventory().firstEmpty() > -1 ||
                    chest.getBlockInventory().firstEmpty() > chest.getBlockInventory().getSize() - 3) {
                chest.getBlockInventory().addItem(new ItemStack(Material.CHEST_MINECART));
            } else {
                chest.getBlockInventory().setItem(chest.getBlockInventory().getSize() -1,
                        new ItemStack(Material.CHEST_MINECART));
            }
        } catch (Exception e) {
        }
        if (removeFromCarts) {
            carts.remove(region);
        }
    }

    private boolean isDestinationChestFull(Region region) {
        if (!cacheDestinationRegions.containsKey(region)) {
            return false;
        }
        Region destinationRegion = cacheDestinationRegions.get(region);
        Block destinationBlock = destinationRegion.getLocation().getBlock();
        BlockState state = destinationBlock.getState();
        if (!(state instanceof Chest)) {
            return true;
        }
        Chest chest = (Chest) state;
        return chest.getBlockInventory().firstEmpty() < 0 ||
                chest.getBlockInventory().firstEmpty() > chest.getBlockInventory().getSize() - 3;
    }

    private void handleExistingCarts(Region r) {
        HashSet<Region> removeMe = new HashSet<>();

        if (carts.containsKey(r)) {
            StorageMinecart sm = carts.get(r);
            if (!sm.isValid() || sm.isDead()) {
                carts.remove(r);
                return;
            }
            if (!Util.isLocationWithinSightOfPlayer(sm.getLocation())) {
                returnCart(r, true);
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
                HashSet<ItemStack> cartInventory = new HashSet<>(Arrays.asList(sm.getInventory().getContents()));

                Inventory originInv = null;
                try {
                    originInv = ((Chest) carts.get(r).getLocation().getBlock().getState()).getInventory();
                } catch (Exception e) {
                }
                boolean isFull = false;
                for (ItemStack is : cartInventory) {
                    if (is == null || is.getType() == Material.AIR) {
                        continue;
                    }
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
                returnCart(r, false);
                removeMe.add(r);
                if (!cacheDestinationRegions.containsKey(r)) {
                    cacheDestinationRegions.put(r, region);
                }
            }
        }

        for (Region rr : removeMe) {
            carts.remove(rr);
        }
    }

    public void onDisable() {
        disabled = true;
        HashMap<Region, StorageMinecart> tempCarts = new HashMap<>(carts);
        for (Region region : tempCarts.keySet()) {
            returnCart(region, true);
        }
    }
}

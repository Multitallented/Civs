package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Chunk;
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
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.events.TwoSecondEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.DebugLogger;
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
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        carts.remove(event.getRegion());
        cacheSpawnPoints.remove(event.getRegion());
        cacheDestinationRegions.remove(event.getRegion());
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
    public void onTwoSecond(TwoSecondEvent event) {
        for (Region r : new HashSet<>(carts.keySet())) {
            handleExistingCarts(r);
        }
    }

    @EventHandler
    public void onCustomEvent(RegionTickEvent event) {
        if (disabled || !event.getRegion().getEffects().containsKey(KEY) ||
                !cacheSpawnPoints.containsKey(event.getRegion())) {
            return;
        }
        Region r = event.getRegion();
        Location l = r.getLocation();

        //Check if has effect conveyor
        String conveyorString = r.getEffects().get(KEY);
        Material conveyor = Material.valueOf(conveyorString);

        //Check if has reagents
//        if (!RegionManager.getInstance().hasRegionChestChanged(r)) {
//            cacheDestinationRegions.remove(r);
//            return;
//        }

        Location loc = cacheSpawnPoints.get(r);

        Inventory cInv = UnloadedInventoryHandler.getInstance().getChestInventory(l);
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
            if (!cacheDestinationRegions.containsKey(r)) {
                return;
            }
            Inventory cachedInventory = UnloadedInventoryHandler.getInstance().getChestInventory(cacheDestinationRegions.get(r).getLocation());
            if (cachedInventory.firstEmpty() < 0 ||
                    cachedInventory.firstEmpty() > cachedInventory.getSize() - 3) {
                return;
            }
            for (ItemStack is : iss) {
                cInv.removeItem(is);
            }
            try {
                if (!iss.isEmpty() && ConfigManager.getInstance().isDebugLog()) {
                    DebugLogger.inventoryModifications++;
                }
                for (ItemStack is : iss) {
                    cachedInventory.addItem(is);
                }
            } catch (Exception e) {
            }
            return;
        } else {
            for (ItemStack is : iss) {
                cInv.removeItem(is);
            }

            ItemStack tempCart = new ItemStack(Material.CHEST_MINECART, 1);
            cInv.removeItem(tempCart);

            StorageMinecart cart = loc.getWorld().spawn(loc, StorageMinecart.class);

            if (!iss.isEmpty() && ConfigManager.getInstance().isDebugLog()) {
                DebugLogger.inventoryModifications++;
            }
            for (ItemStack is : iss) {
                cart.getInventory().addItem(is);
            }
            carts.put(r, cart);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        for (Region r : new HashMap<>(carts).keySet()) {
            StorageMinecart sm = carts.get(r);
            if (chunk.equals(sm.getLocation().getChunk())) {
                returnCart(r, true);
            }
        }
    }

    private void returnCart(Region region, boolean removeFromCarts) {
        if (!carts.containsKey(region)) {
            return;
        }
        StorageMinecart sm = carts.get(region);
        try {
            Inventory returnInventory = UnloadedInventoryHandler.getInstance().getChestInventory(region.getLocation());
            if (returnInventory.firstEmpty() > -1) {
                returnInventory.addItem(new ItemStack(Material.CHEST_MINECART));
            } else {
                returnInventory.setItem(returnInventory.getSize() - 1,
                        new ItemStack(Material.CHEST_MINECART));
            }

            for (ItemStack itemStack : sm.getInventory()) {
                if (returnInventory.firstEmpty() < 0) {
                    break;
                }
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    continue;
                }
                returnInventory.addItem(itemStack);
            }
            sm.getInventory().clear();
            sm.remove();
        } catch (Exception e) {
            e.printStackTrace();
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
        if (destinationRegion == null) {
            return true;
        }
        Inventory destinationInventory = UnloadedInventoryHandler.getInstance()
                .getChestInventory(destinationRegion.getLocation());
        return destinationInventory == null || destinationInventory.firstEmpty() < 0 ||
                destinationInventory.firstEmpty() > destinationInventory.getSize() - 3;
    }

    private void handleExistingCarts(Region r) {
        if (!carts.containsKey(r)) {
            return;
        }

        StorageMinecart sm = carts.get(r);
        if (sm.isDead()) {
            carts.remove(r);
            return;
        }
        if (!sm.isValid() && !Util.isChunkLoadedAt(sm.getLocation())) {
            returnCart(r, true);
            return;
        }

        if (!Util.isLocationWithinSightOfPlayer(sm.getLocation())) {
            returnCart(r, true);
            return;
        }

        Region region = RegionManager.getInstance().getRegionAt(sm.getLocation());
        if (region == null || region.equals(r)) {
            return;
        }

        Inventory destinationInventory = UnloadedInventoryHandler.getInstance().getChestInventory(region.getLocation());
        HashSet<ItemStack> cartInventory = new HashSet<>(Arrays.asList(sm.getInventory().getContents()));

        Inventory originInv = UnloadedInventoryHandler.getInstance().getChestInventory(carts.get(r).getLocation());
        boolean isDestinationChestFull = false;
        for (ItemStack is : cartInventory) {
            if (is == null || is.getType() == Material.AIR) {
                continue;
            }
            try {
                if (!isDestinationChestFull) {
                    if (destinationInventory.firstEmpty() < 0) {
                        isDestinationChestFull = true;
                        if (originInv == null || originInv.firstEmpty() < 0) {
                            break;
                        } else {
                            originInv.addItem(is);
                            sm.getInventory().removeItem(is);
                        }
                    }
                    if (ConfigManager.getInstance().isDebugLog()) {
                        DebugLogger.inventoryModifications++;
                    }
                    sm.getInventory().removeItem(is);
                    destinationInventory.addItem(is);
                    RegionManager.getInstance().removeCheckedRegion(region);
                } else {
                    sm.getInventory().removeItem(is);
                    if (ConfigManager.getInstance().isDebugLog()) {
                        DebugLogger.inventoryModifications++;
                    }
                    originInv.addItem(is);
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                break;
            }
        }
        returnCart(r, false);
        carts.remove(r);
        if (!cacheDestinationRegions.containsKey(r)) {
            cacheDestinationRegions.put(r, region);
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

package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class UnloadedInventoryHandler {
    public static UnloadedInventoryHandler instance = null;

    public UnloadedInventoryHandler() {
        instance = this;
    }

    private static final HashMap<String, HashMap<String, Inventory>> unloadedChestInventories = new HashMap<>();
    private static final HashMap<String, ArrayList<Integer>> delayedUpkeeps = new HashMap<>();

    public void syncInventories(String locationString) {
        Location location = Region.idToLocation(locationString);
        String chunkString = getChunkString(location);
        if (!unloadedChestInventories.containsKey(chunkString) ||
                !unloadedChestInventories.get(chunkString).containsKey(locationString)) {
            getInventoryForce(location);
            return;
        }
        Inventory loadedInventory = unloadedChestInventories.get(chunkString).get(locationString);
        Inventory realInventory = getInventoryForce(location);
        if (realInventory == null) {
            return;
        }
        for (int i=0; i<27; i++) {
            ItemStack itemStack = loadedInventory.getItem(i);
            if (itemStack == null) {
                realInventory.setItem(i, new ItemStack(Material.AIR));
                continue;
            }
            if (!itemStack.equals(realInventory.getItem(i))) {
                realInventory.setItem(i, itemStack);
            }
        }
        unloadedChestInventories.get(chunkString).put(locationString, realInventory);
        runDelayedUpkeeps(locationString);
    }

    private void runDelayedUpkeeps(String locationString) {
        if (!delayedUpkeeps.containsKey(locationString)) {
            return;
        }
        Region region = RegionManager.getInstance().getRegionById(locationString);
        if (region == null) {
            return;
        }
        for (Integer i : delayedUpkeeps.get(locationString)) {
            region.runUpkeep(i);
        }
        delayedUpkeeps.remove(locationString);
    }

    public Inventory getChestInventory(Location location) {
        String locationString = Region.locationToString(location);
        if (Util.isChunkLoadedAt(location)) {
            return getInventoryForce(location);
        }
        String chunkString = getChunkString(location);
        if (!unloadedChestInventories.containsKey(chunkString) ||
                !unloadedChestInventories.get(chunkString).containsKey(locationString)) {
            return getInventoryForce(location);
        }
        return unloadedChestInventories.get(chunkString).get(locationString);
    }

    public void syncAllInventoriesInChunk(Chunk chunk) {
        String chunkString = "c:" + chunk.getX() + ":" + chunk.getZ();
        if (!unloadedChestInventories.containsKey(chunkString)) {
            return;
        }
        for (String locationString : unloadedChestInventories.get(chunkString).keySet()) {
            syncInventories(locationString);
        }
    }

    public void addUpkeep(Location location, int upkeepIndex) {
        String locationString = Region.locationToString(location);
        if (!delayedUpkeeps.containsKey(locationString)) {
            delayedUpkeeps.put(locationString, new ArrayList<>());
        }
        delayedUpkeeps.get(locationString).add(upkeepIndex);
        new Runnable() {

            @Override
            public void run() {
                location.getWorld().loadChunk(location.getChunk());
            }
        }.run();
    }

    public static String getChunkString(Location location) {
        int x = getChunkX(location);
        int z = getChunkZ(location);
        return "c:" + x + ":" + z;
    }

    public static int getChunkX(Location location) {
        return (int) Math.floor(location.getX() / 16);
    }

    public static int getChunkZ(Location location) {
        return (int) Math.floor(location.getZ() / 16);
    }

    private Inventory getInventoryForce(Location location) {
        try {
            BlockState blockState = location.getBlock().getState();
            if (blockState instanceof Chest) {
                Inventory inventory = ((Chest) blockState).getBlockInventory();
                setUnloadedChestInventory(getChunkString(location), Region.locationToString(location), inventory);
                return inventory;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        deleteUnloadedChestInventory(getChunkString(location), Region.locationToString(location));
        return null;
    }

    public void setUnloadedChestInventory(String chunkString, String locationString, Inventory inventory) {
        if (unloadedChestInventories.containsKey(chunkString)) {
            unloadedChestInventories.get(chunkString).put(locationString, inventory);
            return;
        }
        HashMap<String, Inventory> tempMap = new HashMap<>();
        tempMap.put(locationString, inventory);
        unloadedChestInventories.put(chunkString, tempMap);
    }

    public void deleteUnloadedChestInventory(Location location) {
        deleteUnloadedChestInventory(getChunkString(location), Region.locationToString(location));
    }

    public void deleteUnloadedChestInventory(String chunkString, String locationString) {
        if (unloadedChestInventories.containsKey(chunkString)) {
            unloadedChestInventories.get(chunkString).remove(locationString);
            if (unloadedChestInventories.get(chunkString).isEmpty()) {
                unloadedChestInventories.remove(chunkString);
            }
        }
    }

    public static UnloadedInventoryHandler getInstance() {
        if (instance == null) {
            new UnloadedInventoryHandler();
        }
        return instance;
    }
}

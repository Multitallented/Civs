package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.regions.Region;

import java.util.HashMap;
import java.util.Map;

@CivsSingleton()
public class UnloadedInventoryHandler {
    public static UnloadedInventoryHandler instance = null;

    public UnloadedInventoryHandler() {
        instance = this;
    }

    private static final HashMap<String, HashMap<String, CVInventory>> unloadedChestInventories = new HashMap<>();

    public void loadChunks() {
        for (Map.Entry<String, HashMap<String, CVInventory>> outerEntry : unloadedChestInventories.entrySet()) {
            for (Map.Entry<String, CVInventory> entry : outerEntry.getValue().entrySet()) {
                CVInventory cvInventory = entry.getValue();
                if (cvInventory.getLastUnloadedModification() != -1 &&
                        System.currentTimeMillis() > ConfigManager.getInstance().getUnloadedChestRefreshRate() +
                                cvInventory.getLastUnloadedModification()) {
                    Chunk chunk = cvInventory.getLocation().getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }
                }
            }
        }
    }

    public void syncInventory(String locationString) {
        Location location = Region.idToLocation(locationString);
        String chunkString = getChunkString(location);
        if (!unloadedChestInventories.containsKey(chunkString) ||
                !unloadedChestInventories.get(chunkString).containsKey(locationString)) {
            CVInventory cvInventory = new CVInventory(location);
            if (cvInventory.isValid()) {
                setUnloadedChestInventory(chunkString, locationString, cvInventory);
            }
            return;
        }
        CVInventory loadedInventory = unloadedChestInventories.get(chunkString).get(locationString);
        loadedInventory.setInventory();
        loadedInventory.sync();
    }

    public void updateInventoriesInChunk(Chunk chunk) {
        if (chunk == null) {
            return;
        }
        String chunkString = getChunkString(chunk);
        if (unloadedChestInventories.containsKey(chunkString)) {
            for (String key : unloadedChestInventories.get(chunkString).keySet()) {
                updateInventoryAtLocation(Region.idToLocation(key));
            }
        }
    }

    public void updateInventoryAtLocation(Location location) {
        if (location == null) {
            return;
        }
        String chunkString = getChunkString(location);
        String locationString = Region.locationToString(location);
        if (unloadedChestInventories.containsKey(chunkString) &&
                unloadedChestInventories.get(chunkString).containsKey(locationString)) {
            unloadedChestInventories.get(chunkString).get(locationString).update();
        }
    }

    public CVInventory getChestInventory(Location location) {
        String locationString = Region.locationToString(location);
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
            syncInventory(locationString);
        }
    }

    public static String getChunkString(Location location) {
        int x = (int) Math.floor(location.getX() / 16);
        int z = (int) Math.floor(location.getZ() / 16);
        return "c:" + x + ":" + z;
    }

    public static String getChunkString(Chunk chunk) {
        return "c:" + chunk.getX() + ":" + chunk.getZ();
    }

    private CVInventory getInventoryForce(Location location) {
        CVInventory cvInventory = new CVInventory(location);
        setUnloadedChestInventory(getChunkString(location), Region.locationToString(location), cvInventory);
        return cvInventory;
    }

    public void setUnloadedChestInventory(String chunkString, String locationString, CVInventory inventory) {
        if (unloadedChestInventories.containsKey(chunkString)) {
            unloadedChestInventories.get(chunkString).put(locationString, inventory);
            return;
        }
        HashMap<String, CVInventory> tempMap = new HashMap<>();
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

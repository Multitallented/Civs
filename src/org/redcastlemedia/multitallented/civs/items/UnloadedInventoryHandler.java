package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;

public class UnloadedInventoryHandler {
    public static UnloadedInventoryHandler instance = null;

    public UnloadedInventoryHandler() {
        instance = this;
    }

    private static final HashMap<String, Inventory> unloadedChestInventories = new HashMap<>();

    public void syncInventories(String locationString, Inventory inventory) {
        // TODO sync inventories
    }

    public Inventory getChestInventory(Location location) {
        if (Util.isLocationWithinSightOfPlayer(location) && location.getChunk().isLoaded()) {
            try {
                BlockState blockState = location.getBlock().getState();
                if (blockState instanceof Chest) {
                    return ((Chest) blockState).getBlockInventory();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return getUnloadedChestInventory(Region.locationToString(location));
    }

    private Inventory getUnloadedChestInventory(String locationString) {
        if (!unloadedChestInventories.containsKey(locationString)) {
            Location location = Region.idToLocation(locationString);
            try {
                BlockState blockState = location.getBlock().getState();
                if (blockState instanceof Chest) {
                    unloadedChestInventories.put(locationString, ((Chest) blockState).getBlockInventory());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return unloadedChestInventories.get(locationString);
    }

    public void setUnloadedChestInventory(String locationString, Inventory inventory) {
        unloadedChestInventories.put(locationString, inventory);
    }

    public void deleteUnloadedChestInventory(String locationString) {
        unloadedChestInventories.remove(locationString);
    }

    public static UnloadedInventoryHandler getInstance() {
        if (instance == null) {
            new UnloadedInventoryHandler();
        }
        return instance;
    }
}

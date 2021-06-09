package org.redcastlemedia.multitallented.civs.items;

import org.bukkit.Location;
import org.redcastlemedia.multitallented.civs.CivsSingleton;

@CivsSingleton()
public class UnloadedInventoryHandler {
    public static UnloadedInventoryHandler instance = null;

    public UnloadedInventoryHandler() {
        instance = this;
    }

    public CVInventory getChestInventory(Location location) {
        return new CVInventory(location);
    }

    public static UnloadedInventoryHandler getInstance() {
        if (instance == null) {
            new UnloadedInventoryHandler();
        }
        return instance;
    }
}

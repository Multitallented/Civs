package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu implements Listener {
    private final String MENU_NAME;

    public Menu(String menuName) {
        this.MENU_NAME = menuName;
    }

    abstract void handleInteract(InventoryClickEvent event);

    @EventHandler
    public void onMenuInteract(InventoryClickEvent event) {
        if (event.getClickedInventory() == null ||
                event.getClickedInventory().getTitle() == null ||
                !event.getClickedInventory().getTitle().equals(MENU_NAME)) {
            return;
        }
        handleInteract(event);
    }
    static int getInventorySize(int count) {
        int size = 9;
        if (count > size) {
            size = count + 9 - (count % 9);
            if (count % 9 == 0) {
                size -= 9;
            }
        }
        size += 9;
        return size;
    }
}

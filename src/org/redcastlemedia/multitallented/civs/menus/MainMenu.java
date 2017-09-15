package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MainMenu extends Menu {
    private static final String MENU_NAME = "Civs Menu";
    public MainMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        //TODO finish this stub
    }

    public static Inventory createMenu() {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);
        //TODO add items to the inventory
        inventory.setItem(0, new ItemStack(Material.MAP));
        return inventory;
    }

}

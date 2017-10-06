package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CivItem;

public class ConfirmationMenu extends Menu {
    static String MENU_NAME = "CivConfirm";
    public ConfirmationMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian, CivItem civItem) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        //TODO finish this stub
        return inventory;
    }
}

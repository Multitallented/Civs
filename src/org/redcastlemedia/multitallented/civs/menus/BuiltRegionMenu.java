package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class BuiltRegionMenu extends Menu {
    private static final String MENU_NAME = "CivsRegionStash";
    public BuiltRegionMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        //Do nothing
    }

    public static Inventory createMenu(Civilian civilian) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(civilian.getStashItems().size()), MENU_NAME);
        //TODO finish this stub
        //
//        int i=0;
//        for (CivItem cvItem : civilian.getStashItems()) {
//            if (!cvItem.getItemType().equals(CivItem.ItemType.REGION)) {
//                continue;
//            }
//            List<String> lore = new ArrayList<>();
//            lore.add(civilian.getUuid().toString());
//            lore.addAll(Util.parseColors(cvItem.getDescription()));
////            lore.addAll(cvItem.getLore());
//            cvItem.setLore(lore);
//            inventory.setItem(i, cvItem.createItemStack());
//            i++;
//        }

        return inventory;
    }
}

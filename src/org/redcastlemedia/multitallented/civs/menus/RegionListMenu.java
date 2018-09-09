package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RegionListMenu extends Menu {

    private static final String MENU_NAME = "CivRegionList";

    public RegionListMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
//        if (Menu.isBackButton(event.getCurrentItem(),
//                CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId()).getLocale())) {
//            clickBackButton(event.getWhoClicked());
//            return;
//        }
    }

    public static Inventory createMenu(HashMap<String, Integer> regionTypeNames) {
        int index = 0;


        Inventory inv = Bukkit.createInventory(null, getInventorySize(regionTypeNames.size()), MENU_NAME);

        for (String regionTypeName : regionTypeNames.keySet()) {
            CVItem civItem = ItemManager.getInstance().getItemType(regionTypeName);
            if (civItem == null) {
                civItem = new CVItem(Material.CHEST, 1, 0, regionTypeName);
            }
            ItemStack is = civItem.createItemStack();
            is.setAmount(regionTypeNames.get(regionTypeName));
            inv.setItem(index, is);
            index++;
        }

        return inv;
    }
}
package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.towns.Alliance;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

public class AllianceMenu extends Menu {
    public static final String MENU_NAME = "CivsListAlliances";

    public AllianceMenu() {
        super(MENU_NAME);
    }

    public static Inventory createMenu(Civilian civilian, int page) {
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        ArrayList<Alliance> alliances = TownManager.getInstance().getAlliances();


        int startIndex = Util.createPageButtons(inventory, page, civilian, alliances.size());


        Map<String, Object> data = new HashMap<>();
        data.put("page", page);

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        int i=9;

        ArrayList<Alliance> uuidList = new ArrayList<>();
        for (int k=startIndex; k<alliances.size() && k<startIndex+36; k++) {
            Alliance alliance = alliances.get(k);
            CVItem cvItem = CVItem.createCVItemFromString("GOLD_SWORD");
            cvItem.setDisplayName(alliance.getName());

            ArrayList<String> lore1 = new ArrayList<>();
            lore1.add("" + (i-9));
            for (String townName : alliance.getMembers()) {
                lore1.add(townName);
            }
            uuidList.add(alliance);
            cvItem.setLore(lore1);
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }
        data.put("uuidList", uuidList);
        setNewData(civilian.getUuid(), data);

        return inventory;
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        //TODO view the clicked alliance
    }
}

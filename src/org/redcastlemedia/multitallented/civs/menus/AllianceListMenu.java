package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.Alliance;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

public class AllianceListMenu extends Menu {
    public static final String MENU_NAME = "CivsListAlliances";

    public AllianceListMenu() {
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

        HashMap<String, Alliance> allianceMap = new HashMap<>();
        for (int k=startIndex; k<alliances.size() && k<startIndex+36; k++) {
            Alliance alliance = alliances.get(k);
            CVItem cvItem = CVItem.createCVItemFromString("GOLD_SWORD");
            cvItem.setDisplayName(alliance.getName());

            ArrayList<String> lore1 = new ArrayList<>();
            for (String townName : alliance.getMembers()) {
                lore1.add(townName);
            }
            allianceMap.put(alliance.getName(), alliance);
            cvItem.setLore(lore1);
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }
        data.put("allianceMap", allianceMap);
        setNewData(civilian.getUuid(), data);

        return inventory;
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        int page = (int) getData(civilian.getUuid(), "page");

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem().getType() == Material.EMERALD) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(AllianceListMenu.createMenu(civilian, page + 1));
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(AllianceListMenu.createMenu(civilian, page - 1));
            return;
        }

        HashMap<String, Alliance> allianceHashMap = (HashMap<String, Alliance>) getData(civilian.getUuid(), "allianceMap");
        String allianceName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (allianceHashMap.containsKey(allianceName)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(AllianceMenu.createMenu(civilian, allianceHashMap.get(allianceName)));
            return;
        }
    }
}

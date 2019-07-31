package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import java.util.*;

public class RegionListMenu extends Menu {

    private static final String MENU_NAME = "CivRegionList";

    public RegionListMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }

        if (Menu.isBackButton(event.getCurrentItem(),
                CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId()).getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        HashMap<String, Integer> regionTypeNames = (HashMap<String, Integer>) getData(civilian.getUuid(), "regionTypeNames");
        int page = (int) getData(civilian.getUuid(), "page");
        if (event.getCurrentItem().getType() == Material.EMERALD &&
                localeManager.getTranslation(civilian.getLocale(), "next-button")
                        .equals(event.getCurrentItem().getItemMeta().getDisplayName())) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionListMenu.createMenu(civilian, regionTypeNames, page + 1));
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE &&
                localeManager.getTranslation(civilian.getLocale(), "prev-button")
                        .equals(event.getCurrentItem().getItemMeta().getDisplayName())) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionListMenu.createMenu(civilian, regionTypeNames, page - 1));
            return;
        }

        String processedName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        String regionTypeName = processedName.replace(
                ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase();
        CivItem civItem = ItemManager.getInstance().getItemType(regionTypeName);
        if (civItem instanceof TownType) {
            TownType townType = (TownType) civItem;
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownTypeInfoMenu.createMenu(civilian, townType));
            return;
        }
        if (!(civItem instanceof RegionType)) {
            return;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
        event.getWhoClicked().closeInventory();
        event.getWhoClicked().openInventory(RegionTypeInfoMenu.createMenu(civilian, regionType));
    }

    public static Inventory createMenu(Civilian civilian, HashMap<String, Integer> regionTypeNames, int page) {
        int index = 9;

        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("regionTypeNames", regionTypeNames);
        setNewData(civilian.getUuid(), data);

        LocaleManager localeManager = LocaleManager.getInstance();
        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }
        int startIndex = page * 36;
        //8 Next button
        if (startIndex + 36 < regionTypeNames.size()) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }

        inventory.setItem(8, getBackButton(civilian));

        for (String regionTypeName : regionTypeNames.keySet()) {
            CivItem civItem = ItemManager.getInstance().getItemType(regionTypeName);
            if (civItem == null) {
                CVItem cvItem = new CVItem(Material.CHEST, regionTypeNames.get(regionTypeName), 0, regionTypeName);
                inventory.setItem(index, cvItem.createItemStack());
            } else {
                ItemStack is = civItem.getShopIcon().clone().createItemStack();
                is.setAmount(regionTypeNames.get(regionTypeName));
                inventory.setItem(index, is);
            }
            index++;
        }

        return inventory;
    }
}

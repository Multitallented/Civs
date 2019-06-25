package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.lang.reflect.Array;
import java.util.*;

public class TownListMenu extends Menu {
    public static String MENU_NAME = "CivsTownList";

    public TownListMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() ||
                (event.getCurrentItem().getType() == Material.STONE &&
                event.getCurrentItem().getItemMeta().getDisplayName().startsWith("Icon"))) {
            return;
        }
        String itemName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        Integer dataPage = (Integer) getData(civilian.getUuid(), "page");
        int page = dataPage == null ? 0 : dataPage;
        UUID uuid = (UUID) getData(civilian.getUuid(), "uuid");
        ArrayList<Town> towns = (ArrayList<Town>) getData(civilian.getUuid(), "townList");

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        LocaleManager localeManager = LocaleManager.getInstance();
        if (event.getCurrentItem().getType() == Material.EMERALD &&
                itemName.equals(ChatColor.stripColor(localeManager.getTranslation(civilian.getLocale(), "next-button")))) {
            if (uuid == null) {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            } else {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page + "," + uuid.toString());
            }
            event.getWhoClicked().closeInventory();
            if (towns == null) {
                event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, page + 1, uuid));
            } else {
                event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, page + 1, towns));
            }
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE &&
                itemName.equals(ChatColor.stripColor(localeManager.getTranslation(civilian.getLocale(), "prev-button")))) {
            if (uuid == null) {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            } else {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page + "," + uuid.toString());
            }
            event.getWhoClicked().closeInventory();
            if (towns == null) {
                event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, page - 1, uuid));
            } else {
                event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, page - 1, towns));
            }
            return;
        }
        String townName = event.getCurrentItem().getItemMeta().getDisplayName();
        Town town = TownManager.getInstance().getTown(townName);
        if (town != null) {
            if (uuid == null) {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            } else {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page + "," + uuid.toString());
            }
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownActionMenu.createMenu(civilian, town));
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, int page, ArrayList<Town> towns) {
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("townList", towns);
        setNewData(civilian.getUuid(), data);

        setItems(inventory, page, civilian, towns);

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, int page) {
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        setNewData(civilian.getUuid(), data);

        setItems(inventory, page, civilian, TownManager.getInstance().getTowns());

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, int page, UUID uuid) {
        return createMenu(civilian, page, uuid, true);
    }
    public static Inventory createMenu(Civilian civilian, int page, UUID uuid, boolean showAllied) {
        List<Town> towns = TownManager.getInstance().getTowns();
        if (uuid != null) {
            List<Town> newTownList = new ArrayList<>();
            for (Town town : towns) {
                if (showAllied && town.getPeople().containsKey(uuid)) {
                    newTownList.add(town);
                } else if (!showAllied && town.getRawPeople().containsKey(uuid) &&
                        !town.getRawPeople().get(uuid).contains("ally")) {
                    newTownList.add(town);
                }
            }
            towns = newTownList;
        }
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);
        int startIndex = page * 36;

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("uuid", uuid);
        setNewData(civilian.getUuid(), data);

        setItems(inventory, startIndex, civilian, towns);


        return inventory;
    }

    private static void setItems(Inventory inventory, int page, Civilian civilian, List<Town> towns) {

        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }

        int startIndex = page * 36;

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));


        //8 Next button
        if (startIndex + 36 < towns.size()) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }

        int i=9;
        for (int k=startIndex; k<towns.size() && k<startIndex+36; k++) {
            Town town = towns.get(k);
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            CVItem cvItem1 = townType.getShopIcon().clone();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "town-power").replace("$1", "" + town.getPower())
                            .replace("$2", "" + town.getMaxPower()));
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "pop-desc")
                    .replace("$1", town.getPopulation() + "")
                    .replace("$2", town.getHousing() + "")
                    .replace("$3", town.getVillagers() + ""));
            lore.addAll(Util.textWrap(Util.parseColors(townType.getDescription(civilian.getLocale()))));

            CVItem cycleItem = null;
            boolean govTypesAllowed = ConfigManager.getInstance().isAllowChangingOfGovType();
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            if (govTypesAllowed && government != null) {
                cycleItem = new CVItem(government.getIcon(civilian.getLocale()).getMat(), 1);
            }

            cvItem1.setDisplayName(town.getName());
            cvItem1.setLore(lore);
            inventory.setItem(i, cvItem1.createItemStack());

            if (govTypesAllowed && government != null) {
                ArrayList<CVItem> cycleList = new ArrayList<>();
                cycleItem.setDisplayName(town.getName());
                cycleItem.setLore(lore);
                cycleList.add(cvItem1);
                cycleList.add(cycleItem);
                Menu.addCycleItems(civilian.getUuid(), inventory, i, cycleList);
            }
            i++;
        }
    }
}

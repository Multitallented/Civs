package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
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

import java.util.*;

public class TownListMenu extends Menu {
    public static String MENU_NAME = "CivsTownList";

    public TownListMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || (event.getCurrentItem().getType() == Material.STONE &&
                event.getCurrentItem().getItemMeta().getDisplayName().startsWith("Icon"))) {
            return;
        }
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        int page = (int) getData(civilian.getUuid(), "page");
        UUID uuid = (UUID) getData(civilian.getUuid(), "uuid");

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        LocaleManager localeManager = LocaleManager.getInstance();
        if (event.getCurrentItem().getType() == Material.EMERALD &&
                itemName.equals(localeManager.getTranslation(civilian.getLocale(), "next-button"))) {
            if (uuid == null) {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            } else {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page + "," + uuid.toString());
            }
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, page + 1, uuid));
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE &&
                itemName.equals(localeManager.getTranslation(civilian.getLocale(), "prev-button"))) {
            if (uuid == null) {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            } else {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + page + "," + uuid.toString());
            }
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, page - 1, uuid));
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
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownActionMenu.createMenu(civilian, town));
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, int page, UUID uuid) {
        List<Town> towns = TownManager.getInstance().getTowns();
        if (uuid != null) {
            List<Town> newTownList = new ArrayList<>();
            for (Town town : towns) {
                if (town.getPeople().containsKey(uuid)) {
                    newTownList.add(town);
                }
            }
            towns = newTownList;
        }
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("uuid", uuid);
        setNewData(civilian.getUuid(), data);

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        int startIndex = page * 36;
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
            CVItem cvItem1 = townType.clone();
            CVItem cycleItem = null;
            boolean govTypesAllowed = ConfigManager.getInstance().isAllowChangingOfGovType();
            Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
            if (govTypesAllowed && government != null) {
                cycleItem = new CVItem(government.getIcon(civilian.getLocale()).getMat(), 1);
            }
            cvItem1.setDisplayName(town.getName());
            ArrayList<String> lore = new ArrayList<>();
            cvItem1.setLore(lore);
            inventory.setItem(i, cvItem1.createItemStack());

            if (govTypesAllowed && government != null) {
                ArrayList<CVItem> cycleList = new ArrayList<>();
                cycleItem.setDisplayName(town.getName());
                cycleItem.setLore(lore);
                cycleList.add(cvItem1);
                cycleList.add(cycleItem);
                Menu.addCycleItems(uuid, inventory, i, cycleList);
            }
            i++;
        }

        return inventory;
    }
}

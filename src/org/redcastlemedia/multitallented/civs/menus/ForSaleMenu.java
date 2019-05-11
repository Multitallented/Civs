package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForSaleMenu extends Menu {

    public static final String MENU_NAME = "CivsForSale";
    public ForSaleMenu() {
        super(MENU_NAME);
    }

    public static Inventory createMenu(Civilian civilian, int page) {
        List<Region> regions = new ArrayList<>();
        for (Region r : RegionManager.getInstance().getAllRegions()) {
            if (r.getForSale() != -1 && !r.getRawPeople().containsKey(civilian.getUuid())) {
                regions.add(r);
            }
        }
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);

        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        int startIndex = page * 36;
        //8 Next button
        if (startIndex + 36 < regions.size()) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }

        ArrayList<Region> regionList = new ArrayList<>();
        int i=9;
        for (int k=startIndex; k<regions.size() && k<startIndex+36; k++) {
            Region region = regions.get(k);
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            CVItem cvItem1 = regionType.clone();
            cvItem1.setDisplayName(region.getType() + " $" + region.getForSale());
            Town town = TownManager.getInstance().getTownAt(region.getLocation());
            ArrayList<String> lore = new ArrayList<>();
            lore.add("" + (i-9));
            if (town != null) {
                lore.add(town.getName());
            }
            regionList.add(region);
            cvItem1.setLore(lore);
            inventory.setItem(i, cvItem1.createItemStack());
            i++;
        }
        data.put("regionList", regionList);
        setNewData(civilian.getUuid(), data);

        return inventory;
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }

        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        int page = (int) getData(civilian.getUuid(), "page");

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        LocaleManager localeManager = LocaleManager.getInstance();
        if (event.getCurrentItem().getType() == Material.EMERALD &&
                itemName.equals(localeManager.getTranslation(civilian.getLocale(), "next-button"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ForSaleMenu.createMenu(civilian, page + 1));
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE &&
                itemName.equals(localeManager.getTranslation(civilian.getLocale(), "prev-button"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ForSaleMenu.createMenu(civilian, page - 1));
            return;
        }

        List<Region> regionList = (ArrayList<Region>) getData(civilian.getUuid(), "regionList");
        int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getLore().get(0));
        Region region = regionList.get(index);

        if (region != null) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionActionMenu.createMenu(civilian, region));
            return;
        }
    }
}

package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DestroyConfirmationMenu extends Menu {
    static String MENU_NAME = "CivDestroyConfirm";
    public DestroyConfirmationMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        LocaleManager localeManager = LocaleManager.getInstance();
        CivilianManager civilianManager = CivilianManager.getInstance();

        Civilian civilian = civilianManager.getCivilian(event.getWhoClicked().getUniqueId());

        if (Menu.isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Region region = null;
        Town town = null;
        if (event.getInventory().getItem(0).getItemMeta().getDisplayName().startsWith("R:")) {
            String locationString = event.getInventory().getItem(0).getItemMeta().getDisplayName().replace("R:", "");
            region = RegionManager.getInstance().getRegionAt(Region.idToLocation(locationString));
        } else {
            String townName = event.getInventory().getItem(0).getItemMeta().getDisplayName().replace("T:","");
            town = TownManager.getInstance().getTown(townName);
        }


        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            clearHistory(civilian.getUuid());
            if (region != null) {
                if ((!region.getPeople().containsKey(civilian.getUuid()) ||
                        !region.getPeople().get(civilian.getUuid()).equals("owner")) &&
                        (Civs.perm == null || !Civs.perm.has(player, "civs.admin"))) {
                    clearHistory(civilian.getUuid());
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(civilian.getLocale(), "no-permission"));
                    return;
                }
                RegionManager.getInstance().removeRegion(region, true);
            } else if (town != null) {
                if ((!town.getPeople().containsKey(civilian.getUuid()) ||
                        !town.getPeople().get(civilian.getUuid()).equals("owner")) &&
                        (Civs.perm == null || !Civs.perm.has(player, "civs.admin"))) {
                    clearHistory(civilian.getUuid());
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(Civs.getPrefix() +
                            localeManager.getTranslation(civilian.getLocale(), "no-permission"));
                    return;
                }
                TownManager.getInstance().removeTown(town, true);
            }
            event.getWhoClicked().closeInventory();
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();
        TownType regionType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        CVItem regionTypeIcon = regionType.clone();
        regionTypeIcon.setDisplayName("T:" + town.getName());
        inventory.setItem(0, regionTypeIcon.createItemStack());

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "destroy"));

        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "cancel"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, Region region) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        CVItem regionTypeIcon = regionType.clone();
        regionTypeIcon.setDisplayName("R:" + region.getId());
        inventory.setItem(0, regionTypeIcon.createItemStack());

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "destroy"));

        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("BARRIER");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "cancel"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}

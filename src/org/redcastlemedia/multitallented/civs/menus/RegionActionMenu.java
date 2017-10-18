package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class RegionActionMenu extends Menu {
    public static final String MENU_NAME = "CivsRegion";
    public RegionActionMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        LocaleManager localeManager = LocaleManager.getInstance();
        RegionManager regionManager = RegionManager.getInstance();
        String locationString = event.getInventory().getItem(0).getItemMeta().getDisplayName().split("@")[1];
        Region region = regionManager.getRegionAt(Region.idToLocation(locationString));

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        //TODO add functionality for clicking some other action items

        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "view-members"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ViewMembersMenu.createMenu(civilian, region));
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "add-member"))) {
            //TODO open add members menu
            return;
        }

    }

    public static Inventory createMenu(Civilian civilian, Region region) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);
        //TODO finish this stub

        LocaleManager localeManager = LocaleManager.getInstance();
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        //0 Icon
        CVItem cvItem = new CVItem(regionType.getMat(), 1, regionType.getDamage());
        cvItem.setDisplayName(region.getType() + "@" + region.getId());
        ArrayList<String> lore = new ArrayList<>();
        //TODO set lore
        inventory.setItem(0, cvItem.createItemStack());


        Block block = region.getLocation().getBlock();
        Chest chest = null;
        if (block.getState() instanceof Chest) {
            chest = (Chest) block.getState();
        }
        boolean hasReagents = regionType.getReagents().isEmpty() ||
                (chest != null && Util.containsItems(regionType.getReagents(), chest.getInventory()));
        //1 Is Working
        CVItem cvItem1;
        if (hasReagents) {
            cvItem1 = CVItem.createCVItemFromString("WOOL.5");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "operation"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "region-working"));
            cvItem1.setLore(lore);
        } else {
            cvItem1 = CVItem.createCVItemFromString("WOOL.14");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "operation"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "region-not-working"));
            cvItem1.setLore(lore);
        }
        inventory.setItem(1, cvItem1.createItemStack());

        //2 Location/Town
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town != null) {
            CVItem cvItem2 = CVItem.createCVItemFromString("WOOD_DOOR");
            cvItem2.setDisplayName(town.getName());
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "region-in-town").replace("$1", town.getName()));
            cvItem2.setLore(lore);
            inventory.setItem(2, cvItem2.createItemStack());
        }

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));
        //9 People
        CVItem skull = CVItem.createCVItemFromString("SKULL_ITEM.3");
        skull.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "view-members"));
        inventory.setItem(9, skull.createItemStack());

        //10 Add person - works for people in region only
        CVItem skull2 = CVItem.createCVItemFromString("SKULL_ITEM.3");
        skull2.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "add-member"));
        inventory.setItem(10, skull2.createItemStack());


        return inventory;
    }
}

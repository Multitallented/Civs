package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class RegionTypeInfoMenu extends Menu {
    static String MENU_NAME = "CivRegionInfo";

    public RegionTypeInfoMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemManager itemManager = ItemManager.getInstance();
        String regionName = event.getInventory().getItem(0)
                .getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        RegionType regionType = (RegionType) itemManager.getItemType(regionName);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.IRON_PICKAXE)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(regionType.getReqs(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.CHEST)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(regionType.getReagents(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.HOPPER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(regionType.getInput(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.DISPENSER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(regionType.getOutput(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ConfirmationMenu.createMenu(civilian, regionType));
            return;
        }

        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian, RegionType regionType) {
        return createMenu(civilian, regionType, true);
    }
    public static Inventory createMenu(Civilian civilian, RegionType regionType, boolean showPrice) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        ItemManager itemManager = ItemManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Icon
        CVItem cvItem = regionType.clone();
        List<String> lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "size") +
                ": " + (regionType.getBuildRadiusX() * 2 + 1) + "x" + (regionType.getBuildRadiusZ() * 2 + 1) + "x" + (regionType.getBuildRadiusY() * 2 + 1));
        if (regionType.getEffectRadius() != regionType.getBuildRadius()) {
            lore.add(localeManager.getTranslation(civilian.getLocale(), "range") +
                    ": " + regionType.getEffectRadius());
        }
        lore.addAll(Util.textWrap("", Util.parseColors(regionType.getDescription(civilian.getLocale()))));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        String itemName = regionType.getProcessedName();
        boolean hasShopPerms = Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop");
        boolean isAtMax = civilian.isAtMax(regionType);
        if (showPrice && hasShopPerms && !isAtMax) {
            CVItem priceItem = CVItem.createCVItemFromString("EMERALD");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + regionType.getPrice());
            priceItem.setLore(lore);
            inventory.setItem(1, priceItem.createItemStack());
        }

        //2 Rebuild
        if (regionType.getRebuild() != null) {
            CVItem rebuildItem = itemManager.getItemType(regionType.getRebuild().toLowerCase()).clone();
            lore = new ArrayList<>();
//            lore.add();
            rebuildItem.setLore(lore);
            inventory.setItem(2, rebuildItem.createItemStack());
        }

        //3 evolve
        //4 biome/location reqs
        //5 town reqs

        //8 back button
        inventory.setItem(8, getBackButton(civilian));

        //9 build-reqs
        CVItem cvItem1 = CVItem.createCVItemFromString("IRON_PICKAXE");
        cvItem1.setDisplayName("Build Reqs");
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "build-reqs")
                .replace("$1", regionType.getName()));
        cvItem1.setLore(lore);
        inventory.setItem(9, cvItem1.createItemStack());

        //10 reagents
        CVItem cvItem2 = CVItem.createCVItemFromString("CHEST");
        cvItem2.setDisplayName("Reagents");
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "reagents")
                .replace("$1", regionType.getName()));
        cvItem2.setLore(lore);
        inventory.setItem(10, cvItem2.createItemStack());

        //11 upkeep
        CVItem cvItem3 = CVItem.createCVItemFromString("HOPPER");
        cvItem3.setDisplayName("Upkeep");
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "upkeep")
                .replace("$1", regionType.getName()));
        cvItem3.setLore(lore);
        inventory.setItem(11, cvItem3.createItemStack());
        //12 output
        CVItem cvItem4 = CVItem.createCVItemFromString("DISPENSER");
        cvItem4.setDisplayName("Output");
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "output")
                .replace("$1", regionType.getName()));
        cvItem4.setLore(lore);
        inventory.setItem(12, cvItem4.createItemStack());

        //TODO finish this stub

        return inventory;
    }
}

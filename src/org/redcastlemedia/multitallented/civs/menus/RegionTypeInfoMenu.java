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
            event.getWhoClicked().openInventory(RecipeMenu.createMenuCVItem(regionType.getReqs(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.CHEST)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().replace("Reagents", ""));
            event.getWhoClicked().openInventory(RecipeMenu.createMenuCVItem(regionType.getUpkeeps().get(index).getReagents(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.HOPPER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().replace("Input", ""));
            event.getWhoClicked().openInventory(RecipeMenu.createMenuCVItem(regionType.getUpkeeps().get(index).getInputs(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.DISPENSER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
            event.getWhoClicked().closeInventory();
            int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getDisplayName().replace("Output", ""));
            event.getWhoClicked().openInventory(RecipeMenu.createMenuCVItem(regionType.getUpkeeps().get(index).getOutputs(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
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
        Inventory inventory = Bukkit.createInventory(null, 9 + 9*regionType.getUpkeeps().size(), MENU_NAME);

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

        //6 build-reqs
        CVItem cvItem1 = CVItem.createCVItemFromString("IRON_PICKAXE");
        cvItem1.setDisplayName("Build Reqs");
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "build-reqs")
                .replace("$1", regionType.getName()));
        cvItem1.setLore(lore);
        inventory.setItem(6, cvItem1.createItemStack());

        //8 back button
        inventory.setItem(8, getBackButton(civilian));

        for (int i = 0; i < regionType.getUpkeeps().size(); i++) {
            //9 reagents
            if (!regionType.getUpkeeps().get(i).getReagents().isEmpty()) {
                CVItem cvItem2 = CVItem.createCVItemFromString("CHEST");
                cvItem2.setDisplayName("Reagents" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "reagents")
                        .replace("$1", regionType.getName()));
                cvItem2.setLore(lore);
                inventory.setItem(9 + i * 9, cvItem2.createItemStack());
            }

            //10 upkeep
            if (!regionType.getUpkeeps().get(i).getInputs().isEmpty()) {
                CVItem cvItem3 = CVItem.createCVItemFromString("HOPPER");
                cvItem3.setDisplayName("Input" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "upkeep")
                        .replace("$1", regionType.getName()));
                cvItem3.setLore(lore);
                inventory.setItem(10, cvItem3.createItemStack());
            }

            //11 output
            if (!regionType.getUpkeeps().get(i).getOutputs().isEmpty()) {
                CVItem cvItem4 = CVItem.createCVItemFromString("DISPENSER");
                cvItem4.setDisplayName("Output" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "output")
                        .replace("$1", regionType.getName()));
                cvItem4.setLore(lore);
                inventory.setItem(11, cvItem4.createItemStack());
            }

            if (regionType.getUpkeeps().get(i).getPayout() > 0) {
                CVItem cvItem4 = CVItem.createCVItemFromString("EMERALD_BLOCK");
                cvItem4.setDisplayName("Payout" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "payout")
                        .replace("$1", regionType.getUpkeeps().get(i).getPayout() + ""));
                cvItem4.setLore(lore);
                inventory.setItem(12, cvItem4.createItemStack());
            }
            if (regionType.getUpkeeps().get(i).getPowerInput() > 0) {
                CVItem cvItem4 = CVItem.createCVItemFromString("REDSTONE_ORE");
                cvItem4.setDisplayName("PowerInput" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "power-input")
                        .replace("$1", regionType.getUpkeeps().get(i).getPowerInput() + ""));
                cvItem4.setLore(lore);
                inventory.setItem(13, cvItem4.createItemStack());
            }
            if (regionType.getUpkeeps().get(i).getPowerOutput() > 0) {
                CVItem cvItem4 = CVItem.createCVItemFromString("REDSTONE_TORCH");
                cvItem4.setDisplayName("PowerOutput" + i);
                lore = new ArrayList<>();
                lore.add(localeManager.getTranslation(civilian.getLocale(), "power-output")
                        .replace("$1", regionType.getUpkeeps().get(i).getPowerOutput() + ""));
                cvItem4.setLore(lore);
                inventory.setItem(14, cvItem4.createItemStack());
            }
        }

        //TODO finish this stub

        return inventory;
    }
}

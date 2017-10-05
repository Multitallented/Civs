package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class RegionTypeInfoMenu extends Menu {
    private static String MENU_NAME = "CivRegionInfo";

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

        if (event.getCurrentItem().getType().equals(Material.IRON_PICKAXE)) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(regionType.getReqs(), event.getWhoClicked().getUniqueId()));
            return;
        }

        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian, RegionType regionType) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        ItemManager itemManager = ItemManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();

        CVItem cvItem = regionType.clone();
        List<String> lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "size") +
                ": " + regionType.getBuildRadiusX() + "x" + regionType.getBuildRadiusZ() + "x" + regionType.getBuildRadiusY());
        lore.add(localeManager.getTranslation(civilian.getLocale(), "range") +
                ": " + regionType.getEffectRadius());
        lore.addAll(Util.textWrap(ChatColor.RESET + "" + ChatColor.GOLD, regionType.getDescription()));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        //TODO figure out how to tell what he has bought

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

        CVItem cvItem1 = CVItem.createCVItemFromString("IRON_PICKAXE");
        cvItem1.setDisplayName("Build Reqs");
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "build-reqs"));
        cvItem1.setLore(lore);
        inventory.setItem(9, cvItem1.createItemStack());

        //10 reagents
        //11 upkeep
        //12 output

        //TODO finish this stub

        return inventory;
    }
}

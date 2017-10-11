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
import org.redcastlemedia.multitallented.civs.items.ClassType;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ClassTypeInfoMenu extends Menu {
    static String MENU_NAME = "CivClassInfo";

    public ClassTypeInfoMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemManager itemManager = ItemManager.getInstance();
        String className = event.getInventory().getItem(0)
                .getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        ClassType regionType = (ClassType) itemManager.getItemType(className);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

//        if (event.getCurrentItem().getType().equals(Material.IRON_PICKAXE)) {
//            appendHistory(civilian.getUuid(), MENU_NAME + "," + regionName);
//            event.getWhoClicked().closeInventory();
//            event.getWhoClicked().openInventory(RecipeMenu.createMenu(regionType.getReqs(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
//            return;
//        }
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + className);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ConfirmationMenu.createMenu(civilian, regionType));
            return;
        }

        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian, ClassType classType) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        ItemManager itemManager = ItemManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Icon
        CVItem cvItem = classType.clone();
        List<String> lore = new ArrayList<>();

        //TODO write info about the class

        lore.addAll(Util.parseColors(classType.getDescription()));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        String itemName = classType.getProcessedName();
        boolean hasShopPerms = Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop");
        boolean isNotOverMax = civilian.getCountNonStashItems(itemName) + civilian.getCountStashItems(itemName) < classType.getCivMax();
        if (hasShopPerms && (classType.getCivMax() == -1 || isNotOverMax)) {
            CVItem priceItem = CVItem.createCVItemFromString("EMERALD");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + classType.getPrice());
            priceItem.setLore(lore);
        }


        //8 back button
        inventory.setItem(8, getBackButton(civilian));

        //TODO finish this stub

        return inventory;
    }
}

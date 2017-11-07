package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
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
        ClassType classType = (ClassType) itemManager.getItemType(className);
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
            if (Civs.perm != null && Civs.perm.has(event.getWhoClicked(), "civs.choose") &&
                    Civs.perm.has(event.getWhoClicked(), "civs.shop")) {
                event.getWhoClicked().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "no-permission"));
                return;
            }
            appendHistory(civilian.getUuid(), MENU_NAME + "," + className);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ConfirmationMenu.createMenu(civilian, classType));
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.ENDER_CHEST)) {
            if (Civs.perm != null && Civs.perm.has(event.getWhoClicked(), "civs.choose") &&
                    civilian.getStashItems().contains(classType)) {

                appendHistory(civilian.getUuid(), MENU_NAME + "," + className);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(ConfirmSwitchMenu.createMenu(civilian, classType));
                return;
            }
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

        lore.addAll(Util.textWrap("", Util.parseColors(classType.getDescription(civilian.getLocale()))));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        String itemName = classType.getProcessedName();
        Player player = Bukkit.getPlayer(civilian.getUuid());
        boolean hasShopPerms = Civs.perm != null && Civs.perm.has(player, "civs.shop");
        boolean isNotOverMax = civilian.getCountNonStashItems(itemName) + civilian.getCountStashItems(itemName) < classType.getCivMax();
        boolean hasChoosePerms = Civs.perm != null && Civs.perm.has(player, "civs.choose");
        boolean alreadyIsClass = false;
        for (CivClass civClass : civilian.getCivClasses()) {
            if (civClass.getType().equals(classType.getProcessedName())) {
                alreadyIsClass = true;
            }
        }
        if (hasShopPerms && (classType.getCivMax() == -1 || isNotOverMax)) {
            CVItem priceItem = CVItem.createCVItemFromString("EMERALD");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + classType.getPrice());
            priceItem.setLore(lore);
            inventory.setItem(1, priceItem.createItemStack());
        } else if (hasChoosePerms && !alreadyIsClass &&
                civilian.getStashItems().contains(classType)) {
            CVItem switchItem = CVItem.createCVItemFromString("ENDER_CHEST");
            switchItem.setDisplayName("Switch to class");
            inventory.setItem(1, switchItem.createItemStack());
        }


        //8 back button
        inventory.setItem(8, getBackButton(civilian));

        //9 Items
        //10 Spells
        //11 Regions

        //TODO finish this stub

        return inventory;
    }
}

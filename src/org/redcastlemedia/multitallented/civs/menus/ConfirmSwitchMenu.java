package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.text.NumberFormat;
import java.util.*;

public class ConfirmSwitchMenu extends Menu {
    static String MENU_NAME = "CivSwitch";
    public ConfirmSwitchMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemManager itemManager = ItemManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();
        CivilianManager civilianManager = CivilianManager.getInstance();
        String className = event.getInventory().getItem(0)
                .getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        ClassType classType = (ClassType) itemManager.getItemType(className);
        Civilian civilian = civilianManager.getCivilian(event.getWhoClicked().getUniqueId());

        if (Menu.isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {

            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "class-changed").replace("$1", classType.getProcessedName()));

            Set<CivClass> removeThese = new HashSet<>();
            for (CivClass civClass : civilian.getCivClasses()) {
                if (civClass.getType().equals(className)) {
                    removeThese.add(civClass);
                }
            }
            for (CivClass civClass : removeThese) {
                civilian.getCivClasses().remove(civClass);
                if (!civilian.getStashItems().containsKey(classType.getProcessedName())) {
                    civilian.getStashItems().put(classType.getProcessedName(), classType.getQty());
                }
            }

            ClassManager classManager = ClassManager.getInstance();
            CivClass civClass = classManager.createClass(civilian.getUuid(), className,
                    classType.getManaPerSecond(), classType.getMaxMana());
            classManager.addClass(civClass);
            classManager.saveClass(civClass);
            CivilianManager.getInstance().saveCivilian(civilian);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
            clearHistory(civilian.getUuid());
            event.getWhoClicked().closeInventory();
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, CivItem civItem) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        LocaleManager localeManager = LocaleManager.getInstance();

        inventory.setItem(0, civItem.clone().createItemStack());

        CVItem cvItem = CVItem.createCVItemFromString("EMERALD_BLOCK");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy").replace("$1", civItem.getDisplayName()));
        List<String> lore = new ArrayList<>();
        if (civItem.getPrice() > 0) {
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + civItem.getPrice());
        }
        cvItem.setLore(lore);
        inventory.setItem(3, cvItem.createItemStack());

        CVItem cvItem1 = CVItem.createCVItemFromString("REDSTONE_BLOCK");
        cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "cancel"));
        inventory.setItem(4, cvItem1.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}

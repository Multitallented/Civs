package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;

public class CommunityMenu extends Menu {
    private static final String MENU_NAME = "CivsCommunity";
    public CommunityMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedStack = event.getCurrentItem();
        if (clickedStack == null) {
            return;
        }
        if (clickedStack.getItemMeta() == null) {
            return;
        }
        ItemMeta im = clickedStack.getItemMeta();
        String itemName = im.getDisplayName();
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        String locale = civilian.getLocale();
//        if (itemName.equals(localeManager.getTranslation(locale, "language-menu"))) {

//        }
        //TODO finish this stub
    }

    public static Inventory createMenu(String locale) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        //TODO populate items here

        //0 Players
        CVItem cvItem = CVItem.createCVItemFromString("SKULL.3");
        cvItem.setDisplayName("Players"); //TODO localize
        inventory.setItem(0, cvItem.createItemStack());

        //1 Towns
        CVItem cvItem3 = CVItem.createCVItemFromString("BED");
        cvItem3.setDisplayName("Towns"); //TODO localize
        inventory.setItem(1, cvItem3.createItemStack());

        //2 Your towns
        CVItem cvItem2 = CVItem.createCVItemFromString("CHEST");
        cvItem2.setDisplayName("Your towns"); //TODO localize
        inventory.setItem(2, cvItem2.createItemStack());

        //3 Wars
        CVItem cvItem1 = CVItem.createCVItemFromString("IRON_SWORD");
        cvItem1.setDisplayName("Wars"); //TODO localize
        inventory.setItem(3, cvItem1.createItemStack());

        //4 PvP leaderboard
        CVItem cvItem4 = CVItem.createCVItemFromString("SIGN");
        cvItem4.setDisplayName("Leaderboard"); //TODO localize
        inventory.setItem(4, cvItem4.createItemStack());

        return inventory;
    }
}

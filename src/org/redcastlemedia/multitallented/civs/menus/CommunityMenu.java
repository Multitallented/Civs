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
    public static final String MENU_NAME = "CivsCommunity";
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

        if (isBackButton(clickedStack, locale)) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        if (clickedStack.getType() == Material.ENDER_PEARL) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(PortMenu.createMenu(civilian, 0));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "towns"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, 0, null));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "your-towns"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, 0, civilian.getUuid()));
            return;
        }
        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian) {
        String locale = civilian.getLocale();
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        //TODO populate items here

        //0 Players
        CVItem cvItem = CVItem.createCVItemFromString("PLAYER_HEAD");
        cvItem.setDisplayName(localeManager.getTranslation(locale, "players"));
        inventory.setItem(0, cvItem.createItemStack());

        //1 Towns
        CVItem cvItem3 = CVItem.createCVItemFromString("RED_BED");
        cvItem3.setDisplayName(localeManager.getTranslation(locale, "towns"));
        inventory.setItem(1, cvItem3.createItemStack());

        //2 Your towns
        CVItem cvItem2 = CVItem.createCVItemFromString("CHEST");
        cvItem2.setDisplayName(localeManager.getTranslation(locale, "your-towns"));
        inventory.setItem(2, cvItem2.createItemStack());

        //3 Wars
        CVItem cvItem1 = CVItem.createCVItemFromString("IRON_SWORD");
        cvItem1.setDisplayName(localeManager.getTranslation(locale, "wars"));
        inventory.setItem(3, cvItem1.createItemStack());

        //4 PvP leaderboard
        CVItem cvItem4 = CVItem.createCVItemFromString("SIGN");
        cvItem4.setDisplayName(localeManager.getTranslation(locale, "leaderboard"));
        inventory.setItem(4, cvItem4.createItemStack());

        //5 Ports
        CVItem cvItem5 = CVItem.createCVItemFromString("ENDER_PEARL");
        cvItem5.setDisplayName(localeManager.getTranslation(locale, "ports"));
        inventory.setItem(5, cvItem5.createItemStack());

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}

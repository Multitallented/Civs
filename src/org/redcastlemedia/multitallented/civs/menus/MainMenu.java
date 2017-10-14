package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

public class MainMenu extends Menu {
    static final String MENU_NAME = "CivsMain";
    public MainMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedStack = event.getCurrentItem();
        if (clickedStack.getItemMeta() == null) {
            return;
        }
        if (clickedStack.getItemMeta().getDisplayName() == null) {
            return;
        }
        ItemMeta im = clickedStack.getItemMeta();
        String itemName = im.getDisplayName();
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        String locale = civilian.getLocale();
        if (itemName.equals(localeManager.getTranslation(locale, "language-menu"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(LanguageMenu.createMenu(locale));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "shop"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ShopMenu.createMenu(civilian, null));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "classes"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ShopMenu.createMenu(civilian, null));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "spells"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ShopMenu.createMenu(civilian, null));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "blueprints"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ItemsMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "regions"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ItemsMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "community"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(CommunityMenu.createMenu(locale));
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        String locale = civilian.getLocale();

        //8 Language Select
        LocaleManager localeManager = LocaleManager.getInstance();
        CVItem cvItem = new CVItem(Material.GRASS, 1, -1, 100, localeManager.getTranslation(locale, "language-menu"));
        inventory.setItem(8, cvItem.createItemStack());

        //1 Shop
        if (Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop")) {
            CVItem cvItem1 = new CVItem(Material.EMERALD, 1, -1, 100, localeManager.getTranslation(locale, "shop"));
            inventory.setItem(1, cvItem1.createItemStack());
        }

        //2 Classes
        CVItem cvItemClass = CVItem.createCVItemFromString("DIAMOND_CHESTPLATE");
        cvItemClass.setDisplayName(localeManager.getTranslation(locale, "classes"));
        inventory.setItem(2, cvItemClass.createItemStack());

        //3 Spells
        CVItem cvItemSpell = CVItem.createCVItemFromString("POTION.16385");
        cvItemSpell.setDisplayName(localeManager.getTranslation(locale, "spells"));
        inventory.setItem(3, cvItemSpell.createItemStack());

        //4 Blueprints
        CVItem cvItemBlue = CVItem.createCVItemFromString("MAP");
        cvItemBlue.setDisplayName(localeManager.getTranslation(locale, "blueprints"));
        inventory.setItem(4, cvItemBlue.createItemStack());

        //5 Regions
        CVItem cvItemRegion = CVItem.createCVItemFromString("WOOD.1");
        cvItemRegion.setDisplayName(localeManager.getTranslation(locale, "regions"));
        inventory.setItem(5, cvItemRegion.createItemStack());

//        //4 Items
//        CVItem cvItem2 = new CVItem(Material.CHEST, 1, -1, 100, localeManager.getTranslation(locale, "items"));
//        inventory.setItem(4, cvItem2.createItemStack());

        //6 Community
        CVItem cvItem3 = new CVItem(Material.BOOKSHELF, 1, -1, 100, localeManager.getTranslation(locale, "community"));
        inventory.setItem(6, cvItem3.createItemStack());

        return inventory;
    }

}

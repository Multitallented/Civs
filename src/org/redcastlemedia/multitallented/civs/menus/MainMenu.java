package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
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
        if (Material.ENCHANTED_BOOK == clickedStack.getType()) {
            event.getWhoClicked().closeInventory();
            printTutorial(event.getWhoClicked(), civilian);
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
            event.getWhoClicked().openInventory(ClassMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "spells"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(SpellsMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "blueprints"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(BlueprintsMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "regions"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(BuiltRegionMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(localeManager.getTranslation(locale, "community"))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(CommunityMenu.createMenu(civilian));
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian) {
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);
        String locale = civilian.getLocale();
        clearHistory(civilian.getUuid());
        boolean useClassesAndSpells = ConfigManager.getInstance().getUseClassesAndSpells();

        int i=0;

        //8 Language Select
        LocaleManager localeManager = LocaleManager.getInstance();
        CVItem cvItem = new CVItem(Material.GRASS_BLOCK, 1, 100, localeManager.getTranslation(locale, "language-menu"));
        inventory.setItem(8, cvItem.createItemStack());


        //0 Guide
        if (ConfigManager.getInstance().isUseGuide()) {
            CVItem cvItem4 = new CVItem(Material.ENCHANTED_BOOK, 1, 100,
                    localeManager.getTranslation(locale, "guide"));
            inventory.setItem(i, cvItem4.createItemStack());
        }
        i++;

        //1 Shop
        if (Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop")) {
            CVItem cvItem1 = new CVItem(Material.EMERALD, 1, 100, localeManager.getTranslation(locale, "shop"));
            inventory.setItem(i, cvItem1.createItemStack());
        }

        if (useClassesAndSpells) {
            //2 Classes
            i++;
            CVItem cvItemClass = CVItem.createCVItemFromString("DIAMOND_CHESTPLATE");
            cvItemClass.setDisplayName(localeManager.getTranslation(locale, "classes"));
            inventory.setItem(i, cvItemClass.createItemStack());

            //3 Spells
            i++;
            CVItem cvItemSpell = CVItem.createCVItemFromString("POTION");
            cvItemSpell.setDisplayName(localeManager.getTranslation(locale, "spells"));
            inventory.setItem(i, cvItemSpell.createItemStack());
        }

        //4 Blueprints
        i++;
        CVItem cvItemBlue = CVItem.createCVItemFromString("MAP");
        cvItemBlue.setDisplayName(localeManager.getTranslation(locale, "blueprints"));
        inventory.setItem(i, cvItemBlue.createItemStack());

        //5 Regions
        i++;
        CVItem cvItemRegion = CVItem.createCVItemFromString("OAK_WOOD");
        cvItemRegion.setDisplayName(localeManager.getTranslation(locale, "regions"));
        inventory.setItem(i, cvItemRegion.createItemStack());

//        //4 Items
//        CVItem cvItem2 = new CVItem(Material.CHEST, 1, -1, 100, localeManager.getTranslation(locale, "items"));
//        inventory.setItem(4, cvItem2.createItemStack());

        //6 Community
        i++;
        CVItem cvItem3 = new CVItem(Material.BOOKSHELF, 1, 100, localeManager.getTranslation(locale, "community"));
        inventory.setItem(i, cvItem3.createItemStack());

        return inventory;
    }

    private void printTutorial(HumanEntity player, Civilian civilian) {
        String tutorialUrl = ConfigManager.getInstance().getTutorialUrl();
        player.sendMessage("-----------------" + Civs.NAME + "-----------------");
        player.sendMessage(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "tutorial-click"));
        player.sendMessage(tutorialUrl);
        player.sendMessage("--------------------------------------");
    }

}

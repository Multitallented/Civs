package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;

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
        String itemName = ChatColor.stripColor(im.getDisplayName());
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        String locale = civilian.getLocale();
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "language-menu")))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(LanguageMenu.createMenu(locale));
            return;
        }
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "guide")))) {
            event.getWhoClicked().closeInventory();
            printTutorial(event.getWhoClicked(), civilian);
        }
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "shop")))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ShopMenu.createMenu(civilian, null));
            return;
        }
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "classes")))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ClassMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "spells")))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(SpellsMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "blueprints")))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(BlueprintsMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "regions")))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(BuiltRegionMenu.createMenu(civilian));
            return;
        }
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "community")))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(CommunityMenu.createMenu(civilian));
            return;
        }
        if (im.getLore() != null && !im.getLore().isEmpty()) {
            if (ChatColor.stripColor(im.getLore().get(0)).equals("town") &&
                    TownManager.getInstance().getTown(itemName) != null) {
                appendHistory(civilian.getUuid(), MENU_NAME);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(TownActionMenu.createMenu(civilian,
                        TownManager.getInstance().getTown(itemName)));
                return;
            } else if (ChatColor.stripColor(im.getLore().get(0)).equals("region")) {
                appendHistory(civilian.getUuid(), MENU_NAME);
                event.getWhoClicked().closeInventory();
                Player player = Bukkit.getPlayer(civilian.getUuid());
                Region region = RegionManager.getInstance().getRegionAt(player.getLocation());
                if (region != null) {
                    event.getWhoClicked().openInventory(RegionActionMenu.createMenu(civilian, region));
                }
                return;
            }
        }
    }

    public static Inventory createMenu(Civilian civilian) {
        clearHistory(civilian.getUuid());
        if (civilian.isAskForTutorial() && ConfigManager.getInstance().isUseTutorial()) {
            return StartTutorialMenu.createMenu(civilian);
        }
        if (!TutorialManager.getInstance().getPaths(civilian).isEmpty()) {
            return TutorialChoosePathMenu.createMenu(civilian);
        }

        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);
        String locale = civilian.getLocale();
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
            if (civilian.getTutorialIndex() != -1) {
                ArrayList<String> lore = new ArrayList<>();
                lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "guide-desc"));
                cvItem4.setLore(lore);
            }
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
        if (!civilian.getStashItems().isEmpty()) {
            i++;
            CVItem cvItemBlue = CVItem.createCVItemFromString("MAP");
            cvItemBlue.setDisplayName(localeManager.getTranslation(locale, "blueprints"));
            inventory.setItem(i, cvItemBlue.createItemStack());
        }

        boolean showBuiltRegions = false;
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getRawPeople().containsKey(civilian.getUuid())) {
                showBuiltRegions = true;
                break;
            }
        }
        //5 Regions
        if (showBuiltRegions) {
            i++;
            CVItem cvItemRegion = CVItem.createCVItemFromString("BRICKS");
            cvItemRegion.setDisplayName(localeManager.getTranslation(locale, "regions"));
            inventory.setItem(i, cvItemRegion.createItemStack());
        }

//        //4 Items
//        CVItem cvItem2 = new CVItem(Material.CHEST, 1, -1, 100, localeManager.getTranslation(locale, "items"));
//        inventory.setItem(4, cvItem2.createItemStack());

        //7 Community
        i++;
        CVItem cvItem3 = new CVItem(Material.BOOKSHELF, 1, 100, localeManager.getTranslation(locale, "community"));
        inventory.setItem(i, cvItem3.createItemStack());

        //9
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Town town = TownManager.getInstance().getTownAt(player.getLocation());
        if (town != null) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            CVItem townIcon = townType.getShopIcon().clone();
            townIcon.setDisplayName(town.getName());
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLACK + "town");
            lore.addAll(Util.textWrap("", LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    townType.getProcessedName() + "-desc")));
            townIcon.setLore(lore);
            inventory.setItem(9, townIcon.createItemStack());
        }

        //10
        Region region = RegionManager.getInstance().getRegionAt(player.getLocation());
        if (region != null) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            CVItem regionIcon = regionType.getShopIcon().clone();
            regionIcon.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    regionType.getProcessedName() + "-name"));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLACK + "region");
            lore.addAll(Util.textWrap("", LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    regionType.getProcessedName() + "-desc")));
            regionIcon.setLore(lore);
            inventory.setItem(10, regionIcon.createItemStack());
        }

        return inventory;
    }

    private void printTutorial(HumanEntity player, Civilian civilian) {
        String tutorialUrl = ConfigManager.getInstance().getTutorialUrl();
        player.sendMessage(Util.parseColors(ConfigManager.getInstance().getTopGuideSpacer()));
        TutorialManager.getInstance().sendMessageForCurrentTutorialStep(civilian, false);
        player.sendMessage(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "tutorial-click"));
        player.sendMessage(tutorialUrl);
        player.sendMessage(Util.parseColors(ConfigManager.getInstance().getBottomGuideSpacer()));
    }

}

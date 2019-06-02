package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

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
        String itemName = ChatColor.stripColor(im.getDisplayName());
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
        if (clickedStack.getType() == Material.OAK_SIGN) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(LeaderboardMenu.createMenu(civilian, 0));
            return;
        }
        if (clickedStack.getType() == Material.EMERALD) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ForSaleMenu.createMenu(civilian, 0));
            return;
        }
        if (clickedStack.getType() == Material.IRON_SWORD) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(AllianceListMenu.createMenu(civilian, 0));
            return;
        }
        if (itemName.equals(ChatColor.stripColor(localeManager.getTranslation(locale, "players")))) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ListAllPlayersMenu.createMenu(civilian, 0));
            return;
        }
        if (clickedStack.getType() == Material.RED_BED) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, 0, null));
            return;
        }
        if (clickedStack.getType() == Material.CHEST) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, 0, civilian.getUuid()));
            return;
        }
        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian) {
        String locale = civilian.getLocale();
        Inventory inventory = Bukkit.createInventory(null, 9, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Players
        int i=0;
        CVItem cvItem = CVItem.createCVItemFromString("PLAYER_HEAD");
        cvItem.setDisplayName(localeManager.getTranslation(locale, "players"));
        inventory.setItem(i, cvItem.createItemStack());

        //1 Towns
        i++;
        CVItem cvItem3 = CVItem.createCVItemFromString("RED_BED");
        cvItem3.setDisplayName(localeManager.getTranslation(locale, "towns"));
        inventory.setItem(i, cvItem3.createItemStack());

        boolean isInATown = false;
        for (Town town : TownManager.getInstance().getTowns()) {
            if (town.getRawPeople().containsKey(civilian.getUuid())) {
                isInATown = true;
                break;
            }
        }
        //2 Your towns
        if (isInATown) {
            i++;
            CVItem cvItem2 = CVItem.createCVItemFromString("CHEST");
            cvItem2.setDisplayName(localeManager.getTranslation(locale, "your-towns"));
            inventory.setItem(i, cvItem2.createItemStack());
        }

        //3 Alliances
        i++;
        CVItem cvItem1 = CVItem.createCVItemFromString("IRON_SWORD");
        cvItem1.setDisplayName(localeManager.getTranslation(locale, "alliances"));
        inventory.setItem(i, cvItem1.createItemStack());

        //4 PvP leaderboard
        i++;
        CVItem cvItem4 = CVItem.createCVItemFromString("OAK_SIGN");
        cvItem4.setDisplayName(localeManager.getTranslation(locale, "leaderboard"));
        inventory.setItem(i, cvItem4.createItemStack());

        boolean hasPort = false;
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (!region.getEffects().containsKey("port")) {
                continue;
            }
            if (!region.getPeople().containsKey(civilian.getUuid())) {
                continue;
            }
            //Don't show private ports
            if (region.getEffects().get("port") != null &&
                    !region.getPeople().get(civilian.getUuid()).contains("member") &&
                    !region.getPeople().get(civilian.getUuid()).contains("owner")) {
                continue;
            }
            hasPort = true;
            break;
        }

        //5 Ports
        if (hasPort) {
            i++;
            CVItem cvItem5 = CVItem.createCVItemFromString("ENDER_PEARL");
            cvItem5.setDisplayName(localeManager.getTranslation(locale, "ports"));
            inventory.setItem(i, cvItem5.createItemStack());
        }

        boolean hasRegionsForSale = false;
        for (Region r : RegionManager.getInstance().getAllRegions()) {
            if (r.getForSale() != -1 && !r.getRawPeople().containsKey(civilian.getUuid())) {
                hasRegionsForSale = true;
                break;
            }
        }
        //6 Regions for sale
        if (hasRegionsForSale) {
            i++;
            CVItem cvItem6 = CVItem.createCVItemFromString("EMERALD");
            cvItem6.setDisplayName(localeManager.getTranslation(locale, "regions-for-sale"));
            inventory.setItem(i, cvItem6.createItemStack());
        }

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));

        return inventory;
    }
}

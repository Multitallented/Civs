package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerProfileMenu extends Menu {
    public static final String MENU_NAME = "CivsPlayerProfile";
    public PlayerProfileMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        String playerName = event.getInventory().getItem(0).getItemMeta().getDisplayName();
        Player player = Bukkit.getPlayer(playerName);

//        clearHistory(civilian.getUuid());
//        appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);

        if (event.getCurrentItem().getType().equals(Material.OAK_DOOR)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + playerName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, 0, player.getUniqueId()));
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, String playerName) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        Player player = Bukkit.getPlayer(playerName);
        Civilian currCivilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        //0 Icon
        ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta isMeta = (SkullMeta) is.getItemMeta();
        isMeta.setDisplayName(playerName);
        isMeta.setOwningPlayer(player);
        is.setItemMeta(isMeta);
        inventory.setItem(0, is);

        //1 Friends

        //2 Money
        if (Civs.econ != null) {
            CVItem cvItem = CVItem.createCVItemFromString("EMERALD");
            double money = Civs.econ.getBalance(player);
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "money").replace("$1", money + ""));
        }

        //3 Towns
        {
            CVItem cvItem = CVItem.createCVItemFromString("OAK_DOOR");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "towns"));
            inventory.setItem(3, cvItem.createItemStack());
        }

        //6 Add friend / Remove friend

        //8 Back button
        inventory.setItem(8, getBackButton(civilian));

        //9 Points
        {
            CVItem cvItem = CVItem.createCVItemFromString("DIAMOND");
            double points = currCivilian.getPoints();
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "points").replace("$1", points + ""));
            inventory.setItem(9, cvItem.createItemStack());
        }

        //10 Karma
        {
            CVItem cvItem = CVItem.createCVItemFromString("CREEPER_HEAD");
            int karma = currCivilian.getKarma();
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "karma").replace("$1", karma + ""));
            inventory.setItem(10, cvItem.createItemStack());
        }

        //11 Kills
        {
            CVItem cvItem = CVItem.createCVItemFromString("PLAYER_HEAD");
            int kills = currCivilian.getKills();
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "kills").replace("$1", kills + ""));
            inventory.setItem(11, cvItem.createItemStack());
        }

        //12 Deaths
        {
            CVItem cvItem = CVItem.createCVItemFromString("SKELETON_SKULL");
            int deaths = currCivilian.getDeaths();
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "deaths").replace("$1", deaths + ""));
            inventory.setItem(12, cvItem.createItemStack());
        }

        //13 Killstreak
        {
            CVItem cvItem = CVItem.createCVItemFromString("IRON_SWORD");
            int killstreak = currCivilian.getKillStreak();
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "killstreak").replace("$1", killstreak + ""));
            inventory.setItem(13, cvItem.createItemStack());
        }

        //14 Highest Killstreak
        {
            CVItem cvItem = CVItem.createCVItemFromString("DIAMOND_SWORD");
            int hKillstreak = currCivilian.getKillStreak();
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "highest-killstreak").replace("$1", hKillstreak + ""));
            inventory.setItem(14, cvItem.createItemStack());
        }

        return inventory;
    }
}

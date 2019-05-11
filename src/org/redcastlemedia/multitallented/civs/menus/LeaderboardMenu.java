package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.*;

public class LeaderboardMenu extends Menu {
    public static String MENU_NAME = "CivsLeaderboard";

    public LeaderboardMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || (event.getCurrentItem().getType() == Material.STONE &&
                event.getCurrentItem().getItemMeta().getDisplayName().startsWith("Icon"))) {
            return;
        }
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        int page = (int) getData(civilian.getUuid(), "page");

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        LocaleManager localeManager = LocaleManager.getInstance();
        if (event.getCurrentItem().getType() == Material.EMERALD &&
                itemName.equals(localeManager.getTranslation(civilian.getLocale(), "next-button"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(LeaderboardMenu.createMenu(civilian, page + 1));
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE &&
                itemName.equals(localeManager.getTranslation(civilian.getLocale(), "prev-button"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(LeaderboardMenu.createMenu(civilian, page - 1));
            return;
        }
        int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getLore().get(0));
        UUID uuid = ((ArrayList<UUID>) getData(civilian.getUuid(), "uuidList")).get(index);
        if (event.getWhoClicked() instanceof Player) {
            appendHistory(civilian.getUuid(), MENU_NAME);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(PlayerProfileMenu.createMenu(civilian, uuid));
        }
    }

    public static Inventory createMenu(Civilian civilian, int page) {
        ArrayList<Civilian> civilianList = CivilianManager.getInstance().getSortedCivilians();

        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        int startIndex = page * 36;
        //8 Next button
        if (startIndex + 36 < civilianList.size()) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }

        ArrayList<UUID> uuidList = new ArrayList<>();
        int i=9;
        for (int k=startIndex; k<civilianList.size() && k<startIndex+36; k++) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(civilianList.get(k).getUuid());
            ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta isMeta = (SkullMeta) is.getItemMeta();
            isMeta.setDisplayName(player.getName());
            ArrayList<String> lore1 = new ArrayList<>();
            lore1.add("" + (i-9));
            isMeta.setLore(lore1);
            uuidList.add(player.getUniqueId());
            isMeta.setOwningPlayer(player);
            is.setItemMeta(isMeta);
            inventory.setItem(i, is);
            i++;
        }
        data.put("uuidList", uuidList);
        setNewData(civilian.getUuid(), data);

        return inventory;
    }
}

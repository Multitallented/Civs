package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.*;

public class ListAllPlayersMenu extends Menu {
    public static String MENU_NAME = "CivsPlayers";
    public ListAllPlayersMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.STONE
                || event.getCurrentItem().getType() == Material.AIR || !event.getCurrentItem().hasItemMeta()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        int page = (int) getData(civilian.getUuid(), "page");
        String id = (String) getData(civilian.getUuid(), "id");
        List<Player> blackList = (List<Player>) getData(civilian.getUuid(), "blackList");
        if (blackList == null) {
            blackList = new ArrayList<>();
        }
        String name = (String) getData(civilian.getUuid(), "name");

        if (event.getCurrentItem().getType() == Material.EMERALD) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ListAllPlayersMenu.createMenu(civilian, name, blackList, page + 1, id));
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE) {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ListAllPlayersMenu.createMenu(civilian, name, blackList, page - 1, id));
            return;
        }

        String playerName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (event.getWhoClicked() instanceof Player) {
            if (name != null && (name.equals("Player List") || name.equals("Friend List"))) {
                if (id == null) {
                    appendHistory(civilian.getUuid(), MENU_NAME);
                } else {
                    appendHistory(civilian.getUuid(), MENU_NAME + "," + id);
                }
                event.getWhoClicked().closeInventory();
                int index = Integer.parseInt(event.getCurrentItem().getItemMeta().getLore().get(0));
                UUID uuid = ((ArrayList<UUID>) getData(civilian.getUuid(), "uuidList")).get(index);
                event.getWhoClicked().openInventory(PlayerProfileMenu.createMenu(civilian, uuid));
            } else {
                event.getWhoClicked().closeInventory();
                clearHistory(civilian.getUuid());
                ((Player) event.getWhoClicked()).performCommand("cv " + name + " " + playerName + " " + id);
            }
        }
    }

    public static Inventory createMenu(Civilian civilian, int page) {
        return createMenu(civilian, null, null, page, null);
    }

    public static Inventory createMenu(Civilian civilian, int page, UUID id) {
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        List<OfflinePlayer> players = new ArrayList<>();
        Civilian cCivilian = CivilianManager.getInstance().getCivilian(id);
        for (UUID uuid : cCivilian.getFriends()) {
            players.add(Bukkit.getOfflinePlayer(uuid));
        }
        int startIndex = Util.createPageButtons(inventory, page, civilian, players.size());

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("name", "Friend List");
        if (id != null) {
            data.put("id", id);
        }

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        int i=9;
        Collections.sort(players, new Comparator<OfflinePlayer>() {
            @Override
            public int compare(OfflinePlayer player1, OfflinePlayer player2) {
                return player1.getName().compareTo(player2.getName());
            }
        });
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (int k=startIndex; k<players.size() && k<startIndex+36; k++) {
            OfflinePlayer player = players.get(k);
            ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta isMeta = (SkullMeta) is.getItemMeta();
            isMeta.setDisplayName(player.getName());
            ArrayList<String> lore1 = new ArrayList<>();
            lore1.add("" + (i-9));
            uuidList.add(player.getUniqueId());
            isMeta.setLore(lore1);
            isMeta.setOwningPlayer(player);
            is.setItemMeta(isMeta);
            inventory.setItem(i, is);
            i++;
        }
        data.put("uuidList", uuidList);
        setNewData(civilian.getUuid(), data);

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, String name, List<Player> blackList, int page, String id) {
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
        data.put("blackList", blackList);
        data.put("name", name);
        if (id != null) {
            data.put("id", id);
        }

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (blackList != null) {
            players.removeAll(blackList);
        }
        int startIndex = page * 36;
        //8 Next button
        if (startIndex + 36 < players.size()) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }

        int i=9;
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player1.getName().compareTo(player2.getName());
            }
        });
        ArrayList<UUID> uuidList = new ArrayList<>();
        for (int k=startIndex; k<players.size() && k<startIndex+36; k++) {
            Player player = players.get(k);
            ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
            ArrayList<String> lore2 = new ArrayList<>();
            uuidList.add(player.getUniqueId());
            lore2.add("" + (i-9));
            SkullMeta isMeta = (SkullMeta) is.getItemMeta();
            isMeta.setDisplayName(player.getName());
            isMeta.setOwningPlayer(player);
            isMeta.setLore(lore2);
            is.setItemMeta(isMeta);
            inventory.setItem(i, is);
            i++;
        }
        data.put("uuidList", uuidList);
        setNewData(civilian.getUuid(), data);

        return inventory;
    }
}

package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.*;

public class ListAllPlayersMenu extends Menu {
    public static String MENU_NAME = "CivsPlayers";
    public ListAllPlayersMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }

        //TODO perform add command
    }

    public static Inventory createMenu(Civilian civilian, String name, List<Player> blackList, int page) {
        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        //0 Prev button

        //2 Icon
        CVItem cvItem = CVItem.createCVItemFromString("STONE");
        cvItem.setDisplayName(name);
        inventory.setItem(2, cvItem.createItemStack());

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        //8 Next button

        int i=9;
        List<Player> players = new ArrayList<>();
        players.addAll(Bukkit.getOnlinePlayers());
        players.removeAll(blackList);
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player player1, Player player2) {
                return player1.getName().compareTo(player2.getName());
            }
        });
        for (Player player : players) {
            //TODO paginate all players
        }

        return inventory;
    }
}

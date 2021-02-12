package org.redcastlemedia.multitallented.civs.menus.towns;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

@CivsMenu(name = "select-town") @SuppressWarnings("unused")
public class SelectTownMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        Set<Town> towns = new HashSet<>();
        if (params.containsKey("uuid")) {
            UUID uuid = UUID.fromString(params.get("uuid"));
            for (Town town : TownManager.getInstance().getTowns()) {
                if (town.getRawPeople().containsKey(uuid)) {
                    towns.add(town);
                }
            }
        } else if (params.containsKey("townList")) {
            for (String townName : params.get("townList").split(",")) {
                towns.add(TownManager.getInstance().getTown(townName));
            }
        } else if (params.containsKey("ally")) {
            data.put("allyTown", params.get("allyTown"));
            boolean isAlly = params.get("ally").equals("true");
            data.put("ally", isAlly);
            towns.addAll(TownManager.getInstance().getOwnedTowns(civilian));
        } else {
            towns.addAll(TownManager.getInstance().getTowns());
        }
        data.put("towns", towns);
        int maxPage = (int) Math.ceil((double) towns.size() / (double) itemsPerPage.get("towns"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        for (String key : params.keySet()) {
            if (key.equals("page") || key.equals("maxPage") || "ally".equals(key)) {
                continue;
            }
            data.put(key, params.get(key));
        }

        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("towns")) {
            Set<Town> towns = (Set<Town>) MenuManager.getData(civilian.getUuid(), "towns");
            Integer page = (Integer) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = (page != null ? page : 0) * menuIcon.getIndex().size();
            if (towns == null) {
                return new ItemStack(Material.AIR);
            }
            Town[] townArray = new Town[towns.size()];
            townArray = towns.toArray(townArray);
            if (townArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Town town = townArray[startIndex + count];
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.getLore().clear();
            ItemStack itemStack = cvItem.createItemStack();
            boolean isAllianceSelect = MenuManager.getAllData(civilian.getUuid()).containsKey("ally");
            if (isAllianceSelect) {
                Boolean ally = (Boolean) MenuManager.getData(civilian.getUuid(), "ally");
                List<String> currentActions = new ArrayList<>();

                if (ally != null && ally) {
                    currentActions.add("ally");
                } else {
                    currentActions.add("unally");
                }
                actions.get(civilian.getUuid()).put(itemStack.getType().name() + ":" + itemStack.getItemMeta().getDisplayName(), currentActions);
            } else {
                putActions(civilian, menuIcon, itemStack, count);
            }
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return true;
        }
        boolean isAllianceSelect = MenuManager.getAllData(civilian.getUuid()).containsKey("ally");
        if (isAllianceSelect) {
            String townName = ChatColor.stripColor(itemStack.getItemMeta().getDisplayName());
            Town fromTown = TownManager.getInstance().getTown(townName);
            String allianceTown = (String) MenuManager.getData(civilian.getUuid(), "allyTown");
            Town toTown = TownManager.getInstance().getTown(allianceTown);
            if (toTown == null || fromTown == null) {
                return true;
            }
            if ("ally".equals(actionString)) {
                AllianceManager.getInstance().sendAllyInvites(toTown, fromTown, player);
            } else if ("unally".equals(actionString)) {
                AllianceManager.getInstance().unAllyBroadcast(toTown, fromTown);
            }
            player.closeInventory();
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}

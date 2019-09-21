package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CommandUtil;
import org.redcastlemedia.multitallented.civs.util.PermissionUtil;

public abstract class CustomMenu {
    protected HashMap<Integer, MenuIcon> itemIndexes;
    protected HashMap<String, Integer> itemsPerPage = new HashMap<>();
    protected HashMap<UUID, HashMap<ItemStack, List<String>>> actions = new HashMap<>();
    protected int size;

    public abstract Map<String, Object> createData(Civilian civilian, Map<String, String> params);

    public Inventory createMenu(Civilian civilian, Map<String, String> params) {
        MenuManager.clearData(civilian.getUuid());
        Map<String, Object> newData = createData(civilian, params);
        MenuManager.setNewData(civilian.getUuid(), newData);
        return createMenu(civilian);
    }
    public Inventory createMenu(Civilian civilian) {
        actions.put(civilian.getUuid(), new HashMap<>());
        Inventory inventory = Bukkit.createInventory(null, this.size, Civs.NAME + getKey());
        HashMap<String, Integer> duplicateCount = new HashMap<>();
        for (Integer i : itemIndexes.keySet()) {
            MenuIcon menuIcon = itemIndexes.get(i);
            if (duplicateCount.containsKey(menuIcon.getKey())) {
                duplicateCount.put(menuIcon.getKey(), duplicateCount.get(menuIcon.getKey()) + 1);
            } else {
                duplicateCount.put(menuIcon.getKey(), 0);
            }
            inventory.setItem(i, createItemStack(civilian, menuIcon, duplicateCount.get(menuIcon.getKey())));
        }
        return inventory;
    }
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (!menuIcon.getPerm().isEmpty()) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (!player.isOp() && (Civs.perm == null || !Civs.perm.has(player, menuIcon.getPerm()))) {
                return new ItemStack(Material.AIR);
            }
        }
        if (menuIcon.getKey().equals("prev")) {
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");

            if (page < 2) {
                return new ItemStack(Material.AIR);
            }
        }
        if (menuIcon.getKey().equals("next")) {
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int maxPage = (int) MenuManager.getData(civilian.getUuid(), "maxPage");

            if (page + 1 > maxPage) {
                return new ItemStack(Material.AIR);
            }
        }

        ItemStack itemStack = menuIcon.createCVItem(civilian.getLocale()).createItemStack();
        List<String> currentActions = new ArrayList<>();
        currentActions.add(menuIcon.getKey());
        actions.get(civilian.getUuid()).put(itemStack, currentActions);
        return itemStack;
    }
    public void loadConfig(HashMap<Integer, MenuIcon> itemIndexes,
                    int size) {
        this.itemIndexes = itemIndexes;
        this.size = size;
        for (MenuIcon menuIcon : itemIndexes.values()) {
            if (menuIcon.getIndex().size() > 1) {
                itemsPerPage.put(menuIcon.getKey(), menuIcon.getIndex().size());
            }
        }
    }
    public abstract String getKey();
    public abstract String getFileName();

    public boolean doActionAndCancel(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        if (!actions.containsKey(civilian.getUuid())) {
            return false;
        }
        List<String> actionStrings = actions.get(civilian.getUuid()).get(clickedItem);
        if (actionStrings == null || actionStrings.isEmpty()) {
            return false;
        }
        for (String actionString : actionStrings) {
            actionString = replaceVariables(civilian, clickedItem, actionString);
            if (actionString.startsWith("menu:")) {
                String menuString = actionString.replace("menu:", "");
                String[] menuSplit = menuString.split("\\?");
                String[] queryString = menuSplit[1].split(",");
                Player player = Bukkit.getPlayer(civilian.getUuid());
                Map<String, String> params = new HashMap<>();
                for (String queryParams : queryString) {
                    String[] splitParams = queryParams.split("=");
                    if (clickedItem.getItemMeta() == null) {
                        params.put(splitParams[0], splitParams[1]);
                    } else {
                        params.put(splitParams[0], splitParams[1]
                                .replace("$itemName$", clickedItem.getItemMeta().getDisplayName()));
                    }
                }
                MenuManager.getInstance().openMenu(player, menuSplit[0], params);
            } else if (actionString.startsWith("command:")) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(civilian.getUuid());
                CommandUtil.performCommand(offlinePlayer, actionString
                        .replace("command:", ""));
            } else if (actionString.startsWith("permission:")) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(civilian.getUuid());
                PermissionUtil.applyPermission(offlinePlayer, actionString
                        .replace("permission:", ""));
            } else if (actionString.equals("close")) {
                Player player = Bukkit.getPlayer(civilian.getUuid());
                player.closeInventory();
            }
        }
        return true;
    }
    public String replaceVariables(Civilian civilian, ItemStack clickedItem, String actionString) {
        if (clickedItem.getItemMeta() != null) {
            actionString = actionString.replaceAll("\\$itemName\\$",
                    clickedItem.getItemMeta().getDisplayName());
        }
        Map<String, Object> data = MenuManager.getAllData(civilian.getUuid());
        for (String key : data.keySet()) {
            String replaceString = "";
            if (key.equals("town")) {
                Town town = (Town) data.get(key);
                replaceString = town.getName();
            } else if (key.equals("alliance")) {
                Alliance alliance = (Alliance) data.get(key);
                replaceString = alliance.getName();
            } else if (key.equals("region")) {
                Region region = (Region) data.get(key);
                replaceString = region.getId();
            } else if (key.equals("regionType")) {
                RegionType regionType = (RegionType) data.get(key);
                replaceString = regionType.getProcessedName();
            } else if (key.equals("townType")) {
                TownType townType = (TownType) data.get(key);
                replaceString = townType.getProcessedName();
            } else {
                replaceString = (String) data.get(key);
            }
            actionString = actionString.replaceAll("\\$" + key + "\\$", replaceString);
        }
        return actionString;
    }
}

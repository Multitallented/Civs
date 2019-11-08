package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.CommandUtil;
import org.redcastlemedia.multitallented.civs.util.PermissionUtil;

public abstract class CustomMenu {
    protected HashMap<Integer, MenuIcon> itemIndexes;
    protected HashMap<String, Integer> itemsPerPage = new HashMap<>();
    protected HashMap<UUID, HashMap<ItemStack, List<String>>> actions = new HashMap<>();
    protected int size;
    private String name;

    public abstract Map<String, Object> createData(Civilian civilian, Map<String, String> params);

    public String beforeOpenMenu(Civilian civilian) {
        // optional override
        return null;
    }

    public Inventory createMenu(Civilian civilian, Map<String, String> params) {
        MenuManager.clearData(civilian.getUuid());
        Map<String, Object> newData = createData(civilian, params);
        MenuManager.setNewData(civilian.getUuid(), newData);
        return createMenu(civilian);
    }
    public Inventory createMenuFromHistory(Civilian civilian, Map<String, Object> data) {
        MenuManager.setNewData(civilian.getUuid(), data);
        return createMenu(civilian);
    }
    public Inventory createMenu(Civilian civilian) {
        actions.put(civilian.getUuid(), new HashMap<>());
        Inventory inventory = Bukkit.createInventory(null, this.size, Civs.NAME + getName());
        HashMap<String, Integer> duplicateCount = new HashMap<>();
        for (Integer i : itemIndexes.keySet()) {
            MenuIcon menuIcon = itemIndexes.get(i);
            if (duplicateCount.containsKey(menuIcon.getKey())) {
                duplicateCount.put(menuIcon.getKey(), duplicateCount.get(menuIcon.getKey()) + 1);
            } else {
                duplicateCount.put(menuIcon.getKey(), 0);
            }
            ItemStack itemStack = createItemStack(civilian, menuIcon, duplicateCount.get(menuIcon.getKey()));
            if (itemStack.getType() != Material.AIR) {
                inventory.setItem(i, itemStack);
            }
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

            if (page < 1) {
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
        putActions(civilian, menuIcon, itemStack, count);
        return itemStack;
    }
    protected void putActions(Civilian civilian, MenuIcon menuIcon, ItemStack itemStack, int count) {
        List<String> currentActions = new ArrayList<>();
        if (menuIcon.getActions().isEmpty()) {
            currentActions.add(menuIcon.getKey());
        } else {
            for (String action : menuIcon.getActions()) {
                String newAction = action.replace("$count$", "" + count);
                newAction = newAction.replace("$itemName$",
                        ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()));
                currentActions.add(newAction);
            }
        }
        actions.get(civilian.getUuid()).put(itemStack, currentActions);
    }
    public void loadConfig(HashMap<Integer, MenuIcon> itemIndexes,
                    int size, String name) {
        this.itemIndexes = itemIndexes;
        this.size = size;
        this.name = name;
        for (MenuIcon menuIcon : itemIndexes.values()) {
            if (menuIcon.getIndex().size() > 1) {
                itemsPerPage.put(menuIcon.getKey(), menuIcon.getIndex().size());
            }
        }
    }
    public String getName() {
        return this.name;
    }
    public abstract String getFileName();

    public boolean doActionsAndCancel(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        if (!actions.containsKey(civilian.getUuid())) {
            return false;
        }
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return true;
        }
        List<String> actionStrings = actions.get(civilian.getUuid()).get(clickedItem);
        if (actionStrings == null || actionStrings.isEmpty()) {
            return true;
        }
        boolean shouldCancel = false;
        for (String actionString : actionStrings) {
            shouldCancel = doActionAndCancel(civilian, actionString, clickedItem) | shouldCancel;
        }
        return shouldCancel;
    }

    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if (actionString.equals("print-tutorial")) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            TutorialManager.getInstance().printTutorial(player, civilian);
        } else if (actionString.equals("close")) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            MenuManager.clearHistory(civilian.getUuid());
            player.closeInventory();
        } else if (actionString.startsWith("message:")) {
            String messageKey = actionString.split(":")[1];
            Player player = Bukkit.getPlayer(civilian.getUuid());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    messageKey));
        } else if (actionString.startsWith("menu:")) {
            actionString = replaceVariables(civilian, itemStack, actionString);
            String menuString = actionString.replace("menu:", "");
            String[] menuSplit = menuString.split("\\?");
            Player player = Bukkit.getPlayer(civilian.getUuid());
            Map<String, String> params = new HashMap<>();
            if (menuSplit.length > 1) {
                String[] queryString = menuSplit[1].split("&");
                for (String queryParams : queryString) {
                    String[] splitParams = queryParams.split("=");
                    if (itemStack.getItemMeta() == null) {
                        params.put(splitParams[0], splitParams[1]);
                    } else {
                        if (splitParams[0].equals("preserveData")) {
                            Map<String, Object> data = MenuManager.getAllData(civilian.getUuid());
                            for (String key : data.keySet()) {
                                String dataString = stringifyData(key, data.get(key));
                                if (dataString != null) {
                                    params.put(key, dataString);
                                }
                            }
                        } else {
                            params.put(splitParams[0], splitParams[1]);
                        }
                    }
                }
            }
            MenuManager.getInstance().openMenu(player, menuSplit[0], params);
        } else if (actionString.startsWith("command:")) {
            actionString = replaceVariables(civilian, itemStack, actionString);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(civilian.getUuid());
            CommandUtil.performCommand(offlinePlayer, actionString
                    .replace("command:", ""));
        } else if (actionString.startsWith("permission:")) {
            actionString = replaceVariables(civilian, itemStack, actionString);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(civilian.getUuid());
            PermissionUtil.applyPermission(offlinePlayer, actionString
                    .replace("permission:", ""));
        }
        return true;
    }

    private String stringifyData(String key, Object data) {
        if (key.equals("town")) {
            Town town = (Town) data;
            return town.getName();
        } else if (key.equals("alliance")) {
            Alliance alliance = (Alliance) data;
            return alliance.getName();
        } else if (key.equals("region")) {
            Region region = (Region) data;
            return region.getId();
        } else if (key.equals("regionType")) {
            RegionType regionType = (RegionType) data;
            return regionType.getProcessedName();
        } else if (key.equals("townType")) {
            TownType townType = (TownType) data;
            return townType.getProcessedName();
        } else if (data instanceof String) {
            return (String) data;
        } else {
            return "";
        }
    }

    private String replaceVariables(Civilian civilian, ItemStack clickedItem, String actionString) {
        if (clickedItem.getItemMeta() != null) {
            actionString = actionString.replaceAll("\\$itemName\\$",
                    clickedItem.getItemMeta().getDisplayName());
        }
        Map<String, Object> data = MenuManager.getAllData(civilian.getUuid());
        for (String key : data.keySet()) {
            if (!actionString.contains("$" + key + "$")) {
                continue;
            }
            String replaceString = stringifyData(key, data.get(key));
            actionString = actionString.replaceAll("\\$" + key + "\\$", replaceString);
        }
        return actionString;
    }

    public void onCloseMenu(Civilian civilian, Inventory inventory) {
        // Do nothing normally
    }
}

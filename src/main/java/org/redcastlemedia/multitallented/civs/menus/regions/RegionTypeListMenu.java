package org.redcastlemedia.multitallented.civs.menus.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.FolderType;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "region-type-list") @SuppressWarnings("unused")
public class RegionTypeListMenu extends CustomMenu {
    private static final String REGION_TYPES = "regionTypes";

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        String category = params.get("category");
        String townName = params.get("town");
        String regionList = params.get("regionList");
        HashMap<String, Integer> regionTypes;
        if (regionList != null) {
            String[] regionListSplit = regionList.split(",");
            regionTypes = new HashMap<>();
            for (String region : regionListSplit) {
                String[] regionSplit = region.split(":");
                if (regionSplit.length < 2) {
                    regionTypes.put(region, 1);
                } else {
                    regionTypes.put(regionSplit[0], Integer.parseInt(regionSplit[1]));
                }
            }
        } else if (category == null || townName == null) {
            regionTypes = new HashMap<>();
        } else if (category.equals("reqs")) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(townName);
            regionTypes = new HashMap<>(townType.getReqs());
        } else if (category.equals("limits")) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(townName);
            regionTypes = new HashMap<>(townType.getRegionLimits());
        } else {
            regionTypes = new HashMap<>();
        }
        for (Map.Entry<String, Integer> entry : new HashMap<>(regionTypes).entrySet()) {
            if (entry.getValue() < 1) {
                regionTypes.remove(entry.getKey());
            }
        }
        data.put(REGION_TYPES, regionTypes);

        int maxPage = (int) Math.ceil((double) regionTypes.size() / (double) itemsPerPage.get(REGION_TYPES));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        for (String key : params.keySet()) {
            if (key.equals("page") || key.equals("maxPage")) {
                continue;
            }
            data.put(key, params.get(key));
        }

        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if (menuIcon.getKey().equals(REGION_TYPES)) {
            HashMap<String, Integer> regionTypes;
            if (MenuManager.getData(civilian.getUuid(), REGION_TYPES) != null) {
                regionTypes = (HashMap<String, Integer>) MenuManager.getData(civilian.getUuid(), REGION_TYPES);
            } else {
                return new ItemStack(Material.AIR);
            }
            if (regionTypes == null) {
                return new ItemStack(Material.AIR);
            }
            ArrayList<String> regionTypeNames = new ArrayList<>();
            ArrayList<CVItem> fullListRegionTypes = new ArrayList<>();
            for (String regionTypeName : regionTypes.keySet()) {
                CivItem civItem = ItemManager.getInstance().getItemType(regionTypeName);
                CVItem currentItem;
                if (civItem == null) {
                    regionTypeNames.add("g:" + regionTypeName);
                    currentItem = CVItem.createCVItemFromString("CHEST");
                    currentItem.setDisplayName("g:" + regionTypeName);
                } else if (civItem instanceof FolderType) {
                    currentItem = civItem.getShopIcon(civilian.getLocale()).clone();
                    regionTypeNames.add(civItem.getProcessedName());
                } else {
                    regionTypeNames.add(civItem.getProcessedName());
                    currentItem = civItem.getShopIcon(civilian.getLocale());
                }
                currentItem.setQty(regionTypes.get(regionTypeName));
                fullListRegionTypes.add(currentItem);
            }
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (fullListRegionTypes.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = fullListRegionTypes.get(startIndex + count);
            ItemStack itemStack;
            if (cvItem.getDisplayName().startsWith("g:")) {
                String groupKey = cvItem.getDisplayName().replace("g:", "");
                String localGroupName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        groupKey + LocaleConstants.GROUP_SUFFIX);
                cvItem.setDisplayName(localGroupName);
                itemStack = cvItem.createItemStack();
                ArrayList<String> actionList = new ArrayList<>();
                List<CivItem> group = ItemManager.getInstance().getItemGroup(cvItem.getDisplayName());
                StringBuilder action = new StringBuilder("menu:region-type-list?regionList=");
                for (CivItem item : group) {
                    action.append(item.getProcessedName()).append(",");
                }
                action = new StringBuilder(action.substring(0, action.length() - 1));
                actionList.add(action.toString());
                actions.get(civilian.getUuid()).put(itemStack, actionList);
            } else if (cvItem.getDisplayName().startsWith("f:")) {
                String folderKey = cvItem.getDisplayName().replace("f:", "");
                String localFolderName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                        folderKey + LocaleConstants.NAME_SUFFIX);
                cvItem.setDisplayName(localFolderName);
                FolderType folderType = (FolderType) ItemManager.getInstance().getItemType(regionTypeNames.get(startIndex + count));
                itemStack = cvItem.createItemStack();
                ArrayList<String> actionList = new ArrayList<>();
                List<CivItem> group = folderType.getChildren();
                StringBuilder action = new StringBuilder("menu:region-type-list?regionList=");
                for (CivItem item : group) {
                    action.append(item.getProcessedName()).append(",");
                }
                action = new StringBuilder(action.substring(0, action.length() - 1));
                actionList.add(action.toString());
                actions.get(civilian.getUuid()).put(itemStack, actionList);
            } else {
                itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                if (actions.get(civilian.getUuid()).get(itemStack).contains("view-type")) {
                    int index = actions.get(civilian.getUuid()).get(itemStack).indexOf("view-type");
                    actions.get(civilian.getUuid()).get(itemStack).set(index,
                            "menu:region-type?regionType=" + regionTypeNames.get(startIndex + count));
                }
            }
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}

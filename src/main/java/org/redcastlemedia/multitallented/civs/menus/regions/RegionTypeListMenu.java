package org.redcastlemedia.multitallented.civs.menus.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
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
            for (String regionString : regionListSplit) {
                String[] regionSplit = regionString.split(":");
                if (regionSplit.length < 2) {
                    addType(regionTypes, regionString, 1);
                } else {
                    addType(regionTypes, regionSplit[0], Integer.parseInt(regionSplit[1]));
                }
            }
        } else if (category == null || townName == null) {
            regionTypes = new HashMap<>();
        } else if (category.equals("reqs")) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(townName);
            regionTypes = new HashMap<>();
            addAllTypes(regionTypes, townType.getReqs());
        } else if (category.equals("limits")) {
            regionTypes = new HashMap<>();
            TownType townType = (TownType) ItemManager.getInstance().getItemType(townName);
            addAllTypes(regionTypes, townType.getRegionLimits());
        } else {
            regionTypes = new HashMap<>();
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
                    currentItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                            "any-region")));
                } else if (civItem instanceof FolderType) {
                    currentItem = civItem.getShopIcon(civilian.getLocale()).clone();
                    currentItem.setDisplayName("f:" + currentItem.getDisplayName());
                    regionTypeNames.add(civItem.getProcessedName());
                    currentItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                            "any-region")));
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
                String localGroupName = LocaleManager.getInstance().getTranslation(player,
                        groupKey + LocaleConstants.GROUP_SUFFIX);
                cvItem.setDisplayName(localGroupName);
                itemStack = cvItem.createItemStack();
                ArrayList<String> actionList = new ArrayList<>();
                List<CivItem> group = ItemManager.getInstance().getItemGroup(groupKey);
                StringBuilder action = new StringBuilder("menu:region-type-list?regionList=");
                for (CivItem item : group) {
                    action.append(item.getProcessedName()).append(",");
                }
                action = new StringBuilder(action.substring(0, action.length() - 1));
                actionList.add(action.toString());
                putActionList(civilian, itemStack, actionList);
            } else if (cvItem.getDisplayName().startsWith("f:")) {
                String folderName = cvItem.getDisplayName().replace("f:", "");
                cvItem.setDisplayName(folderName);
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
                putActionList(civilian, itemStack, actionList);
            } else {
                itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                List<String> actionList = getActions(civilian, itemStack);
                if (actionList.contains("view-type")) {
                    int index = actionList.indexOf("view-type");
                    actionList.set(index, "menu:region-type?regionType=" + regionTypeNames.get(startIndex + count));
                }
            }
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    private void addAllTypes(Map<String, Integer> typeMap, Map<String, Integer> inputMap) {
        for (Map.Entry<String, Integer> entry : inputMap.entrySet()) {
            addType(typeMap, entry.getKey(), entry.getValue());
        }
    }

    private void addType(Map<String, Integer> typeMap, String type, int count) {
        if (count > 0) {
            CivItem civItem = ItemManager.getInstance().getItemType(type);
            if (civItem == null || civItem.getInShop()) {
                typeMap.put(type, count);
            }
        }
    }
}

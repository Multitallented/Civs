package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.menus.MenuUtil;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

public class RecipeMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        String category = params.get("category");
        String regionTypeName = params.get("regionType");
        List<List<CVItem>> items;
        if (category == null || regionTypeName == null) {
            items = new ArrayList<>();
        } else if (category.equals("reqs")) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = regionType.getReqs();
        } else if (category.startsWith("reagents")) {
            int index = Integer.parseInt(category.replace("reagents", ""));
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = regionType.getUpkeeps().get(index).getReagents();
        } else if (category.startsWith("input")) {
            int index = Integer.parseInt(category.replace("input", ""));
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = regionType.getUpkeeps().get(index).getInputs();
        } else if (category.startsWith("output")) {
            int index = Integer.parseInt(category.replace("output", ""));
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = regionType.getUpkeeps().get(index).getOutputs();
        } else if (category.startsWith("g:")) {
            items = new ArrayList<>();
            String groupName = category.replace("g:", "");
            String groupString = ConfigManager.getInstance().getItemGroups().get(groupName);
            for (String matString : groupString.split(",")) {
                List<CVItem> tempMap = new ArrayList<>();
                CVItem cvItem = CVItem.createCVItemFromString(matString);
                tempMap.add(cvItem);
                items.add(tempMap);
            }
        } else {
            items = new ArrayList<>();
        }
        data.put("items", items);

        int maxPage = (int) Math.ceil((double) items.size() / (double) itemsPerPage.get("items"));
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

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("items")) {
            List<List<CVItem>> items;
            if (MenuManager.getData(civilian.getUuid(), "items") != null) {
                items = (List<List<CVItem>>) MenuManager.getData(civilian.getUuid(), "items");
                if (items == null) {
                    return new ItemStack(Material.AIR);
                }
            } else {
                return new ItemStack(Material.AIR);
            }
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (items.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            ItemStack firstStack = null;
            for (CVItem cvItem : items.get(startIndex + count)) {
                ItemStack itemStack = cvItem.createItemStack();
                MenuUtil.sanitizeItem(itemStack);
                MenuManager.addCycleItem(civilian.getUuid(), menuIcon.getIndex().get(count), itemStack);
                if (firstStack == null) {
                    firstStack = itemStack;
                }
                if (cvItem.getGroup() != null) {
                    ArrayList<String> actionList = new ArrayList<>();
                    actionList.add("menu:recipe?category=g:" + cvItem.getGroup());
                    actions.get(civilian.getUuid()).put(itemStack, actionList);
                }
            }
            if (firstStack != null) {
                return firstStack;
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getFileName() {
        return "recipe";
    }
}

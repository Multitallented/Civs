package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.menus.MenuUtil;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsMenu(name = "recipe") @SuppressWarnings("unused")
public class RecipeMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        super.cycleItems.remove(civilian.getUuid());
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        String recipe = params.get("recipe");
        String regionTypeName = params.get(Constants.REGION_TYPE);
        if (regionTypeName != null) {
            data.put(Constants.REGION_TYPE, ItemManager.getInstance().getItemType(regionTypeName));
        }
        List<List<CVItem>> items;
        if (recipe == null || regionTypeName == null) {
            items = new ArrayList<>();
        } else if (recipe.startsWith("failing:")) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = new ArrayList<>();
            Region region = RegionManager.getInstance().getRegionById(params.get("region"));
            if (!region.getMissingBlocks().isEmpty()) {
                items = new ArrayList<>(region.getMissingBlocks());
            }
            if (items.isEmpty()) {
                String[] failingUpkeeps = recipe.replace("failing:", "").split(",");
                for (String index : failingUpkeeps) {
                    if (!index.isEmpty()) {
                        items.addAll(regionType.getUpkeeps().get(Integer.parseInt(index)).getInputs());
                    }
                }
            }
        } else if (recipe.equals("reqs")) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = regionType.getReqs();
        } else if (recipe.startsWith("reagent")) {
            int index = Integer.parseInt(recipe.replace("reagent", ""));
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = regionType.getUpkeeps().get(index).getReagents();
        } else if (recipe.startsWith("input")) {
            int index = Integer.parseInt(recipe.replace("input", ""));
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = regionType.getUpkeeps().get(index).getInputs();
        } else if (recipe.startsWith("output")) {
            int index = Integer.parseInt(recipe.replace("output", ""));
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(regionTypeName);
            items = regionType.getUpkeeps().get(index).getOutputs();
        } else if (recipe.startsWith("broken")) {
            Region region = RegionManager.getInstance().getRegionById(params.get("region"));
            items = new ArrayList<>(region.getMissingBlocks());
        } else if (recipe.startsWith("g:")) {
            items = new ArrayList<>();
            List<CVItem> cvItems = CVItem.createListFromString(recipe);
            for (CVItem cvItem : cvItems) {
                List<CVItem> singleList = new ArrayList<>();
                singleList.add(cvItem);
                items.add(singleList);
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

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(
                (String) MenuManager.getData(civilian.getUuid(), Constants.REGION_TYPE));
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
                super.addCycleItem(civilian.getUuid(), menuIcon.getIndex().get(count), itemStack);
                if (firstStack == null) {
                    firstStack = itemStack;
                }
                if (cvItem.getGroup() != null) {
                    ArrayList<String> actionList = new ArrayList<>();
                    actionList.add("menu:recipe?recipe=g:" + cvItem.getGroup() + "&regionType=" + regionType.getProcessedName());
                    putActionList(civilian, itemStack, actionList);
                }
            }
            if (firstStack != null) {
                return firstStack;
            }
            return new ItemStack(Material.AIR);
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}

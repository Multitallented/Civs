package org.redcastlemedia.multitallented.civs.menus.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class RegionListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<Region> regions = new ArrayList<>();
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getRawPeople().containsKey(civilian.getUuid())) {
                regions.add(region);
            }
        }
        int maxPage = (int) Math.ceil((double) regions.size() / (double) itemsPerPage.get("regions"));
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
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if (clickedItem.getItemMeta() != null && clickedItem.getItemMeta().getLore() != null &&
                !clickedItem.getItemMeta().getLore().isEmpty()) {
            String regionId = ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(0));
            Region region = RegionManager.getInstance().getRegionById(regionId);
            if (region != null) {
                MenuManager.putData(civilian.getUuid(), "region", region);
            }
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("regions")) {
            List<Region> regions;
            if (MenuManager.getData(civilian.getUuid(), "regions") != null) {
                regions = (ArrayList<Region>) MenuManager.getData(civilian.getUuid(), "regions");
            } else {
                regions = new ArrayList<>();
                for (Region region : RegionManager.getInstance().getAllRegions()) {
                    if (region.getRawPeople().containsKey(civilian.getUuid())) {
                        regions.add(region);
                    }
                }
            }
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            Region[] regionArray = new Region[regions.size()];
            regionArray = regions.toArray(regionArray);
            if (regionArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Region region = regionArray[startIndex + count];
            CVItem cvItem = ItemManager.getInstance().getItemType(region.getType()).getShopIcon().clone();
            cvItem.getLore().add(0, ChatColor.BLACK + region.getId());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getFileName() {
        return "region-list";
    }
}

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
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "region-list") @SuppressWarnings("unused")
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
        if (params.containsKey("sell")) {
            for (Region r : RegionManager.getInstance().getAllRegions()) {
                if (r.getForSale() != -1 && !r.getRawPeople().containsKey(civilian.getUuid())) {
                    Town town = TownManager.getInstance().getTownAt(r.getLocation());
                    if (town == null || town.getPeople().containsKey(civilian.getUuid())) {
                        regions.add(r);
                    }
                }
            }
        } else {
            for (Region region : RegionManager.getInstance().getAllRegions()) {
                if (region.getRawPeople().containsKey(civilian.getUuid())) {
                    regions.add(region);
                }
            }
        }
        data.put("regionMap", new HashMap<ItemStack, Region>());
        data.put("regions", regions);
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

    @Override @SuppressWarnings("unchecked")
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if ("view-region".equals(actionString)) {
            Region region = ((HashMap<ItemStack, Region>) MenuManager.getData(civilian.getUuid(), "regionMap")).get(clickedItem);
            if (region != null) {
                MenuManager.putData(civilian.getUuid(), "region", region);
                MenuManager.openMenuFromString(civilian, "region?region=" + region.getId() + "&preserveData=true");
                return true;
            }
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if (menuIcon.getKey().equals("regions")) {
            List<Region> regions;
            if (MenuManager.getData(civilian.getUuid(), "regions") != null) {
                regions = (List<Region>) MenuManager.getData(civilian.getUuid(), "regions");
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
            CVItem cvItem = ItemManager.getInstance().getItemType(region.getType()).getShopIcon(civilian.getLocale());
            Town town = TownManager.getInstance().getTownAt(region.getLocation());
            if (town != null) {
                cvItem.getLore().add(town.getName());
            }
            cvItem.setLore(Util.textWrap(civilian, region.getSummary(player)));
            ItemStack itemStack = cvItem.createItemStack();
            ((HashMap<ItemStack, Region>) MenuManager.getData(civilian.getUuid(), "regionMap")).put(itemStack, region);
            List<String> actionList = getActions(civilian, itemStack);
            putActions(civilian, menuIcon, itemStack, count);

            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}

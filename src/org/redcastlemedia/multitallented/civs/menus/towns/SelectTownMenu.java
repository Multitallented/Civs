package org.redcastlemedia.multitallented.civs.menus.towns;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class SelectTownMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        Set<Town> towns = TownManager.getInstance().getOwnedTowns(civilian);
        int maxPage = (int) Math.ceil((double) towns.size() / (double) itemsPerPage.get("towns"));
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
        if (menuIcon.getKey().equals("towns")) {
            Set<Town> towns = TownManager.getInstance().getOwnedTowns(civilian);
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
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
            putActions(civilian, menuIcon, itemStack);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public String getFileName() {
        return "select-town";
    }
}

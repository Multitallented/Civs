package org.redcastlemedia.multitallented.civs.menus.alliance;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@CivsMenu(name = "alliance-list") @SuppressWarnings("unused")
public class AllianceListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        ArrayList<Alliance> alliances = AllianceManager.getInstance().getAllSortedAlliances();

        int maxPage = (int) Math.ceil((double) alliances.size() / (double) itemsPerPage.get("alliances"));
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
        if (menuIcon.getKey().equals("alliances")) {
            ArrayList<Alliance> alliances = AllianceManager.getInstance().getAllSortedAlliances();
            Integer page = (Integer) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = (page != null ? page : 0) * menuIcon.getIndex().size();
            Alliance[] allianceArray = new Alliance[alliances.size()];
            allianceArray = alliances.toArray(allianceArray);
            if (allianceArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Alliance alliance = allianceArray[startIndex + count];
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
            cvItem.setDisplayName(alliance.getName());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}

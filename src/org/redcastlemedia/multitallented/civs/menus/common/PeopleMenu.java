package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;

public class PeopleMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<Civilian> civilians = new ArrayList<>();
        if (params.containsKey("region")) {
            if (params.containsKey("invite")) {
                // TODO
            } else {
                // TODO
            }
        } else if (params.containsKey("town")) {
            if (params.containsKey("invite")) {
                // TODO
            } else {
                // TODO
            }
        } else {
            civilians.addAll(CivilianManager.getInstance().getCivilians());
        }
        data.put("civilians", civilians);
        int maxPage = (int) Math.ceil((double) civilians.size() / (double) itemsPerPage.get("people"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("people".equals(menuIcon.getKey())) {
            // TODO
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if ("take-action".equals(actionString)) {
            // TODO
        }
        return true;
    }

    @Override
    public String getFileName() {
        return "people";
    }
}

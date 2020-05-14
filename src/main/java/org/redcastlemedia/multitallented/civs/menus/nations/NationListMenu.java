package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;

@CivsMenu(name = "nation-list") @SuppressWarnings("unused")
public class NationListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<Nation> nationList = new ArrayList<>(NationManager.getInstance().getAllNations());
        if (params.containsKey("sort") && "power".equals(params.get("sort"))) {
            nationList.sort(Comparator.comparingInt(Nation::getPower));
        } else if (params.containsKey("sort") && "alphabetical".equals(params.get("sort"))) {
            nationList.sort(Comparator.comparing(Nation::getName));
        }
        data.put("nationList", nationList);

        int maxPage = (int) Math.ceil((double) nationList.size() / (double) itemsPerPage.get("nations"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("nations")) {
            List<Nation> nationList = (List<Nation>) MenuManager.getData(civilian.getUuid(), "nationList");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (nationList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Nation nation = nationList.get(startIndex + count);
            ItemStack itemStack = nation.getIcon();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}

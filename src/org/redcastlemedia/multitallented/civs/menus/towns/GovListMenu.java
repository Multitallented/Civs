package org.redcastlemedia.multitallented.civs.menus.towns;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.*;

import java.util.*;

public class GovListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("town")) {
            data.put("town", TownManager.getInstance().getTown(params.get("town")));
        }
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<String> govList = new ArrayList<>(GovernmentManager.getInstance().getGovermentTypes());
        data.put("govList", govList);
        data.put("govMap", new HashMap<ItemStack, String>());

        int maxPage = (int) Math.ceil((double) govList.size() / (double) itemsPerPage.get("governments"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("governments")) {
            List<String> govList = (List<String>) MenuManager.getData(civilian.getUuid(), "govList");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (govList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            String govName = govList.get(startIndex + count);
            Government government = GovernmentManager.getInstance().getGovernment(govName);
            ItemStack itemStack = government.getIcon(civilian.getLocale()).createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            ((HashMap<ItemStack, String>) MenuManager.getData(civilian.getUuid(), "govMap")).put(itemStack, govName);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if ("select-gov".equals(actionString)) {
            Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
            HashMap<ItemStack, String> govMap = (HashMap<ItemStack, String>) MenuManager.getData(civilian.getUuid(), "govMap");
            String govName = govMap.get(clickedItem);
            GovernmentManager.getInstance().transitionGovernment(town, govName, true);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }

    @Override
    public String getFileName() {
        return "gov-list";
    }
}

package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;

@CivsMenu(name = "tutorial-choose-path")
public class TutorialChoosePathMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }

        List<CVItem> itemList = TutorialManager.getInstance().getPaths(civilian);
        int maxPage = (int) Math.ceil((double) itemList.size() / (double) itemsPerPage.get("paths"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        data.put("pathNames", new HashMap<ItemStack, String>());
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("paths".equals(menuIcon.getKey())) {
            List<CVItem> itemList = TutorialManager.getInstance().getPaths(civilian);
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (itemList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = itemList.get(startIndex + count).clone();
            HashMap<ItemStack, String> pathNames = (HashMap<ItemStack, String>) MenuManager.getData(civilian.getUuid(), "pathNames");
            String pathName = cvItem.getLore().get(0);
            cvItem.getLore().clear();
            ItemStack itemStack = cvItem.createItemStack();
            pathNames.put(itemStack, pathName);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if ("choose-path".equals(actionString)) {
            civilian.setTutorialIndex(0);
            civilian.setTutorialProgress(0);
            String pathName = ((HashMap<ItemStack, String>) MenuManager.getData(civilian.getUuid(), "pathNames")).get(clickedItem);
            civilian.setTutorialPath(pathName);
            TutorialManager.getInstance().sendMessageForCurrentTutorialStep(civilian, true);
            CivilianManager.getInstance().saveCivilian(civilian);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}

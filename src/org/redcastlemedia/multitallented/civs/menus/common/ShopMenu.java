package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class ShopMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        CivItem parent;
        if (params.get("parent") == null) {
            parent = null;
        } else {
            parent = ItemManager.getInstance().getItemType(params.get("parent"));
            data.put("parent", parent);
        }
        List<CivItem> shopItems = ItemManager.getInstance().getShopItems(civilian, parent);
        int maxPage = (int) Math.ceil((double) shopItems.size() / (double) itemsPerPage.get("items"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        for (String key : params.keySet()) {
            if (key.equals("page") || key.equals("maxPage") ||
                    key.equals("parent")) {
                continue;
            }
            data.put(key, params.get(key));
        }

        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("parent")) {
            CivItem parent = (CivItem) MenuManager.getData(civilian.getUuid(), "parent");
            if (parent == null) {
                return new ItemStack(Material.AIR);
            }
            CVItem icon = parent.clone();
            icon.setDisplayName(LocaleManager.getInstance()
                    .getTranslation(civilian.getLocale(), parent.getProcessedName() + "-name"));
            icon.getLore().clear();
            icon.getLore().add(ChatColor.BLACK + parent.getProcessedName());
            icon.getLore().addAll(Util.textWrap(LocaleManager.getInstance()
                    .getTranslation(civilian.getLocale(),
                    parent.getProcessedName() + "-desc")));
            return icon.createItemStack();
        } else if (menuIcon.getKey().equals("items")) {
            // TODO fill items from parent or level
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

        @Override
    public boolean doActionAndCancel(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        if (!actions.containsKey(civilian.getUuid())) {
            return false;
        }
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return true;
        }
        List<String> actionStrings = actions.get(civilian.getUuid()).get(clickedItem);
        for (String actionString : actionStrings) {
            if (actionString.equals("view-item")) {
                // TODO open folder, town-type, or region-type menu
                return true;
            }
        }
        return super.doActionAndCancel(civilian, cursorItem, clickedItem);
    }

    @Override
    public String getFileName() {
        return "shop";
    }
}

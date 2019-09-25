package org.redcastlemedia.multitallented.civs.menus.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class BlueprintsMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }

        int maxPage = (int) Math.ceil((double) civilian.getStashItems().keySet().size() / (double) itemsPerPage.get("regions"));
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
    public void onCloseMenu(Civilian civilian, Inventory inventory) {
        HashMap<String, Integer> stashItems = civilian.getStashItems();
        HashSet<String> removeItems = new HashSet<>();
        for (String currentName : stashItems.keySet()) {
            CivItem item = ItemManager.getInstance().getItemType(currentName);
            if (item.getItemType() == CivItem.ItemType.REGION ||
                    item.getItemType() == CivItem.ItemType.TOWN) {
                removeItems.add(currentName);
            }
        }
        for (String currentName : removeItems) {
            stashItems.remove(currentName);
        }
        for (ItemStack is : inventory) {
            if (!CVItem.isCivsItem(is)) {
                continue;
            }
            CivItem civItem = CivItem.getFromItemStack(is);
            String name = civItem.getProcessedName();
            if (stashItems.containsKey(name)) {
                stashItems.put(name, is.getAmount() + stashItems.get(name));
            } else {
                stashItems.put(name, is.getAmount());
            }
        }
        civilian.setStashItems(stashItems);
        CivilianManager.getInstance().saveCivilian(civilian);
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("blueprints")) {
            HashMap<String, Integer> stashItems = civilian.getStashItems();
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            String[] stashArray = new String[stashItems.size()];
            stashArray = stashItems.keySet().toArray(stashArray);
            String currentStashItemName = stashArray[startIndex + count];
            CivItem civItem = ItemManager.getInstance().getItemType(currentStashItemName);
            if (civItem == null) {
                civilian.getStashItems().remove(currentStashItemName);
                CivilianManager.getInstance().saveCivilian(civilian);
            }
            CVItem cvItem = civItem.clone();
            List<String> lore = new ArrayList<>();
            lore.add(civilian.getUuid().toString());
            lore.add(cvItem.getDisplayName());
            boolean isTown = civItem.getItemType().equals(CivItem.ItemType.TOWN);
            if (isTown) {
                lore.add(ChatColor.GREEN + Util.parseColors(LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "town-instructions")
                        .replace("$1", civItem.getProcessedName())));
            } else {
                lore.addAll(Util.textWrap(Util.parseColors(civItem.getDescription(civilian.getLocale()))));
            }
            cvItem.setLore(lore);
            cvItem.setQty(civilian.getStashItems().get(currentStashItemName));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        return false;
    }

    @Override
    public String getFileName() {
        return "blueprints";
    }
}

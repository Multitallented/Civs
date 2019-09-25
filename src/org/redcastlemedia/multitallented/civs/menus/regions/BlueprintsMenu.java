package org.redcastlemedia.multitallented.civs.menus.regions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;

public class BlueprintsMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        return new HashMap<>();
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
    public boolean doActionAndCancel(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        return false;
    }

    @Override
    public String getFileName() {
        return "blueprints";
    }
}

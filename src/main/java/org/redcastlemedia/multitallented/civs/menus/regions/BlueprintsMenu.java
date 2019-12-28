package org.redcastlemedia.multitallented.civs.menus.regions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "blueprints") @SuppressWarnings("unused")
public class BlueprintsMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }

        Map<String, Integer> stashItems = civilian.getStashItems();
        Map<String, Integer> newItems = ItemManager.getInstance().getNewItems(civilian);
        for (Map.Entry<String, Integer> entry : newItems.entrySet()) {
            String itemName = entry.getKey();
            stashItems.put(itemName, newItems.get(itemName));
        }
        CivilianManager.getInstance().saveCivilian(civilian);
        data.put(Constants.ITEMS_IN_VIEW, new HashMap<String, Integer>());
        int maxPage = (int) Math.ceil((double) stashItems.keySet().size() / (double) itemsPerPage.get("blueprints"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key.equals("page") || key.equals("maxPage")) {
                continue;
            }
            data.put(key, params.get(key));
        }
        return data;
    }

    @Override @SuppressWarnings("unchecked")
    public void onCloseMenu(Civilian civilian, Inventory inventory) {
        Map<String, Integer> stashItems = civilian.getStashItems();
        HashMap<String, Integer> itemsInView = (HashMap<String, Integer>) MenuManager.getData(civilian.getUuid(), Constants.ITEMS_IN_VIEW);
        if (itemsInView == null) {
            itemsInView = new HashMap<>();
        }
        addItemsToStash(inventory, itemsInView, stashItems);
        for (Map.Entry<String, Integer> entry : itemsInView.entrySet()) {
            String name = entry.getKey();
            if (!stashItems.containsKey(name)) {
                stashItems.put(name, itemsInView.get(name));
                continue;
            }
            if (stashItems.get(name) <= itemsInView.get(name)) {
                stashItems.remove(name);
            } else {
                int amount = stashItems.get(name) - itemsInView.get(name);
                stashItems.put(name, amount);
            }
        }
        civilian.setStashItems(stashItems);
        CivilianManager.getInstance().saveCivilian(civilian);
        itemsInView.clear();
    }
    private void addItemsToStash(Inventory inventory,
                                 HashMap<String, Integer> itemsInView,
                                 Map<String, Integer> stashItems) {
        for (ItemStack is : inventory) {
            if (!CVItem.isCivsItem(is)) {
                continue;
            }
            CivItem civItem = CivItem.getFromItemStack(is);
            String name = civItem.getProcessedName();
            if (itemsInView.containsKey(name)) {
                addItemFromItemsInView(itemsInView, name, is, stashItems);
            } else {
                if (stashItems.containsKey(name)) {
                    stashItems.put(name, is.getAmount() + stashItems.get(name));
                } else {
                    stashItems.put(name, is.getAmount());
                }
            }
        }
    }

    private void addItemFromItemsInView(HashMap<String, Integer> itemsInView,
                                        String name,
                                        ItemStack is,
                                        Map<String, Integer> stashItems) {
        if (itemsInView.get(name) >= is.getAmount()) {
            itemsInView.put(name, itemsInView.get(name) - is.getAmount());
        } else {
            if (itemsInView.get(name) < is.getAmount()) {
                int amount = is.getAmount() - itemsInView.get(name);
                if (stashItems.containsKey(name)) {
                    stashItems.put(name, amount + stashItems.get(name));
                } else {
                    stashItems.put(name, amount);
                }
            }
            itemsInView.remove(name);
        }
    }

    @Override @SuppressWarnings("unchecked")
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("blueprints")) {
            Map<String, Integer> stashItems = civilian.getStashItems();
            if (stashItems.isEmpty()) {
                return new ItemStack(Material.AIR);
            }
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            String[] stashArray = new String[stashItems.size()];
            stashArray = stashItems.keySet().toArray(stashArray);
            if (stashArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            String currentStashItemName = stashArray[startIndex + count];
            CivItem civItem = ItemManager.getInstance().getItemType(currentStashItemName);
            if (civItem == null) {
                civilian.getStashItems().remove(currentStashItemName);
                CivilianManager.getInstance().saveCivilian(civilian);
                return new ItemStack(Material.AIR);
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
            HashMap<String, Integer> itemsInView = (HashMap<String, Integer>) MenuManager.getData(civilian.getUuid(), Constants.ITEMS_IN_VIEW);
            if (itemsInView == null) {
                return new ItemStack(Material.AIR);
            }
            itemsInView.put(currentStashItemName, civilian.getStashItems().get(currentStashItemName));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.isCancelled() && event.getCurrentItem() != null && !CVItem.isCivsItem(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

}

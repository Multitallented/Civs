package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class BlueprintsMenu extends Menu {
    private static final String MENU_NAME = "CivsRegionStash";
    public BlueprintsMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        //Do nothing
    }

    //TODO make this more secure?
    @Override @EventHandler
    @SuppressWarnings("unchecked")
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);
        Inventory inventory = event.getInventory();
        try {
            if (!event.getView().getTitle().equals(MENU_NAME)) {
                return;
            }
        } catch (IllegalStateException stateException) {
            return;
        }

        ItemManager itemManager = ItemManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
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
            CivItem civItem = itemManager.getItemType(is.getItemMeta().getDisplayName());
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

    public static Inventory createMenu(Civilian civilian) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(civilian.getStashItems().size()), MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        int i=0;
        for (String currentName : civilian.getStashItems().keySet()) {
            CivItem civItem = ItemManager.getInstance().getItemType(currentName);
            boolean isTown = civItem.getItemType().equals(CivItem.ItemType.TOWN);
            if (!civItem.getItemType().equals(CivItem.ItemType.REGION) && !isTown) {
                continue;
            }
            CVItem cvItem = civItem.clone();
            List<String> lore = new ArrayList<>();
            lore.add(civilian.getUuid().toString());
            lore.add(cvItem.getDisplayName());
            if (isTown) {
                lore.add(ChatColor.GREEN + Util.parseColors(localeManager.getTranslation(civilian.getLocale(), "town-instructions")
                        .replace("$1", civItem.getProcessedName())));
            } else {
                lore.addAll(Util.textWrap("", Util.parseColors(civItem.getDescription(civilian.getLocale()))));
            }
            cvItem.setLore(lore);
            cvItem.setQty(civilian.getStashItems().get(currentName));
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }

        return inventory;
    }
}

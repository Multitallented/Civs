package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.*;

public class ClassMenu extends Menu {
    public static final String MENU_NAME = "CivsClassStash";
    public ClassMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        ItemStack clickedStack = event.getCurrentItem();
        if (clickedStack == null || !clickedStack.hasItemMeta()) {
            return;
        }
        ItemStack iconStack = event.getInventory().getItem(2);
        if (iconStack == null || !iconStack.hasItemMeta()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());


        if (clickedStack.getItemMeta().getDisplayName().equals(
                LocaleManager.getInstance().getTranslation(civilian.getLocale(), "spells"))) {
            event.setCancelled(true);
            return;
        }

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            event.setCancelled(true);
            clickBackButton(event.getWhoClicked());
            return;
        }
    }

    //TODO make this more secure?
    @Override @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        super.onInventoryClose(event);
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
        for (String name : stashItems.keySet()) {
            CivItem item = ItemManager.getInstance().getItemType(name);
            if (item.getItemType().equals(CivItem.ItemType.CLASS)) {
                removeItems.add(name);
            }
        }
        for (String name : removeItems) {
            stashItems.remove(name);
        }
        for (ItemStack is : event.getInventory()) {
            if (!CVItem.isCivsItem(is)) {
                continue;
            }
            CivItem civItem = CivItem.getFromItemStack(is);
            if (stashItems.containsKey(civItem.getProcessedName())) {
                stashItems.put(civItem.getProcessedName(), is.getAmount() + stashItems.get(civItem.getProcessedName()));
            } else {
                stashItems.put(civItem.getProcessedName(), is.getAmount());
            }
        }
        CivilianManager.getInstance().saveCivilian(civilian);
    }

    public static Inventory createMenu(Civilian civilian) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(civilian.getStashItems().size()), MENU_NAME);

        int i=0;
        for (String currentName : civilian.getStashItems().keySet()) {
            CivItem civItem = ItemManager.getInstance().getItemType(currentName);
            if (!civItem.getItemType().equals(CivItem.ItemType.CLASS)) {
                continue;
            }
            CVItem cvItem = civItem.clone();
            List<String> lore = new ArrayList<>();
            lore.add(civilian.getUuid().toString());
            lore.addAll(Util.textWrap("", Util.parseColors(civItem.getDescription(civilian.getLocale()))));
//            lore.addAll(cvItem.getLore());
            cvItem.setLore(lore);
            cvItem.setQty(civilian.getStashItems().get(currentName));
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }

        return inventory;
    }
}

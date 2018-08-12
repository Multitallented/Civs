package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpellsMenu extends Menu {
    private static final String MENU_NAME = "CivsSpellStash";
    public SpellsMenu() {
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
        Civilian civilian = CivilianManager.getInstance()
                .getCivilian(UUID.fromString(iconStack.getItemMeta().getLore().get(0)));


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
        if (!event.getInventory().getTitle().equals(MENU_NAME)) {
            return;
        }

        ItemManager itemManager = ItemManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        ArrayList<CivItem> stashItems = civilian.getStashItems();
        ArrayList<CivItem> removeItems = new ArrayList<>();
        for (CivItem item : stashItems) {
            if (item.getItemType().equals(CivItem.ItemType.SPELL)) {
                removeItems.add(item);
            }
        }
        stashItems.removeAll(removeItems);
        for (ItemStack is : event.getInventory()) {
            if (is == null || !CVItem.isCivsItem(is)) {
                continue;
            }
            CivItem civItem = itemManager.getItemType(is.getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase());
            civItem.setQty(is.getAmount());
            stashItems.add(civItem);
        }
        CivilianManager.getInstance().saveCivilian(civilian);
    }

    public static Inventory createMenu(Civilian civilian) {

        int i=0;
        ArrayList<CVItem> spellList = new ArrayList<>();
        for (CivItem cvItem : civilian.getStashItems()) {
            if (!cvItem.getItemType().equals(CivItem.ItemType.SPELL)) {
                continue;
            }
            CVItem newItem = cvItem.clone();
            List<String> lore = new ArrayList<>();
            lore.add(civilian.getUuid().toString());
            lore.addAll(Util.textWrap("", Util.parseColors(cvItem.getDescription(civilian.getLocale()))));
//            lore.addAll(cvItem.getLore());
            newItem.setLore(lore);
            spellList.add(newItem);
            i++;
        }

        Inventory inventory = Bukkit.createInventory(null, getInventorySize(i+9), MENU_NAME);
        CVItem icon = CVItem.createCVItemFromString("STONE");
        icon.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "spells"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(civilian.getUuid().toString());
        icon.setLore(lore);
        inventory.setItem(2, icon.createItemStack());
        inventory.setItem(6, getBackButton(civilian));

        for (CVItem cvItem : spellList) {
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }

        return inventory;
    }
}

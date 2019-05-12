package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Alliance;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

public class AllianceMenu extends Menu {
    public static final String MENU_NAME = "CivsAlliance";

    public AllianceMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());


        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        String townName = event.getCurrentItem().getItemMeta().getDisplayName();
        if (townName.isEmpty()) {
            return;
        }
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            return;
        }
        event.getWhoClicked().closeInventory();
        event.getWhoClicked().openInventory(TownActionMenu.createMenu(civilian, town));
    }

    public static Inventory createMenu(Civilian civilian, Alliance alliance) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(alliance.getMembers().size()) + 9, MENU_NAME);

        //0 Icon
        {
            CVItem cvItem = CVItem.createCVItemFromString("GOLD_SWORD");
            cvItem.setDisplayName(alliance.getName());
            inventory.setItem(0, cvItem.createItemStack());
        }

        boolean isOwnerOfTown = false;
        for (String townName : alliance.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            if (town.getPeople().containsKey(civilian.getUuid()) &&
                    town.getPeople().get(civilian.getUuid()).equals("owner")) {
                isOwnerOfTown = true;
                break;
            }
        }

        //2 Rename
        if (isOwnerOfTown) {
            CVItem cvItem = CVItem.createCVItemFromString("PAPER");
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "rename-alliance"));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "rename-alliance-desc")
                    .replace("$1", alliance.getName()));
            inventory.setItem(2, cvItem.createItemStack());
        }

        //8 Back button
        inventory.setItem(8, getBackButton(civilian));

        int i=9;
        for (String townName : alliance.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.getLore().clear();
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }

        return inventory;
    }
}

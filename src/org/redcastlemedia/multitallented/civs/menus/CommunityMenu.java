package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;

public class CommunityMenu extends Menu {
    private static final String MENU_NAME = "CivsCommunity";
    public CommunityMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        if (!event.getClickedInventory().getTitle().equals(MENU_NAME)) {
            return;
        }
        event.setCancelled(true);

        ItemStack clickedStack = event.getCursor();
        if (clickedStack == null) {
            return;
        }
        if (clickedStack.getItemMeta() == null) {
            return;
        }
        ItemMeta im = clickedStack.getItemMeta();
        String itemName = im.getDisplayName();
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        String locale = civilian.getLocale();
//        if (itemName.equals(localeManager.getTranslation(locale, "language-menu"))) {

//        }
        //TODO finish this stub
    }

    public static Inventory createMenu(String locale) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        //TODO populate items here

        return inventory;
    }
}

package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.HashMap;

public class ShopLevelMenu extends Menu {
    public static final String MENU_NAME = "CivsShopLevel";

    public ShopLevelMenu() {
        super(MENU_NAME);
    }

    public static Inventory createMenu(Civilian civilian) {
        int size = getInventorySize(ConfigManager.getInstance().getLevelList().size()) + 9;
        Inventory inventory = Bukkit.createInventory(null, size, MENU_NAME);

        inventory.setItem(8, getBackButton(civilian));

        int i = 9;
        int level = 1;
        for (String matString : ConfigManager.getInstance().getLevelList()) {
            CVItem cvItem = CVItem.createCVItemFromString(matString);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "level").replace("$1", "" + level));
            ArrayList<String> lore = new ArrayList<>();
            lore.add("" + level);
            cvItem.setLore(lore);
            inventory.setItem(i, cvItem.createItemStack());
            i++;
            level++;
        }
        return inventory;
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack clickedStack = event.getCurrentItem();
        if (clickedStack == null || !clickedStack.hasItemMeta()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        if (isBackButton(clickedStack, civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        int level = Integer.parseInt(clickedStack.getItemMeta().getLore().get(0));
        event.getWhoClicked().closeInventory();
        HashMap<String, Integer> returnMap = new HashMap<>();
        for (CivItem civItem : ItemManager.getInstance().getItemsByLevel(level, true)) {
            returnMap.put(civItem.getProcessedName(), 1);
        }
        appendHistory(civilian.getUuid(), MENU_NAME);
        event.getWhoClicked().openInventory(RegionListMenu.createMenu(civilian, returnMap));
    }
}

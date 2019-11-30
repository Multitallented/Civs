package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.ArrayList;

public class LanguageMenu extends Menu {
    private static final String MENU_NAME = "CivsLang";
    public LanguageMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        ItemStack clickedStack = event.getCurrentItem();
        if (clickedStack == null || !clickedStack.hasItemMeta()) {
            return;
        }
        ItemMeta im = clickedStack.getItemMeta();
        String itemName = im.getDisplayName();
        LocaleManager localeManager = LocaleManager.getInstance();
        CivilianManager civilianManager = CivilianManager.getInstance();
        Civilian civilian = civilianManager.getCivilian(event.getWhoClicked().getUniqueId());

        String newLocale = im.getLore().get(0);
        civilian.setLocale(newLocale);
        civilianManager.saveCivilian(civilian);
        event.getWhoClicked().closeInventory();
        clearHistory(civilian.getUuid());
        event.getWhoClicked().sendMessage(Civs.getPrefix() +
                localeManager.getTranslation(newLocale, "language-set").replace("$1", itemName));
    }

    public static Inventory createMenu(String locale) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();
        int i=0;
        for (String currentLang : localeManager.getAllLanguages()) {

            ArrayList<String> lore = new ArrayList<>();
            lore.add(currentLang);
            CVItem cvItem = CVItem.createCVItemFromString(ChatColor.stripColor(localeManager.getTranslation(currentLang, "icon")));
            if (cvItem == null) {
                cvItem = new CVItem(Material.GRASS, i+1);
            }
            String name = localeManager.getTranslation(currentLang, "name");
            if (name == null) {
                name = "Error";
            }
            cvItem.setDisplayName(ChatColor.stripColor(name));
            cvItem.setLore(lore);
            inventory.setItem(i, cvItem.createItemStack());
            i++;
        }
        return inventory;
    }
}

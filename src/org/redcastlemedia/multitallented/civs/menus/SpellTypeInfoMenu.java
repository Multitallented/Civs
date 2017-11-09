package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class SpellTypeInfoMenu extends Menu {
    static String MENU_NAME = "CivSpellInfo";

    public SpellTypeInfoMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemManager itemManager = ItemManager.getInstance();
        String spellName = event.getInventory().getItem(0)
                .getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        SpellType spellType = (SpellType) itemManager.getItemType(spellName);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + spellName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ConfirmationMenu.createMenu(civilian, spellType));
            return;
        }

        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian, SpellType spellType) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        ItemManager itemManager = ItemManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Icon
        CVItem cvItem = spellType.clone();
        List<String> lore = new ArrayList<>();
//        lore.add(localeManager.getTranslation(civilian.getLocale(), "size") +
//                ": " + (spellType.getBuildRadiusX() * 2 + 1) + "x" + (spellType.getBuildRadiusZ() * 2 + 1) + "x" + (spellType.getBuildRadiusY() * 2 + 1));
        lore.addAll(Util.textWrap("", Util.parseColors(spellType.getDescription(civilian.getLocale()))));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        String itemName = spellType.getProcessedName();
        boolean hasShopPerms = Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop");
        boolean isAtMax = civilian.isAtMax(spellType);
        if (hasShopPerms && !isAtMax) {
            CVItem priceItem = CVItem.createCVItemFromString("EMERALD");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + spellType.getPrice());
            priceItem.setLore(lore);
            inventory.setItem(1, priceItem.createItemStack());
        }



        //8 back button
        inventory.setItem(8, getBackButton(civilian));


        //TODO finish this stub

        return inventory;
    }
}

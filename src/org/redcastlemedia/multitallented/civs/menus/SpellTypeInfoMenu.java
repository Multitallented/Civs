package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellTypeInfoMenu extends Menu {
    static String MENU_NAME = "CivSpellInfo";

    public SpellTypeInfoMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        SpellType spellType = (SpellType) getData(civilian.getUuid(), "spellType");
        String spellName = spellType.getProcessedName();

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

        LocaleManager localeManager = LocaleManager.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("spellType", spellType);
        setNewData(civilian.getUuid(), data);

        //0 Icon
        CVItem cvItem = spellType.clone();
        List<String> lore = new ArrayList<>();
        lore.addAll(Util.textWrap(Util.parseColors(spellType.getDescription(civilian.getLocale()))));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        String itemName = spellType.getProcessedName();
        boolean hasShopPerms = Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop");
        String maxLimit = civilian.isAtMax(spellType);
        lore = new ArrayList<>();
        if (hasShopPerms && maxLimit == null) {
            CVItem priceItem = CVItem.createCVItemFromString("EMERALD");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + spellType.getPrice());
            priceItem.setLore(lore);
            inventory.setItem(1, priceItem.createItemStack());
        } else if (hasShopPerms) {
            CVItem priceItem = CVItem.createCVItemFromString("BARRIER");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            int max = maxLimit.equals(spellType.getProcessedName()) ? spellType.getCivMax() :
                    ConfigManager.getInstance().getGroups().get(maxLimit);
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "max-item")
                    .replace("$1", maxLimit)
                    .replace("$2", "" + max));
        }



        //8 back button
        inventory.setItem(8, getBackButton(civilian));


        //TODO finish this stub

        return inventory;
    }
}

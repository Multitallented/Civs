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
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;

public class TownTypeInfoMenu extends Menu {
    static String MENU_NAME = "CivTownInfo";

    public TownTypeInfoMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemManager itemManager = ItemManager.getInstance();
        String townName = event.getInventory().getItem(0)
                .getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        TownType townType = (TownType) itemManager.getItemType(townName);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        /*if (event.getCurrentItem().getType().equals(Material.IRON_PICKAXE)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(townType.getReqs(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.CHEST)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(townType.getReagents(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.HOPPER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(townType.getInput(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.DISPENSER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RecipeMenu.createMenu(townType.getOutput(), event.getWhoClicked().getUniqueId(), event.getInventory().getItem(0)));
            return;
        }*/
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ConfirmationMenu.createMenu(civilian, townType));
            return;
        }

        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian, TownType townType) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        ItemManager itemManager = ItemManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Icon
        CVItem cvItem = townType.clone();
        List<String> lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "size") +
                ": " + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadiusY() * 2 + 1));
        lore.addAll(Util.parseColors(townType.getDescription()));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        String itemName = townType.getProcessedName();
        boolean hasShopPerms = Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop");
        boolean isAtMax = civilian.isAtMax(townType);
        if (hasShopPerms && !isAtMax) {
            CVItem priceItem = CVItem.createCVItemFromString("EMERALD");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + townType.getPrice());
            priceItem.setLore(lore);
            inventory.setItem(1, priceItem.createItemStack());
        }

        //2 Rebuild
        /*if (townType.getRebuild() != null) {
            CVItem rebuildItem = itemManager.getItemType(townType.getRebuild().toLowerCase()).clone();
            lore = new ArrayList<>();
//            lore.add();
            rebuildItem.setLore(lore);
            inventory.setItem(2, rebuildItem.createItemStack());
        }*/

        //3 Population/Charter
        //TODO population/charter

        //8 back button
        inventory.setItem(8, getBackButton(civilian));

        //9 build-reqs
        /*CVItem cvItem1 = CVItem.createCVItemFromString("IRON_PICKAXE");
        cvItem1.setDisplayName("Build Reqs");
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "build-reqs")
                .replace("$1", itemName));
        cvItem1.setLore(lore);
        inventory.setItem(9, cvItem1.createItemStack());*/

        //10 Expenses
        //TODO expenses

        //11 taxes
        //TODO taxes

        //TODO finish this stub

        return inventory;
    }
}

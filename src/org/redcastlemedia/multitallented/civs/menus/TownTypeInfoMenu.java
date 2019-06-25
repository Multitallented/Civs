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
import org.redcastlemedia.multitallented.civs.items.ItemManager;
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
        String townTypeName = event.getInventory().getItem(0)
                .getItemMeta().getDisplayName().replace(ConfigManager.getInstance().getCivsItemPrefix(), "").toLowerCase();
        TownType townType = (TownType) itemManager.getItemType(townTypeName);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.OAK_PLANKS)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townTypeName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionListMenu.createMenu(civilian, townType.getReqs(), 0));
            return;
        }
        /*if (event.getCurrentItem().getType().equals(Material.CHEST)) {
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
        }*/
        if (event.getCurrentItem().getType().equals(Material.IRON_PICKAXE)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townTypeName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionListMenu.createMenu(civilian, townType.getReqs(), 0));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.RED_WOOL)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townTypeName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionListMenu.createMenu(civilian, townType.getRegionLimits(), 0));
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.EMERALD)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townTypeName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ConfirmationMenu.createMenu(civilian, townType));
            return;
        }

        //TODO finish this stub
    }

    public static Inventory createMenu(Civilian civilian, TownType townType) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Icon
        CVItem cvItem = townType.clone();
        List<String> lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), "size") +
                ": " + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadius() * 2 + 1) + "x" + (townType.getBuildRadiusY() * 2 + 1));
        lore.addAll(Util.textWrap(Util.parseColors(townType.getDescription(civilian.getLocale()))));
        cvItem.setLore(lore);
        inventory.setItem(0, cvItem.createItemStack());

        //1 Price
        String itemName = townType.getProcessedName();
        boolean hasShopPerms = Civs.perm != null && Civs.perm.has(Bukkit.getPlayer(civilian.getUuid()), "civs.shop");
        String maxLimit = civilian.isAtMax(townType);
        if (hasShopPerms && maxLimit == null) {
            CVItem priceItem = CVItem.createCVItemFromString("EMERALD");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "price") + ": " + townType.getPrice());
            priceItem.setLore(lore);
            inventory.setItem(1, priceItem.createItemStack());
        } else if (hasShopPerms) {
            CVItem priceItem = CVItem.createCVItemFromString("BARRIER");
            priceItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "buy-item"));
            int max = maxLimit.equals(townType.getProcessedName()) ? townType.getCivMax() :
                    ConfigManager.getInstance().getGroups().get(maxLimit);
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "max-item")
                    .replace("$1", maxLimit)
                    .replace("$2", "" + max));
        }

        //2 Rebuild
        if (townType.getChild() != null) {
            CVItem rebuildItem = ItemManager.getInstance()
                    .getItemType(townType.getChild().toLowerCase()).clone();
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "rebuild-required")
                    .replace("$1", townType.getProcessedName())
                    .replace("$2", townType.getChild()));
            rebuildItem.setLore(lore);
            inventory.setItem(2, rebuildItem.createItemStack());
        }

        //3 Population/Charter
        if (townType.getChild() != null &&
                townType.getChildPopulation() > 0) {
            CVItem cvItem1 = CVItem.createCVItemFromString("PLAYER_HEAD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "population"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "population-req")
                    .replace("$1", townType.getChild())
                    .replace("$2", "" +townType.getChildPopulation()));
            cvItem1.setLore(lore);
            inventory.setItem(3, cvItem1.createItemStack());
        }
        //7 effects
        {
            CVItem cvItem1 = CVItem.createCVItemFromString("POTION");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "effects"));
            lore = new ArrayList<>(townType.getEffects().keySet());
            cvItem1.setLore(lore);
            inventory.setItem(7, cvItem1.createItemStack());
        }

        //8 back button
        inventory.setItem(8, getBackButton(civilian));

        //9 build-reqs
        {
            CVItem cvItem1 = CVItem.createCVItemFromString("OAK_PLANKS");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "build-reqs-title"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "build-reqs")
                    .replace("$1", itemName));
            cvItem1.setLore(lore);
            inventory.setItem(9, cvItem1.createItemStack());
        }

        //11 Expenses
        //TODO expenses?

        //12 taxes
        //TODO taxes?

        //16 Limits
        {
            CVItem cvItem1 = CVItem.createCVItemFromString("RED_WOOL");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "limits"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "click-for-region-limits"));
            cvItem1.setLore(lore);
            inventory.setItem(16, cvItem1.createItemStack());
        }

        //17 Create
        {
            CVItem cvItem1 = CVItem.createCVItemFromString("IRON_PICKAXE");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "create"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "click-for-req-regions"));
            lore.add(localeManager.getTranslation(civilian.getLocale(), "town-instructions"));
            cvItem1.setLore(lore);
            inventory.setItem(17, cvItem1.createItemStack());
        }

        //TODO finish this stub

        return inventory;
    }
}

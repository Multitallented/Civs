package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.items.FolderType;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.List;

public class ShopMenu extends Menu {
    static final String MENU_NAME = "CivShop";
    private CivItem parent = null;
    public ShopMenu() {
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
        String itemName = ChatColor.stripColor(im.getDisplayName());
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        if (isBackButton(clickedStack, civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        String history = MENU_NAME;
        if (itemName.equals(ChatColor.stripColor(LocaleManager.getInstance()
                .getTranslation(civilian.getLocale(), "sort-by-level")))) {
            event.getWhoClicked().closeInventory();
            appendHistory(civilian.getUuid(), history);
            event.getWhoClicked().openInventory(ShopLevelMenu.createMenu(civilian));
            return;
        }

        ItemManager itemManager = ItemManager.getInstance();
        if (im.getLore() == null || im.getLore().isEmpty()) {
            return;
        }
        itemName = im.getLore().get(0);
        itemName = CivItem.processItemName(itemName);
        CivItem civItem = itemManager.getItemType(itemName);
        if (civItem == null) {
            Civs.logger.severe("Error! Unable to find item " + itemName);
            return;
        }
        if (event.getInventory().getItem(0) != null) {
            String parentName = CivItem.processItemName(event.getInventory().getItem(0).getItemMeta().getDisplayName());
            history += "," + parentName;
        }
        if (civItem.getItemType().equals(CivItem.ItemType.FOLDER)) {
            appendHistory(civilian.getUuid(), history);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ShopMenu.createMenu(civilian, civItem));
            return;
        }
        if (civItem.getItemType().equals(CivItem.ItemType.TOWN)) {
            appendHistory(civilian.getUuid(), history);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownTypeInfoMenu.createMenu(civilian, (TownType) civItem));
            return;
        }
        if (civItem.getItemType().equals(CivItem.ItemType.REGION)) {
            appendHistory(civilian.getUuid(), history);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionTypeInfoMenu.createMenu(civilian, (RegionType) civItem));
            return;
        }
        if (civItem.getItemType().equals(CivItem.ItemType.SPELL)) {
            appendHistory(civilian.getUuid(), history);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(SpellTypeInfoMenu.createMenu(civilian, (SpellType) civItem));
            return;
        }
        if (civItem.getItemType().equals(CivItem.ItemType.CLASS)) {
            boolean hasClass = false;
            for (String currentName : civilian.getStashItems().keySet()) {
                if (currentName.equals(civItem.getProcessedName())) {
                    hasClass = true;
                    break;
                }
            }
            if (hasClass) {
                appendHistory(civilian.getUuid(), history);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(ShopMenu.createMenu(civilian, civItem));
                return;
            } else {
                appendHistory(civilian.getUuid(), history);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(ClassTypeInfoMenu.createMenu(civilian, (ClassType) civItem));
                return;
            }
        }
    }

    public static Inventory createMenu(Civilian civilian, CivItem parent) {
        ItemManager itemManager = ItemManager.getInstance();
        LocaleManager localeManager = LocaleManager.getInstance();
        List<CivItem> shopItems = itemManager.getShopItems(civilian, parent);
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(shopItems.size()) + 9, MENU_NAME);
        Player player = Bukkit.getPlayer(civilian.getUuid());

        if (parent != null) {
            inventory.setItem(0, parent.createItemStack());
        }

        CVItem cvItem = CVItem.createCVItemFromString("BOOKSHELF");
        cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "sort-by-level"));
        inventory.setItem(6, cvItem.createItemStack());

        inventory.setItem(8, getBackButton(civilian));

        int i=9;
        for (CivItem civItem : shopItems) {
            CVItem civItem1 = civItem.getShopIcon().clone();
            if (civItem.getItemType() == CivItem.ItemType.FOLDER) {
                FolderType folderType = (FolderType) civItem;
                if (!folderType.getVisible() &&
                        (Civs.perm == null || !Civs.perm.has(player, "civs.admin"))) {
                    continue;
                }
                civItem1.getLore().add(folderType.getDisplayName());
            }
            String maxLimit = civilian.isAtMax(civItem);
            if (civItem.getItemType() != CivItem.ItemType.FOLDER && maxLimit != null) {
                CVItem item = CVItem.createCVItemFromString("BARRIER");
                item.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                        civItem.getProcessedName() + "-name"));
                int limit = maxLimit.equals(civItem.getProcessedName()) ? civItem.getCivMax() :
                        ConfigManager.getInstance().getGroups().get(maxLimit);
                item.getLore().add(localeManager.getTranslation(civilian.getLocale(),
                        "max-item").replace("$1", maxLimit)
                            .replace("$2", limit + ""));
                item.getLore().addAll(Util.textWrap(Util.parseColors(civItem.getDescription(civilian.getLocale()))));
                inventory.setItem(i, item.createItemStack());
                i++;
                continue;
            }
            if (!civItem.getItemType().equals(CivItem.ItemType.FOLDER)) {
                civItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                        civItem.getProcessedName() + "-name"));
                civItem1.getLore().clear();
                civItem1.getLore().add(ChatColor.BLACK + civItem.getProcessedName());
                civItem1.getLore().add(localeManager.getTranslation(civilian.getLocale(), "price") +
                        ": " + Util.getNumberFormat(civItem.getPrice(), civilian.getLocale()));
                civItem1.getLore().addAll(Util.textWrap(Util.parseColors(civItem.getDescription(civilian.getLocale()))));
            }
            inventory.setItem(i, civItem1.createItemStack());
            i++;
        }

        return inventory;
    }
}

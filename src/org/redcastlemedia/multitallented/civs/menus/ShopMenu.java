package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

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
        String itemName = im.getDisplayName();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        if (itemName.equals(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "back-button"))) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        ItemManager itemManager = ItemManager.getInstance();
        if (!CVItem.isCivsItem(clickedStack)) {
            return;
        }
        CivItem civItem = itemManager.getItemType(itemName);
        if (civItem == null) {
            return;
        }
        String parentName = event.getInventory().getItem(0).getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        if (civItem.getItemType().equals(CivItem.ItemType.FOLDER)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + parentName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ShopMenu.createMenu(civilian, civItem));
            return;
        }
        if (civItem.getItemType().equals(CivItem.ItemType.REGION)) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + parentName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionTypeInfoMenu.createMenu(civilian, (RegionType) civItem));
            return;
        }
        if (civItem.getItemType().equals(CivItem.ItemType.SPELL)) {
            //TODO finish this stub
        }
        if (civItem.getItemType().equals(CivItem.ItemType.CLASS)) {
            boolean hasClass = false;
            for (CivItem civItem1 : civilian.getStashItems()) {
                if (civItem1.getDisplayName().equals(civItem.getDisplayName())) {
                    hasClass = true;
                    break;
                }
            }
            if (hasClass) {
                appendHistory(civilian.getUuid(), MENU_NAME + "," + parentName);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().openInventory(ShopMenu.createMenu(civilian, civItem));
                return;
            } else {
                //TODO open class info menu
            }
        }
    }

    public static Inventory createMenu(Civilian civilian, CivItem parent) {
        ItemManager itemManager = ItemManager.getInstance();
        List<CivItem> shopItems = itemManager.getShopItems(civilian, parent);
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(shopItems.size()) + 9, MENU_NAME);

        inventory.setItem(0, parent.createItemStack());
        inventory.setItem(8, getBackButton(civilian));

        int i=9;
        for (CivItem civItem : shopItems) {
            CivItem civItem1 = civItem.clone();
            civItem1.getLore().add(civilian.getUuid().toString());
            inventory.setItem(i, civItem1.createItemStack());
            i++;
        }

        return inventory;
    }
}

package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TownInviteMenu extends Menu {
    public static String MENU_NAME = "CivsTownInvites";

    public TownInviteMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta() || (event.getCurrentItem().getType() == Material.STONE &&
                event.getCurrentItem().getItemMeta().getDisplayName().startsWith("Icon"))) {
            return;
        }
        String itemName = event.getCurrentItem().getItemMeta().getDisplayName();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());
        int page = (int) getData(civilian.getUuid(), "page");
        String yourTownName = (String) getData(civilian.getUuid(), "townName");

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        LocaleManager localeManager = LocaleManager.getInstance();
        if (event.getCurrentItem().getType() == Material.EMERALD &&
                itemName.equals(localeManager.getTranslation(civilian.getLocale(), "next-button"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page + "," + yourTownName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownInviteMenu.createMenu(civilian, page + 1, yourTownName));
            return;
        }
        if (event.getCurrentItem().getType() == Material.REDSTONE &&
                itemName.equals(localeManager.getTranslation(civilian.getLocale(), "prev-button"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page + "," + yourTownName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownInviteMenu.createMenu(civilian, page - 1, yourTownName));
            return;
        }
        String townName = event.getCurrentItem().getItemMeta().getDisplayName();
        Town town = TownManager.getInstance().getTown(townName.toLowerCase());
        if (town != null) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + page + "," + yourTownName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(TownInviteConfirmationMenu.createMenu(civilian, townName));
            return;
        }
    }

    public static Inventory createMenu(Civilian civilian, int page, String townName) {
        List<String> invites = TownManager.getInstance().getTown(townName).getAllyInvites();

        Inventory inventory = Bukkit.createInventory(null, 45, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        //0 Prev button
        if (page > 0) {
            CVItem cvItem = CVItem.createCVItemFromString("REDSTONE");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "prev-button"));
            inventory.setItem(0, cvItem.createItemStack());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("page", page);
        data.put("townName", townName);
        setNewData(civilian.getUuid(), data);

        //6 Back button
        inventory.setItem(6, getBackButton(civilian));

        int startIndex = page * 36;
        //8 Next button
        if (startIndex + 36 < invites.size()) {
            CVItem cvItem1 = CVItem.createCVItemFromString("EMERALD");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "next-button"));
            inventory.setItem(8, cvItem1.createItemStack());
        }

        int i=9;
        for (int k=startIndex; k<invites.size() && k<startIndex+36; k++) {
            String currentTownName = invites.get(k);
            Town currentTown = TownManager.getInstance().getTown(currentTownName);
            TownType townType = (TownType) ItemManager.getInstance().getItemType(currentTown.getType());
            CVItem cvItem1 = townType.clone();
            cvItem1.setDisplayName(currentTownName);
            inventory.setItem(i, cvItem1.createItemStack());
            i++;
        }

        return inventory;
    }
}

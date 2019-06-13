package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class SelectGovTypeMenu extends Menu {
    public static final String MENU_NAME = "CivsSelectGovType";

    public SelectGovTypeMenu() {
        super(MENU_NAME);
    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Inventory inventory = Bukkit.createInventory(null, 27, MENU_NAME);

        inventory.setItem(8, getBackButton(civilian));

        int i=9;
        for (GovernmentType governmentType : GovernmentManager.getInstance().getGovermentTypes()) {
            if (governmentType == GovernmentType.COLONIALISM &&
                    TownManager.getInstance().getOwnedTowns(civilian).size() < 2) {
                continue;
            }
            Government government = GovernmentManager.getInstance().getGovernment(governmentType);
            inventory.setItem(i, government.getIcon(civilian.getLocale()).createItemStack());
            i++;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("town", town);
        setNewData(civilian.getUuid(), data);

        return inventory;
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        Town town = (Town) getData(civilian.getUuid(), "town");
        if (town.isGovTypeChangedToday()) {
            event.getWhoClicked().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "cant-change-gov-type-daily"));
            return;
        }

        String govName = event.getCurrentItem().getItemMeta().getLore().get(0).replace("Gov Type: ", "");
        GovernmentType governmentType = GovernmentType.valueOf(govName);


        GovernmentManager.getInstance().transitionGovernment(town, governmentType, false);
        Town owningTown = null;
        for (Town cTown : TownManager.getInstance().getOwnedTowns(civilian)) {
            if (cTown.equals(town) || cTown.getGovernmentType() == GovernmentType.COLONIALISM) {
                continue;
            }
            owningTown = cTown;
            break;
        }
        if (owningTown != null) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (player != null) {
                player.performCommand("cv colony " + town.getName() + " " + owningTown.getName());
            }
        }
        TownManager.getInstance().saveTown(town);

        clearHistory(civilian.getUuid());
        event.getWhoClicked().closeInventory();
        event.getWhoClicked().openInventory(TownActionMenu.createMenu(civilian, town));
    }
}

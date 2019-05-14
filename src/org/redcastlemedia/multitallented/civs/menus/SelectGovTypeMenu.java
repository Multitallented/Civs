package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class SelectGovTypeMenu extends Menu {
    public static final String MENU_NAME = "CivsSelectGovType";

    public SelectGovTypeMenu() {
        super(MENU_NAME);
    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Inventory inventory = Bukkit.createInventory(null, 27, MENU_NAME);

        inventory.setItem(8, getBackButton(civilian));

        int i=9;
        for (String govTypeString : ConfigManager.getInstance().getAllowedGovTypes()) {
            GovernmentType governmentType = GovernmentType.valueOf(govTypeString);
            inventory.setItem(i, Util.getGovermentTypeIcon(civilian, governmentType).createItemStack());
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

        String govName = event.getCurrentItem().getItemMeta().getLore().get(0).replace("Gov Type: ", "");
        GovernmentType governmentType = GovernmentType.valueOf(govName);


        for (UUID uuid : town.getPeople().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            Civilian civilian1 = CivilianManager.getInstance().getCivilian(uuid);
            String oldGovName = LocaleManager.getInstance().getTranslation(civilian1.getLocale(),
                    town.getGovernmentType().name().toLowerCase());
            String newGovName = LocaleManager.getInstance().getTranslation(civilian1.getLocale(),
                    governmentType.name().toLowerCase());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian1.getLocale(),
                    "gov-type-change").replace("$1", oldGovName).replace("$2", newGovName));
        }

        // TODO any other changes that need to be made

        if (governmentType == GovernmentType.COMMUNISM) {
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(uuid).equals("owner")) {
                    town.setPeople(uuid, "owner");
                }
            }
        }

        if (governmentType == GovernmentType.LIBERTARIAN ||
                governmentType == GovernmentType.LIBERTARIAN_SOCIALISM ||
                governmentType == GovernmentType.CYBERSYNACY) {
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(uuid).equals("owner")) {
                    town.setPeople(uuid, "member");
                }
            }
        }

        town.setColonialTown(null);
        // TODO set colonial town

        town.setGovernmentType(governmentType);
        TownManager.getInstance().saveTown(town);

        clearHistory(civilian.getUuid());
        event.getWhoClicked().closeInventory();
        event.getWhoClicked().openInventory(TownActionMenu.createMenu(civilian, town));
    }
}

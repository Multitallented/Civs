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

        String govName = event.getCurrentItem().getItemMeta().getLore().get(0).replace("Gov Type: ", "");
        GovernmentType governmentType = GovernmentType.valueOf(govName);


        for (UUID uuid : town.getPeople().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            Civilian civilian1 = CivilianManager.getInstance().getCivilian(uuid);
            String oldGovName = GovernmentManager.getInstance().getGovernment(town.getGovernmentType())
                    .getNames().get(civilian1.getLocale());
            String newGovName = GovernmentManager.getInstance().getGovernment(governmentType)
                    .getNames().get(civilian1.getLocale());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslation(civilian1.getLocale(), "gov-type-change")
                    .replace("$1", town.getName())
                    .replace("$2", oldGovName).replace("$3", newGovName));
        }

        // TODO any other changes that need to be made

        if (governmentType == GovernmentType.MERITOCRACY) {
            Util.promoteWhoeverHasMostMerit(town, false);
        }

        if (governmentType == GovernmentType.COMMUNISM) {
            HashSet<UUID> setThesePeople = new HashSet<>(town.getRawPeople().keySet());
            for (UUID uuid : setThesePeople) {
                town.setPeople(uuid, "owner");
            }
        }

        if (governmentType == GovernmentType.LIBERTARIAN ||
                governmentType == GovernmentType.LIBERTARIAN_SOCIALISM ||
                governmentType == GovernmentType.CYBERSYNACY) {
            HashSet<UUID> setThesePeople = new HashSet<>(town.getRawPeople().keySet());
            for (UUID uuid : setThesePeople) {
                town.setPeople(uuid, "member");
            }
        }
        if (town.getBankAccount() > 0 && Civs.econ != null &&
                (governmentType == GovernmentType.COMMUNISM ||
                governmentType == GovernmentType.ANARCHY ||
                governmentType == GovernmentType.LIBERTARIAN_SOCIALISM ||
                governmentType == GovernmentType.LIBERTARIAN)) {
            double size = town.getRawPeople().size();
            for (UUID uuid : town.getRawPeople().keySet()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer != null) {
                    Civs.econ.depositPlayer(offlinePlayer, town.getBankAccount() / size);
                }
            }
            town.setBankAccount(0);
        }

        if (governmentType == GovernmentType.COOPERATIVE ||
                governmentType == GovernmentType.CAPITALISM ||
                governmentType == GovernmentType.DEMOCRACY ||
                governmentType == GovernmentType.DEMOCRATIC_SOCIALISM) {
            town.setLastVote(System.currentTimeMillis());
        }

        town.getVotes().clear();
        town.setTaxes(0);
        town.setColonialTown(null);
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

        town.setGovernmentType(governmentType);
        TownManager.getInstance().saveTown(town);

        clearHistory(civilian.getUuid());
        event.getWhoClicked().closeInventory();
        event.getWhoClicked().openInventory(TownActionMenu.createMenu(civilian, town));
    }
}

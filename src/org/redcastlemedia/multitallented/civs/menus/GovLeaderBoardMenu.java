package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;

public class GovLeaderBoardMenu extends Menu {
    public static final String MENU_NAME = "CivGovLeaderboard";

    public GovLeaderBoardMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }
        String govTypeName = event.getCurrentItem().getItemMeta().getLore().get(0).replace("Gov Type: ", "");
        GovernmentType governmentType = GovernmentType.valueOf(govTypeName);
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        ArrayList<Town> towns = new ArrayList<>();
        for (Town town : TownManager.getInstance().getTowns()) {
            if (town.getGovernmentType().equals(governmentType)) {
                towns.add(town);
            }
        }

        appendHistory(civilian.getUuid(), MENU_NAME);
        event.getWhoClicked().closeInventory();
        event.getWhoClicked().openInventory(TownListMenu.createMenu(civilian, 0, towns));
    }

    public static Inventory createMenu(Civilian civilian) {

        HashMap<GovernmentType, Integer> govPower = new HashMap<>();
        for (Town town : TownManager.getInstance().getTowns()) {
            if (govPower.containsKey(town.getGovernmentType())) {
                govPower.put(town.getGovernmentType(), town.getPower() + govPower.get(town.getGovernmentType()));
            } else {
                govPower.put(town.getGovernmentType(), town.getPower());
            }
        }
        Inventory inventory = Bukkit.createInventory(null,
                getInventorySize(govPower.size()), MENU_NAME);
        ArrayList<GovernmentType> govTypeSortedArray = new ArrayList<>(govPower.keySet());
        govTypeSortedArray.sort(new Comparator<GovernmentType>() {
            @Override
            public int compare(GovernmentType o1, GovernmentType o2) {
                return govPower.get(o2).compareTo(govPower.get(o1));
            }
        });

        int i=0;
        for (GovernmentType governmentType : govTypeSortedArray) {
            CVItem icon = GovernmentManager.getInstance().getGovernment(governmentType)
                    .getIcon(civilian.getLocale(), false);
            icon.getLore().add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "points")
                    .replace("$1", "" + govPower.get(governmentType)));
            inventory.setItem(i, icon.createItemStack());
            i++;
        }

        return inventory;
    }
}

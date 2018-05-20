package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TownActionMenu extends Menu {
    public static final String MENU_NAME = "CivsTown";
    public TownActionMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        LocaleManager localeManager = LocaleManager.getInstance();

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        TownManager townManager = TownManager.getInstance();
        String townName = event.getInventory().getItem(0).getItemMeta().getDisplayName().split("@")[1];
        Town town = townManager.getTown(townName);
        //TODO add functionality for clicking some other action items

        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "view-members"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + townName);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ViewMembersMenu.createMenu(civilian, town));
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "add-member"))) {
            event.getWhoClicked().closeInventory();
            List<Player> people = new ArrayList<>();
            for (UUID uuid : town.getPeople().keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    people.add(player);
                }
            }
            event.getWhoClicked().openInventory(ListAllPlayersMenu.createMenu(civilian, "add", people, 0, town.getName()));
            return;
        }

    }

    public static Inventory createMenu(Civilian civilian, Town town) {
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);
        //TODO finish this stub

        LocaleManager localeManager = LocaleManager.getInstance();
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        //0 Icon
        CVItem cvItem = new CVItem(townType.getMat(), 1, townType.getDamage());
        cvItem.setDisplayName(town.getType() + "@" + town.getName());
        ArrayList<String> lore = new ArrayList<>();
        //TODO set lore
        inventory.setItem(0, cvItem.createItemStack());


        //1 Power
        CVItem cvItem1;
        //1 Is Working
        if (town.getPower() > 0) {
            cvItem1 = CVItem.createCVItemFromString("WOOL.5");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(),
                    "town-power").replace("$1", "" + town.getPower())
                    .replace("$2", "" + town.getMaxPower()));
        } else {
            cvItem1 = CVItem.createCVItemFromString("WOOL.14");
            //TODO show grace period
        }
        inventory.setItem(1, cvItem1.createItemStack());
//        if (hasReagents) {
//            cvItem1 = CVItem.createCVItemFromString("WOOL.5");
//            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "operation"));
//            lore = new ArrayList<>();
//            lore.add(localeManager.getTranslation(civilian.getLocale(), "region-working"));
//            cvItem1.setLore(lore);
//        } else {
//            cvItem1 = CVItem.createCVItemFromString("WOOL.14");
//            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "operation"));
//            lore = new ArrayList<>();
//            lore.add(localeManager.getTranslation(civilian.getLocale(), "region-not-working"));
//            cvItem1.setLore(lore);
//        }
//        inventory.setItem(1, cvItem1.createItemStack());

        //2 Location/Nation?
//        CVItem cvItem2 = CVItem.createCVItemFromString("WOOD_DOOR");
//        cvItem2.setDisplayName(town.getName());
//        lore = new ArrayList<>();
//        lore.add(localeManager.getTranslation(civilian.getLocale(), "region-in-town").replace("$1", town.getName()));
//        cvItem2.setLore(lore);
//        inventory.setItem(2, cvItem2.createItemStack());

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));
        //9 People
        if (town.getPeople().get(civilian.getUuid()).equals("owner")) {
            CVItem skull = CVItem.createCVItemFromString("SKULL_ITEM.3");
            skull.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "view-members"));
            inventory.setItem(9, skull.createItemStack());

            //10 Add person - works for people in region only
            CVItem skull2 = CVItem.createCVItemFromString("SKULL_ITEM.3");
            skull2.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "add-member"));
            inventory.setItem(10, skull2.createItemStack());
        }


        return inventory;
    }
}

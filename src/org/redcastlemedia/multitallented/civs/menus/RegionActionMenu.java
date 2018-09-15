package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.redcastlemedia.multitallented.civs.Civs;
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
import java.util.Set;
import java.util.UUID;

public class RegionActionMenu extends Menu {
    public static final String MENU_NAME = "CivsRegion";
    public RegionActionMenu() {
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
        RegionManager regionManager = RegionManager.getInstance();
        String locationString = event.getInventory().getItem(0).getItemMeta().getDisplayName().split("@")[1];
        System.out.println(Civs.getPrefix() + locationString);
        Location location = Region.idToLocation(locationString);
        System.out.println(location.getX() + ":" + location.getY() + ":" + location.getZ());
        Region region = regionManager.getRegionAt(location);

//        if (region == null) {
//            Set<Region> regionSet = RegionManager.getInstance().getContainingRegions(location,0);
//            for (Region r : regionSet) {
//
//            }
//        }
        if (region == null) {
            Civs.logger.severe("Unable to find region at " + locationString);
        }

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }
        //TODO add functionality for clicking some other action items

        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "view-members"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ViewMembersMenu.createMenu(civilian, region));
            return;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "region-type"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionTypeInfoMenu.createMenu(civilian, regionType, false));
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "destroy"))) {
            event.getWhoClicked().closeInventory();
            appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);
            event.getWhoClicked().openInventory(DestroyConfirmationMenu.createMenu(civilian, region));
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "add-member"))) {
            event.getWhoClicked().closeInventory();
            List<Player> people = new ArrayList<>();
            for (UUID uuid : region.getPeople().keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    people.add(player);
                }
            }
            event.getWhoClicked().openInventory(ListAllPlayersMenu.createMenu(civilian, "add", people, 0, region.getId()));
            return;
        }

    }

    public static Inventory createMenu(Civilian civilian, Region region) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Inventory inventory = Bukkit.createInventory(null, 18, MENU_NAME);
        //TODO finish this stub

        LocaleManager localeManager = LocaleManager.getInstance();
        RegionType regionType;
        try {
            regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        } catch (NullPointerException npe) {
            if (region == null) {
                Civs.logger.severe("Unable load null region");
                System.out.println("Unable load null region");
            } else {
                Civs.logger.severe("Unable to load region type " + region.getType());
                System.out.println("Unable to load region type " + region.getType());
            }
            return inventory;
        }
        //0 Icon
        CVItem cvItem = new CVItem(regionType.getMat(), 1);
        cvItem.setDisplayName(region.getType() + "@" + region.getId());
        ArrayList<String> lore;
        //TODO set lore
        inventory.setItem(0, cvItem.createItemStack());

        //1 Region Type button
        if (region.getOwners().contains(civilian.getUuid())) {
            CVItem cvItemType = regionType.clone();
            cvItemType.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "region-type"));
            inventory.setItem(1, cvItemType.createItemStack());
        }


        //2 Is Working
        CVItem cvItem1;
        if (region.hasUpkeepItems()) {
            cvItem1 = CVItem.createCVItemFromString("GREEN_WOOL");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "operation"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "region-working"));
            int nextUpkeep = region.getSecondsTillNextTick();
            if (nextUpkeep < 64 && nextUpkeep > 0) {
                cvItem1.setQty(nextUpkeep);
            }
            lore.add(localeManager.getTranslation(civilian.getLocale(), "cooldown")
                    .replace("$1", nextUpkeep + ""));
            cvItem1.setLore(lore);
        } else {
            cvItem1 = CVItem.createCVItemFromString("RED_WOOL");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "operation"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "region-not-working"));
            cvItem1.setLore(lore);
        }
        inventory.setItem(2, cvItem1.createItemStack());

        //3 Location/Town
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town != null) {
            CVItem cvItem2 = CVItem.createCVItemFromString("OAK_DOOR");
            cvItem2.setDisplayName(town.getName());
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "region-in-town").replace("$1", town.getName()));
            cvItem2.setLore(lore);
            inventory.setItem(3, cvItem2.createItemStack());
        }

        //4 Rebuild
        //TODO add rebuild

        //6 Destroy
        if (!regionType.getEffects().containsKey("indestructible") &&
                region.getPeople().containsKey(civilian.getUuid()) ||
                (Civs.perm != null && Civs.perm.has(player, "civs.admin"))) {
            CVItem destroy = CVItem.createCVItemFromString("BARRIER");
            destroy.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "destroy"));
            inventory.setItem(6, destroy.createItemStack());
        }

        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));
        //9 People
        if (Util.hasOverride(region, civilian, town) || (region.getPeople().get(civilian.getUuid()) != null &&
                region.getPeople().get(civilian.getUuid()).equals("owner"))) {
            CVItem skull = CVItem.createCVItemFromString("PLAYER_HEAD");
            skull.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "view-members"));
            inventory.setItem(9, skull.createItemStack());

            //10 Add person - works for people in region only
            CVItem skull2 = CVItem.createCVItemFromString("PLAYER_HEAD");
            skull2.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "add-member"));
            inventory.setItem(10, skull2.createItemStack());
        }


        return inventory;
    }
}

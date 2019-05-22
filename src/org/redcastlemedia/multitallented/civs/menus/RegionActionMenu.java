
package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.redcastlemedia.multitallented.civs.regions.effects.ForSaleEffect;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.text.NumberFormat;
import java.util.*;

public class RegionActionMenu extends Menu {
    public static final String MENU_NAME = "CivsRegion";
    public RegionActionMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null ||
                event.getCurrentItem().getItemMeta() == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        LocaleManager localeManager = LocaleManager.getInstance();
        Region region = (Region) getData(civilian.getUuid(), "region");

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

        if (event.getCurrentItem().getItemMeta().getDisplayName() != null &&
                event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "view-members"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + region.getId());
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(ViewMembersMenu.createMenu(civilian, region));
            return;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "region-type"))) {
            appendHistory(civilian.getUuid(), MENU_NAME + "," + region.getId());
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().openInventory(RegionTypeInfoMenu.createMenu(civilian, regionType, false));
            return;
        }
        if (event.getCurrentItem().getItemMeta().getDisplayName() != null &&
                event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(),
                        "destroy"))) {
            event.getWhoClicked().closeInventory();
            appendHistory(civilian.getUuid(), MENU_NAME + "," + region.getId());
            event.getWhoClicked().openInventory(DestroyConfirmationMenu.createMenu(civilian, region));
            return;
        }
        if (event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
            event.getWhoClicked().closeInventory();
            clearHistory(civilian.getUuid());
            event.getWhoClicked().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "use-sell-command"));
            return;
        }
        if (event.getCurrentItem().getType() == Material.EMERALD_ORE) {
            event.getWhoClicked().closeInventory();
            ((Player) event.getWhoClicked()).performCommand("cv sell");
            event.getWhoClicked().openInventory(RegionActionMenu.createMenu(civilian, region));
            return;
        }

        if (event.getCurrentItem().getType() == Material.EMERALD) {
            event.getWhoClicked().closeInventory();
            Player player = (Player) event.getWhoClicked();
            if (Civs.econ != null && Civs.econ.has(player, region.getForSale())) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "region-bought").replace("$1", region.getType())
                        .replace("$2", Util.getNumberFormat(region.getForSale(), civilian.getLocale())));

                Civs.econ.withdrawPlayer(player, region.getForSale());
                Civs.econ.depositPlayer(Bukkit.getOfflinePlayer(region.getPeople().keySet().iterator().next()), region.getForSale());
                region.getRawPeople().clear();
                region.getRawPeople().put(civilian.getUuid(), "owner");
                region.setForSale(-1);
                RegionManager.getInstance().saveRegion(region);
            } else {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "not-enough-money").replace("$1", "" + region.getForSale()));
            }
            clearHistory(civilian.getUuid());
            return;
        }

        if (event.getCurrentItem().getItemMeta().getDisplayName().equals(
                localeManager.getTranslation(civilian.getLocale(), "add-member"))) {
            event.getWhoClicked().closeInventory();
            List<Player> people = new ArrayList<>();
            HashMap<UUID, String> peopleMap = region.getPeople();
            for (UUID uuid : peopleMap.keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && !peopleMap.get(uuid).equals("ally")) {
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
            } else {
                Civs.logger.severe("Unable to load region type " + region.getType());
            }
            return inventory;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("region", region);
        setNewData(civilian.getUuid(), data);

        ArrayList<String> lore;

        //0 Icon
        {
            CVItem cvItem = regionType.getShopIcon().clone();
            lore = new ArrayList<>(Util.textWrap("", regionType.getDescription(civilian.getLocale())));
            cvItem.setLore(lore);
            inventory.setItem(0, cvItem.createItemStack());
        }

        //1 Region Type button
        {
            CVItem cvItemType = regionType.getShopIcon().clone();
            cvItemType.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "region-type"));
            lore = new ArrayList<>();
            lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "click-info"));
            cvItemType.setLore(lore);
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
            if (nextUpkeep < 65 && nextUpkeep > 1) {
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
                region.getPeople().get(civilian.getUuid()).contains("owner"))) {
            CVItem skull = CVItem.createCVItemFromString("PLAYER_HEAD");
            skull.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "view-members"));
            inventory.setItem(9, skull.createItemStack());

            //10 Add person - works for people in region only
            CVItem skull2 = CVItem.createCVItemFromString("PLAYER_HEAD");
            skull2.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "add-member"));
            inventory.setItem(10, skull2.createItemStack());

            int personCount = 0;
            for (String role : region.getRawPeople().values()) {
                if (role.contains("owner") || role.contains("member")) {
                    personCount++;
                }
            }
            if (personCount == 1 && regionType.getEffects().containsKey(ForSaleEffect.KEY)) {
                //11 Set sale
                CVItem emeraldBlock = CVItem.createCVItemFromString("EMERALD_BLOCK");
                emeraldBlock.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "sell-region"));
                if (region.getForSale() > -1) {
                    lore = new ArrayList<>();
                    lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-sale-set")
                        .replace("$1", region.getType())
                        .replace("$2", Util.getNumberFormat(region.getForSale(), civilian.getLocale())));
                    emeraldBlock.setLore(lore);
                }
                inventory.setItem(11, emeraldBlock.createItemStack());

                if (region.getForSale() != -1) {
                    //12 Cancel sale
                    CVItem emeraldOre = CVItem.createCVItemFromString("EMERALD_ORE");
                    emeraldOre.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            "cancel-sale"));
                    inventory.setItem(12, emeraldOre.createItemStack());
                }
            }
        }

        if (!region.getRawPeople().containsKey(civilian.getUuid()) && region.getForSale() != -1 &&
                !civilian.isAtMax(regionType)) {
            //13 Buy region button
            CVItem emerald = CVItem.createCVItemFromString("EMERALD");
            emerald.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "buy-region").replace("$1", region.getType())
                    .replace("$2", Util.getNumberFormat(region.getForSale(), civilian.getLocale())));
            inventory.setItem(13, emerald.createItemStack());
        }


        return inventory;
    }
}

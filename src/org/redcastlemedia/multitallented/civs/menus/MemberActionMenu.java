package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.text.NumberFormat;
import java.util.*;

public class MemberActionMenu extends Menu {
    public static final String MENU_NAME = "CivsMemAct";
    public MemberActionMenu() {
        super(MENU_NAME);
    }

    @Override
    void handleInteract(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getWhoClicked().getUniqueId());

        String locationString = "";
        if (getData(civilian.getUuid(), "region") != null) {
            Region region = (Region) getData(civilian.getUuid(), "region");
            locationString = region.getId();
        } else {
            Town town = (Town) getData(civilian.getUuid(), "town");
            locationString = town.getName();
        }
        UUID uuid = (UUID) getData(civilian.getUuid(), "uuid");

        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        Player cPlayer = Bukkit.getPlayer(civilian.getUuid());

        if (isBackButton(event.getCurrentItem(), civilian.getLocale())) {
            clickBackButton(event.getWhoClicked());
            return;
        }

//        clearHistory(civilian.getUuid());
//        appendHistory(civilian.getUuid(), MENU_NAME + "," + locationString);

        if (event.getCurrentItem().getType().equals(Material.GOLD_BLOCK)) {
            cPlayer.performCommand("cv setowner " + player.getName() + " " + locationString);
            clickBackButton(cPlayer);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.IRON_BLOCK)) {
            cPlayer.performCommand("cv setmember " + player.getName() + " " + locationString);
            clickBackButton(cPlayer);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.DIORITE)) {
            cPlayer.performCommand("cv setguest " + player.getName() + " " + locationString);
            clickBackButton(cPlayer);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.REDSTONE_BLOCK)) {
            cPlayer.performCommand("cv removemember " + player.getName() + " " + locationString + " " + uuid);
            clickBackButton(cPlayer);
            return;
        }
        if (event.getCurrentItem().getType().equals(Material.JUKEBOX)) {
            clearHistory(civilian.getUuid());
            cPlayer.closeInventory();
            Town town = TownManager.getInstance().getTown(locationString);
            if (town != null) {
                double price = ConfigManager.getInstance().getCapitalismVotingCost();
                if (town.getVotes().containsKey(civilian.getUuid())) {
                    if (Civs.econ == null || !Civs.econ.has(cPlayer, price)) {
                        cPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                                civilian.getLocale(), "not-enough-money")
                                .replace("$1", price + ""));
                        return;
                    }

                    Civs.econ.withdrawPlayer(cPlayer, price);

                    if (town.getVotes().get(civilian.getUuid()).containsKey(uuid)) {
                        town.getVotes().get(civilian.getUuid()).put(uuid,
                                town.getVotes().get(civilian.getUuid()).get(uuid) + 1);
                    } else {
                        town.getVotes().get(civilian.getUuid()).put(uuid, 1);
                    }
                } else {
                    HashMap<UUID, Integer> vote = new HashMap<>();
                    vote.put(uuid, 1);
                    town.getVotes().put(civilian.getUuid(), vote);
                }
                cPlayer.openInventory(TownActionMenu.createMenu(civilian, town));
                cPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "voted").replace("$1", player.getName()));
                TownManager.getInstance().saveTown(town);
                return;
            }
        }
    }

    private static void addItems(Inventory inventory, Civilian civilian, String role, boolean viewingSelf) {
        addItems(inventory, civilian, role, viewingSelf, GovernmentType.DICTATORSHIP, 0, true, true);
    }

    private static void addItems(Inventory inventory, Civilian civilian, String role, boolean viewingSelf,
                                 GovernmentType governmentType, double price, boolean isOwner, boolean alreadyVoted) {
        //8 Back Button
        inventory.setItem(8, getBackButton(civilian));
        LocaleManager localeManager = LocaleManager.getInstance();
        ArrayList<String> lore;

        Player player = Bukkit.getPlayer(civilian.getUuid());
        boolean isAdmin = player != null && (player.isOp() || (Civs.perm != null && Civs.perm.has(player, "civs.admin")));

        if (governmentType == GovernmentType.ANARCHY) {
            viewingSelf = false;
        }

        boolean isVoteOnly = !isOwner && (governmentType == GovernmentType.CAPITALISM ||
                governmentType == GovernmentType.COOPERATIVE ||
                governmentType == GovernmentType.DEMOCRACY ||
                governmentType == GovernmentType.DEMOCRATIC_SOCIALISM);

        boolean cantAddOwners = governmentType == GovernmentType.LIBERTARIAN ||
                governmentType == GovernmentType.LIBERTARIAN_SOCIALISM;

        //9 set owner
        if (isAdmin || ((!viewingSelf || governmentType == GovernmentType.OLIGARCHY) &&
                !isVoteOnly && !role.contains("owner") && !cantAddOwners)) {
            CVItem cvItem1 = CVItem.createCVItemFromString("GOLD_BLOCK");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-owner"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "owner-description"));
            if (governmentType == GovernmentType.OLIGARCHY && !isOwner) {
                String priceString = NumberFormat.getCurrencyInstance().format(price);
                lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "buy")
                        .replace("$1", priceString));
            }

            cvItem1.setLore(lore);
            inventory.setItem(9, cvItem1.createItemStack());
        }

        //10 set member
        if (isAdmin || (!viewingSelf && isOwner && !role.contains("member"))) {
            CVItem cvItem1 = CVItem.createCVItemFromString("IRON_BLOCK");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-member"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "member-description"));
//            if (governmentType == GovernmentType.OLIGARCHY && !isOwner) {
//                String priceString = NumberFormat.getCurrencyInstance().format(price);
//                lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "buy")
//                        .replace("$1", priceString));
//            }
            cvItem1.setLore(lore);
            inventory.setItem(10, cvItem1.createItemStack());
        }

        //11 set guest
        if (isAdmin || (isOwner && !viewingSelf && !role.contains("guest") && !cantAddOwners)) {
            CVItem cvItem1 = CVItem.createCVItemFromString("DIORITE");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "set-guest"));
            lore = new ArrayList<>();
            lore.add(localeManager.getTranslation(civilian.getLocale(), "guest-description"));
//            if (governmentType == GovernmentType.OLIGARCHY && !isOwner) {
//                String priceString = NumberFormat.getCurrencyInstance().format(price);
//                lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "buy")
//                        .replace("$1", priceString));
//            }
            cvItem1.setLore(lore);
            inventory.setItem(11, cvItem1.createItemStack());
        }

        //12 remove member
        if (viewingSelf || isOwner) {
            CVItem cvItem1 = CVItem.createCVItemFromString("REDSTONE_BLOCK");
            cvItem1.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "remove-member"));
//            if (governmentType == GovernmentType.OLIGARCHY && !isOwner) {
//                lore = new ArrayList<>();
//                String priceString = NumberFormat.getCurrencyInstance().format(price);
//                lore.add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "buy")
//                        .replace("$1", priceString));
//                cvItem1.setLore(lore);
//            }
            inventory.setItem(12, cvItem1.createItemStack());
        }

        //13 vote
        if ((governmentType == GovernmentType.DEMOCRACY ||
                governmentType == GovernmentType.DEMOCRATIC_SOCIALISM ||
                governmentType == GovernmentType.CAPITALISM ||
                governmentType == GovernmentType.COOPERATIVE) &&
                (governmentType == GovernmentType.CAPITALISM || !alreadyVoted)) {
            CVItem cvItem = CVItem.createCVItemFromString("JUKEBOX");
            cvItem.setDisplayName(localeManager.getTranslation(civilian.getLocale(), "vote-member"));
            if (governmentType == GovernmentType.CAPITALISM && alreadyVoted) {
                lore = new ArrayList<>();
                String votingCost = Util.getNumberFormat(ConfigManager.getInstance().getCapitalismVotingCost(), civilian.getLocale());
                lore.add(localeManager.getTranslation(civilian.getLocale(), "capitalism-voting-cost")
                        .replace("$1", votingCost));
                cvItem.setLore(lore);
            }
            inventory.setItem(13, cvItem.createItemStack());
        }
    }

    public static Inventory createMenu(Civilian civilian, Town town, UUID uuid, boolean viewingSelf, boolean isOwner) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(town.getPeople().size()) + 9, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("town", town);
        data.put("uuid", uuid);
        setNewData(civilian.getUuid(), data);

        ArrayList<String> lore;

        //1 Player
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String role = town.getPeople().get(uuid);
        ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta im = (SkullMeta) playerItem.getItemMeta();
        im.setDisplayName(player.getName());
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), town.getPeople().get(uuid)));
        im.setLore(lore);
        im.setOwningPlayer(player);
        playerItem.setItemMeta(im);
        inventory.setItem(1, playerItem);

        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        double price = 2* townType.getPrice();

        boolean alreadyVoted = town.getVotes().containsKey(civilian.getUuid()) &&
                !town.getVotes().get(civilian.getUuid()).isEmpty();

        addItems(inventory, civilian, role, viewingSelf, town.getGovernmentType(), price, isOwner, alreadyVoted);

        return inventory;
    }

    public static Inventory createMenu(Civilian civilian, Region region, UUID uuid, boolean viewingSelf) {
        Inventory inventory = Bukkit.createInventory(null, getInventorySize(region.getPeople().size()) + 9, MENU_NAME);

        LocaleManager localeManager = LocaleManager.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("region", region);
        data.put("uuid", uuid);
        setNewData(civilian.getUuid(), data);

        ArrayList<String> lore;

        //1 Player
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        String role = region.getPeople().get(uuid);
        ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta im = (SkullMeta) playerItem.getItemMeta();
        im.setDisplayName(player.getName());
        lore = new ArrayList<>();
        lore.add(localeManager.getTranslation(civilian.getLocale(), region.getPeople().get(uuid)));
        im.setLore(lore);
        im.setOwningPlayer(player);
        playerItem.setItemMeta(im);
        inventory.setItem(1, playerItem);

        addItems(inventory, civilian, role, viewingSelf);

        return inventory;
    }
}

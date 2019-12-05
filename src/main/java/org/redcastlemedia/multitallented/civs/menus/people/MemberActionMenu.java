package org.redcastlemedia.multitallented.civs.menus.people;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CivsMenu(name = "member-action") @SuppressWarnings("unused")
public class MemberActionMenu extends CustomMenu {
    private static final String REGION = "region";
    private static final String OWNER = "owner";
    private static final String MEMBER = "member";
    private static final String RECRUITER = "recruiter";
    private static final String GUEST = "guest";

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("uuid")) {
            data.put("uuid", UUID.fromString(params.get("uuid")));
        }
        if (params.containsKey("town")) {
            data.put("town", TownManager.getInstance().getTown(params.get("town")));
            data.put("key", params.get("town"));
        }
        if (params.containsKey(REGION)) {
            data.put(REGION, RegionManager.getInstance().getRegionById(params.get(REGION)));
            data.put("key", params.get(REGION));
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
        UUID uuid = (UUID) MenuManager.getData(civilian.getUuid(), "uuid");
        Region region = (Region) MenuManager.getData(civilian.getUuid(), REGION);
        if (town == null && region != null) {
            town = TownManager.getInstance().getTownAt(region.getLocation());
        }
        double price = 0;
        GovernmentType governmentType;
        String role = "";
        boolean alreadyVoted = true;
        if (town != null) {
            if (town.getRawPeople().containsKey(civilian.getUuid())) {
                role = town.getRawPeople().get(civilian.getUuid());
            }
            alreadyVoted = town.getVotes().containsKey(civilian.getUuid()) &&
                    !town.getVotes().get(civilian.getUuid()).isEmpty();
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            price = 2* townType.getPrice();
            governmentType = GovernmentManager.getInstance().getGovernment(town.getGovernmentType()).getGovernmentType();
        } else {
            governmentType = GovernmentType.DICTATORSHIP;
        }
        boolean viewingSelf = civilian.getUuid().equals(uuid);
        boolean isAdmin = player != null && (player.isOp() || (Civs.perm != null &&
                Civs.perm.has(player, "civs.admin")));

        if (governmentType == GovernmentType.ANARCHY) {
            viewingSelf = false;
        }
        boolean isOwner = false;
        if (region != null) {
            isOwner = region.getRawPeople().containsKey(civilian.getUuid()) &&
                    region.getRawPeople().get(uuid).contains(OWNER);
        } else if (town != null) {
            isOwner = town.getRawPeople().containsKey(civilian.getUuid()) &&
                    town.getRawPeople().get(uuid).contains(OWNER);
        }

        boolean isVoteOnly = !isOwner && (governmentType == GovernmentType.CAPITALISM ||
                governmentType == GovernmentType.COOPERATIVE ||
                governmentType == GovernmentType.DEMOCRACY ||
                governmentType == GovernmentType.DEMOCRATIC_SOCIALISM);

        boolean cantAddOwners = governmentType == GovernmentType.LIBERTARIAN ||
                governmentType == GovernmentType.LIBERTARIAN_SOCIALISM;

        if ("icon".equals(menuIcon.getKey())) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            String displayName = offlinePlayer.getName();
            if (displayName == null) {
                displayName = "Unknown";
            }
            CVItem cvItem = CVItem.createCVItemFromString(Material.PLAYER_HEAD.name());
            cvItem.setDisplayName(displayName);
            String rankString;
            if (region != null) {
                rankString = region.getPeople().get(uuid);
            } else if (town != null) {
                rankString = town.getPeople().get(uuid);
            } else {
                rankString = "";
            }
            String localizedRanks = getLocalizedRanks(rankString, civilian.getLocale());
            cvItem.getLore().add(localizedRanks);
            ItemStack itemStack = cvItem.createItemStack();
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(offlinePlayer);
                itemStack.setItemMeta(skullMeta);
            }
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("set-owner".equals(menuIcon.getKey())) {
            if (isOwner && viewingSelf) {
                return new ItemStack(Material.AIR);
            }
            if (isAdmin || ((!viewingSelf || governmentType == GovernmentType.OLIGARCHY || isOwner) &&
                    !isVoteOnly && !role.contains(OWNER) && !cantAddOwners)) {
                CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
                if (governmentType == GovernmentType.OLIGARCHY && !isOwner) {
                    String priceString = Util.getNumberFormat(price, civilian.getLocale());
                    cvItem.getLore().add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "buy")
                            .replace("$1", priceString));
                }
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("set-member".equals(menuIcon.getKey())) {
            if (viewingSelf && role.contains(MEMBER)) {
                return new ItemStack(Material.AIR);
            }
            if (!(isAdmin || (!viewingSelf && isOwner && !role.contains(MEMBER)))) {
                return new ItemStack(Material.AIR);
            }
        } else if ("set-guest".equals(menuIcon.getKey())) {
            if (viewingSelf && role.contains(GUEST)) {
                return new ItemStack(Material.AIR);
            }
            if (!(isAdmin || (isOwner && !viewingSelf && !role.contains(GUEST) && !cantAddOwners))) {
                return new ItemStack(Material.AIR);
            }
        } else if ("set-recruiter".equals(menuIcon.getKey())) {
            if (town == null) {
                return new ItemStack(Material.AIR);
            }
            if (viewingSelf && role.contains(RECRUITER)) {
                return new ItemStack(Material.AIR);
            }
            if (!(isAdmin || (isOwner && !viewingSelf && !role.contains(RECRUITER) && !cantAddOwners))) {
                return new ItemStack(Material.AIR);
            }
        } else if ("remove-member".equals(menuIcon.getKey())) {
            if (!(viewingSelf || isOwner)) {
                return new ItemStack(Material.AIR);
            }
        } else if ("vote".equals(menuIcon.getKey())) {
            if ((governmentType == GovernmentType.DEMOCRACY ||
                    governmentType == GovernmentType.DEMOCRATIC_SOCIALISM ||
                    governmentType == GovernmentType.CAPITALISM ||
                    governmentType == GovernmentType.COOPERATIVE) &&
                    (governmentType == GovernmentType.CAPITALISM || !alreadyVoted)) {
                CVItem cvItem = menuIcon.createCVItem(civilian.getLocale(), count);
                if (governmentType == GovernmentType.CAPITALISM && alreadyVoted) {
                    String votingCost = Util.getNumberFormat(ConfigManager.getInstance().getCapitalismVotingCost(), civilian.getLocale());
                    cvItem.getLore().add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "capitalism-voting-cost")
                            .replace("$1", votingCost));
                }
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    private String getLocalizedRanks(String rankString, String locale) {
        String localizedRanks = "";
        if (rankString.contains(OWNER)) {
            localizedRanks += LocaleManager.getInstance().getTranslation(locale, OWNER) + ", ";
        }
        if (rankString.contains(MEMBER)) {
            localizedRanks += LocaleManager.getInstance().getTranslation(locale, MEMBER) + ", ";
        }
        if (rankString.contains(GUEST)) {
            localizedRanks += LocaleManager.getInstance().getTranslation(locale, GUEST) + ", ";
        }
        if (rankString.contains(RECRUITER)) {
            localizedRanks += LocaleManager.getInstance().getTranslation(locale, RECRUITER) + ", ";
        }
        if (localizedRanks.length() > 0) {
            localizedRanks = localizedRanks.substring(0, localizedRanks.length() - 2);
        }
        return localizedRanks;
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if ("vote".equals(actionString)) {
            Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
            UUID uuid = (UUID) MenuManager.getData(civilian.getUuid(), "uuid");
            if (town == null) {
                return true;
            }
            double price = ConfigManager.getInstance().getCapitalismVotingCost();
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (town.getVotes().containsKey(civilian.getUuid())) {
                if (Civs.econ == null || !Civs.econ.has(player, price)) {
                    player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                            civilian.getLocale(), "not-enough-money")
                            .replace("$1", price + ""));
                    return true;
                }

                Civs.econ.withdrawPlayer(player, price);

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
            if (player != null) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "voted").replace("$1", player.getName()));
            }
            TownManager.getInstance().saveTown(town);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}

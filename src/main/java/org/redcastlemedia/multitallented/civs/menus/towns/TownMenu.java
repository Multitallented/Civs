package org.redcastlemedia.multitallented.civs.menus.towns;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.GovTransition;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = Constants.TOWN) @SuppressWarnings("unused")
public class TownMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey(Constants.TOWN)) {
            Town town = TownManager.getInstance().getTown(params.get(Constants.TOWN));
            data.put(Constants.TOWN, town);
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            data.put(Constants.TOWN_TYPE, townType);
            data.put("hardship", Util.getNumberFormat(town.getHardship(), civilian.getLocale()));
            data.put("worth", Util.getNumberFormat(town.getWorth(), civilian.getLocale()));
        }
        if (params.containsKey(Constants.SELECTED_TOWN)) {
            if (!params.containsKey(Constants.TOWN)) {
                Town town = TownManager.getInstance().getTown(params.get(Constants.SELECTED_TOWN));
                data.put(Constants.TOWN, town);
                data.put(Constants.TOWN_TYPE, ItemManager.getInstance().getItemType(town.getType()));
                Town selectedTown = TownManager.getInstance().isOwnerOfATown(civilian);
                data.put(Constants.SELECTED_TOWN, selectedTown);
                data.put("hardship", Util.getNumberFormat(town.getHardship(), civilian.getLocale()));
                data.put("worth", Util.getNumberFormat(town.getWorth(), civilian.getLocale()));
            } else {
                data.put(Constants.SELECTED_TOWN, TownManager.getInstance().getTown(params.get(Constants.SELECTED_TOWN)));
            }
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Town town = (Town) MenuManager.getData(civilian.getUuid(), Constants.TOWN);
        TownType townType = (TownType) MenuManager.getData(civilian.getUuid(), Constants.TOWN_TYPE);
        Town selectedTown = (Town) MenuManager.getData(civilian.getUuid(), Constants.SELECTED_TOWN);
        if (selectedTown == null) {
            selectedTown = TownManager.getInstance().isOwnerOfATown(civilian);
        }
        boolean isAllied = selectedTown != null && selectedTown != town &&
                AllianceManager.getInstance().isAllied(selectedTown, town);
        boolean isOwner = town.getPeople().get(civilian.getUuid()) != null &&
                town.getPeople().get(civilian.getUuid()).contains(Constants.OWNER);
        boolean isRecruiter = town.getPeople().get(civilian.getUuid()) != null &&
                town.getPeople().get(civilian.getUuid()).contains(Constants.RECRUITER);
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        boolean colonialOverride = OwnershipUtil.hasColonialOverride(town, civilian);
        boolean govTypeDisable = government.getGovernmentType() == GovernmentType.LIBERTARIAN ||
                government.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                government.getGovernmentType() == GovernmentType.CYBERSYNACY ||
                government.getGovernmentType() == GovernmentType.COMMUNISM;

        boolean govTypeOpenToAnyone = town.getRawPeople().containsKey(civilian.getUuid()) &&
                !town.getRawPeople().get(civilian.getUuid()).contains("foreign") &&
                (government.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                        government.getGovernmentType() == GovernmentType.LIBERTARIAN);
        boolean govTypeOwnerOverride =
                town.getRawPeople().containsKey(civilian.getUuid()) &&
                (government.getGovernmentType() == GovernmentType.OLIGARCHY ||
                government.getGovernmentType() == GovernmentType.COOPERATIVE ||
                government.getGovernmentType() == GovernmentType.DEMOCRACY ||
                government.getGovernmentType() == GovernmentType.DEMOCRATIC_SOCIALISM ||
                government.getGovernmentType() == GovernmentType.CAPITALISM);
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || townType == null) {
            return new ItemStack(Material.AIR);
        }
        boolean isAdmin = player.isOp() || (Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION));

        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = townType.getShopIcon(civilian.getLocale()).clone();
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("power-protected".equals(menuIcon.getKey())) {
            if (town.getPower() < 1) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()).replace("$1", "" + town.getPower())
                    .replace("$2", "" + town.getMaxPower()));
            // TODO power history
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("power-unprotected".equals(menuIcon.getKey())) {
            if (town.getPower() > 0) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()).replace("$1", "" + town.getPower())
                    .replace("$2", "" + town.getMaxPower()));
            cvItem.getLore().add(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getDesc()).replace("$1",
                    (TownManager.getInstance().getRemainingGracePeriod(town) / 1000) + ""));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("location".equals(menuIcon.getKey())) {
            if (town.getPeople().containsKey(civilian.getUuid())) {
                CVItem cvItem = menuIcon.createCVItem(player, count);
                World world = town.getLocation().getWorld();
                String worldName = world == null ? "null" : world.getName();
                cvItem.getLore().add(worldName + " " +
                        (int) town.getLocation().getX() + "x " +
                        (int) town.getLocation().getY() + "y " +
                        (int) town.getLocation().getZ() + "z");
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("set-ally".equals(menuIcon.getKey())) {
            if (selectedTown != null && selectedTown != town && !isAllied) {
                CVItem cvItem = menuIcon.createCVItem(player, count);
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                        menuIcon.getName()).replace("$1", town.getName()));
                cvItem.getLore().clear();
                cvItem.getLore().add(selectedTown.getName());
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("remove-ally".equals(menuIcon.getKey())) {
            if (!isAllied) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()).replace("$1", town.getName()));
            cvItem.getLore().clear();
            cvItem.getLore().add(selectedTown.getName());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("population".equals(menuIcon.getKey())) {
            if (isAdmin || (!govTypeDisable && (isOwner || govTypeOwnerOverride || colonialOverride))) {
                CVItem cvItem = menuIcon.createCVItem(player, count);
                cvItem.getLore().clear();
                cvItem.getLore().add(LocaleManager.getInstance().getTranslation(player,
                        menuIcon.getDesc())
                        .replace("$1", town.getPopulation() + "")
                        .replace("$2", town.getHousing() + "")
                        .replace("$3", town.getVillagers() + ""));
                if (town.getPopulation() >= town.getHousing()) {
                    cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                            "max-housing")));
                }
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("bounty".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()).replace("$1", town.getName()));
            cvItem.getLore().clear();
            int i=0;
            for (Bounty bounty : town.getBounties()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(bounty.getIssuer());
                cvItem.getLore().add(op.getName() + ": " + Util.getNumberFormat(bounty.getAmount(), civilian.getLocale()));
                if (i>5) {
                    break;
                }
                i++;
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("destroy".equals(menuIcon.getKey())) {
            if ((town.getPeople().containsKey(civilian.getUuid()) &&
                    town.getPeople().get(civilian.getUuid()).contains(Constants.OWNER) &&
                    government.getGovernmentType() != GovernmentType.ANARCHY &&
                    government.getGovernmentType() != GovernmentType.COMMUNISM &&
                    government.getGovernmentType() != GovernmentType.COLONIALISM) ||
                    (Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_PERMISSION))) {
                return super.createItemStack(civilian, menuIcon, count);
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("people".equals(menuIcon.getKey())) {
            if (isAdmin || (!govTypeDisable && (isOwner || govTypeOwnerOverride || colonialOverride))) {
                return super.createItemStack(civilian, menuIcon, count);
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("add-person".equals(menuIcon.getKey())) {
            if (govTypeOpenToAnyone || isOwner || colonialOverride || isAdmin || isRecruiter) {
                return super.createItemStack(civilian, menuIcon, count);
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("leave-town".equals(menuIcon.getKey())) {
            if (town.getRawPeople().containsKey(civilian.getUuid()) &&
                    !town.getRawPeople().get(civilian.getUuid()).contains("ally")) {
                return super.createItemStack(civilian, menuIcon, count);
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("alliance-invites".equals(menuIcon.getKey())) {
            if ((!govTypeDisable || government.getGovernmentType() == GovernmentType.COMMUNISM) &&
                    (isOwner || colonialOverride) && !town.getAllyInvites().isEmpty()) {
                return super.createItemStack(civilian, menuIcon, count);
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("government-type".equals(menuIcon.getKey())) {
            boolean canChangeGovType = town.getRawPeople().containsKey(civilian.getUuid()) &&
                    town.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER);
            CVItem cvItem = government.getIcon(civilian);
            if (canChangeGovType && !town.isGovTypeChangedToday()) {
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                cvItem.getLore().addAll(Util.textWrap(civilian,
                        LocaleManager.getInstance().getTranslation(player, "gov-type-changed-recently")));
                return cvItem.createItemStack();
            }
        } else if ("bank".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            String bankBalance = Util.getNumberFormat(town.getBankAccount(), civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(player,
                    menuIcon.getName()).replace("$1", bankBalance));
            cvItem.getLore().clear();
            if (town.getTaxes() > 0) {
                String taxString = Util.getNumberFormat(town.getTaxes(), civilian.getLocale());
                cvItem.getLore().add(LocaleManager.getInstance().getTranslation(player, "town-tax")
                        .replace("$1", taxString));
            }
            if (isOwner || colonialOverride) {
                if (government.getGovernmentType() != GovernmentType.COOPERATIVE &&
                        government.getGovernmentType() != GovernmentType.COMMUNISM &&
                        government.getGovernmentType() != GovernmentType.ANARCHY) {
                    cvItem.getLore().add(LocaleManager.getInstance().getTranslation(
                            player, "town-tax-desc")
                            .replace("$1", town.getName()));
                }
                cvItem.getLore().add(LocaleManager.getInstance().getTranslation(
                        player, "town-bank-desc")
                        .replace("$1", town.getName()));
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("join-revolt".equals(menuIcon.getKey())) {
            if (town.getRevolt().contains(civilian.getUuid())) {
                return new ItemStack(Material.AIR);
            }
            if (hasRevolt(town) && town.getRawPeople().containsKey(civilian.getUuid()) &&
                    town.getRawPeople().get(civilian.getUuid()).contains(Constants.MEMBER)) {
                CVItem costItem = CVItem.createCVItemFromString(ConfigManager.getInstance().getRevoltCost());
                CVItem cvItem = menuIcon.createCVItem(player, count);
                cvItem.getLore().clear();
                cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(
                        player, menuIcon.getDesc()).replace("$1", town.getName())
                        .replace("$2", "" + costItem.getQty()).replace("$3", costItem.getMat().name())));
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("leave-revolt".equals(menuIcon.getKey())) {
            if (!town.getRevolt().contains(civilian.getUuid())) {
                return new ItemStack(Material.AIR);
            }
            if (hasRevolt(town) && town.getRawPeople().containsKey(civilian.getUuid()) &&
                    town.getRawPeople().get(civilian.getUuid()).contains(Constants.MEMBER)) {
                CVItem cvItem = menuIcon.createCVItem(player, count);
                cvItem.getLore().clear();
                cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(
                        player, "cancel-revolt").replace("$1", town.getName())));
                cvItem.getLore().addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                        "revolt-display").replace("$1", town.getRevolt().size() + "")
                        .replace("$2", town.getRawPeople().size() + "")));
                ItemStack itemStack = cvItem.createItemStack();
                putActions(civilian, menuIcon, itemStack, count);
                return itemStack;
            } else {
                return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    private boolean hasRevolt(Town town) {
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        boolean hasRevolt = false;
        for (GovTransition govTransition : government.getTransitions()) {
            if (govTransition.getRevolt() > -1) {
                hasRevolt = true;
                break;
            }
        }
        return hasRevolt;
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        Town town = (Town) MenuManager.getData(civilian.getUuid(), Constants.TOWN);
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || town == null) {
            return true;
        }
        if ("ally".equals(actionString) || "unally".equals(actionString)) {
            Town selectedTown;
            Set<Town> towns = TownManager.getInstance().getOwnedTowns(civilian);
            if (towns.size() > 1) {
                String menuString = "menu:select-town?ally=" + "ally".equals(actionString) + "&allyTown=" + town.getName();
                return super.doActionAndCancel(civilian, menuString, clickedItem);
            } else {
                selectedTown = towns.iterator().next();
            }
            if ("ally".equals(actionString)) {
                if (selectedTown != null) {
                    AllianceManager.getInstance().sendAllyInvites(town, selectedTown, player);
                }
            } else {
                if (selectedTown == null) {
                    return true;
                }
                AllianceManager.getInstance().unAllyBroadcast(town, selectedTown);
            }
            MenuManager.getInstance().refreshMenu(civilian);
            return true;
        }
        if (actionString.equals("join-revolt")) {
            CVItem costItem = CVItem.createCVItemFromString(ConfigManager.getInstance().getRevoltCost());
            if (!player.getInventory().contains(costItem.createItemStack())) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        player, "item-cost").replace("$1", "" + costItem.getQty())
                        .replace("$2", costItem.getMat().name()));
                return true;
            }

            player.getInventory().removeItem(costItem.createItemStack());
            town.getRevolt().add(civilian.getUuid());
            TownManager.getInstance().saveTown(town);
            return true;
        } else if (actionString.equals("leave-revolt")) {
            town.getRevolt().remove(civilian.getUuid());
            TownManager.getInstance().saveTown(town);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}

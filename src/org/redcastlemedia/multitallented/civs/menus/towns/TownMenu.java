package org.redcastlemedia.multitallented.civs.menus.towns;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Bounty;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.menus.TownActionMenu;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TownMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("town")) {
            data.put("town", TownManager.getInstance().getTown(params.get("town")));
        }
        if (params.containsKey("selectedTown")) {
            data.put("selectedTown", params.get("selectedTown"));
        }
        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
        String selectedTownName = (String) MenuManager.getData(civilian.getUuid(), "selectedTown");
        Town selectedTown = null;
        if (selectedTownName != null) {
            selectedTown = TownManager.getInstance().getTown(selectedTownName);
        } else {
            selectedTown = TownManager.getInstance().isOwnerOfATown(civilian);
        }
        boolean isAllied = selectedTown != null && selectedTown != town &&
                AllianceManager.getInstance().isAllied(selectedTown, town);
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        boolean isOwner = town.getPeople().get(civilian.getUuid()) != null &&
                town.getPeople().get(civilian.getUuid()).contains("owner");
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        boolean colonialOverride = OwnershipUtil.hasColonialOverride(town, civilian);
        boolean govTypeDisable = government.getGovernmentType() == GovernmentType.LIBERTARIAN ||
                government.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                government.getGovernmentType() == GovernmentType.CYBERSYNACY ||
                government.getGovernmentType() == GovernmentType.COMMUNISM;

        boolean govTypeOpenToAnyone = town.getRawPeople().containsKey(civilian.getUuid()) &&
                !town.getRawPeople().get(civilian.getUuid()).contains("foreign") &&
                (government.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                        government.getGovernmentType() == GovernmentType.LIBERTARIAN ||
                        government.getGovernmentType() == GovernmentType.ANARCHY);
        boolean govTypeOwnerOverride = government.getGovernmentType() == GovernmentType.OLIGARCHY ||
                government.getGovernmentType() == GovernmentType.COOPERATIVE ||
                government.getGovernmentType() == GovernmentType.DEMOCRACY ||
                government.getGovernmentType() == GovernmentType.DEMOCRATIC_SOCIALISM ||
                government.getGovernmentType() == GovernmentType.CAPITALISM;
        Player player = Bukkit.getPlayer(civilian.getUuid());
        boolean isAdmin = player.isOp() || (Civs.perm != null && Civs.perm.has(player, "civs.admin"));

        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = townType.getShopIcon(civilian.getLocale()).clone();
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("power-protected".equals(menuIcon.getKey())) {
            if (town.getPower() < 1) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
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
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", "" + town.getPower())
                    .replace("$2", "" + town.getMaxPower()));
            cvItem.getLore().add(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getDesc()).replace("$1",
                    (TownManager.getInstance().getRemainingGracePeriod(town) / 1000) + ""));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("location".equals(menuIcon.getKey())) {
            if (town.getPeople().containsKey(civilian.getUuid())) {
                CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
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
                CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
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
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", town.getName()));
            cvItem.getLore().clear();
            cvItem.getLore().add(selectedTown.getName());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("population".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.getLore().clear();
            cvItem.getLore().add(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getDesc())
                    .replace("$1", town.getPopulation() + "")
                    .replace("$2", town.getHousing() + "")
                    .replace("$3", town.getVillagers() + ""));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("bounty".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
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
                    town.getPeople().get(civilian.getUuid()).contains("owner") &&
                    government.getGovernmentType() != GovernmentType.ANARCHY &&
                    government.getGovernmentType() != GovernmentType.COMMUNISM &&
                    government.getGovernmentType() != GovernmentType.COLONIALISM) ||
                    (Civs.perm != null && Civs.perm.has(player, "civs.admin"))) {
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
            if (govTypeOpenToAnyone || isOwner || colonialOverride || isAdmin) {
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
                    (isOwner || government.getGovernmentType() == GovernmentType.ANARCHY || colonialOverride) &&
                    !town.getAllyInvites().isEmpty()) {

                return super.createItemStack(civilian, menuIcon, count);
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("government-type".equals(menuIcon.getKey())) {
            CVItem cvItem = government.getIcon(civilian.getLocale()).clone();
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("bank".equals(menuIcon.getKey())) {
            if (!town.getRevolt().contains(civilian.getUuid())) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
            String bankBalance = Util.getNumberFormat(town.getBankAccount(), civilian.getLocale());
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getName()).replace("$1", bankBalance));
            cvItem.getLore().clear();
            if (town.getTaxes() > 0) {
                String taxString = Util.getNumberFormat(town.getTaxes(), civilian.getLocale());
                cvItem.getLore().add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "town-tax")
                        .replace("$1", taxString));
            }
            if (isOwner || colonialOverride) {
                if (government.getGovernmentType() != GovernmentType.COOPERATIVE &&
                        government.getGovernmentType() != GovernmentType.COMMUNISM &&
                        government.getGovernmentType() != GovernmentType.ANARCHY) {
                    cvItem.getLore().add(LocaleManager.getInstance().getTranslation(
                            civilian.getLocale(), "town-tax-desc")
                            .replace("$1", town.getName()));
                }
                cvItem.getLore().add(LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "town-bank-desc")
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
                    town.getRawPeople().get(civilian.getUuid()).contains("member")) {
                CVItem costItem = CVItem.createCVItemFromString(ConfigManager.getInstance().getRevoltCost());
                CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
                cvItem.getLore().clear();
                cvItem.getLore().addAll(Util.textWrap(LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), menuIcon.getDesc()).replace("$1", town.getName())
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
                    town.getRawPeople().get(civilian.getUuid()).contains("member")) {
                CVItem cvItem = menuIcon.createCVItem(civilian.getLocale());
                cvItem.getLore().clear();
                cvItem.getLore().addAll(Util.textWrap(LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(), "cancel-revolt").replace("$1", town.getName())));
                cvItem.getLore().addAll(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
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
        Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
        String townName = town.getName();
        Player player = Bukkit.getPlayer(civilian.getUuid());
        String selectedTownName = (String) MenuManager.getData(civilian.getUuid(), "selectedTown");
        Town selectedTown = null;
        if (selectedTownName != null) {
            selectedTown = TownManager.getInstance().getTown(selectedTownName);
        } else {
            selectedTown = TownManager.getInstance().isOwnerOfATown(civilian);
        }
        if (actionString.equals("ally")) {
            if (selectedTown == null) {
                return true;
            }
            town.getAllyInvites().add(selectedTown.getName());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "town-ally-request-sent").replace("$1", townName));
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(uuid).contains("owner")) {
                    Player pSend = Bukkit.getPlayer(uuid);
                    if (pSend != null && pSend.isOnline()) {
                        pSend.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                                "town-ally-request-sent").replace("$1", townName));
                    }
                }
            }
            return true;
        } else if (actionString.equals("unally")) {
            if (selectedTown == null) {
                return true;
            }
            AllianceManager.getInstance().unAlly(selectedTown, town);
            for (Player cPlayer : Bukkit.getOnlinePlayers()) {
                cPlayer.sendMessage(Civs.getPrefix() + ChatColor.RED + LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "town-ally-removed")
                        .replace("$1", selectedTown.getName())
                        .replace("$2", townName));
            }
            return true;
        } else if (actionString.equals("leave-town")) {
            // TODO leave confirmation menu
        } else if (actionString.equals("join-revolt")) {
            CVItem costItem = CVItem.createCVItemFromString(ConfigManager.getInstance().getRevoltCost());
            if (!player.getInventory().contains(costItem.createItemStack())) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                        civilian.getLocale(),
                        "item-cost").replace("$1", "" + costItem.getQty())
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

    @Override
    public String getFileName() {
        return "town";
    }
}

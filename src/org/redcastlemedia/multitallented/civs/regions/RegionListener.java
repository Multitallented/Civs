package org.redcastlemedia.multitallented.civs.regions;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownEvolveEvent;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.RegionTypeInfoMenu;
import org.redcastlemedia.multitallented.civs.towns.GovTypeBuff;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

public class RegionListener implements Listener {

    /**
     * If placing a region block, try to create a region
     * @param blockPlaceEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        RegionManager regionManager = RegionManager.getInstance();
        if (blockPlaceEvent.getBlockPlaced().getState() == null) {
            return;
        }

        if (ConfigManager.getInstance().getBlackListWorlds()
                .contains(blockPlaceEvent.getBlockPlaced().getLocation().getWorld().getName())) {
            return;
        }

        if (!blockPlaceEvent.getItemInHand().hasItemMeta()) {
            return;
        }
        ItemStack heldItem = blockPlaceEvent.getItemInHand();

        if (!CVItem.isCivsItem(heldItem)) {
            return;
        }
        CivItem civItem = ItemManager.getInstance().getItemType(heldItem.getItemMeta().getDisplayName());

        if (civItem.getItemType() == CivItem.ItemType.TOWN) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(blockPlaceEvent.getPlayer().getUniqueId());
            blockPlaceEvent.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                    civilian.getLocale(), "cant-place-town"));
            return;
        }

        if (civItem.getItemType() != CivItem.ItemType.REGION) {
            return;
        }
        regionManager.detectNewRegion(blockPlaceEvent);
    }

    /**
     * Open region info menu if right clicking air with region
     * @param event
     */
    @EventHandler
    public void onRegionInfo(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (event.getAction() != Action.RIGHT_CLICK_AIR || !CVItem.isCivsItem(heldItem)) {
            return;
        }
        CivItem civItem = ItemManager.getInstance().getItemType(heldItem.getItemMeta().getDisplayName());
        if (civItem.getItemType() != CivItem.ItemType.REGION) {
            return;
        }
        RegionType regionType = (RegionType) civItem;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        player.openInventory(RegionTypeInfoMenu.createMenu(civilian, regionType));
    }

    @EventHandler
    public void onRegionCreatedEvent(RegionCreatedEvent event) {
        Town town = TownManager.getInstance().getTownAt(event.getRegion().getLocation());

        if (town == null) {
            return;
        }
        Util.checkMerit(town, event.getPlayer());
        if (Civs.econ == null) {
            return;
        }
        applyCostBuff(event, town);
        if (town.getGovernmentType() != GovernmentType.COOPERATIVE ||
                !event.getRegionType().getGroups().contains("utility")) {
            return;
        }
        double price = event.getRegionType().getPrice();
        price = Math.min(price, town.getBankAccount());
        Player player = Bukkit.getPlayer(event.getRegion().getRawPeople().keySet().iterator().next());
        if (player == null) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Civs.econ.depositPlayer(player, price);
        town.setBankAccount(town.getBankAccount() - price);
        TownManager.getInstance().saveTown(town);
        String priceString = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(civilian.getLocale()))
                .format(price);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "town-assist-price").replace("$1", priceString)
                .replace("$2", event.getRegion().getType()));
    }

    private void applyCostBuff(RegionCreatedEvent event, Town town) {
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (government == null) {
            return;
        }
        GovTypeBuff buff = null;
        for (GovTypeBuff cBuff : government.getBuffs()) {
            if (cBuff.getBuffType() != GovTypeBuff.BuffType.COST) {
                continue;
            }
            buff = cBuff;
        }
        if (buff == null) {
            return;
        }
        boolean applies = false;
        if (buff.getRegions().contains(event.getRegion().getType())) {
            applies = true;
        } else {
            for (String groupName : buff.getGroups()) {
                if (event.getRegionType().getGroups().contains(groupName)) {
                    applies = true;
                    break;
                }
            }
        }
        if (!applies) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        double amount = event.getRegionType().getPrice() * (double) buff.getAmount() / 100;
        String amountString = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(civilian.getLocale()))
                .format(amount);
        Civs.econ.depositPlayer(event.getPlayer(), amount);
        event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                civilian.getLocale(), "cost-buff"
        ).replace("$1", amountString)
                .replace("$2", event.getRegionType().getDisplayName())
                .replace("$3", government.getNames().get(civilian.getLocale())));
    }
}

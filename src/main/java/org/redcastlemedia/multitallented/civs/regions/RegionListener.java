package org.redcastlemedia.multitallented.civs.regions;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionCreatedEvent;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.GovTypeBuff;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsSingleton
public class RegionListener implements Listener {

    public static void getInstance() {
        RegionListener regionListener = new RegionListener();
        Bukkit.getPluginManager().registerEvents(regionListener, Civs.getInstance());
    }

    /**
     * If placing a region block, try to create a region
     * @param blockPlaceEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
        RegionManager regionManager = RegionManager.getInstance();

        if (!blockPlaceEvent.getItemInHand().hasItemMeta()) {
            return;
        }
        ItemStack heldItem = blockPlaceEvent.getItemInHand();

        if (!CVItem.isCivsItem(heldItem)) {
            return;
        }
        if (ConfigManager.getInstance().getBlackListWorlds()
                .contains(blockPlaceEvent.getBlockPlaced().getWorld().getName())) {
            blockPlaceEvent.setCancelled(true);
            blockPlaceEvent.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                    blockPlaceEvent.getPlayer(), LocaleConstants.PERMISSION_DENIED));
            return;
        }
        CivItem civItem = CivItem.getFromItemStack(heldItem);

        if (civItem.getItemType() == CivItem.ItemType.TOWN) {
            blockPlaceEvent.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                    blockPlaceEvent.getPlayer(), "cant-place-town"));
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
        CivItem civItem = CivItem.getFromItemStack(heldItem);
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civItem.getItemType() == CivItem.ItemType.TOWN) {
            TownType townType = (TownType) civItem;
            MenuManager.clearHistory(civilian.getUuid());
            HashMap<String, String> params = new HashMap<>();
            params.put(Constants.TOWN_TYPE, townType.getProcessedName());
            MenuManager.getInstance().openMenu(player, "town-type", params);
            return;
        }

        if (civItem.getItemType() == CivItem.ItemType.REGION) {
            RegionType regionType = (RegionType) civItem;
            MenuManager.clearHistory(player.getUniqueId());
            MenuManager.clearHistory(civilian.getUuid());
            HashMap<String, String> params = new HashMap<>();
            params.put(Constants.REGION_TYPE, regionType.getProcessedName());
            params.put(Constants.INFINITE_BOUNDING_BOX, "true");
            MenuManager.getInstance().openMenu(player, "region-type", params);
        }
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
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (government.getGovernmentType() != GovernmentType.COOPERATIVE ||
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
        String priceString = Util.getNumberFormat(price, civilian.getLocale());
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
        String amountString = Util.getNumberFormat(amount, civilian.getLocale());
        Civs.econ.depositPlayer(event.getPlayer(), amount);
        event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                civilian.getLocale(), "cost-buff"
        ).replace("$1", amountString)
                .replace("$2", event.getRegionType().getDisplayName())
                .replace("$3", LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        government.getName().toLowerCase() + "-name")));
    }
}

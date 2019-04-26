package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.redcastlemedia.multitallented.civs.BlockLogger;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.Menu;
import org.redcastlemedia.multitallented.civs.menus.RegionActionMenu;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.PlaceHook;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class CivilianListener implements Listener {

    private static CivilianListener civilianListener;

    public CivilianListener() {
        civilianListener = this;
    }

    public static CivilianListener getInstance() {
        if (civilianListener == null) {
            new CivilianListener();
        }
        return civilianListener;
    }

    @EventHandler
    public void onCivilianJoin(PlayerJoinEvent event) {
        CivilianManager civilianManager = CivilianManager.getInstance();
        civilianManager.loadCivilian(event.getPlayer());
        ConfigManager configManager = ConfigManager.getInstance();
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        outer: if (configManager.getUseStarterBook()) {
            CVItem cvItem = CVItem.createCVItemFromString("WRITTEN_BOOK");
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "starter-book"));

            ItemStack stack = cvItem.createItemStack();
            for (ItemStack is : player.getInventory()) {
                if (is == null || is.getType() != Material.WRITTEN_BOOK || !is.hasItemMeta()
                        || !is.getItemMeta().getDisplayName().equals(
                                LocaleManager.getInstance().getTranslation(civilian.getLocale(), "starter-book"))) {
                    continue;
                }
                break outer;
            }
            player.getInventory().addItem(stack);
        }
    }

    @EventHandler
    public void onCivilianQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);

        if (civilian.isInCombat() && ConfigManager.getInstance().getCombatLogPenalty() > 0) {
            int penalty = (int) (player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() *
                    ConfigManager.getInstance().getCombatLogPenalty() / 100);
            if (civilian.getLastDamager() != null) {
                Player damager = Bukkit.getPlayer(civilian.getLastDamager());
                if (damager != null && damager.isOnline()) {
                    player.damage(penalty);
                }
            } else {
                player.damage(penalty);
            }
        }
//        civilianManager.unloadCivilian(player);
        CommonScheduler.lastRegion.remove(uuid);
        CommonScheduler.lastTown.remove(uuid);
        Menu.clearHistory(uuid);
        TownManager.getInstance().clearInvite(uuid);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCivilianGainExp(PlayerExpChangeEvent event) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        civilian.setExpOrbs(event.getAmount());
    }

    @EventHandler(ignoreCancelled = true)
    public void onCivilianUseExp(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        if (civilian.getMana() < 1 || civilian.getMana() > 99) {
            return;
        }
        Material mat = event.getClickedBlock().getType();
        if (mat == Material.ANVIL ||
                mat == Material.ENCHANTING_TABLE) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                    civilian.getLocale(), "mana-use-exp"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCivilianDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        if (!ConfigManager.getInstance().getAllowSharingCivsItems() &&
                item.getItemStack().getItemMeta() != null &&
                item.getItemStack().getItemMeta().getDisplayName() != null &&
                item.getItemStack().getItemMeta().getDisplayName().contains("Civs ")) {
            item.remove();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCivilianBlockPlace(BlockPlaceEvent event) {
        ItemStack is = new ItemStack(event.getBlockPlaced().getType(), 1);
        if (!CVItem.isCivsItem(is)) {
            return;
        }
        String itemTypeName = is.getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        CivItem civItem = ItemManager.getInstance().getItemType(itemTypeName);
        if (!civItem.isPlaceable()) {
            event.setCancelled(true);
            return;
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onCivilianDispense(BlockDispenseEvent event) {
        ItemStack is = event.getItem();
        if (!CVItem.isCivsItem(is)) {
            return;
        }
        String itemTypeName = is.getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase();
        CivItem civItem = ItemManager.getInstance().getItemType(itemTypeName);
        if (!civItem.isPlaceable()) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onStarterBookClick(PlayerInteractEvent event) {
        if (event.getItem() == null ||
                (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) ||
                event.getItem().getType() != Material.WRITTEN_BOOK ||
                !event.getItem().hasItemMeta()) {
            return;
        }
        Player player = event.getPlayer();
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (!localeManager.getTranslation(civilian.getLocale(), "starter-book").equals(event.getItem().getItemMeta().getDisplayName())) {
            return;
        }
        event.setCancelled(true);
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            player.performCommand("cv");
            return;
        }
        Block block = event.getClickedBlock();
        Region region = RegionManager.getInstance().getRegionAt(block.getLocation());
        if (region == null) {
            Set<Region> regionSet = RegionManager.getInstance().getContainingRegions(block.getLocation(), 0);
            for (Region r : regionSet) {
                player.openInventory(RegionActionMenu.createMenu(civilian, r));
                return;
            }
            player.performCommand("cv");
            return;
        }
        player.openInventory(RegionActionMenu.createMenu(civilian, region));
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = true)
    public void onCivilianBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        BlockLogger blockLogger = BlockLogger.getInstance();
        CVItem cvItem = blockLogger.getBlock(location);
        if (cvItem == null) {
            return;
        }
        UUID uuid = null;
        if (cvItem.getLore() != null && cvItem.getLore().size() > 0) {
            uuid = UUID.fromString(cvItem.getLore().get(0));
        }
        blockLogger.removeBlock(event.getBlock().getLocation());
        Region region = RegionManager.getInstance()
                .getRegionById(Region.locationToString(event.getBlock().getLocation()));
        if (region != null) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            boolean cancelled = ProtectionHandler.removeRegionIfNotIndestructible(region, regionType, event);
            if (cancelled) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        }
        cvItem.setQty(1);
        if (!ConfigManager.getInstance().getAllowSharingCivsItems() ||
                uuid == null || cvItem.getMat() != event.getBlock().getType() ||
                !uuid.equals(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        } else {
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
            ItemStack itemStack = cvItem.createItemStack();
            Player player = event.getPlayer();
            int firstEmptyIndex = player.getInventory().firstEmpty();
            if (firstEmptyIndex > -1) {
                player.getInventory().setItem(firstEmptyIndex, itemStack);
            } else {
                Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
                CivItem civItem = ItemManager.getInstance().getItemType(cvItem.getDisplayName());
                if (civilian.getStashItems().containsKey(civItem.getProcessedName())) {
                    civilian.getStashItems().put(civItem.getProcessedName(),
                            civilian.getStashItems().get(civItem.getProcessedName()) + 1);
                } else {
                    civilian.getStashItems().put(civItem.getProcessedName(), 1);
                }
                CivilianManager.getInstance().saveCivilian(civilian);
            }
//            location.getWorld().dropItemNaturally(location, itemStack);
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
//        ItemStack is = event.getBlock().getState().getData().toItemStack(1);
        ItemStack is = event.getItemInHand();
        if (event.getPlayer() == null || !CVItem.isCivsItem(is)) {
            return;
        }
        CivItem civItem = ItemManager.getInstance().getItemType(is.getItemMeta().getDisplayName().replace("Civs ", "").toLowerCase());
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        if (!civItem.isPlaceable()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "not-allowed-place").replace("$1", civItem.getDisplayName()));
            return;
        }
        CVItem cvItem = CVItem.createFromItemStack(is);
        if (cvItem.getLore() == null || cvItem.getLore().isEmpty()) {
            ArrayList<String> lore = new ArrayList<>();
            lore.add(civilian.getUuid().toString());
            lore.add(cvItem.getDisplayName());
            lore.addAll(Util.textWrap("", Util.parseColors(civItem.getDescription(civilian.getLocale()))));
            cvItem.setLore(lore);
        }
        BlockLogger blockLogger = BlockLogger.getInstance();
        blockLogger.putBlock(event.getBlock().getLocation(), cvItem);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveEvent(InventoryMoveItemEvent event) {
        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
            return;
        }
        if (!CVItem.isCivsItem(event.getItem())) {
            return;
        }
        if (!(event.getDestination() instanceof PlayerInventory)) {
            event.setCancelled(true);
            if (!event.getSource().getViewers().isEmpty()) {
                HumanEntity humanEntity = event.getSource().getViewers().get(0);
                Civilian civilian = CivilianManager.getInstance().getCivilian(humanEntity.getUniqueId());
                humanEntity.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "prevent-civs-item-share"));
            }
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (!"PlaceholderAPI".equals(event.getPlugin().getName())) {
            return;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHook().register();
        }
    }

    /*@EventHandler
    public void onCivilianClickItem(InventoryClickEvent event) {
        if (!CVItem.isCivsItem(event.getCurrentItem()) || event.getClickedInventory().getTitle().startsWith("Civ")) {
            return;
        }
        HumanEntity humanEntity = event.getWhoClicked();
        ItemStack clickedStack = event.getCurrentItem();
        String uuidString;
        try {
            uuidString = clickedStack.getItemMeta().getLore().get(0);
        } catch (Exception e) {
            Civs.logger.warning("Unable to find Civs Item UUID");
            return;
        }
        if (!ConfigManager.getInstance().getAllowSharingCivsItems() &&
                CVItem.isCivsItem(clickedStack) &&
                !humanEntity.getUniqueId().toString().equals(uuidString)) {
            event.setCancelled(true);
            Civilian civilian = CivilianManager.getInstance().getCivilian(humanEntity.getUniqueId());
            humanEntity.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "prevent-civs-item-share"));
            return;
        }
    }*/
}

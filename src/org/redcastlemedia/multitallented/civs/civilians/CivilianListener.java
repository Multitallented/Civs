package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
import org.redcastlemedia.multitallented.civs.menus.*;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.PlaceHook;
import org.redcastlemedia.multitallented.civs.util.StructureUtil;
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
        if (configManager.getUseStarterBook()) {
            boolean hasStarterBook = false;
            for (ItemStack is : player.getInventory()) {
                if (is != null && Util.isStarterBook(is)) {
                    hasStarterBook = true;
                    break;
                }
            }
            if (!hasStarterBook) {
                ItemStack stack = Util.createStarterBook(civilian.getLocale());
                player.getInventory().addItem(stack);
            }
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
        StructureUtil.removeBoundingBox(uuid);
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
                item.getItemStack().getItemMeta().getDisplayName().contains(ConfigManager.getInstance().getCivsItemPrefix())) {
            item.remove();
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            String itemName = item.getItemStack().getItemMeta().getDisplayName()
                    .replace(ConfigManager.getInstance().getCivsItemPrefix(), "").toLowerCase();
            if (civilian.getStashItems().containsKey(itemName)) {
                civilian.getStashItems().put(itemName, civilian.getStashItems().get(itemName) + 1);
            } else {
                civilian.getStashItems().put(itemName, 1);
            }
            CivilianManager.getInstance().saveCivilian(civilian);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onCivilianBlockPlace(BlockPlaceEvent event) {
        ItemStack is = new ItemStack(event.getBlockPlaced().getType(), 1);
        if (!CVItem.isCivsItem(is)) {
            return;
        }
        String itemTypeName = is.getItemMeta().getDisplayName()
                .replace(ConfigManager.getInstance().getCivsItemPrefix(), "").toLowerCase();
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
        String itemTypeName = is.getItemMeta().getDisplayName()
                .replace(ConfigManager.getInstance().getCivsItemPrefix(), "").toLowerCase();
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
        if (ConfigManager.getInstance().getBlackListWorlds()
                .contains(event.getPlayer().getWorld().getName())) {
            return;
        }
        Player player = event.getPlayer();
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (!localeManager.getTranslation(civilian.getLocale(), "starter-book")
                .equals(event.getItem().getItemMeta().getDisplayName()) &&
                !localeManager.getTranslation("en", "starter-book")
                .equals(event.getItem().getItemMeta().getDisplayName())) {
            return;
        }
        event.setCancelled(true);
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            player.performCommand("cv");
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            player.performCommand("cv");
            return;
        }
        Location location = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        Region region = RegionManager.getInstance().getRegionAt(location);
        if (region == null) {
            Set<Region> regionSet = RegionManager.getInstance().getContainingRegions(block.getLocation(), 0);
            for (Region r : regionSet) {
                player.openInventory(RegionActionMenu.createMenu(civilian, r));
                return;
            }
            player.performCommand("cv");
            return;
        }
        StructureUtil.showGuideBoundingBox(player, region.getLocation(), region);
        player.openInventory(RegionActionMenu.createMenu(civilian, region));
    }

    @EventHandler(priority=EventPriority.LOWEST, ignoreCancelled = true)
    public void onCivilianBlockBreak(BlockBreakEvent event) {
        boolean shouldCancel = shouldCancelBlockBreak(event.getBlock(), event.getPlayer());
        if (shouldCancel) {
            event.setCancelled(true);
        }
    }

    public boolean shouldCancelBlockBreak(Block block, Player player) {
        Location location = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        BlockLogger blockLogger = BlockLogger.getInstance();
        CVItem cvItem = blockLogger.getBlock(location);
        if (cvItem == null) {
            return false;
        }
        UUID uuid = null;
        if (cvItem.getLore() != null && cvItem.getLore().size() > 0) {
            uuid = UUID.fromString(ChatColor.stripColor(cvItem.getLore().get(0)));
        }
        blockLogger.removeBlock(block.getLocation());
//        Region region = RegionManager.getInstance()
//                .getRegionById(Region.blockLocationToString(block.getLocation()));
//        if (region != null) {
//            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
//            boolean cancelled = ProtectionHandler.removeRegionIfNotIndestructible(region, regionType, event);
//            if (cancelled) {
//                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
//                player.sendMessage(Civs.getPrefix() +
//                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
//            }
//        }
        cvItem.setQty(1);
        if (player != null && (!ConfigManager.getInstance().getAllowSharingCivsItems() ||
                uuid == null || cvItem.getMat() != block.getType() ||
                !uuid.equals(player.getUniqueId()))) {
            block.setType(Material.AIR);
        } else {
            block.setType(Material.AIR);
            ItemStack itemStack = cvItem.createItemStack();
            int firstEmptyIndex = player == null ? -1 : player.getInventory().firstEmpty();
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
        return true;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
//        ItemStack is = event.getBlock().getState().getData().toItemStack(1);
        ItemStack is = event.getItemInHand();
        if (event.getPlayer() == null || !CVItem.isCivsItem(is)) {
            return;
        }
        CivItem civItem = ItemManager.getInstance().getItemType(is.getItemMeta().getDisplayName()
                .replace(ConfigManager.getInstance().getCivsItemPrefix(), "").toLowerCase());
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

    // for hoppers and the like
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

//    @EventHandler(ignoreCancelled = true)
//    public void onItemMoveEvent(InventoryMoveItemEvent event) {
//        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
//            return;
//        }
//        if (!CVItem.isCivsItem(event.getItem())) {
//            return;
//        }
//        event.setCancelled(true);
//    }

    @EventHandler(ignoreCancelled = true)
    public void onCivilianDragItem(InventoryDragEvent event) {
        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
            return;
        }
        ItemStack dragged = event.getOldCursor();

//        if (checkMoveNormalItems(event)) {
//            return;
//        }

        if (!CVItem.isCivsItem(dragged) ||
                event.getInventory().getTitle().startsWith("Civ")) {
            return;
        }

        int inventorySize = event.getInventory().getSize();
        for (int i : event.getRawSlots()) {
            if (i < inventorySize) {
                event.setCancelled(true);
                HumanEntity humanEntity = event.getWhoClicked();
                Civilian civilian = CivilianManager.getInstance().getCivilian(humanEntity.getUniqueId());
                humanEntity.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "prevent-civs-item-share"));
                return;
            }
        }
    }

    private boolean checkMoveNormalItems(InventoryDragEvent event) {
        if (!event.getView().getTitle().equals(BlueprintsMenu.MENU_NAME) &&
                !event.getView().getTitle().equals(SpellsMenu.MENU_NAME) &&
                !event.getView().getTitle().equals(ClassMenu.MENU_NAME)) {
            return false;
        }
        if (CVItem.isCivsItem(event.getOldCursor())) {
            return false;
        }
        event.setCancelled(true);
        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCivilianClickItem(InventoryClickEvent event) {
        if (ConfigManager.getInstance().getAllowSharingCivsItems()) {
            return;
        }
        boolean shiftClick = event.getClick().isShiftClick() && event.getClickedInventory() != null &&
                event.getClickedInventory().equals(event.getWhoClicked().getInventory());
        boolean dragToChest = event.getClickedInventory() != null &&
                !event.getClickedInventory().equals(event.getWhoClicked().getInventory());

        ItemStack stackInQuestion = shiftClick ? event.getCurrentItem() : event.getCursor();

        if (stackInQuestion == null || (!shiftClick && !dragToChest)) {
            return;
        }

//        if (checkMoveNormalItems(event, stackInQuestion)) {
//            return;
//        }

        if (!CVItem.isCivsItem(stackInQuestion) || event.getClickedInventory().getTitle().startsWith("Civ")) {
            return;
        }
        HumanEntity humanEntity = event.getWhoClicked();
        event.setCancelled(true);
        Civilian civilian = CivilianManager.getInstance().getCivilian(humanEntity.getUniqueId());
        humanEntity.sendMessage(Civs.getPrefix() +
                LocaleManager.getInstance().getTranslation(civilian.getLocale(), "prevent-civs-item-share"));
    }

    private boolean checkMoveNormalItems(InventoryClickEvent event, ItemStack stackInQuestion) {
        if (!event.getView().getTitle().equals(BlueprintsMenu.MENU_NAME) &&
                !event.getView().getTitle().equals(SpellsMenu.MENU_NAME) &&
                !event.getView().getTitle().equals(ClassMenu.MENU_NAME)) {
            return false;
        }
        if (CVItem.isCivsItem(stackInQuestion)) {
            return false;
        }
        event.setCancelled(true);
        return true;
    }
}

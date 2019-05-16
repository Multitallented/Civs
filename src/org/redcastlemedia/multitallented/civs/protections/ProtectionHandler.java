package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.RecipeMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProtectionHandler implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        RegionManager regionManager = RegionManager.getInstance();
        Location location = Region.idToLocation(Region.blockLocationToString(event.getBlock().getLocation()));
        boolean adminOverride = event.getPlayer().getGameMode() != GameMode.SURVIVAL ||
                (Civs.perm != null && Civs.perm.has(event.getPlayer(), "civs.admin"));
        boolean setCancelled = event.isCancelled() || shouldBlockAction(event.getBlock(), event.getPlayer(), "block_break");
        if (setCancelled && !adminOverride) {
            event.setCancelled(true);
        }
        if (event.isCancelled()) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
        if (!event.isCancelled()) {
            Region region = regionManager.getRegionAt(location);
            if (region == null) {
                return;
            }
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (Util.equivalentLocations(region.getLocation(), location)) {
                removeRegionIfNotIndestructible(region, regionType, event);
                return;
            }
            boolean containsReq = false;
            outer: for (List<CVItem> reqList : regionType.getReqs()) {
                for (CVItem item : reqList) {
                    if (item.getMat().equals(event.getBlock().getType())) {
                        containsReq = true;
                        break outer;
                    }
                }
            }
            if (!containsReq) {
                return;
            }
            Player player = event.getPlayer();
            boolean isNotMember = player == null ||
                    (!region.getOwners().contains(player.getUniqueId()) &&
                    !region.getPeople().containsKey(player.getUniqueId()));
            if (isNotMember && !region.hasRequiredBlocks()) {
                removeRegionIfNotIndestructible(region, regionType, event);
            }
            if (isNotMember) {
                return;
            }
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            int[] radii = Region.hasRequiredBlocksOnCenter(regionType, region.getLocation());
            if (radii.length == 0) {
                List<HashMap<Material, Integer>> missingBlocks = Region.hasRequiredBlocks(region.getType(),
                        region.getLocation(),
                        new ItemStack(event.getBlock().getType(), 1));
                if (missingBlocks != null && !missingBlocks.isEmpty()) {
                    event.setCancelled(true);
                    player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            "broke-own-region").replace("$1", region.getType()));
                    player.openInventory(RecipeMenu.createMenu(missingBlocks, player.getUniqueId(), regionType.createItemStack()));
                    return;
                }
            }
        }
    }

    public static boolean removeRegionIfNotIndestructible(Region region, RegionType regionType, BlockBreakEvent event) {
        if (regionType.getEffects().containsKey("indestructible")) {
            event.setCancelled(true);
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            return true;
        } else {
            if (Civs.econ != null &&
                    region.getRawPeople().containsKey(event.getPlayer().getUniqueId()) &&
                    region.getRawPeople().get(event.getPlayer().getUniqueId()).contains("owner")) {
                double salvage = regionType.getPrice() / 2;
                Civs.econ.depositPlayer(event.getPlayer(), salvage);
            }
            RegionManager.getInstance().removeRegion(region, true, true);
            return false;
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL ||
                (Civs.perm != null && Civs.perm.has(event.getPlayer(), "civs.admin"))) {
            return;
        }
        boolean setCancelled = event.isCancelled() || shouldBlockAction(event.getBlockPlaced(), event.getPlayer(), "block_build");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (!event.getBlock().getType().equals(Material.CAKE)) {
            return;
        }
        boolean setCancelled = event.isCancelled() || shouldBlockAction(event.getBlock(), event.getPlayer(), "block_break");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.AIR ||
                !ConfigManager.getInstance().isCheckWaterSpread()) {
            return;
        }
        boolean shouldTakeActionFrom = shouldBlockAction(event.getBlock(), null, "block_liquid");
        boolean shouldTakeActionTo = shouldBlockAction(event.getToBlock(), null, "block_liquid");
        boolean setCancelled = event.isCancelled() || (!shouldTakeActionFrom && shouldTakeActionTo);
        if (setCancelled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEndermanPickup(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.ENDERMAN) {
            return;
        }
        boolean setCancelled = event.isCancelled() || shouldBlockAction(event.getBlock().getLocation(), "block_break");
        if (setCancelled) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getIgnitingBlock() == null) {
            return;
        }
        boolean setCancelled = event.isCancelled() || shouldBlockAction(event.getIgnitingBlock(), event.getPlayer(), "block_fire");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        boolean setCancelled = event.isCancelled() || shouldBlockAction(event.getBlock(), event.getPlayer(), "block_break");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        boolean allProtected = shouldBlockAction(event.getBlock(), null, "block_build");
        for (Block block : event.getBlocks()) {
            boolean checkLocation = shouldBlockAction(block, null, "block_build");
            if (!checkLocation) {
                allProtected = false;
            }
            if (checkLocation && !allProtected) {
                event.setCancelled(true);
                break;
            }
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onPaintingPlace(HangingPlaceEvent event) {
        boolean setCancelled = event.isCancelled() ||
                shouldBlockAction(event.getBlock(), event.getPlayer(), "block_build");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }

    private void onPaintingBreak(HangingBreakByEntityEvent event) {
        Player player = null;
        if (event.getRemover() instanceof Player) {
            player = (Player) event.getRemover();
        }
        boolean setCancelled = event.isCancelled() || shouldBlockAction(event.getEntity().getLocation(), player, "block_break");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && player != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakEvent(HangingBreakEvent event) {
        if (event instanceof HangingBreakByEntityEvent) {
            onPaintingBreak((HangingBreakByEntityEvent) event);
            return;
        }
        shouldBlockAction(event.getEntity().getLocation(), null, "block_break");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityLight(BlockIgniteEvent event) {
        Location location = null;
        if (event.getIgnitingBlock() != null) {
            location = event.getIgnitingBlock().getLocation();
        } else if (event.getIgnitingEntity() != null) {
            location = event.getIgnitingEntity().getLocation();
        } else {
            return;
        }
        boolean shouldDeny = shouldBlockAction(location, "block_fire");
        if (!event.isCancelled() && shouldDeny) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled() && !ConfigManager.getInstance().getExplosionOverride()) {
            return;
        }
        boolean setCancelled = !event.isCancelled() &&
                shouldBlockActionEffect(event.getLocation(), null, "block_explosion", 5);
        if (setCancelled) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Creeper) {
            setCancelled = !event.isCancelled() && shouldBlockActionEffect(event.getLocation(), null, "block_creeper", 5);
        } else if (event.getEntity() instanceof Fireball) {
            setCancelled = !event.isCancelled() && shouldBlockActionEffect(event.getLocation(), null, "block_ghast", 5);
        } else if (event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            Player player = null;
            if (tnt.getSource() instanceof Player) {
                player = (Player) tnt.getSource();
            }
            setCancelled = !event.isCancelled() && shouldBlockActionEffect(event.getLocation(), player, "block_tnt", 5);
            if (setCancelled && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
            if (shouldBlockActionEffect(event.getLocation(), player, "power_shield", 0)) {
                Town town = TownManager.getInstance().getTownAt(event.getLocation());
                if (town != null) {
                    int powerReduce = 1;
                    TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
                    if (townType.getEffects().get("power_shield") != null) {
                        powerReduce = Integer.parseInt(townType.getEffects().get("power_shield"));
                    }
                    TownManager.getInstance().setTownPower(town, town.getPower() - powerReduce);
                    setCancelled = true;
                }
            }
        }
        if (setCancelled) {
            event.setCancelled(true);
            return;
        }

        final Location location = event.getLocation();
        CheckRegionBlocks checkRegionBlocks = new CheckRegionBlocks(location);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), checkRegionBlocks, 1L);
    }

    class CheckRegionBlocks implements Runnable {
        private final Location location;
        CheckRegionBlocks(Location location) {
            this.location = location;
        }
        @Override
        public void run() {
            RegionManager regionManager = RegionManager.getInstance();
            Set<Region> tempArray = new HashSet<>();
            for (Region region : regionManager.getContainingRegions(location, 5)) {
                RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                if (Region.hasRequiredBlocksOnCenter(regionType, region.getLocation()).length == 0 &&
                        Region.hasRequiredBlocks(region.getType(), region.getLocation()).length == 0) {
                    tempArray.add(region);
                }
            }
            for (Region region : tempArray) {
                regionManager.removeRegion(region, true, true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        boolean cancel = shouldBlockAction(event.getBlockClicked().getLocation(), event.getPlayer(), "block_build");
        if (cancel) {
            event.setCancelled(true);
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketFillEvent event) {
        boolean cancel = shouldBlockAction(event.getBlockClicked().getLocation(), event.getPlayer(), "block_break");
        if (cancel) {
            event.setCancelled(true);
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }
        handleInteract(event.getBlock(), null, event);
    }

    private void handleInteract(Block clickedBlock, Player player, Cancellable event) {
        if (clickedBlock == null || clickedBlock.getType() == Material.CRAFTING_TABLE) {
            return;
        }
        Material mat = clickedBlock.getType();
        if (mat == Material.OAK_DOOR ||
                mat == Material.BIRCH_DOOR ||
                mat == Material.SPRUCE_DOOR ||
                mat == Material.JUNGLE_DOOR ||
                mat == Material.DARK_OAK_DOOR ||
                mat == Material.ACACIA_DOOR ||
                mat == Material.OAK_TRAPDOOR ||
                mat == Material.BIRCH_TRAPDOOR ||
                mat == Material.SPRUCE_TRAPDOOR ||
                mat == Material.JUNGLE_TRAPDOOR ||
                mat == Material.DARK_OAK_TRAPDOOR ||
                mat == Material.ACACIA_TRAPDOOR ||
                mat == Material.IRON_DOOR ||
                mat == Material.IRON_TRAPDOOR) {
            event.setCancelled(event.isCancelled() || shouldBlockAction(clickedBlock, player, "door_use", null));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else if (mat == Material.CHEST ||
                mat == Material.FURNACE ||
                mat == Material.TRAPPED_CHEST ||
                mat == Material.ENDER_CHEST ||
                mat == Material.BOOKSHELF ||
                mat == Material.SHULKER_BOX) {
            event.setCancelled(event.isCancelled() || shouldBlockAction(clickedBlock, player, "chest_use"));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else if (mat == Material.WHEAT ||
                mat == Material.CARROT ||
                mat == Material.POTATO) {
            event.setCancelled(event.isCancelled() || shouldBlockAction(clickedBlock, player, "block_break", null));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else if (mat == Material.LEVER ||
                mat == Material.STONE_BUTTON ||
                mat == Material.BIRCH_BUTTON ||
                mat == Material.SPRUCE_BUTTON ||
                mat == Material.JUNGLE_BUTTON ||
                mat == Material.DARK_OAK_BUTTON ||
                mat == Material.ACACIA_BUTTON ||
                mat == Material.OAK_BUTTON) {
            event.setCancelled(event.isCancelled() || shouldBlockAction(clickedBlock, player, "button_use", null));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else {
            event.setCancelled(event.isCancelled() || shouldBlockAction(clickedBlock, player, "block_use", null));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event) {
        handleInteract(event.getClickedBlock(), event.getPlayer(), event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onMobSpawn(CreatureSpawnEvent event) {
        if ((!(event.getEntity() instanceof Monster) && !(event.getEntity() instanceof Phantom)) ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.INFECTION ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
            return;
        }
        boolean cancel = event.isCancelled() || shouldBlockAction(event.getLocation(), null, "deny_mob_spawn");
        if (cancel) {
            event.setCancelled(true);
        }
    }

    boolean shouldBlockActionEffect(Location location, Player player, String type, int mod) {
//        if (player != null && Civs.perm != null && Civs.perm.has(player, "civs.admin")) {
//            return false;
//        }
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }
        Town town = TownManager.getInstance().getTownAt(location);
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getContainingRegions(location, mod)) {
            if (!region.effects.keySet().contains(type)) {
                continue;
            }
            if (player == null) {
                return true;
            }
            String role = region.getPeople().get(player.getUniqueId());
            if (town != null) {
                if (town.getGovernmentType() == GovernmentType.COMMUNISM) {
                    role = "owner";
                }
            }
            if (role == null || (role.contains("member") && !Util.equivalentLocations(location, region.getLocation()))) {
                return true;
            }
            return true;
        }
        if (town != null) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            if (!townType.getEffects().keySet().contains(type)) {
                return false;
            }
            boolean hasPower = town.getPower() > 0;
            boolean hasGrace = hasPower || TownManager.getInstance().hasGrace(town, true);
            if (!hasGrace) {
                return false;
            }
            if (player == null) {
                return true;
            }
//            String role = town.getPeople().get(player.getUniqueId());
//            if (role == null || (role.contains("member"))) {
//                return false;
//            }
            return false;
        }
        return false;
    }

    static boolean shouldBlockAction(Block block, Player player, String type) {
        Location location = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        return shouldBlockAction(location, player, type);
    }
    static boolean shouldBlockAction(Block block, Player player, String type, String pRole) {
        Location location = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        return shouldBlockAction(location, player, type, pRole);
    }
    static boolean shouldBlockAction(Location location, Player player, String type) {
        return shouldBlockAction(location, player, type, "member");
    }

    static boolean shouldBlockAction(Location location, String type) {
        RegionManager regionManager = RegionManager.getInstance();
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(location);
        if (town != null) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            if (townType.getEffects().containsKey(type)) {

                boolean hasPower = town.getPower() > 0;
                boolean hasGrace = hasPower || TownManager.getInstance().hasGrace(town, true);
                if (hasGrace) {
                    return true;
                }
            }
        }
        Region region = regionManager.getRegionAt(location);
        if (region == null ||
                !region.effects.keySet().contains(type)) {
            return false;
        }
        return true;
    }

    static boolean shouldBlockAction(Location location, Player player, String type, String pRole) {
//        if (player != null && Civs.perm != null && Civs.perm.has(player, "civs.admin")) {
//            return false;
//        }
        if (player != null && player.getGameMode() == GameMode.CREATIVE) {
            return false;
        }
        RegionManager regionManager = RegionManager.getInstance();
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(location);
        outer: if (town != null) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            if (!townType.getEffects().containsKey(type)) {
                break outer;
            }
            boolean hasPower = town.getPower() > 0;
            boolean hasGrace = hasPower || TownManager.getInstance().hasGrace(town, true);
            if (!hasGrace) {
                break outer;
            }

            if (player == null) {
                return true;
            }
            String role = town.getPeople().get(player.getUniqueId());
            if (role == null || (!role.contains("owner") && pRole != null && !role.contains(pRole))) {
                return true;
            }
        }
        Region region = regionManager.getRegionAt(location);
        if (region == null ||
                !region.getEffects().containsKey(type)) {
            return false;
        }
        if (player == null) {
            return true;
        }
        String role = region.getPeople().get(player.getUniqueId());
        if (role == null) {
            return true;
        }
        if (town != null) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (town.getGovernmentType() == GovernmentType.COMMUNISM ||
                    town.getGovernmentType() == GovernmentType.ANARCHY) {
                role = "owner";
            } else if ((town.getGovernmentType() == GovernmentType.SOCIALISM ||
                    town.getGovernmentType() == GovernmentType.DEMOCRATIC_SOCIALISM ||
                    town.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM) &&
                    (regionType.getGroups().contains("mine") ||
                    regionType.getGroups().contains("quarry") ||
                    regionType.getGroups().contains("farm") ||
                    regionType.getGroups().contains("factory"))) {
                role = "owner";
            }
        }
        if (role.contains("owner")) {
            return false;
        }
        if (Util.equivalentLocations(location, region.getLocation())) {
            return true;
        }
        if (pRole == null || role.contains(pRole)) {
            return false;
        }
        return true;
    }
}

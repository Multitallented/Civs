package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import java.util.HashSet;
import java.util.Set;

public class ProtectionHandler implements Listener {


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        RegionManager regionManager = RegionManager.getInstance();
        boolean setCancelled = event.isCancelled() || checkLocation(event.getBlock(), event.getPlayer(), "block_break");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
        if (!event.isCancelled()) {
            Region region = regionManager.getRegionAt(event.getBlock().getLocation());
            if (region == null) {
                return;
            }
            if (region.getLocation().equals(event.getBlock().getLocation())) {
                regionManager.removeRegion(region, true);
                return;
            }
            int[] radii = Region.hasRequiredBlocks(region.getType().toLowerCase(), region.getLocation());
            if (radii.length == 0) {
                regionManager.removeRegion(region, true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        boolean setCancelled = event.isCancelled() || checkLocation(event.getBlockPlaced(), event.getPlayer(), "block_build");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (!event.getBlock().getType().equals(Material.CAKE)) {
            return;
        }
        boolean setCancelled = event.isCancelled() || checkLocation(event.getBlock(), event.getPlayer(), "block_break");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.getBlock().getType() == Material.AIR) {
            return;
        }
        boolean setCancelled = event.isCancelled() || checkLocation(event.getBlock(), null, "block_liquid");
        if (setCancelled) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getIgnitingBlock() == null) {
            return;
        }
        boolean setCancelled = event.isCancelled() || checkLocation(event.getIgnitingBlock(), event.getPlayer(), "block_fire");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        boolean setCancelled = event.isCancelled() || checkLocation(event.getBlock(), event.getPlayer(), "block_break");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            boolean checkLocation = checkLocation(block, null, "block_build");
            if (checkLocation) {
                event.setCancelled(true);
                break;
            }
        }
    }
    @EventHandler
    public void onPaintingPlace(HangingPlaceEvent event) {
        boolean setCancelled = event.isCancelled() || checkLocation(event.getBlock(), event.getPlayer(), "block_build");
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
        boolean setCancelled = event.isCancelled() || checkLocation(event.getEntity().getLocation(), player, "block_break");
        if (setCancelled) {
            event.setCancelled(true);
        }
        if (event.isCancelled() && player != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler
    public void onHangingBreakEvent(HangingBreakEvent event) {
        if (event instanceof HangingBreakByEntityEvent) {
            onPaintingBreak((HangingBreakByEntityEvent) event);
            return;
        }
        checkLocation(event.getEntity().getLocation(), null, "block_break");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled() && !ConfigManager.getInstance().getExplosionOverride()) {
            return;
        }
        boolean setCancelled = !event.isCancelled() &&
                checkEffectAt(event.getLocation(), null, "block_explosion", 5);
        if (setCancelled) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() instanceof Creeper) {
            setCancelled = !event.isCancelled() && checkEffectAt(event.getLocation(), null, "block_creeper", 5);
        } else if (event.getEntity() instanceof Fireball) {
            setCancelled = !event.isCancelled() && checkEffectAt(event.getLocation(), null, "block_ghast", 5);
        } else if (event.getEntity() instanceof TNTPrimed) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            Player player = null;
            if (tnt.getSource() instanceof Player) {
                player = (Player) tnt.getSource();
            }
            setCancelled = !event.isCancelled() && checkEffectAt(event.getLocation(), player, "block_tnt", 5);
            if (setCancelled && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
            if (checkEffectAt(event.getLocation(), player, "power_shield", 0)) {
                Town town = TownManager.getInstance().getTownAt(event.getLocation());
                if (town != null) {
                    town.setPower(town.getPower() - 1);
                    TownManager.getInstance().saveTown(town);
                    setCancelled = true;
                }
            }
        }
        if (setCancelled) {
            event.setCancelled(true);
            return;
        }
        //TODO power shield for super regions

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
                if (Region.hasRequiredBlocks(region.getType(), region.getLocation()).length == 0) {
                    tempArray.add(region);
                }
            }
            for (Region region : tempArray) {
                regionManager.removeRegion(region, true);
            }
        }
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
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
            event.setCancelled(event.isCancelled() || checkLocation(clickedBlock, player, "door_use", null));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else if (mat == Material.CHEST ||
                mat == Material.FURNACE ||
                mat == Material.TRAPPED_CHEST ||
                mat == Material.ENDER_CHEST ||
                mat == Material.BOOKSHELF) {
            event.setCancelled(event.isCancelled() || checkLocation(clickedBlock, player, "chest_use"));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else if (mat == Material.WHEAT ||
                mat == Material.CARROT ||
                mat == Material.POTATO) {
            event.setCancelled(event.isCancelled() || checkLocation(clickedBlock, player, "block_break", null));
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
            event.setCancelled(event.isCancelled() || checkLocation(clickedBlock, player, "button_use", null));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else {
            event.setCancelled(event.isCancelled() || checkLocation(clickedBlock, player, "block_use", null));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        handleInteract(event.getClickedBlock(), event.getPlayer(), event);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster) ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.INFECTION ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS ||
                event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT) {
            return;
        }
        event.setCancelled(event.isCancelled() || checkLocation(event.getLocation(), null, "deny_mob_spawn"));
    }

    private boolean checkEffectAt(Location location, Player player, String type, int mod) {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getContainingRegions(location, mod)) {
            if (!region.effects.keySet().contains(type)) {
                continue;
            }
            if (player == null) {
                continue;
            }
            String role = region.getPeople().get(player.getUniqueId());
            if (role == null || (role.contains("member") && location != region.getLocation())) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean checkLocation(Block block, Player player, String type) {
        return checkLocation(block.getLocation(), player, type);
    }
    private boolean checkLocation(Block block, Player player, String type, String pRole) {
        return checkLocation(block.getLocation(), player, type, pRole);
    }
    private boolean checkLocation(Location location, Player player, String type) {
        return checkLocation(location, player, type, "member");
    }

    private boolean checkLocation(Location location, Player player, String type, String pRole) {
        RegionManager regionManager = RegionManager.getInstance();
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(location);
        outer: if (town != null) {
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            if (player == null || !townType.getEffects().contains(type)) {
                break outer;
            }
            String role = town.getPeople().get(player.getUniqueId());
            if (role == null || (!role.contains("owner") && pRole != null && !role.contains(pRole))) {
                return true;
            }
        }
        Region region = regionManager.getRegionAt(location);
        if (region == null ||
                !region.effects.keySet().contains(type) ||
                player == null) {
            return false;
        }
        String role = region.getPeople().get(player.getUniqueId());
        if (role == null) {
            return true;
        }
        if (role.contains("owner")) {
            return false;
        }
        if (location == region.getLocation()) {
            return true;
        }
        if (pRole == null || role.contains(pRole)) {
            return false;
        }
        return true;
    }
}

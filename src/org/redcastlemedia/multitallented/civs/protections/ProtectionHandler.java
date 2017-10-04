package org.redcastlemedia.multitallented.civs.protections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
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

import java.util.HashSet;
import java.util.Set;

public class ProtectionHandler implements Listener {


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        RegionManager regionManager = RegionManager.getInstance();
        event.setCancelled(checkLocation(event.getBlock(), event.getPlayer(), "block_break"));
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
        if (event.isCancelled()) {
            Region region = regionManager.getRegionAt(event.getBlock().getLocation());
            if (region == null) {
                return;
            }
            int[] radii = Region.hasRequiredBlocks(region.getType(), region.getLocation());
            if (radii.length == 0) {
                regionManager.removeRegion(region, true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(checkLocation(event.getBlockPlaced(), event.getPlayer(), "block_build"));
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (!event.getBlock().getType().equals(Material.CAKE_BLOCK)) {
            return;
        }
        event.setCancelled(checkLocation(event.getBlock(), event.getPlayer(), "block_break"));
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
        event.setCancelled(checkLocation(event.getBlock(), null, "block_liquid"));
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getIgnitingBlock() == null) {
            return;
        }
        event.setCancelled(checkLocation(event.getIgnitingBlock(), event.getPlayer(), "block_fire"));
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        checkLocation(event.getBlock(), event.getPlayer(), "block_break");
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
        event.setCancelled(checkLocation(event.getBlock(), event.getPlayer(), "block_build"));
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
        event.setCancelled(checkLocation(event.getEntity().getLocation(), player, "block_break"));
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
        if (event.getEntity().getClass().equals(Creeper.class)) {
            event.setCancelled(checkEffectAt(event.getLocation(), null, "block_creeper", 5));
        } else if (event.getEntity().getClass().equals(Fireball.class)) {
            event.setCancelled(checkEffectAt(event.getLocation(), null, "block_ghast", 5));
        } else if (event.getEntity().getClass().equals(TNTPrimed.class)) {
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            Player player = null;
            if (tnt.getSource() instanceof Player) {
                player = (Player) tnt.getSource();
            }
            event.setCancelled(checkEffectAt(event.getLocation(), player, "block_tnt", 5));
            if (event.isCancelled() && player != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        }
        event.setCancelled(checkEffectAt(event.getLocation(), null, "block_explosion", 5));
        //TODO power shield for super regions

        final Location location = event.getLocation();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
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
        }, 1L);
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Material mat = event.getClickedBlock().getType();
        if (mat == Material.WOODEN_DOOR ||
                mat == Material.TRAP_DOOR ||
                mat == Material.IRON_DOOR_BLOCK ||
                mat == Material.IRON_TRAPDOOR) {
            event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "door_use"));
            if (event.isCancelled() && event.getPlayer() != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else if (mat == Material.CHEST ||
                mat == Material.FURNACE ||
                mat == Material.BURNING_FURNACE ||
                mat == Material.TRAPPED_CHEST ||
                mat == Material.ENDER_CHEST ||
                mat == Material.BOOKSHELF) {
            event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "chest_use"));
            if (event.isCancelled() && event.getPlayer() != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else if (mat == Material.CROPS ||
                mat == Material.CARROT ||
                mat == Material.POTATO) {
            event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "block_break"));
            if (event.isCancelled() && event.getPlayer() != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        } else if (mat == Material.LEVER ||
                mat == Material.STONE_BUTTON ||
                mat == Material.WOOD_BUTTON) {
            event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "button_use"));
            if (event.isCancelled() && event.getPlayer() != null) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
            }
        }
        event.setCancelled(checkLocation(event.getClickedBlock(), event.getPlayer(), "block_use"));
        if (event.isCancelled() && event.getPlayer() != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
            event.getPlayer().sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "region-protected"));
        }
    }

    private boolean checkEffectAt(Location location, Player player, String type, int mod) {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getContainingRegions(location, mod)) {
            if (!region.effects.contains(type)) {
                continue;
            }
            if (player == null) {
                continue;
            }
            if (region.getOwners().contains(player.getUniqueId())) {
                continue;
            }
            if (region.getMembers().contains(player.getUniqueId()) &&
                    region.getLocation() != location) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean checkLocation(Block block, Player player, String type) {
        return checkLocation(block.getLocation(), player, type);
    }
    private boolean checkLocation(Location location, Player player, String type) {
        RegionManager regionManager = RegionManager.getInstance();
        Region region = regionManager.getRegionAt(location);
        if (region == null) {
            return false;
        }
        if (!region.effects.contains(type)) {
            return false;
        }
        if (player == null) {
            return false;
        }
        if (region.getOwners().contains(player.getUniqueId())) {
            return false;
        }
        if (region.getMembers().contains(player.getUniqueId()) &&
                region.getLocation() != location) {
            return false;
        }
        return true;
    }
}

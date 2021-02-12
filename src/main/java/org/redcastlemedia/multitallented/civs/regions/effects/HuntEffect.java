package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsSingleton
public class HuntEffect implements Listener, CreateRegionListener {
    public static final String KEY = "hunt";

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new HuntEffect(), Civs.getInstance());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION || !(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        long cooldown = 20000;
        if (cooldowns.containsKey(player.getUniqueId()) &&
                cooldowns.get(player.getUniqueId()) + cooldown > System.currentTimeMillis()) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean createRegionHandler(Block block, Player player, RegionType rt) {
        if (!rt.getEffects().containsKey(KEY)) {
            return true;
        }
        Location l = Region.idToLocation(Region.blockLocationToString(block.getLocation()));

        Player targetPlayer = hasValidSign(l, player.getUniqueId());

        return targetPlayer != null;
    }

    private Player hasValidSign(Location l, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return null;
        }
        Block block = l.getBlock().getRelative(BlockFace.UP);
        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return null;
        }

        Sign sign = (Sign) state;

        Player targetPlayer = null;
        try {
            targetPlayer = Bukkit.getPlayer(sign.getLine(0));
        } catch (Exception e) {
            block.breakNaturally();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "invalid-name"));
            return null;
        }
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            block.breakNaturally();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "invalid-name"));
            return null;
        }

        if (!ConfigManager.getInstance().isAllowHuntNewPlayers() &&
                !targetPlayer.getWorld().equals(player.getWorld())) {
            block.breakNaturally();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "target-not-in-world").replace("$1", targetPlayer.getDisplayName()));
            return null;
        }

        return targetPlayer;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getHand() == null) {
            return;
        }

        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) ||
                event.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }

        Region r = RegionManager.getInstance().getRegionAt(event.getClickedBlock().getLocation());

        if (r == null || !r.getEffects().containsKey(KEY) ||
                !Util.equivalentLocations(event.getClickedBlock().getLocation(), r.getLocation())) {
            return;
        }

        Player player = event.getPlayer();

        if (cooldowns.containsKey(player.getUniqueId())) {
            long cooldown = 30;
            String cooldownString = r.getEffects().get(KEY);
            if (cooldownString != null && !cooldownString.isEmpty()) {
                cooldown = Long.parseLong(cooldownString);
            }
            cooldown = cooldown * 1000;
            if (cooldowns.get(player.getUniqueId()) + cooldown > System.currentTimeMillis()) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "cooldown").replace("$1", (cooldown / 1000) + "s"));
                return;
            } else {
                cooldowns.remove(player.getUniqueId());
            }
        }


        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(r.getType());
        Player targetPlayer = hasValidSign(r.getLocation(), player.getUniqueId());
        if (targetPlayer == null) {
            return;
        }
        if (!ConfigManager.getInstance().isAllowHuntNewPlayers() &&
                TownManager.getInstance().getTownsForPlayer(targetPlayer.getUniqueId()).isEmpty()) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "no-hunting-new-players"));
            return;
        }

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        Civilian targetCiv = CivilianManager.getInstance().getCivilian(targetPlayer.getUniqueId());
        double hardhipThreshold = 20000;
        if (Civs.econ != null) {
            hardhipThreshold = Civs.econ.getBalance(targetPlayer);
        }
        if ( targetCiv.getHardship() > civilian.getHardship() + hardhipThreshold) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "hardship-too-high").replace("$1", targetPlayer.getDisplayName()));
            return;
        }

        if (!regionType.getUpkeeps().isEmpty() && !r.runUpkeep(false)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "region-missing-upkeep-items"));
            return;
        }

        Location location = targetPlayer.getLocation();
        int radius = 200;

        Location teleportTarget = findNearbyLocationForTeleport(location, radius, player);
        if (teleportTarget != null) {
            player.teleport(teleportTarget);
            messageNearbyPlayers(player, "hunting-players", null);
            double karmaChange = ConfigManager.getInstance().getHuntKarma();
            if (karmaChange != 0) {
                CivilianManager.getInstance().exchangeHardship(player.getUniqueId(), targetPlayer.getUniqueId(), karmaChange);
            }
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public static void messageNearbyPlayers(Player player, String messageKey, String replace1) {
        for (Player player1 : Bukkit.getOnlinePlayers()) {
            if (!player1.getWorld().equals(player.getWorld()) ||
                    player1.getLocation().distanceSquared(player.getLocation()) > 90000) {
                continue;
            }
            String message = LocaleManager.getInstance().getTranslation(player1, messageKey);
            if (replace1 != null) {
                message = message.replace("$1", replace1);
            }
            player1.sendMessage(Civs.getPrefix() + message);
        }
    }

    public static Location findNearbyLocationForTeleport(Location location, int radius, Player player) {
        int times = 0;
        Block targetBlock;
        do {
            times++;
            int xRadius = (int) (Math.random()*radius);
            if (Math.random() > .5) {
                xRadius = xRadius *-1;
            }
            int x = location.getBlockX() + xRadius;
            int zRadius = (int) ((Math.sqrt(radius*radius - xRadius*xRadius)));
            if (Math.random() > .5) {
                zRadius = zRadius *-1;
            }
            int z = location.getBlockZ() + zRadius;
            targetBlock = location.getWorld().getHighestBlockAt(x, z);
        } while (times < 5 && (targetBlock.getType() == Material.LAVA ||
                !targetBlock.getWorld().getWorldBorder().isInside(targetBlock.getLocation())));

        if (times == 5) {
            return null;
        }

        return new Location(targetBlock.getWorld(),
                targetBlock.getX(),
                (double) targetBlock.getY() + 0.5,
                targetBlock.getZ());
    }
}

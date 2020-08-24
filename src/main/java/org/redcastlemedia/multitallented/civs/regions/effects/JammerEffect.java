package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.spells.Vector3D;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.DiscordUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.UUID;

@CivsSingleton @SuppressWarnings("unused")
public class JammerEffect implements Listener, RegionCreatedListener {

    public static String KEY = "jammer";
    private static HashMap<UUID, Long> cooldowns = new HashMap<>();

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new JammerEffect(), Civs.getInstance());
    }

    public JammerEffect() {
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (cooldowns.containsKey(event.getPlayer().getUniqueId())) {
            long offCooldown = cooldowns.get(event.getPlayer().getUniqueId());
            if (offCooldown > System.currentTimeMillis()) {
                event.setCancelled(true);
                long cooldown = offCooldown - System.currentTimeMillis();
                event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                        .getTranslation(event.getPlayer(), "cooldown")
                        .replace("$1", Util.formatTime(cooldown)));
                return;
            } else {
                cooldowns.remove(event.getPlayer().getUniqueId());
            }
        }
        if (event.getTo() == null) {
            return;
        }

        Player player = event.getPlayer();
        if (Civs.perm != null && Civs.perm.has(player, Constants.PVP_EXEMPT_PERMISSION)) {
            return;
        }

        HashMap<Location, Region> jammers = new HashMap<>();
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            if (region.getEffects().containsKey(KEY)) {
                jammers.put(region.getLocation(), region);
            }
        }

        Vector3D observerStart = new Vector3D(event.getFrom());
        Vector3D observerEnd = new Vector3D(event.getTo());

        for (Location location : jammers.keySet()) {
            Region region = jammers.get(location);
            if (region.getPeople().containsKey(player.getUniqueId())) {
                continue;
            }

            String effectString = region.getEffects().get(KEY);
            int radius = 100;
            long cooldown = 30000;
            int landingRadius = 20;
            if (effectString != null) {
                String[] splitString = effectString.split("\\.");
                radius = Integer.parseInt(splitString[0]);
                if (splitString.length > 1) {
                    cooldown = 1000 * Long.parseLong(splitString[1]);
                }
                if (splitString.length > 2) {
                    landingRadius = Integer.parseInt(splitString[2]);
                }
            }

            Vector3D targetPos = new Vector3D(location);
            Vector3D minimum = targetPos.add(-radius, -radius, -radius);
            Vector3D maximum = targetPos.add(radius, radius, radius);

            if (Vector3D.hasIntersection(observerStart, observerEnd, minimum, maximum)) {
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldown);
                Location targetLocation = HuntEffect.findNearbyLocationForTeleport(region.getLocation(), landingRadius, player);
                if (targetLocation != null) {
                    event.setTo(targetLocation);
                    Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                    RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                    String localizedRegionName = LocaleManager.getInstance().getTranslation(
                            civilian.getLocale(), regionType.getProcessedName() + "-name");
                    HuntEffect.messageNearbyPlayers(player, "jammer-redirect", localizedRegionName);
                }
            }
            return;
        }

    }

    @Override
    public void regionCreatedHandler(Region region) {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        for (Player p : Bukkit.getOnlinePlayers()) {
            Civilian civ = CivilianManager.getInstance().getCivilian(p.getUniqueId());
            String jammerLocalName = LocaleManager.getInstance().getTranslation(civ.getLocale(), regionType.getProcessedName() + "-name");
            p.sendMessage(Civs.getPrefix() + ChatColor.RED + LocaleManager.getInstance().getTranslation(
                    civ.getLocale(), "jammer-built").replace("$1", jammerLocalName));
        }
        if (Civs.discordSRV != null) {
            String jammerLocalName = LocaleManager.getInstance().getTranslation(ConfigManager.getInstance().getDefaultLanguage(),
                    regionType.getProcessedName() + "-name");
            String defaultMessage = Civs.getPrefix() + ChatColor.RED + LocaleManager.getInstance().getTranslation(
                    ConfigManager.getInstance().getDefaultLanguage(), "jammer-built")
                    .replace("$1", jammerLocalName);
            DiscordUtil.sendMessageToMainChannel(defaultMessage);
        }
    }
}

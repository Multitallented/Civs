package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.spells.Vector3D;
import org.redcastlemedia.multitallented.civs.util.AnnouncementUtil;

import java.util.HashMap;
import java.util.UUID;

public class JammerEffect implements Listener {

    public static String KEY = "jammer";
    private static HashMap<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (cooldowns.containsKey(event.getPlayer().getUniqueId())) {
            long offCooldown = cooldowns.get(event.getPlayer().getUniqueId());
            if (offCooldown > System.currentTimeMillis()) {
                event.setCancelled(true);
                long cooldown = offCooldown - System.currentTimeMillis();
                Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                        .getTranslation(civilian.getLocale(), "cooldown")
                        .replace("$1", AnnouncementUtil.formatTime(cooldown)));
                return;
            } else {
                cooldowns.remove(event.getPlayer().getUniqueId());
            }
        }
        if (event.getTo() == null) {
            return;
        }

        Player player = event.getPlayer();
        if (Civs.perm != null && Civs.perm.has(player, "civs.bypasspvp")) {
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
            String effectString = region.getEffects().get(KEY);
            int radius = 20;
            long cooldown = 30000;
            if (effectString != null) {
                String[] splitString = effectString.split("\\.");
                radius = Integer.parseInt(splitString[0]);
                if (splitString.length > 1) {
                    cooldown = 1000 * Long.parseLong(splitString[1]);
                }
            }

            Vector3D targetPos = new Vector3D(location);
            Vector3D minimum = targetPos.add(-radius, -radius, -radius);
            Vector3D maximum = targetPos.add(radius, radius, radius);

            if (Vector3D.hasIntersection(observerStart, observerEnd, minimum, maximum)) {
                cooldowns.put(player.getUniqueId(), System.currentTimeMillis() + cooldown);
                // TODO teleport them nearby
            }
        }

    }
}

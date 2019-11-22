package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterTownEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitTownEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.HashMap;

@CivsSingleton
public class IntruderEffect implements Listener {

    private final HashMap<String, Long> lastMessage = new HashMap<>();
    private final static String KEY = "intruder";

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new IntruderEffect(), Civs.getInstance());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (lastMessage.containsKey(event.getPlayer().getName())) {
            lastMessage.remove(event.getPlayer().getName());
        }
    }

    @EventHandler
    public void onSRegionEnter(PlayerEnterTownEvent event) {
        RegionManager rm = RegionManager.getInstance();
        Town town = event.getTown();
        if (town == null) {
            return;
        }

        if (town.getPeople().containsKey(event.getUuid())) {
            return;
        }

        Player player = Bukkit.getPlayer(event.getUuid());
        if (Civs.perm != null && Civs.perm.has(player, "civs.bypasspvp")) {
            return;
        }

        Region r = getIntruderRegion(town);

        if (r == null) {
            return;
        }

        if (!r.hasUpkeepItems()) {
            return;
        }

        r.runUpkeep(true);

        broadcastMessageToAllTownMembers(town, true, player.getDisplayName());
    }

    @EventHandler
    public void onSRegionExit(PlayerExitTownEvent event) {
        Town town = event.getTown();
        if (town.getPeople().containsKey(event.getUuid())) {
            return;
        }
        Player player = Bukkit.getPlayer(event.getUuid());
        if (Civs.perm != null && Civs.perm.has(player, "civs.bypasspvp")) {
            return;
        }

        Region r = getIntruderRegion(town);

        if (r == null) {
            return;
        }

        if (!r.hasUpkeepItems()) {
            return;
        }

        r.runUpkeep(true);

        broadcastMessageToAllTownMembers(town, false, player.getDisplayName());
    }

    private Region getIntruderRegion(Town town) {
        for (Region r : TownManager.getInstance().getContainingRegions(town.getName())) {
            if (r.getEffects().containsKey(KEY)) {
                return r;
            }
        }
        return null;
    }

    private void broadcastMessageToAllTownMembers(Town town, boolean entering, String playerName) {
        if (lastMessage.containsKey(playerName)) {
            if (lastMessage.get(playerName) + 60000 > System.currentTimeMillis()) {
                return;
            }
        }
        lastMessage.put(playerName, System.currentTimeMillis());

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (town.getPeople().containsKey(p.getUniqueId())) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(p.getUniqueId());
                String message;
                if (entering) {
                    message = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            "intruder-enter").replace("$1", playerName).replace("$2", town.getName());
                } else {
                    message = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                            "intruder-exit").replace("$1", playerName).replace("$2", town.getName());
                }
                p.sendMessage(Civs.getPrefix() + ChatColor.RED + message);
                p.playSound(p.getLocation(), Sound.ENTITY_WOLF_HOWL, 1, 1);
            }
        }
    }
}

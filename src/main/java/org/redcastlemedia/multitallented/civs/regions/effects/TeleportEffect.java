package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsSingleton
public class TeleportEffect implements Listener, RegionCreatedListener {
    public static String KEY = "teleport";
    public static String KEY_PUBLIC = "teleport_public";

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new TeleportEffect(), Civs.getInstance());
    }

    public TeleportEffect() {
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
    }

    @EventHandler
    public void onPlayerInTeleport(PlayerInRegionEvent event) {
        Region region = event.getRegion();
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        Player player = Bukkit.getPlayer(event.getUuid());
        Location regionLocation = event.getRegion().getLocation();
        Location regionLocationPlus1 = new Location(regionLocation.getWorld(),
                regionLocation.getX(),
                regionLocation.getY() + 1,
                regionLocation.getZ());
        if (!(Util.equivalentLocations(regionLocation, player.getLocation()) ||
                Util.equivalentLocations(regionLocationPlus1, player.getLocation()))) {
            return;
        }
        String locationString = region.getEffects().get(KEY);
        if (locationString == null) {
            if (region.getOwners().contains(player.getUniqueId())) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                if (!MenuManager.getInstance().hasMenuOpen(civilian.getUuid(), "port") &&
                        hasPotentialDestinations(region)) {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("region", region.getId());
                    MenuManager.clearHistory(player.getUniqueId());
                    MenuManager.getInstance().openMenu(player, "port", params);
                }
            }
            return;
        }

        if (player.isSneaking() || (!region.getEffects().containsKey(KEY_PUBLIC) &&
                !region.getPeople().containsKey(player.getUniqueId()))) {
            return;
        }
        Location destination = Region.idToLocation(locationString);
        player.teleport(destination);
    }

    @Override
    public void regionCreatedHandler(Region region) {
        for (UUID uuid : region.getOwners()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return;
            }
            Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
            if (!MenuManager.getInstance().hasMenuOpen(civilian.getUuid(), "port") &&
                    hasPotentialDestinations(region)) {
                HashMap<String, String> params = new HashMap<>();
                params.put("region", region.getId());
                MenuManager.clearHistory(player.getUniqueId());
                MenuManager.getInstance().openMenu(player, "port", params);
            }
            break;
        }
    }

    private static boolean hasPotentialDestinations(Region region) {
        for (Region currentRegion : RegionManager.getInstance().getAllRegions()) {
            if (isPotentialTeleportDestination(region, currentRegion)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPotentialTeleportDestination(Region region, Region currentRegion) {
        if (!currentRegion.getEffects().containsKey(TeleportEffect.KEY) ||
                !region.getEffects().containsKey(TeleportEffect.KEY)) {
            return false;
        }
        for (UUID uuid : currentRegion.getRawPeople().keySet()) {
            if (!currentRegion.getRawPeople().get(uuid).contains(Constants.OWNER)) {
                continue;
            }
            if (!region.getRawPeople().containsKey(uuid) ||
                    !region.getRawPeople().get(uuid).contains(Constants.OWNER)) {
                continue;
            }
            return true;
        }
        return false;
    }
}

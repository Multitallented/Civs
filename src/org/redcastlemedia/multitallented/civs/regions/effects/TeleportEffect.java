package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.menus.TeleportDestinationMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.util.Util;

public class TeleportEffect implements Listener, RegionCreatedListener {
    public static String KEY = "teleport";



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
                player.openInventory(TeleportDestinationMenu.createMenu(civilian, region));
            }
            return;
        }

        if (!region.getPeople().containsKey(player.getUniqueId())) {
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
            player.openInventory(TeleportDestinationMenu.createMenu(civilian, region));
            break;
        }
    }
}

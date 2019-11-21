package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

import java.util.UUID;

@CivsSingleton
public class BedEffect implements RegionCreatedListener {
    private static String KEY = "bed";

    public static void getInstance() {
        new BedEffect();
    }

    public BedEffect() {
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
    }

    @Override
    public void regionCreatedHandler(Region region) {
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        if (region.getRawPeople().isEmpty() || region.getOwners().isEmpty()) {
            return;
        }
        UUID uuid = region.getOwners().iterator().next();
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        player.setBedSpawnLocation(new Location(region.getLocation().getWorld(),
                region.getLocation().getX(), region.getLocation().getY() + 1, region.getLocation().getZ()));
    }
}

package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

public class SpawnEffect implements Listener {

    public final String KEY = "spawn";

    @EventHandler(ignoreCancelled = true)
    public void onUpkeep(RegionUpkeepEvent event) {
        Location location = event.getRegion().getLocation();
        if (!event.getRegion().getEffects().containsKey(KEY) ||
                !location.getChunk().isLoaded()) {
            return;
        }
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(event.getRegion().getEffects().get(KEY));
        } catch (Exception ex) {
            Civs.logger.severe("Wrong entity type " + event.getRegion().getEffects().get(KEY) + " for " +
                    event.getRegion().getType());
            return;
        }

        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(event.getRegion().getType());
//        int entityCount = 0;
        //TODO fix this so that it detects the correct type of entity
        int radius = Math.max(regionType.getEffectRadius(), regionType.getBuildRadius());
//        for (Entity e : location.getWorld().getNearbyEntities(location,
//                radius, radius, radius)) {
//            if (entityType.getEntityClass().isAssignableFrom(e.getClass())) {
//                entityCount++;
//                if (entityCount > 5) {
//                    return;
//                }
//            }
//        }
        if (location.getWorld().getNearbyEntities(location, radius, radius, radius).size() > 5) {
            return;
        }

        Location spawnLocation = new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ());

        location.getWorld().spawnEntity(spawnLocation, entityType);
    }
}

package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionUpkeep;
import org.redcastlemedia.multitallented.civs.util.Util;

import io.lumine.xikage.mythicmobs.MythicMobs;

@CivsSingleton
public class SpawnEffect implements Listener {

    public final String KEY = "spawn";

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new SpawnEffect(), Civs.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onUpkeep(RegionUpkeepEvent event) {
        Location location = event.getRegion().getLocation();
        if (!event.getRegion().getEffects().containsKey(KEY) ||
                !Util.isLocationWithinSightOfPlayer(location)) {
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

        String entityTypeString = event.getRegion().getEffects().get(KEY);

        Location spawnLocation = new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ());
        if (entityTypeString.startsWith("mythic:")) {
            spawnMythicMob(spawnLocation, entityTypeString.replace("mythic:", ""));
        } else {
            spawnEntity(event, spawnLocation, entityTypeString);
        }
    }

    private void spawnMythicMob(Location spawnLocation, String mythicMobType) {
        MythicMobs.inst().getMobManager().spawnMob(mythicMobType, spawnLocation);
    }

    private void spawnEntity(RegionUpkeepEvent event, Location spawnLocation, String entityTypeString) {
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeString);
        } catch (Exception ex) {
            Civs.logger.severe("Wrong entity type " + event.getRegion().getEffects().get(KEY) + " for " +
                    event.getRegion().getType());
            return;
        }


        spawnLocation.getWorld().spawnEntity(spawnLocation, entityType);
    }
}

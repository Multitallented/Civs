package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

@CivsSingleton
public class ActiveEffect implements Listener {

    public static final String LAST_ACTIVE_KEY = "last-active";
    public static final String KEY = "active";

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new ActiveEffect(), Civs.getInstance());
    }

    @EventHandler
    public void onRegionTick(RegionTickEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }

        Region region = event.getRegion();
        long inactiveDuration = Long.parseLong(region.getEffects().get(KEY)) * 1000;

        long lastActive = region.getLastActive();

        if (lastActive < 1 || inactiveDuration + lastActive > System.currentTimeMillis()) {
            return;
        }
        region.getEffects().clear();
    }

    @EventHandler
    public void onPlayerInRegion(PlayerInRegionEvent event) {
        Region region = event.getRegion();
        if (!region.getEffects().containsKey(KEY) ||
                !region.getRawPeople().containsKey(event.getUuid())) {
            return;
        }
        if (region.getLastActive() + 10000 > System.currentTimeMillis()) {
            region.setLastActive(System.currentTimeMillis());
            return;
        }
        region.setLastActive(System.currentTimeMillis());
        RegionManager.getInstance().saveRegion(region);
    }
}

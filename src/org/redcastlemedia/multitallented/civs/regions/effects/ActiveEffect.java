package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class ActiveEffect implements Listener {

    public final String KEY = "active";

    @EventHandler
    public void onRegionTick(RegionTickEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }

        Region region = event.getRegion();
        long inactiveDuration = 120960;
        try {
            inactiveDuration = Long.parseLong(region.getEffects().get(KEY)) * 1000;
        } catch (Exception e) {
            Civs.logger.warning("Invalid config for active " + event.getRegionType().getProcessedName() + ".yml");
        }

        long lastActive = region.getLastActive();

        if (lastActive > 0 && inactiveDuration + lastActive > System.currentTimeMillis()) {
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

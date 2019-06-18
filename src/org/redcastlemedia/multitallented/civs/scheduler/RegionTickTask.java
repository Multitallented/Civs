package org.redcastlemedia.multitallented.civs.scheduler;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.events.TwoSecondEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

public class RegionTickTask implements Listener {

    @EventHandler
    public void onTwoSecondEvent(TwoSecondEvent event) {
        RegionManager regionManager = RegionManager.getInstance();
        Set<Region> destroyThese = new HashSet<>();
        for (Region region : regionManager.getAllRegions()) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            boolean shouldTick = !regionType.isDailyPeriod() && region.shouldTick();

            boolean hasUpkeep = false;
            try {
                hasUpkeep = shouldTick && region.runUpkeep();
            } catch (Exception e) {
                e.printStackTrace();
            }

            RegionTickEvent regionTickEvent = new RegionTickEvent(region, regionType, hasUpkeep, shouldTick);
            Bukkit.getPluginManager().callEvent(regionTickEvent);
            if (regionTickEvent.getShouldDestroy()) {
                destroyThese.add(regionTickEvent.getRegion());
            }
        }
        for (Region region : destroyThese) {
            RegionManager.getInstance().removeRegion(region, true, true);
            CivilianListener.getInstance().shouldCancelBlockBreak(region.getLocation().getBlock(), null);
        }
    }
}

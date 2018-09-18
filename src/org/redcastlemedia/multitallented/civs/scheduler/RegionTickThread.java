package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.VillagerEffect;

public class RegionTickThread implements Runnable {

    @Override
    public void run() {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getAllRegions()) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            boolean shouldTick = region.shouldTick();

            boolean hasUpkeep = false;
            try {
                hasUpkeep = !regionType.isDailyPeriod() && region.runUpkeep();
            } catch (NullPointerException npe) {
                
            }

            RegionTickEvent regionTickEvent = new RegionTickEvent(region, regionType, hasUpkeep, shouldTick);
            Bukkit.getPluginManager().callEvent(regionTickEvent);
        }
    }
}

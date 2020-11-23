package org.redcastlemedia.multitallented.civs.scheduler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

public final class RegionTickUtil {
    private static final int MAX_TPCycle = 10;
    private static int i = 0;
    private RegionTickUtil() {

    }

    public static void runUpkeeps() {
        RegionManager.getInstance().cleanupUnloadedRegions();
        List<Region> regionList = new ArrayList<>(RegionManager.getInstance().getAllRegions());
        int chunk = regionList.size() / MAX_TPCycle;
        for (int j = chunk * i; j < (i == MAX_TPCycle - 1 ? regionList.size() : chunk * (i + 1)); j++) {
            Region region = regionList.get(j);
            try {
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
                    RegionManager.getInstance().removeRegion(region, true, true);
                    CivilianListener.getInstance().shouldCancelBlockBreak(region.getLocation().getBlock(), null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (i == MAX_TPCycle - 1) {
            i = 0;
        } else {
            i++;
        }
    }
}

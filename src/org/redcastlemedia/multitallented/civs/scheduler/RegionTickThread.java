package org.redcastlemedia.multitallented.civs.scheduler;

import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class RegionTickThread implements Runnable {

    @Override
    public void run() {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getAllRegions()) {

            //TODO items
            boolean hasReagents = region.hasReagents();

        }
    }
}

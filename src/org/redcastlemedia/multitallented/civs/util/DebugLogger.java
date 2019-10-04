package org.redcastlemedia.multitallented.civs.util;

import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.regions.Region;

import java.util.HashMap;

public final class DebugLogger {
    public static int saves = 0;
    public static int inventoryModifications = 0;
    private static final HashMap<Region, Integer> regionActivity = new HashMap<>();

    private DebugLogger() {

    }

    public static Runnable timedDebugTask() {
        return new Runnable() {

            @Override
            public void run() {
                Civs.getInstance().getLogger().info("Saves:                   " + saves);
                Civs.getInstance().getLogger().info("Inventory Modifications: " + inventoryModifications);
                int highestActivity = 0;
                Region mostActiveRegion = null;
                for (Region region : regionActivity.keySet()) {
                    int currentActivity = regionActivity.get(region);
                    if (highestActivity < currentActivity) {
                        highestActivity = currentActivity;
                        mostActiveRegion = region;
                    }
                }
                if (mostActiveRegion != null) {
                    Civs.getInstance().getLogger().info("Most Active Region: " +
                            (int) mostActiveRegion.getLocation().getX() + "x, " +
                            (int) mostActiveRegion.getLocation().getY() + "y, " +
                            (int) mostActiveRegion.getLocation().getZ() + "z, " +
                            mostActiveRegion.getType());
                    Civs.getInstance().getLogger().info("^^Ran " + highestActivity + " times");
                }
                saves = 0;
                inventoryModifications = 0;
                regionActivity.clear();
            }
        };
    }

    public static void incrementRegion(Region region) {
        if (!regionActivity.containsKey(region)) {
            regionActivity.put(region, 1);
        } else {
            regionActivity.put(region, regionActivity.get(region) + 1);
        }
    }
}

package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

import java.util.*;

public class CommonScheduler implements Runnable {
    private final int MAX_TPS = 5;
    public final HashMap<UUID, ArrayList<Region>> lastRegion = new HashMap<>();
//    public final HashMap<UUID, ArrayList<SuperRegion>> lastSRegion = new HashMap<UUID, ArrayList<SuperRegion>>();
    private int i = 0;

    @Override
    public void run() {

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        int chunk = players.size() / MAX_TPS;
        for (int j=chunk * i; j<(i==MAX_TPS - 1 ? players.size() : chunk * (i+1)); j++) {
            try {
                playerInRegion((Player) players.toArray()[j]);
            } catch (Exception e) {

            }
//            Thread.yield();
        }
        if (i == MAX_TPS -1) {
            i=0;
            RegionTickThread regionTickThread = new RegionTickThread();
            regionTickThread.run();
        } else {
            i++;
        }
    }

    private void playerInRegion(Player player) {
        RegionManager regionManager = RegionManager.getInstance();
        ArrayList<Region> containedRegions = new ArrayList<>();
        containedRegions.addAll(regionManager.getRegionEffectsAt(player.getLocation(), 0));

        for (Region region : containedRegions) {
            //TODO do things when a player is in a region
        }

        ArrayList<Region> previousRegions = lastRegion.get(player.getUniqueId());
        if (previousRegions == null) {
            previousRegions = new ArrayList<>();
        }

        for (Region r : containedRegions) {
            if (!previousRegions.contains(r)) {
                //TODO enter region things
            }
        }

        for (Region r : previousRegions) {
            if (!containedRegions.contains(r)) {
                //TODO exit region things
            }
        }

        if (!containedRegions.isEmpty()) {
            lastRegion.put(player.getUniqueId(), containedRegions);
        } else {
            lastRegion.remove(player.getUniqueId());
        }
    }
}

package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.*;

public class CommonScheduler implements Runnable {
    private final int MAX_TPS = 5;
    public static final HashMap<UUID, ArrayList<Region>> lastRegion = new HashMap<>();
    public static final HashMap<UUID, Town> lastTown = new HashMap<>();
    private int i = 0;

    @Override
    public void run() {

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        int chunk = players.size() / MAX_TPS;
        for (int j=chunk * i; j<(i==MAX_TPS - 1 ? players.size() : chunk * (i+1)); j++) {
            try {
                Player player = (Player) players.toArray()[j];
                playerInRegion(player);
                playerInTown(player);
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
    private void playerInTown(Player player) {
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(player.getLocation());
        Town prevTown = lastTown.get(player.getUniqueId());
        if (town != null) {
            //TODO when player in town
        }

        if (prevTown == null && town != null) {
            //TODO when player enters town
        } else if (prevTown != null && town != null &&
                prevTown.equals(town)) {
            //TODO exit last town
            //TODO enter new town
        } else if (town == null && prevTown != null) {
            //TODO exit last town
        }

        if (town == null && prevTown != null) {
            lastTown.remove(player.getUniqueId());
        } else if (town != null) {
            lastTown.put(player.getUniqueId(), town);
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

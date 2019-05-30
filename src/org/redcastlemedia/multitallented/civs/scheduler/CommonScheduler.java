package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.*;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.StructureUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class CommonScheduler implements Runnable {
    private final int MAX_TPS = 5;
    public static final HashMap<UUID, ArrayList<Region>> lastRegion = new HashMap<>();
    public static final HashMap<UUID, Town> lastTown = new HashMap<>();
    private int i = 0;
    private boolean notTwoSecond = true;
    public static boolean run = true;

    @Override
    public void run() {
        try {
            if (!run) {
                return;
            }
            depreciateKarma();
            StructureUtil.cleanUpExpiredBoundingBoxes();

            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            int chunk = players.size() / MAX_TPS;
            for (int j = chunk * i; j < (i == MAX_TPS - 1 ? players.size() : chunk * (i + 1)); j++) {
                try {
                    Player player = (Player) players.toArray()[j];
                    playerInRegion(player);
                    playerInTown(player);
                    incrementMana(player);
                } catch (Exception e) {

                }
                //            Thread.yield();
            }
            if (i == MAX_TPS - 1) {
                i = 0;
                RegionTickThread regionTickThread = new RegionTickThread();
                regionTickThread.run();
                notTwoSecond = !notTwoSecond;
                if (!notTwoSecond) {
                    Bukkit.getPluginManager().callEvent(new TwoSecondEvent());
                }
            } else {
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void depreciateKarma() {
        long karmaPeriod = ConfigManager.getInstance().getKarmaDepreciatePeriod() * 1000;
        //TODO lazy loop this
        for (Civilian civilian : CivilianManager.getInstance().getCivilians()) {
            if (civilian.getKarma() < 2 && civilian.getKarma() > -2) {
                continue;
            }
            if (civilian.getLastKarmaDepreciation() + karmaPeriod > System.currentTimeMillis()) {
                continue;
            }
            civilian.setLastKarmaDepreciation(System.currentTimeMillis());
            civilian.setKarma(civilian.getKarma() / 2);
            CivilianManager.getInstance().saveCivilian(civilian);
        }
    }

    void incrementMana(Player player) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        double maxMana = 0;
        double maxManaPerSecond = 0;
        for (CivClass civClass : civilian.getCivClasses()) {
            maxMana = Math.max(maxMana, civClass.getMaxMana());
            maxManaPerSecond = Math.max(maxManaPerSecond, civClass.getManaPerSecond());
        }
        setConvertedMana(civilian, maxMana, maxManaPerSecond);
    }
    void setConvertedMana(Civilian civilian, double maxMana, double manaPerSecond) {
        if (civilian.getMana() < 100 && manaPerSecond > 0) {
            double currentConvertedMana = (double) civilian.getMana() / 100 * maxMana;
            int newMana = (int) ((currentConvertedMana + manaPerSecond) / maxMana * 100);
            civilian.setMana(newMana);
        }
    }
    void playerInTown(Player player) {
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTownAt(player.getLocation());
        Town prevTown = lastTown.get(player.getUniqueId());
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        TownType townType = null;
        if (town != null) {
            townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            PlayerInTownEvent playerInTownEvent = new PlayerInTownEvent(player.getUniqueId(),
                    town, townType);
            Bukkit.getPluginManager().callEvent(playerInTownEvent);
        }
        TownType prevTownType = null;
        if (prevTown != null) {
            prevTownType = (TownType) ItemManager.getInstance().getItemType(prevTown.getType());
        }

        if (prevTown == null && town != null) {
            enterTown(player, civilian, town, townType);
        } else if (prevTown != null && town != null &&
                !prevTown.equals(town)) {
            exitTown(player, civilian, prevTown, prevTownType);
            enterTown(player, civilian, town, townType);
        } else if (town == null && prevTown != null) {
            exitTown(player, civilian, prevTown, prevTownType);
        }

        if (town == null && prevTown != null) {
            lastTown.remove(player.getUniqueId());
        } else if (town != null) {
            lastTown.put(player.getUniqueId(), town);
        }
    }

    private void enterTown(Player player, Civilian civilian, Town town, TownType townType) {
        PlayerEnterTownEvent playerEnterTownEvent = new PlayerEnterTownEvent(player.getUniqueId(),
                town, townType);
        Bukkit.getPluginManager().callEvent(playerEnterTownEvent);
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        String govName = "Unknown";
        if (government != null) {
            govName = government.getNames().get(civilian.getLocale());
        }
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "enter-town").replace("$1", town.getName())
                .replace("$2", govName));
    }
    private void exitTown(Player player, Civilian civilian, Town town, TownType townType) {
        PlayerExitTownEvent playerExitTownEvent = new PlayerExitTownEvent(player.getUniqueId(),
                town, townType);
        Bukkit.getPluginManager().callEvent(playerExitTownEvent);
        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        String govName = "Unknown";
        if (government != null) {
            govName = government.getNames().get(civilian.getLocale());
        }
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "exit-town").replace("$1", town.getName())
                .replace("$2", govName));
    }

    private void playerInRegion(Player player) {
        RegionManager regionManager = RegionManager.getInstance();
        ArrayList<Region> containedRegions = new ArrayList<>();
        containedRegions.addAll(regionManager.getRegionEffectsAt(player.getLocation(), 0));

        for (Region region : containedRegions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            PlayerInRegionEvent playerInRegionEvent = new PlayerInRegionEvent(player.getUniqueId(),
                    region, regionType);
            Bukkit.getPluginManager().callEvent(playerInRegionEvent);
        }

        ArrayList<Region> previousRegions = lastRegion.get(player.getUniqueId());
        if (previousRegions == null) {
            previousRegions = new ArrayList<>();
        }

        for (Region r : containedRegions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(r.getType());
            if (!previousRegions.contains(r)) {
                PlayerEnterRegionEvent playerEnterRegionEvent = new PlayerEnterRegionEvent(player.getUniqueId(),
                        r, regionType);
                Bukkit.getPluginManager().callEvent(playerEnterRegionEvent);
            }
        }

        for (Region r : previousRegions) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(r.getType());
            if (!containedRegions.contains(r)) {
                PlayerExitRegionEvent playerExitRegionEvent = new PlayerExitRegionEvent(player.getUniqueId(),
                        r, regionType);
                Bukkit.getPluginManager().callEvent(playerExitRegionEvent);
            }
        }

        if (!containedRegions.isEmpty()) {
            lastRegion.put(player.getUniqueId(), containedRegions);
        } else {
            lastRegion.remove(player.getUniqueId());
        }
    }
}

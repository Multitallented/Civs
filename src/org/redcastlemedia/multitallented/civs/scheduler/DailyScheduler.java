package org.redcastlemedia.multitallented.civs.scheduler;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class DailyScheduler implements Runnable {


    @Override
    public void run() {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getAllRegions()) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            boolean hasUpkeep = regionType.isDailyPeriod() && region.runUpkeep(false);
        }

        doTaxes();
    }

    private void doTaxes() {
        if (Civs.econ == null) {
            return;
        }
        HashSet<Town> saveThese = new HashSet<>();
        for (Town town : TownManager.getInstance().getTowns()) {
            if (town.getTaxes() < 1) {
                continue;
            }
            for (UUID uuid : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(uuid).contains("ally")) {
                    continue;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer == null || !Civs.econ.has(offlinePlayer, town.getTaxes())) {
                    continue;
                }
                Civs.econ.withdrawPlayer(offlinePlayer, town.getTaxes());
                town.setBankAccount(town.getBankAccount() + town.getTaxes());
                saveThese.add(town);
            }
        }
        for (Town town : saveThese) {
            TownManager.getInstance().saveTown(town);
        }
    }
}

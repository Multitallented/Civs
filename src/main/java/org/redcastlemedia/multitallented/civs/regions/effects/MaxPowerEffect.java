package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

@CivsSingleton
public class MaxPowerEffect implements RegionCreatedListener, DestroyRegionListener {
    public static String KEY = "max_power";
    private static MaxPowerEffect instance = null;

    public static MaxPowerEffect getInstance() {
        if (instance == null) {
            instance = new MaxPowerEffect();
        }
        return instance;
    }

    public MaxPowerEffect() {
        RegionManager regionManager = RegionManager.getInstance();
        regionManager.addRegionCreatedListener(KEY, this);
        regionManager.addDestroyRegionListener(KEY, this);
    }

    @Override
    public void destroyRegionHandler(Region region) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town == null) {
            return;
        }
        town.setMaxPower(town.getMaxPower() - Integer.parseInt(region.getEffects().get(KEY)));
        TownManager.getInstance().saveTown(town);
    }

    @Override
    public void regionCreatedHandler(Region region) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town == null) {
            return;
        }
        town.setMaxPower(town.getMaxPower() + Integer.parseInt(region.getEffects().get(KEY)));
        TownManager.getInstance().saveTown(town);
    }
}

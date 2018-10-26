package org.redcastlemedia.multitallented.civs.regions.effects;

import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class HousingEffect implements RegionCreatedListener, DestroyRegionListener {
    public static String KEY = "housing";

    public HousingEffect() {
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
        RegionManager.getInstance().addDestroyRegionListener(KEY, this);
    }

    @Override
    public void destroyRegionHandler(Region region) {
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        int amount = 1;
        if (region.getEffects().get(KEY) != null) {
            amount = Integer.parseInt(regionType.getEffects().get(KEY));
        }
        if (town != null && town.getHousing() > amount) {
            town.setHousing(town.getHousing() - amount);
            TownManager.getInstance().saveTown(town);
        }
    }

    @Override
    public void regionCreatedHandler(Region region) {
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        int amount = 1;
        if (region.getEffects().get(KEY) != null) {
            amount = Integer.parseInt(region.getEffects().get(KEY));
        }
        if (town != null) {
            town.setHousing(town.getHousing() + amount);
            TownManager.getInstance().saveTown(town);
        }
    }
}

package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class HousingEffect implements CreateRegionListener, DestroyRegionListener {
    public static String KEY = "housing";

    public HousingEffect() {
        RegionManager.getInstance().addCreateRegionListener(KEY, this);
        RegionManager.getInstance().addDestroyRegionListener(KEY, this);
    }

    @Override
    public boolean createRegionHandler(Block block, Player player) {
        Town town = TownManager.getInstance().getTownAt(block.getLocation());
        if (town != null) {
            town.setHousing(town.getHousing() + 1);
            TownManager.getInstance().saveTown(town);
        }
        return true;
    }

    @Override
    public void destroyRegionHandler(Region region) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town != null && town.getHousing() > 1) {
            town.setHousing(town.getHousing() - 1);
            TownManager.getInstance().saveTown(town);
        }
    }
}

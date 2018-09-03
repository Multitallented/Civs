package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class HousingEffect implements CreateRegionListener, DestroyRegionListener {
    public static String KEY = "housing";

    public HousingEffect() {
        RegionManager.getInstance().addCreateRegionListener(KEY, this);
        RegionManager.getInstance().addDestroyRegionListener(KEY, this);
    }

    @Override
    public boolean createRegionHandler(Block block, Player player, RegionType regionType) {
        Town town = TownManager.getInstance().getTownAt(block.getLocation());
        int amount = Integer.parseInt(regionType.getEffects().get(KEY));
        if (town != null) {
            town.setHousing(town.getHousing() + amount);
            TownManager.getInstance().saveTown(town);
        }
        return true;
    }

    @Override
    public void destroyRegionHandler(Region region) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        int amount = Integer.parseInt(regionType.getEffects().get(KEY));
        if (town != null && town.getHousing() > amount) {
            town.setHousing(town.getHousing() - amount);
            TownManager.getInstance().saveTown(town);
        }
    }
}

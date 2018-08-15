package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Villager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class VillagerEffect implements CreateRegionListener, DestroyRegionListener {

    public VillagerEffect() {
        RegionManager regionManager = RegionManager.getInstance();
        regionManager.addCreateRegionListener("villager", this);
        regionManager.addDestroyRegionListener("villager", this);
    }

    @Override
    public boolean createRegionHandler(Block block) {
        if (block.getRelative(BlockFace.UP, 1).getType() != Material.AIR ||
                block.getRelative(BlockFace.UP, 2).getType() != Material.AIR) {
            //TODO send message to player?
            return false;
        }

        block.getWorld().spawn(block.getLocation(), Villager.class);

        //TODO bump the housing
        //TODO track number of villagers per town
        return true;
    }

    @Override
    public void destroyRegionHandler(Region region) {
        //TODO decrement villagers per town
    }
}

package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

public interface CreateRegionListener {
    boolean createRegionHandler(Block block, Player player, RegionType regionType);
}

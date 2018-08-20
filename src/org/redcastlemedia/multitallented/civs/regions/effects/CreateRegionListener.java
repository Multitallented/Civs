package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface CreateRegionListener {
    boolean createRegionHandler(Block block, Player player);
}

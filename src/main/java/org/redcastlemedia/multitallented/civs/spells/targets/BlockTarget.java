package org.redcastlemedia.multitallented.civs.spells.targets;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.spells.Spell;

import java.util.HashSet;
import java.util.Set;

public class BlockTarget extends Target {

    public BlockTarget(Spell spell,
                       String key,
                       Entity origin,
                       int level,
                       ConfigurationSection config) {
        super(spell, key, origin, level, config);
    }


    public Set<?> getTargets() {
        Set<Block> returnSet = new HashSet<>();
        if (!(getOrigin() instanceof LivingEntity)) {
            return returnSet;
        }
        LivingEntity player = (LivingEntity) getOrigin();
        int level = getLevel();
        int range = (int) Math.round(Spell.getLevelAdjustedValue(
                getConfig().getString("range","15"), level, null, null));
        int yOffset = (int) Math.round(Spell.getLevelAdjustedValue(
                getConfig().getString("offset-y","0"), level, null, null));

        if (range < 1) {
            if (yOffset > 0) {
                returnSet.add(player.getLocation().getBlock().getRelative(BlockFace.UP, yOffset));
            } else if (yOffset < 0) {
                returnSet.add(player.getLocation().getBlock().getRelative(BlockFace.DOWN, -yOffset));
            } else {
                returnSet.add(player.getLocation().getBlock());
            }
        } else {
            HashSet<Material> byteSet = new HashSet<>();
            if (yOffset > 0) {
                returnSet.add(player.getTargetBlock(byteSet, range).getRelative(BlockFace.UP, yOffset));
            } else if (yOffset < 0) {
                returnSet.add(player.getTargetBlock(byteSet, range).getRelative(BlockFace.DOWN, -yOffset));
            } else {
                returnSet.add(player.getTargetBlock(byteSet, range));
            }
        }
        return returnSet;
    }
}

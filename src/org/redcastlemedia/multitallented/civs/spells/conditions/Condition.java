package org.redcastlemedia.multitallented.civs.spells.conditions;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.spells.Spell;

import java.util.HashSet;

/**
 *
 * @author Multitallented
 */
public abstract class Condition {
    private final FileConfiguration node;
    public Condition(FileConfiguration config) {
        this.node = config;
    }

    public void meetsRequirement(Spell spell, HashSet<Object> targets, int level) {
        for (Object obj : targets) {
            if (obj.getClass().equals(Player.class)) {
                meetsRequirement(spell, (Player) obj, level);
            } else if (obj.getClass().equals(Block.class)) {
                meetsRequirement(spell, (Block) obj, level);
            } else if (obj instanceof Entity) {
                meetsRequirement(spell, (Entity) obj, level);
            }
        }
    }

    abstract void meetsRequirement(Spell spell, Player player, int level);
    abstract void meetsRequirement(Spell spell, Block block, int level);
    abstract void meetsRequirement(Spell spell, Entity entity, int level);
}
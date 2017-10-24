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

    public void testCondition(Spell spell, HashSet<Object> targets) {
        for (Object obj : targets) {
            if (obj.getClass().equals(Player.class)) {
                testCondition(spell, (Player) obj);
            } else if (obj.getClass().equals(Block.class)) {
                testCondition(spell, (Block) obj);
            } else if (obj instanceof Entity) {
                testCondition(spell, (Entity) obj);
            }
        }
    }

    abstract void testCondition(Spell spell, Player player);
    abstract void testCondition(Spell spell, Block block);
    abstract void testCondition(Spell spell, Entity entity);
}
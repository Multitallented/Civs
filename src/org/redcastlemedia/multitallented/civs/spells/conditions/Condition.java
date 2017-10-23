package org.redcastlemedia.multitallented.civs.spells.conditions;

import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

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

    public void testCondition(HashSet<Object> targets) {
        for (Object obj : targets) {
            if (obj.getClass().equals(Player.class)) {
                testCondition(CivilianManager.getInstance().getCivilian(((Player) obj).getUniqueId()));
            } else if (obj.getClass().equals(Block.class)) {
                testCondition((Block) obj);
            } else if (obj instanceof Entity) {
                testCondition((Entity) obj);
            }
        }
    }

    abstract void testCondition(Civilian civilian);
    abstract void testCondition(Block block);
    abstract void testCondition(Entity entity);
}
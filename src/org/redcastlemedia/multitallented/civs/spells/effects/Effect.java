package org.redcastlemedia.multitallented.civs.spells.effects;

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
public abstract class Effect {
    private final FileConfiguration node;
    public final String NAME;

    public Effect(FileConfiguration config, String name) {
        this.node = config;
        NAME = config.getString("name", "unnamed");
    }

    public void execute(HashSet<Object> targetSet) {
        for (Object obj : targetSet) {
            if (obj.getClass().equals(Player.class)) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(((Player) obj).getUniqueId());
                execute(civilian);
            } else if (obj.getClass().equals(Block.class)) {
                execute((Block) obj);
            } else if (obj instanceof Entity) {
                execute((Entity) obj);
            }
        }
    }

    public abstract void execute(Civilian civilian);
    public abstract void execute(Block block);
    public abstract void execute(Entity entity);
}
package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellComponent;

import java.util.HashSet;

/**
 *
 * @author Multitallented
 */
public abstract class Effect extends SpellComponent {
    protected final ConfigurationSection node;
    public final String NAME;

    public Effect(ConfigurationSection config) {
        this.node = config;
        NAME = config.getString("name", "unnamed");
    }

    public void execute(Spell spell, HashSet<Object> targetSet) {
        for (Object obj : targetSet) {
            if (obj instanceof Player) {
                execute(spell, (Player) obj);
            } else if (obj instanceof Block) {
                execute(spell, (Block) obj);
            } else if (obj instanceof Entity) {
                execute(spell, (Entity) obj);
            }
        }
    }

    public abstract void execute(Spell spell, Player player);
    public abstract void execute(Spell spell, Block block);
    public abstract void execute(Spell spell, Entity entity);
}
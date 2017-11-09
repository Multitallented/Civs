package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellComponent;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Multitallented
 */
public abstract class Effect extends SpellComponent {

    public Effect(Spell spell,
                  String key,
                  Object target,
                  Entity origin,
                  int level,
                  ConfigurationSection section) {
        super(spell, key, target, origin, level);
    }
    public Effect(Spell spell,
                  String key,
                  Object target,
                  Entity origin,
                  int level,
                  String configString) {
        super(spell, key, target, origin, level);
    }

    public abstract void apply();

    public abstract boolean meetsRequirement();

    public void remove() {
        //Optional override
    }
    public HashMap<String, Double> getVariables() {
        //Optional override
        return new HashMap<>();
    }
}
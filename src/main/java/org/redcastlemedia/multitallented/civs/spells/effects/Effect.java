package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellComponent;

import java.util.HashMap;

/**
 *
 * @author Multitallented
 */
public abstract class Effect extends SpellComponent {

    public Effect(Spell spell,
                  String key,
                  Object target,
                  Entity origin,
                  int level) {
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

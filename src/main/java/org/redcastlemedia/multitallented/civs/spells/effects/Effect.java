package org.redcastlemedia.multitallented.civs.spells.effects;

import java.util.HashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellComponent;

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

    public void remove(LivingEntity origin, int level, Spell spell) {
        //Optional override
    }
    @Override
    public HashMap<String, Double> getVariables(Object target, Entity origin, int level, Spell spell) {
        //Optional override
        return new HashMap<>();
    }
}

package org.redcastlemedia.multitallented.civs.spells;

import java.util.HashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public abstract class SpellComponent {
    private final Spell spell;
    private final String key;
    private final Object target;
    private final Entity origin;
    private final int level;

    public SpellComponent(Spell spell,
                          String key,
                          Object target,
                          Entity origin,
                          int level) {
        this.spell = spell;
        this.key = key;
        this.target = target;
        this.origin = origin;
        this.level = level;
    }

    public String getKey() { return key; }
    public String getAbilityName() { return spell.getType(); }
    public Spell getSpell() { return spell; }
    public int getLevel() { return level; }
    public Object getTarget() { return target; }
    public Entity getOrigin() { return origin; }

    public void remove(LivingEntity origin, int level, Spell spell) {
        //Optional override
    }
    public HashMap<String, Double> getVariables(Object target, Entity origin, int level, Spell spell) {
        //Optional override
        return null;
    }
}

package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import java.util.HashMap;

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

    /*public boolean damageCheck(Player damager, Entity damagee) {
        EntityDamageEvent event = new EntityDamageEvent(damagee, EntityDamageEvent.DamageCause.CUSTOM, 0);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    public void damage(Player caster, Entity e, int amount, EntityDamageEvent.DamageCause cause, boolean knockback) {
        if (!(e instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) e;
        livingEntity.damage(amount, caster);
//        e.get(HealthComponent.class).damage(amount, cause, caster, knockback);

    }
    public void damage(Player caster, Player damagee, int amount, EntityDamageEvent.DamageCause cause, boolean knockback) {

//        damagee.damage(amount, cause, caster, knockback);
        damagee.damage(amount, caster);
    }*/

    public String getKey() { return key; }
    public String getAbilityName() { return spell.getType(); }
    public Spell getSpell() { return spell; }
    public int getLevel() { return level; }
    public Object getTarget() { return target; }
    public Entity getOrigin() { return origin; }
//    public abstract void setData(ConfigurationSection section, int level, Object target, Spell spell, HashMap<String, HashMap<Object, HashMap<String, Double>>> vars);
//    public abstract void setData(String data, int level, Object target, Spell spell, HashMap<String, HashMap<Object, HashMap<String, Double>>> vars);

    public void remove(Entity origin, int level, Spell spell) {
        //Optional override
    }
    public HashMap<String, Double> getVariables(Object target, Entity origin, int level, Spell spell) {
        //Optional override
        return null;
    }
}

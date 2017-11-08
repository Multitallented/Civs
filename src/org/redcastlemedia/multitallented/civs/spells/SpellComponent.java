package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;

public abstract class SpellComponent {
    private final String abilityName;
    private final String key;

    public SpellComponent(String abilityName, String key) {
        this.abilityName = abilityName;
        this.key = key;
    }

    public boolean damageCheck(Player damager, Entity damagee) {
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
    }

    public String getKey() { return key; }
    public String getAbilityName() { return abilityName; }
    public void setData(String value, int level) {
        setData(value, level, null);
    }
    public void setData(ConfigurationSection section, int level) {
        setData(section, level, null);
    }
    public abstract void setData(ConfigurationSection section, int level, Object target, Spell spell, HashMap<String, HashMap<Object, HashMap<String, Double>>> vars);
    public abstract void setData(String data, int level, Object target, Spell spell, HashMap<String, HashMap<Object, HashMap<String, Double>>> vars);
    public abstract String getTargetName();

    public void remove(Entity origin, int level, Spell spell) {
        //Optional override
    }
    public HashMap<String, Double> getVariables(Object target, Entity origin, int level, Spell spell) {
        //Optional override
        return null;
    }
}

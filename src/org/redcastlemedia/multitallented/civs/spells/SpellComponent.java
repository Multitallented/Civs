package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SpellComponent {
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
}

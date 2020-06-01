package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;
import org.redcastlemedia.multitallented.civs.spells.effects.particles.CivParticleEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.particles.GreenSparks;

public class ParticleEffect extends Effect {
    private CivParticleEffect particle;
    private Long duration;
    private int taskId;
    private int cancelTaskId;

    public ParticleEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            this.particle = getParticleEffectByName(section.getString(SpellEffectConstants.PARTICLE, "green sparks"));
            this.duration = section.getLong(SpellConstants.DURATION, 100);
        } else if (value instanceof String) {
            this.particle = getParticleEffectByName((String) value);
            this.duration = 100L;
        }
    }

    public boolean meetsRequirement() {
        return true;
    }
    public void apply() {
        Object target = getTarget();
        if (duration < 1) {
            return;
        }

        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        long repeatDelay = this.particle.getRepeatDelay();
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(),
                () -> onUpdate(livingEntity), repeatDelay, repeatDelay);
        this.cancelTaskId = Bukkit.getScheduler().runTaskLater(Civs.getInstance(),
                () -> Bukkit.getScheduler().cancelTask(taskId), this.duration).getTaskId();
    }

    @Override
    public void remove(LivingEntity livingEntity, int level, Spell spell) {
        Bukkit.getScheduler().cancelTask(this.taskId);
        Bukkit.getScheduler().cancelTask(this.cancelTaskId);
    }

    private void onUpdate(LivingEntity target) {
        this.particle.update(target);
    }

    private CivParticleEffect getParticleEffectByName(String name) {
        switch (name) {
            default:
            case "green sparks":
                return new GreenSparks();
        }
    }
}

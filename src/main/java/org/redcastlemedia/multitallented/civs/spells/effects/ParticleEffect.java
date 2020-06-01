package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;
import org.redcastlemedia.multitallented.civs.spells.effects.particles.CivParticleEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.particles.FairyWings;
import org.redcastlemedia.multitallented.civs.spells.effects.particles.FallingAura;
import org.redcastlemedia.multitallented.civs.spells.effects.particles.Helix;
import org.redcastlemedia.multitallented.civs.spells.effects.particles.Single;
import org.redcastlemedia.multitallented.civs.spells.effects.particles.Waves;

import lombok.Getter;

public class ParticleEffect extends Effect {
    private CivParticleEffect pattern;
    private Long duration;
    @Getter
    private Long period;
    private int taskId = -1;
    private int cancelTaskId = -1;
    @Getter
    private Particle particleType;
    @Getter
    private int red;
    @Getter
    private int green;
    @Getter
    private int blue;
    @Getter
    private int size;
    @Getter
    private int count;
    @Getter
    private int note;

    public ParticleEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            this.pattern = getParticleEffectByName(section.getString("pattern", "single"));
            this.duration = section.getLong(SpellConstants.DURATION, 100);
            this.period = section.getLong(SpellConstants.PERIOD, 1);
            this.particleType = Particle.valueOf(section.getString(SpellEffectConstants.PARTICLE, "REDSTONE"));
            this.red = section.getInt("red", 255);
            this.green = section.getInt("green", 255);
            this.blue = section.getInt("blue", 255);
            this.size = section.getInt("size", 1);
            this.count = section.getInt("count", 1);
            this.note = section.getInt("note", 24);
        } else if (value instanceof String) {
            this.pattern = new Single();
            this.duration = 100L;
            this.period = 1L;
            this.particleType = Particle.valueOf((String) value);
            this.red = 255;
            this.green = 255;
            this.blue = 255;
            this.size = 1;
            this.count = 1;
            this.note = 6;
        }
    }

    public boolean meetsRequirement() {
        return true;
    }
    public void apply() {
        Object target = getTarget();

        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        long repeatDelay = this.pattern.getRepeatDelay(this);
        if (repeatDelay > 0) {
            this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(),
                    () -> onUpdate(livingEntity), repeatDelay, repeatDelay);

            if (this.duration > 0) {
                this.cancelTaskId = Bukkit.getScheduler().runTaskLater(Civs.getInstance(),
                        () -> Bukkit.getScheduler().cancelTask(taskId), this.duration / 50).getTaskId();
            }
        } else {
            onUpdate(livingEntity);
        }
    }

    @Override
    public void remove(LivingEntity livingEntity, int level, Spell spell) {
        if (this.taskId > -1) {
            Bukkit.getScheduler().cancelTask(this.taskId);
        }
        if (this.cancelTaskId > -1) {
            Bukkit.getScheduler().cancelTask(this.cancelTaskId);
        }
    }

    private void onUpdate(LivingEntity target) {
        this.pattern.update(target, this);
    }

    private CivParticleEffect getParticleEffectByName(String name) {
        switch (name) {
            case "helix":
                return new Helix();
            case "waves":
                return new Waves();
            case "fairy wings":
                return new FairyWings();
            case "falling aura":
                return new FallingAura();
            case "single":
            default:
                return new Single();
        }
    }

    public void spawnParticle(Particle particleType, Location location, int red, int green, int blue, int count, int size, double x, double y, double z, int note) {
        switch (particleType) {
            case REDSTONE:
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);
                location.getWorld().spawnParticle(particleType, location, count, dustOptions);
                return;
            case SPELL_MOB:
            case SPELL_MOB_AMBIENT:
                location.getWorld().spawnParticle(particleType, location, 0, red / 255D, green / 255D, blue / 255D, 1);
                return;
            case NOTE:
                location.getWorld().spawnParticle(particleType, location, 0, note / 24D, 0, 0, 1);
                return;
            case EXPLOSION_NORMAL:
            case FIREWORKS_SPARK:
            case WATER_BUBBLE:
            case WATER_WAKE:
            case CRIT:
            case CRIT_MAGIC:
            case SMOKE_NORMAL:
            case SMOKE_LARGE:
            case PORTAL:
            case ENCHANTMENT_TABLE:
            case FLAME:
            case CLOUD:
            case DRAGON_BREATH:
            case END_ROD:
            case DAMAGE_INDICATOR:
            case TOTEM:
            case SPIT:
            case SQUID_INK:
            case BUBBLE_POP:
                location.getWorld().spawnParticle(particleType, location, 0, x, y, z, 1);
                return;
            default:
                location.getWorld().spawnParticle(particleType, location, count);
        }
    }
}

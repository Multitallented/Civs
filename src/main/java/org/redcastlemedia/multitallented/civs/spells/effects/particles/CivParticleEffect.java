package org.redcastlemedia.multitallented.civs.spells.effects.particles;

import org.bukkit.entity.LivingEntity;

public interface CivParticleEffect {
    void update(LivingEntity livingEntity);
    long getRepeatDelay();
}

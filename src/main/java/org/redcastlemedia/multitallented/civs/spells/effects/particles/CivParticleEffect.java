package org.redcastlemedia.multitallented.civs.spells.effects.particles;

import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.spells.effects.ParticleEffect;

public interface CivParticleEffect {
    void update(LivingEntity livingEntity, ParticleEffect particleEffect);
    long getRepeatDelay(ParticleEffect particleEffect);
}

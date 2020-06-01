package org.redcastlemedia.multitallented.civs.spells.effects.particles;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.spells.effects.ParticleEffect;

public class Single implements CivParticleEffect {
    @Override
    public void update(LivingEntity livingEntity, ParticleEffect particleEffect) {
        Location location = livingEntity.getLocation().clone().add(0, 1, 0);
        particleEffect.spawnParticle(particleEffect.getParticleType(),
                location,
                particleEffect.getRed(), particleEffect.getGreen(), particleEffect.getBlue(),
                particleEffect.getCount(), particleEffect.getSize(),
                0, 0, 0,
                particleEffect.getNote());
    }

    @Override
    public long getRepeatDelay(ParticleEffect particleEffect) {
        return particleEffect.getPeriod();
    }
}

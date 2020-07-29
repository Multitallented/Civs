package org.redcastlemedia.multitallented.civs.spells.effects.particles;

import org.bukkit.Location;
import org.redcastlemedia.multitallented.civs.spells.effects.ParticleEffect;

public class Single extends CivParticleEffect {
    @Override
    public void update(Object target, Location location, ParticleEffect particleEffect) {
        Location location1 = location.clone().add(0, 1, 0);
        particleEffect.spawnParticle(particleEffect.getParticleType(),
                location1,
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

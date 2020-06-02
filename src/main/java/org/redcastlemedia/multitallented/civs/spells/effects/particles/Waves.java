package org.redcastlemedia.multitallented.civs.spells.effects.particles;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.redcastlemedia.multitallented.civs.spells.effects.ParticleEffect;

public class Waves extends CivParticleEffect {
    private final double RADIUS = 1.1; // radius between player and rods
    private final int U_PER_WAVE = 4; // Amount of "U's" per wave.
    private final double MAX_HEIGHT_DIFF = 0.5; // Max height diff between columns
    private final double HEIGHT_DIFF_STEP = 0.05; // Height diff step...
    private boolean heightFactorDir; // Indicates whether the height diff between columns is going up or down (gives dynamism)
    private double heightFactor = MAX_HEIGHT_DIFF; // Height diff between columns. Variates over time with hoveringDirectionUp.


    @Override
    public void update(Object target, Location location, ParticleEffect particleEffect) {
        if (heightFactorDir) {
            if (heightFactor < MAX_HEIGHT_DIFF) heightFactor += HEIGHT_DIFF_STEP;
            else heightFactorDir = false;
        } else {
            if (heightFactor > -MAX_HEIGHT_DIFF) heightFactor -= HEIGHT_DIFF_STEP;
            else heightFactorDir = true;
        }

        Vector v = new Vector(0, 0, 0);

        for (double angle = 0; angle <= 2 * Math.PI; angle += 2 * Math.PI / 45) {
            v.setX(Math.cos(angle) * RADIUS);
            v.setZ(Math.sin(angle) * RADIUS);
            v.setY(0.5 + Math.sin(angle * U_PER_WAVE) * heightFactor);

            particleEffect.spawnParticle(particleEffect.getParticleType(),
                    location.clone().add(v),
                    particleEffect.getRed(), particleEffect.getGreen(), particleEffect.getBlue(),
                    particleEffect.getCount(), particleEffect.getSize(),
                    0, 0, 0,
                    particleEffect.getNote());
            particleEffect.spawnParticle(particleEffect.getParticleType(),
                    location.clone().add(v).add(0, 1, 0),
                    particleEffect.getRed(), particleEffect.getGreen(), particleEffect.getBlue(),
                    particleEffect.getCount(), particleEffect.getSize(),
                    0, 0, 0,
                    particleEffect.getNote());
        }
    }

    @Override
    public long getRepeatDelay(ParticleEffect particleEffect) {
        return 4;
    }
}

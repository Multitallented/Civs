package org.redcastlemedia.multitallented.civs.spells.effects.particles;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class GreenSparks implements CivParticleEffect {
    boolean up;
    float height;
    int step;

    @Override
    public long getRepeatDelay() {
        return 1;
    }

    @Override
    public void update(LivingEntity livingEntity) {
        if (up) {
            if (height < 2)
                height += 0.05;
            else
                up = false;
        } else {
            if (height > 0)
                height -= 0.05;
            else
                up = true;
        }
        double inc = (2 * Math.PI) / 100;
        double angle = step * inc;
        Vector v = new Vector();
        v.setX(Math.cos(angle) * 1.1);
        v.setZ(Math.sin(angle) * 1.1);
        Location location = livingEntity.getLocation().clone().add(v).add(0, height, 0);
//        UtilParticles.display(getType().getEffect(), location);
        step += 4;
    }
}

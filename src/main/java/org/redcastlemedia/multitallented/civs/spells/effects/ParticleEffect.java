package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

public class ParticleEffect extends Effect {
    private int damage = 0;
    private String target = "self";

    private String particle;
    private String pattern;
    private Long duration;

    public ParticleEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            this.particle = section.getString(SpellEffectConstants.PARTICLE, "reddust");
            this.pattern = section.getString("pattern", "reddust");
            this.duration = section.getLong(SpellConstants.DURATION, 100);

            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            }
        } else if (value instanceof String) {
            this.particle = (String) value;
            this.pattern = (String) value;
            this.duration = 100L;
            this.target = "self";
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
        final BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                onUpdate();
            }
        };
        runnable.runTaskAsynchronously(Civs.getInstance());
        if (duration > 0) {
            BukkitRunnable runnable1 = new BukkitRunnable() {
                @Override
                public void run() {
                    runnable.cancel();
                }
            };
        }
    }
    private void onUpdate() {
        switch (this.pattern) {
            case "Green Sparks":
                //TODO Math goes here
                break;
        }
    }
}

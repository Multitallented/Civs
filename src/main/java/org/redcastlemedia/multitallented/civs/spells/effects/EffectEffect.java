package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.spells.Spell;

public class EffectEffect extends Effect {
    private String effectName;
    private int data = 1;

    public EffectEffect(Spell spell, String key, Object target, Entity origin, int level, Object config) {
        super(spell, key, target, origin, level);
        if (config instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) config;
            this.effectName = section.getString("effect", "EXTINGUISH").toUpperCase();
            this.data = section.getInt("data", 1);
        } else if (config instanceof String) {
            this.effectName = ((String) config).toUpperCase();
            this.data = 1;
        }
    }

    @Override
    public void apply() {
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {

            if (target instanceof Block) {
                Block block = (Block) target;
                Location location = block.getLocation();
                playEffect(location, effectName, this.data);
            }
            return;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        Location location = livingEntity.getLocation();
        playEffect(location, effectName, this.data);
    }

    private void playEffect(Location location, String effectName, int data) {
        switch (effectName) {
            case "LIGHTNING":
                location.getWorld().strikeLightningEffect(location);
                return;
            default:
                location.getWorld().playEffect(location, org.bukkit.Effect.valueOf(effectName), data);
        }

    }

    @Override
    public boolean meetsRequirement() {
        return true;
    }
}

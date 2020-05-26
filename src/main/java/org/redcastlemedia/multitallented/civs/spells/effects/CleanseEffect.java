package org.redcastlemedia.multitallented.civs.spells.effects;

import java.util.HashSet;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

public class CleanseEffect extends Effect {

    private CleanseTypeEnum cleanseTypeEnum;
    private String target = "self";

    public CleanseEffect(Spell spell, String key, Object target, Entity origin,
                         int level, Object configSettings) {
        super(spell, key, target, origin, level);
        if (configSettings instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) configSettings;
            this.cleanseTypeEnum = CleanseTypeEnum.valueOf(section.getString("type"));
            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = "self";
            }
        } else if (configSettings instanceof String) {
            this.cleanseTypeEnum = CleanseTypeEnum.valueOf((String) configSettings);
        }
    }

    @Override
    public void apply() {
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        for (PotionEffect potionEffect : new HashSet<>(livingEntity.getActivePotionEffects())) {
            if (this.cleanseTypeEnum == CleanseTypeEnum.BOTH ||
                    ((this.cleanseTypeEnum == CleanseTypeEnum.HARMFUL &&
                    isHarmful(potionEffect.getType())) ||
                    (this.cleanseTypeEnum == CleanseTypeEnum.BENEFICIAL &&
                    isBeneficial(potionEffect.getType())))) {
                livingEntity.removePotionEffect(potionEffect.getType());
            }
        }
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        if (this.cleanseTypeEnum == CleanseTypeEnum.BOTH &&
                !livingEntity.getActivePotionEffects().isEmpty()) {
            return true;
        }
        for (PotionEffect potionEffect : livingEntity.getActivePotionEffects()) {
            if ((this.cleanseTypeEnum == CleanseTypeEnum.HARMFUL &&
                    isHarmful(potionEffect.getType())) ||
                    (this.cleanseTypeEnum == CleanseTypeEnum.BENEFICIAL &&
                            isBeneficial(potionEffect.getType()))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHarmful(PotionEffectType potionEffectType) {
        switch (potionEffectType.getName().toUpperCase()) {
            case "BAD_OMEN":
            case "BLINDNESS":
            case "CONFUSION":
            case "GLOWING":
            case "HARM":
            case "HUNGER":
            case "POISON":
            case "SLOW":
            case "SLOW_DIGGING":
            case "UNLUCK":
            case "WEAKNESS":
            case "WITHER":
                return true;
            default:
                return false;
        }
    }

    public static boolean isBeneficial(PotionEffectType potionEffectType) {
        switch (potionEffectType.getName().toUpperCase()) {
            case "ABSORPTION":
            case "CONDUIT_POWER":
            case "DAMAGE_RESISTANCE":
            case "DOLPHINS_GRACE":
            case "FAST_DIGGING":
            case "FIRE_RESISTANCE":
            case "HERO_OF_THE_VILLAGE":
            case "HEALTH_BOOST":
            case "HEAL":
            case "INVISIBILITY":
            case "JUMP":
            case "INCREASE_DAMAGE":
            case "LEVITATION":
            case "LUCK":
            case "NIGHT_VISION":
            case "REGENERATION":
            case "SATURATION":
            case "SLOW_FALLING":
            case "SPEED":
            case "WATER_BREATHING":
                return true;
            default:
                return false;
        }
    }

    public enum CleanseTypeEnum {
        HARMFUL,
        BENEFICIAL,
        BOTH
    }
}

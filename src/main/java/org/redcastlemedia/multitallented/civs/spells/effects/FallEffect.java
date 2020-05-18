package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

import java.util.HashMap;

public class FallEffect extends Effect {
    private float distance = 0;
    private String target = SpellConstants.SELF;
    private boolean silent = false;
    private boolean setFall = false;

    public FallEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            String configDistance = section.getString(SpellConstants.DISTANCE, "0");
            if (configDistance != null) {
                this.distance = Math.round(Spell.getLevelAdjustedValue(configDistance, level, target, spell));
            }
            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            this.setFall = section.getBoolean(SpellConstants.SET, false);
            this.silent = section.getBoolean(SpellConstants.SILENT, false);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = SpellConstants.SELF;
            }
        } else if (value instanceof String) {
            this.distance = Math.round(Spell.getLevelAdjustedValue((String) value, level, target, spell));
            this.target = SpellConstants.SELF;
            this.silent = false;
            this.setFall = false;
        }
    }

    public boolean meetsRequirement() {
        Object target = getTarget();
        Entity origin = getOrigin();
        if (!(target instanceof LivingEntity)) {
            return false;
        }

        LivingEntity livingEntity = (LivingEntity) target;

        if (livingEntity.getFallDistance() < this.distance) {
            if (!this.silent && origin instanceof Player) {
                origin.sendMessage(ChatColor.RED + Civs.getPrefix() + " target isn't hasn't fallen " + this.distance + " blocks.");
            }
            return false;
        }
        return true;
    }
    public void apply() {
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        float newFoodValue = livingEntity.getFallDistance();
        if (this.setFall) {
            newFoodValue = this.distance;
        } else {
            newFoodValue += this.distance;
        }
        livingEntity.setFallDistance(newFoodValue);
    }

    @Override
    public HashMap<String, Double> getVariables() {
        HashMap<String, Double> returnMap = new HashMap<>();
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return returnMap;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        returnMap.put("fallDistance", (double) livingEntity.getFallDistance());
        return returnMap;
    }
}

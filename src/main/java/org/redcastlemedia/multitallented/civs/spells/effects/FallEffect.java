package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;

import java.util.HashMap;

public class FallEffect extends Effect {
    private float distance = 0;
    private String target = "self";
    private boolean silent = false;
    private boolean setFall = false;

    public FallEffect(Spell spell, String key, Object target, Entity origin, int level, ConfigurationSection section) {
        super(spell, key, target, origin, level, section);
        String configDistance = section.getString("distance", "0");
        this.distance = Math.round(Spell.getLevelAdjustedValue(configDistance, level, target, spell));
        String tempTarget = section.getString("target", "not-a-string");
        this.setFall = section.getBoolean("set", false);
        this.silent = section.getBoolean("silent", false);
        if (!tempTarget.equals("not-a-string")) {
            this.target = tempTarget;
        } else {
            this.target = "self";
        }
    }

    public FallEffect(Spell spell, String key, Object target, Entity origin, int level, String value) {
        super(spell, key, target, origin, level, value);
        this.distance = Math.round(Spell.getLevelAdjustedValue(value, level, target, spell));
        this.target = "self";
        this.silent = false;
        this.setFall = false;
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
                ((Player) origin).sendMessage(ChatColor.RED + Civs.getPrefix() + " target isn't hasn't fallen " + this.distance + " blocks.");
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
        HashMap<String, Double> returnMap = new HashMap<String, Double>();
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return returnMap;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        returnMap.put("fallDistance", (double) livingEntity.getFallDistance());
        return returnMap;
    }
}

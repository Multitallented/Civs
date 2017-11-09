package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;

import java.util.HashMap;

public class StaminaEffect extends Effect {
    private int stamina = 0;
    private String target = "self";
    private boolean silent = false;
    private boolean setFood = false;

    public StaminaEffect(Spell spell, String key, Object target, Entity origin, int level, ConfigurationSection section) {
        super(spell, key, target, origin, level, section);
        String configDamage = section.getString("stamina", "0");
        this.stamina = (int) Math.round(Spell.getLevelAdjustedValue(configDamage, level, target, spell));
        String tempTarget = section.getString("target", "not-a-string");
        this.silent = section.getBoolean("silent", false);
        this.setFood = section.getBoolean("set", false);
        if (!tempTarget.equals("not-a-string")) {
            this.target = tempTarget;
        } else {
            this.target = "self";
        }
    }
    public StaminaEffect(Spell spell, String key, Object target, Entity origin, int level, String value) {
        super(spell, key, target, origin, level, value);
        this.stamina = (int) Math.round(Spell.getLevelAdjustedValue(value, level, target, spell));
        this.target = "self";
        this.silent = false;
        this.setFood = false;
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        Entity origin = getOrigin();
        if (!(target instanceof Player)) {
            if (!this.silent && origin instanceof Player) {
                ((Player) origin).sendMessage(ChatColor.RED + Civs.getPrefix() + " target doesn't have stamina.");
            }
            return false;
        }

        Player player = (Player) target;
        int newFoodValue = player.getFoodLevel();
        newFoodValue += stamina;

        if (newFoodValue < 0) {
            if (!this.silent && origin instanceof Player) {
                ((Player) origin).sendMessage(ChatColor.RED + Civs.getPrefix() + " target doesn't have " + stamina + "stamina.");
            }
            return false;
        }
        return true;
    }
    @Override
    public void apply() {
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;
        int newFoodValue = player.getFoodLevel();
        if (this.setFood) {
            newFoodValue = this.stamina;
        } else {
            newFoodValue += this.stamina;
        }
        newFoodValue = Math.min(20, newFoodValue);
        newFoodValue = Math.max(0, newFoodValue);
        player.setFoodLevel(newFoodValue);
    }

    @Override
    public HashMap<String, Double> getVariables() {
        HashMap<String, Double> returnMap = new HashMap<String, Double>();
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return returnMap;
        }
        Player player = (Player) target;
        returnMap.put("stamina", (double) player.getFoodLevel());
        return returnMap;
    }
}

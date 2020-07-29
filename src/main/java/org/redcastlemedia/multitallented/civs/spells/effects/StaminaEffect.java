package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

import java.util.HashMap;

public class StaminaEffect extends Effect {
    private int stamina = 0;
    private String target = "self";
    private boolean silent = false;
    private boolean setFood = false;

    public StaminaEffect(Spell spell, String key, Object target, Entity origin, int level, Object sectionObj) {
        super(spell, key, target, origin, level);
        if (sectionObj instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) sectionObj;
            String configDamage = section.getString(SpellEffectConstants.STAMINA, "0");
            if (configDamage != null) {
                this.stamina = (int) Math.round(Spell.getLevelAdjustedValue(configDamage, level, target, spell));
            }
            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            this.silent = section.getBoolean(SpellConstants.SILENT, false);
            this.setFood = section.getBoolean("set", false);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = "self";
            }
        } else if (sectionObj instanceof String) {
            this.stamina = (int) Math.round(Spell.getLevelAdjustedValue((String) sectionObj, level, target, spell));
            this.target = "self";
            this.silent = false;
            this.setFood = false;
        }
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        Entity origin = getOrigin();
        if (!(target instanceof Player)) {
            if (!this.silent && origin instanceof Player) {
                Player originPlayer = (Player) origin;
                originPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                        .getTranslationWithPlaceholders(originPlayer,
                        "need-more-stamina").replace("$1", "" + Math.abs(stamina)));
            }
            return false;
        }

        Player player = (Player) target;
        int newFoodValue = player.getFoodLevel();
        newFoodValue += stamina;

        if (newFoodValue < 0) {
            if (!this.silent && origin instanceof Player) {
                Player originPlayer = (Player) origin;
                originPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                        .getTranslationWithPlaceholders(originPlayer,
                        "need-more-stamina").replace("$1", "" + Math.abs(stamina)));
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
    public HashMap<String, Double> getVariables(Object target, Entity origin, int level, Spell spell) {
        HashMap<String, Double> returnMap = new HashMap<>();
        if (!(target instanceof Player)) {
            return returnMap;
        }
        Player player = (Player) target;
        returnMap.put(SpellEffectConstants.STAMINA, (double) player.getFoodLevel());
        return returnMap;
    }
}

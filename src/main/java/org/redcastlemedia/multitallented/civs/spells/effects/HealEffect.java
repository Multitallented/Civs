package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

import java.util.HashMap;

public class HealEffect extends Effect {
    private int heal = 0;
    private String target = "self";
    private boolean silent = false;

    public HealEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            String configHeal = section.getString(SpellEffectConstants.HEAL, "0");
            if (configHeal != null) {
                this.heal = (int) Math.round(Spell.getLevelAdjustedValue(configHeal, level, target, spell));
            }
            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            this.silent = section.getBoolean(SpellConstants.SILENT, false);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            }
        } else if (value instanceof String) {
            this.heal = (int) Math.round(Spell.getLevelAdjustedValue((String) value, level, target, spell));
            this.target = SpellConstants.SELF;
            this.silent = false;
        }
    }

    public boolean meetsRequirement() {
        Object target = getTarget();
        Entity origin = getOrigin();
        if (!(target instanceof LivingEntity)) {
            if (!this.silent && origin instanceof Player) {
                origin.sendMessage(ChatColor.RED + Civs.getPrefix() + " target cant't be healed.");
            }
            return false;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        if (livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - livingEntity.getHealth() < this.heal) {
            if (!this.silent && origin instanceof Player) {
                origin.sendMessage(ChatColor.RED + Civs.getPrefix() + " already has enough health.");
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
        Player player = null;
        if (livingEntity instanceof Player) {
            player = (Player) livingEntity;
//            NCPExemptionManager.exemptPermanently(player, CheckType.FIGHT);
        }
        EntityRegainHealthEvent event = new EntityRegainHealthEvent(livingEntity, this.heal, EntityRegainHealthEvent.RegainReason.CUSTOM);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (player != null) {
//                NCPExemptionManager.unexempt(player, CheckType.FIGHT);
            }
            return;
        }
        double health = livingEntity.getHealth();
        double maxHealth = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        livingEntity.setHealth(Math.min(maxHealth, health + event.getAmount()));
//        if (player != null) {
//            NCPExemptionManager.unexempt(player, CheckType.FIGHT);
//        }
    }

    @Override
    public HashMap<String, Double> getVariables() {
        Object target = getTarget();
        HashMap<String, Double> returnMap = new HashMap<>();
        if (!(target instanceof LivingEntity)) {
            return returnMap;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        returnMap.put("health", livingEntity.getHealth());
        returnMap.put("maxHealth", livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        return returnMap;
    }
}

package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;

import java.util.HashMap;

/**
 *
 * @author Multitallented
 */
public class DamageEffect extends Effect {
    private int damage = 0;
    private String target = "self";
    private boolean ignoreArmor = false;
    private boolean silent = true;


    public DamageEffect(String abilityName, String key, ConfigurationSection config) {
        super(abilityName, key);
    }

    @Override
    public void setData(String value, int level, Object target, Spell spell, HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables) {
        this.damage = (int) Math.round(Spell.getLevelAdjustedValue(abilityVariables, value, level, target, spell));
        this.target = "self";
        this.ignoreArmor = false;
        this.silent = true;
    }

    @Override
    public void setData(ConfigurationSection section, int level, Object target, Spell spell, HashMap<String, HashMap<Object, HashMap<String, Double>>> abilityVariables) {
        String configDamage = section.getString("damage", "0");
        this.damage = (int) Math.round(Spell.getLevelAdjustedValue(abilityVariables, configDamage, level, target, spell));
        String tempTarget = section.getString("target", "not-a-string");
        this.silent = section.getBoolean("silent", true);
        if (!tempTarget.equals("not-a-string")) {
            this.target = tempTarget;
        }
        this.ignoreArmor = section.getBoolean("ignore-armor", false);
    }

    @Override
    public boolean meetsRequirement(Object target, Entity origin, int level, Spell ability) {
        if (!(target instanceof LivingEntity)) {
            if (!this.silent && origin instanceof Player) {
                ((Player) origin).sendMessage(Civs.getPrefix() + " target cant't take damage.");
            }
            return false;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        Player player = null;
        if (livingEntity instanceof Player) {
            player = (Player) livingEntity;
            NCPExemptionManager.exemptPermanently(player, CheckType.FIGHT);
        }
        if (livingEntity.getHealth() < damage) {
            if (!this.silent && origin instanceof Player) {
                ((Player) origin).sendMessage(Civs.getPrefix() + " target cant't take " + damage + " damage.");
            }
            if (player != null) {
                NCPExemptionManager.unexempt(player, CheckType.FIGHT);
            }
            return false;
        }
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(origin, livingEntity, EntityDamageEvent.DamageCause.CUSTOM, damage);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (!this.silent && origin instanceof Player) {
                ((Player) origin).sendMessage(Civs.getPrefix() + " target can't be damaged.");
            }
            if (player != null) {
                NCPExemptionManager.unexempt(player, CheckType.FIGHT);
            }
            return false;
        }
        if (player != null) {
            NCPExemptionManager.unexempt(player, CheckType.FIGHT);
        }
        return true;
    }
    public String getTargetName() {
        return this.target;
    }

    @Override
    public void apply(Object target, Entity origin, int level, Spell spell) {
        if (!(target instanceof LivingEntity)) {
            return;
        }
        final LivingEntity livingEntity = (LivingEntity) target;
        final Entity finalOrigin = origin;
        Player player = null;
        if (origin instanceof Player) {
            player = (Player) origin;
            NCPExemptionManager.exemptPermanently(player, CheckType.FIGHT);
        }
        int damage = this.damage;
        if (this.ignoreArmor && livingEntity instanceof Player) {
            damage = adjustForArmor(damage, player);
        }
        livingEntity.damage(damage, origin);

        if (player != null) {
            NCPExemptionManager.unexempt(player, CheckType.FIGHT);
        }
    }

    private int adjustForArmor(int damage, Player player) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();
        double red = 0.0;
        if (helmet != null) {
            if (helmet.getType() == Material.LEATHER_HELMET) red = red + 0.04;
            else if (helmet.getType() == Material.GOLD_HELMET) red = red + 0.08;
            else if (helmet.getType() == Material.CHAINMAIL_HELMET) red = red + 0.08;
            else if (helmet.getType() == Material.IRON_HELMET) red = red + 0.08;
            else if (helmet.getType() == Material.DIAMOND_HELMET) red = red + 0.12;
        }

        if (boots != null) {
            if (boots.getType() == Material.LEATHER_BOOTS) red = red + 0.04;
            else if (boots.getType() == Material.GOLD_BOOTS) red = red + 0.04;
            else if (boots.getType() == Material.CHAINMAIL_BOOTS) red = red + 0.04;
            else if (boots.getType() == Material.IRON_BOOTS) red = red + 0.08;
            else if (boots.getType() == Material.DIAMOND_BOOTS) red = red + 0.12;
        }

        if (pants != null) {
            if (pants.getType() == Material.LEATHER_LEGGINGS) red = red + 0.08;
            else if (pants.getType() == Material.GOLD_LEGGINGS) red = red + 0.12;
            else if (pants.getType() == Material.CHAINMAIL_LEGGINGS) red = red + 0.16;
            else if (pants.getType() == Material.IRON_LEGGINGS) red = red + 0.20;
            else if (pants.getType() == Material.DIAMOND_LEGGINGS) red = red + 0.24;
        }

        if (chest != null) {
            if (chest.getType() == Material.LEATHER_CHESTPLATE) red = red + 0.12;
            else if (chest.getType() == Material.GOLD_CHESTPLATE) red = red + 0.20;
            else if (chest.getType() == Material.CHAINMAIL_CHESTPLATE) red = red + 0.20;
            else if (chest.getType() == Material.IRON_CHESTPLATE) red = red + 0.24;
            else if (chest.getType() == Material.DIAMOND_CHESTPLATE) red = red + 0.32;
        }
        if (red == 0) {
            return damage;
        } else {
            return (int) Math.round(damage / (1 - red));
        }
    }

}
package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

/**
 *
 * @author Multitallented
 */
public class DamageEffect extends Effect {
    private int damage = 0;
    private boolean ignoreArmor = false;
    private boolean silent = true;


    public DamageEffect(Spell spell, String key, Object target, Entity origin, int level, Object config) {
        super(spell, key, target, origin, level);
        if (config instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) config;
            String configDamage = section.getString(SpellEffectConstants.DAMAGE, "0");
            this.damage = (int) Math.round(Spell.getLevelAdjustedValue(configDamage, level, target, spell));
            this.silent = section.getBoolean(SpellConstants.SILENT, true);
            this.ignoreArmor = section.getBoolean("ignore-armor", false);
        } else if (config instanceof String) {
            this.damage = (int) Math.round(Spell.getLevelAdjustedValue((String) config, level, target, spell));
            this.ignoreArmor = false;
            this.silent = true;
        }
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        Entity origin = getOrigin();
        if (!(target instanceof LivingEntity)) {
            if (!this.silent && origin instanceof Player) {
                Player originPlayer = (Player) origin;
                originPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                        .getTranslationWithPlaceholders(originPlayer, "invalid-target"));
            }
            return false;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        if (livingEntity.getHealth() < damage) {
            if (!this.silent && origin instanceof Player) {
                Player originPlayer = (Player) origin;
                originPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                        .getTranslationWithPlaceholders(originPlayer,
                                "need-more-health").replace("$1", "" + Math.abs(damage)));
            }
            return false;
        }
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(origin, livingEntity, EntityDamageEvent.DamageCause.CUSTOM, damage);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (!this.silent && origin instanceof Player) {
                Player originPlayer = (Player) origin;
                originPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                        .getTranslationWithPlaceholders(originPlayer, "invalid-target"));
            }
            return false;
        }
        return true;
    }

    @Override
    public void apply() {
        Object target = getTarget();
        Entity origin = getOrigin();
        if (!(target instanceof LivingEntity)) {
            return;
        }
        final LivingEntity livingEntity = (LivingEntity) target;
        Player player = null;
        if (origin instanceof Player) {
            player = (Player) origin;
//            NCPExemptionManager.exemptPermanently(player, CheckType.FIGHT);
        }
        int damage = this.damage;
        if (this.ignoreArmor && livingEntity instanceof Player) {
            damage = adjustForArmor(damage, player);
        }
        livingEntity.damage(damage, origin);

        if (player != null) {
//            NCPExemptionManager.unexempt(player, CheckType.FIGHT);
        }
    }

    public static int adjustForArmor(int damage, Player player) {
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        ItemStack boots = inv.getBoots();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack pants = inv.getLeggings();
        double red = 0.0;
        if (helmet != null) {
            if (helmet.getType() == Material.LEATHER_HELMET) red = red + 0.04;
            else if (helmet.getType() == Material.GOLDEN_HELMET) red = red + 0.08;
            else if (helmet.getType() == Material.CHAINMAIL_HELMET) red = red + 0.08;
            else if (helmet.getType() == Material.IRON_HELMET) red = red + 0.08;
            else if (helmet.getType() == Material.DIAMOND_HELMET) red = red + 0.12;
        }

        if (boots != null) {
            if (boots.getType() == Material.LEATHER_BOOTS) red = red + 0.04;
            else if (boots.getType() == Material.GOLDEN_BOOTS) red = red + 0.04;
            else if (boots.getType() == Material.CHAINMAIL_BOOTS) red = red + 0.04;
            else if (boots.getType() == Material.IRON_BOOTS) red = red + 0.08;
            else if (boots.getType() == Material.DIAMOND_BOOTS) red = red + 0.12;
        }

        if (pants != null) {
            if (pants.getType() == Material.LEATHER_LEGGINGS) red = red + 0.08;
            else if (pants.getType() == Material.GOLDEN_LEGGINGS) red = red + 0.12;
            else if (pants.getType() == Material.CHAINMAIL_LEGGINGS) red = red + 0.16;
            else if (pants.getType() == Material.IRON_LEGGINGS) red = red + 0.20;
            else if (pants.getType() == Material.DIAMOND_LEGGINGS) red = red + 0.24;
        }

        if (chest != null) {
            if (chest.getType() == Material.LEATHER_CHESTPLATE) red = red + 0.12;
            else if (chest.getType() == Material.GOLDEN_CHESTPLATE) red = red + 0.20;
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

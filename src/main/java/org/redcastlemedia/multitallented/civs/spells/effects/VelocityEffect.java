package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

public class VelocityEffect extends Effect {
    private double multiplier = 0;
    private String target = "self";
    private double x = 0;
    private double y = 1;
    private double z = 0;
    private long exemptionTime = 60;
    private boolean pull = false;

    public VelocityEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            String configMultiplier = section.getString(SpellConstants.MULTIPLIER, "1");
            if (configMultiplier != null) {
                this.multiplier = (double) Math.round(Spell.getLevelAdjustedValue(configMultiplier, level, target, spell));
            }
            this.x = Spell.getLevelAdjustedValue(section.getString("x", "0"), level, target, spell);
            this.y = Spell.getLevelAdjustedValue(section.getString("y", "1"), level, target, spell);
            this.z = Spell.getLevelAdjustedValue(section.getString("z", "0"), level, target, spell);
            this.pull = section.getBoolean("pull", false);

            String tempTarget = section.getString("target", "not-a-string");
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = "self";
            }
        } else if (value instanceof String) {
            this.multiplier = (int) Math.round(Spell.getLevelAdjustedValue((String) value, level, target, spell));
            this.x = 0;
            this.y = 1;
            this.z = 0;
            this.target = "self";
            this.exemptionTime = 60;
            this.pull = false;
        }
    }

    public boolean meetsRequirement() {
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        //TODO check if their velocity is greater than a configurable amount
        if (livingEntity.getVelocity().length() > this.multiplier) {
            return true;
        }
        if (livingEntity instanceof Player) {
            livingEntity.sendMessage(
                    ChatColor.RED + Civs.getPrefix() +
                            ((Player) livingEntity).getDisplayName() +
                            " isn't moving fast enough");
        }
        return false;
    }

    public void apply() {
        Object target = getTarget();
        Entity origin = getOrigin();
        if (!(target instanceof LivingEntity) || !(origin instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) target;

        /*Player player = null;
        if (livingEntity instanceof Player) {
            player = (Player) livingEntity;
        }*/

        //Vector vector = livingEntity.getEyeLocation().getDirection().multiply(1.53d);
        //vector = vector.multiply(new Vector(x, y, z)).multiply(multiplier);
        Vector vector;
        if (this.pull) {
            vector = new Vector(origin.getLocation().getX() - livingEntity.getLocation().getX(),
                    origin.getLocation().getY() - livingEntity.getLocation().getY(),
                    origin.getLocation().getZ() - livingEntity.getLocation().getZ());
        } else {
            vector = livingEntity.getLocation().getDirection();
        }

        vector.add(new Vector(x, 0, z)).setY(y).multiply(multiplier);
        if (vector.getY() < 0.5) {
            vector.setY(0.5);
        }
        livingEntity.setVelocity(vector);
    }
}

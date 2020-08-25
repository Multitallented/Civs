package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

import java.util.HashMap;

public class IgniteEffect extends Effect {
    private int ticks = 0;
    private String target = "self";
    private ConfigurationSection config = null;

    public IgniteEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            String configDamage = section.getString(SpellConstants.TICKS, "60");
            if (configDamage != null) {
                this.ticks = (int) Math.round(Spell.getLevelAdjustedValue(configDamage, level, target, spell));
            }
            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = SpellConstants.SELF;
            }
            this.config = section;
        } else if (value instanceof String) {
            this.ticks = (int) Math.round(Spell.getLevelAdjustedValue((String) value, level, target, spell));
            this.target = SpellConstants.SELF;
        }
    }

    public boolean meetsRequirement() {
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        LivingEntity le = (LivingEntity) target;
        if (le.getFireTicks() > this.ticks) {
            return true;
        }
        return false;
    }

    public void apply() {
        Object target = getTarget();
        Spell spell = getSpell();
        String key = getKey();
        if (!(target instanceof LivingEntity)) {
            return;
        }

        LivingEntity livingEntity = (LivingEntity) target;

        livingEntity.setFireTicks(this.ticks);

        if (!(livingEntity instanceof Player)) {
            //TODO make states for mobs
            return;
        }
        Player player = (Player) livingEntity;
        Civilian champion1 = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        CivState state = champion1.getStates().get(spell.getType() + "." + key);
        if (state != null) {
            state.remove(champion1);
            champion1.getStates().remove(spell.getType() + "." + key);
        }
        HashMap<String, Object> variables = new HashMap<String, Object>();

        final Civilian champion = champion1;
        final String stateName = spell.getType() + "." + key;
        int durationId = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                champion.getStates().remove(stateName);
            }
        }, this.ticks);

        if (config != null) {
            state = new CivState(spell, key, durationId, -1, config, variables);
        } else {
            state = new CivState(spell, key, durationId, -1, "" + this.ticks, variables);
        }

        champion.getStates().put(spell.getType() + "." + key, state);
    }

    @Override
    public void remove(LivingEntity origin, int level, Spell spell) {
        origin.setFireTicks(0);
    }

    @Override
    public HashMap<String, Double> getVariables(Object target, Entity origin, int level, Spell spell) {
        HashMap<String, Double> returnMap = new HashMap<>();
        if (!(target instanceof LivingEntity)) {
            return returnMap;
        }
        LivingEntity le = (LivingEntity) target;
        returnMap.put("fireTicks", (double) le.getFireTicks());
        return returnMap;
    }
}

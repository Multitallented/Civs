package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

import java.util.HashMap;

public class CivPotionEffect extends Effect {

    private PotionEffect potion;
    private PotionEffectType type;
    private int level = 1;
    private int ticks = 0;
    private ConfigurationSection config = null;

    private String target = "self";

    public CivPotionEffect(Spell spell, String key, Object target, Entity origin, int level, Object configSettings) {
        super(spell, key, target, origin, level);
        if (configSettings instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) configSettings;
            this.type = PotionEffectType.getByName(section.getString("type", "POISON"));
            this.ticks = (int) Math.round(Spell.getLevelAdjustedValue("" +
                    section.getInt(SpellConstants.TICKS, 40), level, target, spell));
            this.level = (int) Math.round(Spell.getLevelAdjustedValue("" +
                    section.getInt(SpellConstants.LEVEL, 1), level, target, spell));
            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = "self";
            }
            this.config = section;

            this.potion = new PotionEffect(type, ticks, this.level);
        } else if (configSettings instanceof String) {
            this.type = PotionEffectType.getByName((String) configSettings);
            this.ticks = 40;
            this.level = 1;
            this.target = "self";

            this.potion = new PotionEffect(type, ticks, this.level);
        }
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return false;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        return livingEntity.hasPotionEffect(this.type);
    }

    @Override
    public void apply() {
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        Player player = null;
        if (livingEntity instanceof Player) {
            player = (Player) livingEntity;
        }
        livingEntity.addPotionEffect(this.potion);
        if (player == null) {
            return;
        }
        Civilian champion1 = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        CivState state = champion1.getStates().get(getSpell().getType() + "." + super.getKey());
        if (state != null) {
            state.remove(champion1);
            champion1.getStates().remove(getSpell().getType() + "." + super.getKey());
        }
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("ticks", this.ticks);
        variables.put("level", this.level);

        final Civilian champion = champion1;
        final String stateName = getSpell().getType() + "." + super.getKey();
        int durationId = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                champion.getStates().remove(stateName);
            }
        }, this.ticks);

        if (config != null) {
            state = new CivState(getSpell(), super.getKey(), durationId, -1, config, variables);
        } else {
            state = new CivState(getSpell(), super.getKey(), durationId, -1, "" + this.ticks, variables);
        }

        champion.getStates().put(getSpell().getType() + "." + super.getKey(), state);
    }

    @Override
    public HashMap<String, Double> getVariables() {
        HashMap<String, Double> returnMap = new HashMap<String, Double>();
        Object target = getTarget();
        if (!(target instanceof LivingEntity)) {
            return returnMap;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        if (!livingEntity.hasPotionEffect(this.type)) {
            return returnMap;
        }
        for (PotionEffect potionEffect : livingEntity.getActivePotionEffects()) {
            if (potionEffect.getType().equals(this.type)) {
                returnMap.put("ticks", (double) potionEffect.getDuration());
                returnMap.put("level", (double) potionEffect.getAmplifier());
                break;
            }
        }
        return returnMap;
    }
}

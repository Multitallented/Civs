package org.redcastlemedia.multitallented.civs.spells.effects;

import java.util.HashMap;
import java.util.Map;

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
import org.redcastlemedia.multitallented.civs.spells.civstate.BuiltInCivState;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

public class CivStateEffect extends Effect {
    private long duration;
    private BuiltInCivState builtInCivState;
    private int cancelTaskId = -1;

    public CivStateEffect(Spell spell, String key, Object target, Entity origin, int level, Object configSettings) {
        super(spell, key, target, origin, level);
        if (configSettings instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) configSettings;
            this.builtInCivState = BuiltInCivState.valueOf(section.getString("type", "STUN"));
            this.duration = section.getLong(SpellConstants.DURATION, 1000);
        } else if (configSettings instanceof String) {
            this.builtInCivState = BuiltInCivState.valueOf((String) configSettings);
            this.duration = 1000;
        }
    }

    @Override
    public void apply() {
        Object target = getTarget();
        handleOnApply(target);
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;
        final String stateName = getSpell().getType() + "." + super.getKey();
        final Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        CivState state = civilian.getStates().get(stateName);
        if (state != null) {
            state.remove(civilian);
            civilian.getStates().remove(stateName);
        }
        Map<String, Object> variables = new HashMap<>();
        variables.put(builtInCivState.name(), true);
        CivState civState = new CivState(getSpell(), super.getKey(), -1, -1, (String) null, variables);
        if (duration > 0) {
            cancelTaskId = Bukkit.getScheduler().runTaskLater(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    civState.remove(player);
                }
            }, this.duration / 50).getTaskId();
        }
        civState.setDurationId(cancelTaskId);
        civilian.getStates().put(stateName, civState);
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return false;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        return hasState(civilian, builtInCivState.name());
    }

    public static boolean hasState(Civilian civilian, String stateName) {
        for (CivState civState : civilian.getStates().values()) {
            if (civState.getComponentName().equalsIgnoreCase(stateName)) {
                return true;
            }
        }
        return false;
    }

    private void handleOnApply(Object target) {
        if (!(target instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) target;
        int durationInTicks = (int) (duration / 50);
        switch (builtInCivState) {
            case STUN:
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, durationInTicks, 1));
            case ROOT:
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, durationInTicks, 8));
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, durationInTicks, -4));
        }
    }

    @Override
    public void remove(LivingEntity livingEntity, int level, Spell spell) {
        switch (builtInCivState) {
            case STUN:
                livingEntity.removePotionEffect(PotionEffectType.SLOW_DIGGING);
                livingEntity.removePotionEffect(PotionEffectType.WEAKNESS);
                livingEntity.removePotionEffect(PotionEffectType.CONFUSION);
            case ROOT:
                livingEntity.removePotionEffect(PotionEffectType.SLOW);
                livingEntity.removePotionEffect(PotionEffectType.JUMP);
        }
    }
}

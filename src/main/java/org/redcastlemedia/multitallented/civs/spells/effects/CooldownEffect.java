package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;
import org.redcastlemedia.multitallented.civs.spells.targets.Target;

import java.util.HashMap;

public class CooldownEffect extends Effect {
    private int cooldown = 0;
    private String target = "self";
    private String abilityName = "self";
    private boolean silent = false;
    private ConfigurationSection config = null;

    public CooldownEffect(Spell spell, String key, Object target, Entity origin, int level, ConfigurationSection section) {
        super(spell, key, target, origin, level, section);
        String configDamage = section.getString("cooldown", "5000");
        this.silent = section.getBoolean("silent", false);
        this.cooldown = (int) Math.round(Spell.getLevelAdjustedValue(configDamage, level, target, spell));
        String tempTarget = section.getString("target", "not-a-string");
        String abilityName = section.getString("ability", "not-a-string");
        if (!tempTarget.equals("not-a-string")) {
            this.target = tempTarget;
        } else {
            this.target = "self";
        }
        if (!abilityName.equals("not-a-string")) {
            this.abilityName = abilityName;
        } else {
            this.abilityName = "self";
        }
        this.config = section;
    }

    public CooldownEffect(Spell spell, String key, Object target, Entity origin, int level, String config) {
        super(spell, key, target, origin, level, config);
        this.cooldown = (int) Math.round(Spell.getLevelAdjustedValue(config, level, target, spell));
        this.target = "self";
        this.abilityName = "self";
        this.silent = false;
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return false;
        }
        Spell spell = getSpell();
        Player player = (Player) target;
        Civilian champion = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String newAbilityName = abilityName.equals("self") ? spell.getType() : abilityName;

        CivState state = champion.getStates().get(newAbilityName + "." + getKey());

        if (state == null) {
            return true;
        }
        Object rawDuration = state.getVars().get("cooldown");
        if (rawDuration == null || !(rawDuration instanceof Long)) {
            if (!this.silent) {
                player.sendMessage(ChatColor.RED + Civs.getPrefix() + " " + spell.getType() +
                        " has an indefinite cooldown");
            }
            return false;
        }
        Long cooldown = (Long) rawDuration;
        if (System.currentTimeMillis() < cooldown) {
            if (!this.silent) {
                player.sendMessage(ChatColor.RED + Civs.getPrefix() + " " + spell.getType() + " has " +
                        ((int) ((System.currentTimeMillis() - cooldown) / -1000))  +
                        "s remaining cooldown");
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
        Spell spell = getSpell();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String newAbilityName = abilityName.equals("self") ? spell.getType() : abilityName;

        CivState state = civilian.getStates().get(newAbilityName + "." + getKey());
        if (state != null) {
            state.remove(civilian);
            civilian.getStates().remove(newAbilityName + "." + getKey());
        }
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("cooldown", System.currentTimeMillis() + this.cooldown);

        if (config != null) {
            state = new CivState(spell, getKey(), -1, -1, config, variables);
        } else {
            state = new CivState(spell, getKey(), -1, -1, "" + this.cooldown, variables);
        }

        Bukkit.getPlayer(civilian.getUuid()).sendMessage(Civs.getPrefix() +
                newAbilityName + "." + getKey());
        civilian.getStates().put(newAbilityName + "." + getKey(), state);
    }
    @Override
    public void remove() {
        Object origin = getOrigin();
        if (!(origin instanceof Player)) {
            return;
        }
        String newAbilityName = abilityName.equals("self") ? getSpell().getType() : abilityName;
        Player player = (Player) origin;
        Civilian champion = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        champion.getStates().remove(newAbilityName + "." + getKey());
    }

    @Override
    public HashMap<String, Double> getVariables() {
        HashMap<String, Double> returnMap = new HashMap<>();
        returnMap.put("cooldown", 0.0);
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return returnMap;
        }
        Player player = (Player) target;
        Civilian champion = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String newAbilityName = abilityName.equals("self") ? getSpell().getType() : abilityName;

        CivState state = champion.getStates().get(newAbilityName + "." + getKey());

        if (state == null) {
            return returnMap;
        }
        Object rawDuration = state.getSpell().getAbilityVariables().get("cooldown");
        if (rawDuration == null || !(rawDuration instanceof Long)) {
            if (!this.silent) {
                player.sendMessage(ChatColor.RED + Civs.getPrefix() + " " + getSpell().getType() +
                        " has an indefinite cooldown");
            }
            return returnMap;
        }
        Long cooldown = (Long) rawDuration;
        returnMap.put("cooldown", (double) Math.max(0, cooldown - System.currentTimeMillis()));
        return returnMap;
    }
}

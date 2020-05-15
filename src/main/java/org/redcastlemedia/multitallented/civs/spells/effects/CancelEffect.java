package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;
import org.redcastlemedia.multitallented.civs.spells.SpellListener;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CancelEffect extends Effect {
    private String target;
    private String abilityName;
    private boolean silent;
    private List<String> whitelist;
    private List<String> blacklist;
    private ConfigurationSection config = null;

    public CancelEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            this.silent = section.getBoolean(SpellConstants.SILENT, false);
            this.target = section.getString(SpellConstants.TARGET, "self");
            this.abilityName = section.getString(SpellConstants.ABILITY, "self");
            this.whitelist = section.getStringList(SpellConstants.WHITELIST);
            this.blacklist = section.getStringList(SpellConstants.BLACKLIST);
            this.config = section;
        } else if (value instanceof String) {
            this.abilityName = (String) value;
            this.target = "self";
            this.whitelist = new ArrayList<>();
            this.blacklist = new ArrayList<>();
            this.silent = false;
        }
    }

    public boolean meetsRequirement() {
        Spell spell = getSpell();
        Object target = getTarget();
        //TODO allow this to target mobs
        if (!(target instanceof Player)) {
            return false;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String newAbilityName = abilityName.equals("self") ? spell.getType() : abilityName;

        for (String key : civilian.getStates().keySet()) {
            String currentAbilityName = key.split("\\.")[0];
            String currentComponentName = key.split("\\.")[1];
            if (!abilityName.equals("all") && !currentAbilityName.equals(newAbilityName)) {
                continue;
            }
            if (!whitelist.isEmpty() && !whitelist.contains(currentComponentName)) {
                continue;
            }
            if (!blacklist.isEmpty() && blacklist.contains(currentComponentName)) {
                continue;
            }
            return true;
        }

        return false;
    }

    public void apply() {
        Spell ability = getSpell();
        Object target = getTarget();
        Entity origin = getOrigin();
        //TODO allow this to target mobs
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        String newAbilityName = abilityName.equals("self") ? ability.getType() : abilityName;
        HashSet<String> removeMe = new HashSet<>();
        for (String key : civilian.getStates().keySet()) {
            String currentAbilityName = key.split("\\.")[0];
            String currentComponentName = key.split("\\.")[1];
            if (!abilityName.equals("all") && !currentAbilityName.equals(newAbilityName)) {
                continue;
            }
            if (!whitelist.isEmpty() && !whitelist.contains(currentComponentName)) {
                continue;
            }
            if (!blacklist.isEmpty() && blacklist.contains(currentComponentName)) {
                continue;
            }
            removeMe.add(key);
        }
        for (String key : removeMe) {
            CivState currentState = civilian.getStates().get(key);
            if (origin instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) origin;
                SpellListener.getInstance().removeDamageListener(livingEntity);
            }
            if (origin instanceof Projectile) {
                Projectile projectile = (Projectile) origin;
                SpellListener.getInstance().removeProjectileListener(projectile);
            }
            currentState.remove(target);
            civilian.getStates().remove(key);
        }
    }
}

package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellListener;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CancelEffect extends Effect {
    private String target = "self";
    private String abilityName = "self";
    private boolean silent = false;
    private List<String> whitelist = new ArrayList<String>();
    private List<String> blacklist = new ArrayList<String>();
    private ConfigurationSection config = null;

    public CancelEffect(Spell spell, String key, Object target, Entity origin, int level, ConfigurationSection section) {
        super(spell, key, target, origin, level, section);
        this.silent = section.getBoolean("silent", false);
        this.target = section.getString("target", "self");
        this.abilityName = section.getString("ability", "self");
        this.whitelist = section.getStringList("whitelist");
        this.blacklist = section.getStringList("blacklist");
        this.config = section;
    }

    public CancelEffect(Spell spell, String key, Object target, Entity origin, int level, String value) {
        super(spell, key, target, origin, level, value);
        this.abilityName = value;
        this.target = "self";
        this.whitelist = new ArrayList<>();
        this.blacklist = new ArrayList<>();
        this.silent = false;
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

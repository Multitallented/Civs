package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

import java.util.HashMap;

public class ManaEffect extends Effect {
    private int mana;
    private String target;
    private boolean silent;

    public ManaEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            this.mana = (int) Math.round(Spell.getLevelAdjustedValue(
                    section.getString(SpellEffectConstants.MANA, "5"), level, target, spell));
            this.silent = section.getBoolean(SpellConstants.SILENT, false);

            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = SpellConstants.SELF;
            }
        } else if (value instanceof String) {
            this.mana = (int) Math.round(Spell.getLevelAdjustedValue((String) value, level, target, spell));
            this.target = SpellConstants.SELF;
            this.silent = false;
        }
    }

    @Override
    public boolean meetsRequirement() {
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return false;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        boolean hasMana = civilian.getMana() >= this.mana;
        if (!hasMana && !this.silent) {
            //TODO send not enough mana message
        }
        return hasMana;
    }

    @Override
    public void apply() {
        Object target = getTarget();
        if (!(target instanceof Player)) {
            return;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        civilian.setMana(civilian.getMana() + this.mana);
        if (!this.silent) {
            //TODO send mana added message
        }
    }

    @Override
    public HashMap<String, Double> getVariables() {
        Object target = getTarget();
        HashMap<String, Double> returnMap = new HashMap<>();
        if (!(target instanceof Player)) {
            return returnMap;
        }
        Player player = (Player) target;
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        returnMap.put("mana", (double) civilian.getMana());
        return returnMap;
    }

}

package org.redcastlemedia.multitallented.civs.spells.civstate;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellComponent;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;

/**
 *
 * @author Multitallented
 */
public class CivState {
    private int durationId;
    private int periodId;
    private final String COMPONENT_NAME;
    private final ConfigurationSection CONFIG;
    private final String CONFIG_STRING;
    private final Spell SPELL;
    private final HashMap<String, Object> VARS;

    public CivState(Spell spell, String componentName, int durationId, int periodId, String configString, HashMap<String, Object> vars) {
        this.durationId = durationId;
        this.periodId = periodId;
        this.COMPONENT_NAME = componentName;
        this.CONFIG = null;
        this.CONFIG_STRING = configString;
        this.SPELL = spell;
        this.VARS = vars;
    }
    public CivState(Spell spell, String componentName, int durationId, int periodId, ConfigurationSection config, HashMap<String, Object> vars) {
        this.durationId = durationId;
        this.periodId = periodId;
        this.COMPONENT_NAME = componentName;
        this.CONFIG = config;
        this.CONFIG_STRING = null;
        this.SPELL = spell;
        this.VARS = vars;
    }
    public CivState(Spell spell, String componentName, int durationId, int periodId, HashMap<String, Object> vars) {
        this.durationId = durationId;
        this.periodId = periodId;
        this.COMPONENT_NAME = componentName;
        this.CONFIG = null;
        this.CONFIG_STRING = null;
        this.SPELL = spell;
        this.VARS = vars;
    }


    public Spell getSpell() {
        return SPELL;
    }
    public String getComponentName() {
        return COMPONENT_NAME;
    }
    public int getDurationId() {
        return durationId;
    }
    public void setDurationId(int newDurationId) {
        this.durationId = newDurationId;
    }
    public int getPeriodId() {
        return periodId;
    }
    public void setPeriodId(int newPeriodId) {
        this.periodId = newPeriodId;
    }
    public ConfigurationSection getConfig() { return this.CONFIG; }
    public String getConfigString() { return this.CONFIG_STRING; }
    public HashMap<String, Object> getVars() {
        return this.VARS;
    }


    public void remove(Object origin) {
        //TODO make this work for mobs too
        if (!(origin instanceof Player)) {
            return;
        }
        Player player = (Player) origin;
        Civilian champion = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (!champion.getStates().containsKey(SPELL.getType() + "." + COMPONENT_NAME)) {
            return;
        }
        Effect component;

        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType(SPELL.getType());
        int level = champion.getLevel(spellType);
        if (CONFIG != null) {
            component = SpellType.getEffect(COMPONENT_NAME, "", CONFIG, level, null, player, SPELL);
        } else {
            component = SpellType.getEffect(COMPONENT_NAME, "", CONFIG_STRING, level, null, player, SPELL);
        }
        if (component != null && (CONFIG != null || CONFIG_STRING != null)) {
            component.remove(player, level, SPELL);
        }
        if (durationId > -1) {
            Bukkit.getScheduler().cancelTask(durationId);
        }
        if (periodId > -1) {
            Bukkit.getScheduler().cancelTask(periodId);
        }
    }
}

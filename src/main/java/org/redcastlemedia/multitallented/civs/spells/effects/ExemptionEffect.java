package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.anticheat.AntiCheatManager;
import org.redcastlemedia.multitallented.civs.anticheat.ExemptionType;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;


public class ExemptionEffect extends Effect {

    private long duration;
    private ExemptionType exemptionType;

    public ExemptionEffect(Spell spell, String key, Object target, Entity origin, int level, Object configuration) {
        super(spell, key, target, origin, level);
        if (configuration instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) configuration;
            duration = section.getLong(SpellConstants.DURATION, 5) * 1000;
            exemptionType = ExemptionType.valueOf(section.getString("exemption", "FLY"));
        } else if (configuration instanceof String) {
            exemptionType = ExemptionType.valueOf((String) configuration);
            duration = 5000;
        }
    }

    @Override
    public void apply() {
        Entity origin = getOrigin();
        if (!(origin instanceof Player)) {
            return;
        }
        Player player = (Player) origin;
        AntiCheatManager.getInstance().addExemption(player, this.exemptionType, this.duration);
    }

    @Override
    public boolean meetsRequirement() {
        return true;
    }
}

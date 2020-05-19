package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.anticheat.ExemptionType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
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
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        final ExemptionType exemptionTypeFinal = this.exemptionType;
        civilian.getExemptions().add(this.exemptionType);
        Bukkit.getScheduler().runTaskLater(Civs.getInstance(), () -> civilian.getExemptions().remove(exemptionTypeFinal), this.duration / 50);
    }

    @Override
    public boolean meetsRequirement() {
        return true;
    }
}

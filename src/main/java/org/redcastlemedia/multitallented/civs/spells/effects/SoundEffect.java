package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellConstants;

public class SoundEffect extends Effect {
    private String soundName = "EXPLODE";
    private String target = "self";
    private float volume = 1;
    private float pitch = 1;

    public SoundEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            this.soundName = section.getString(SpellEffectConstants.SOUND, "EXPLODE").toUpperCase();
            this.volume = (float) section.getDouble("volume", 1);
            this.pitch = (float) section.getDouble("pitch", 1);
            String tempTarget = section.getString(SpellConstants.TARGET, SpellConstants.NOT_A_STRING);
            if (!SpellConstants.NOT_A_STRING.equals(tempTarget)) {
                this.target = tempTarget;
            } else {
                this.target = "self";
            }
        } else if (value instanceof String) {
            this.target = "self";
            this.soundName = ((String) value).toUpperCase();
            this.volume = 1;
            this.pitch = 1;
        }
    }

    public boolean meetsRequirement() {
        return true;
    }
    public void apply() {
        Object target = getTarget();
        Location location;
        if (target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) target;
            location = livingEntity.getLocation();
        } else if (target instanceof Block) {
            Block block = (Block) target;
            location = block.getLocation();
        } else {
            return;
        }
        Sound sound = Sound.valueOf(this.soundName);

        if (sound == null) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().getWorld().equals(location.getWorld()) && player.getLocation().distanceSquared(location) < 400) {
                player.playSound(location, sound, volume, pitch);
            }
        }
    }
}

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

public class SoundEffect extends Effect {
    private String soundName = "EXPLODE";
    private String target = "self";
    private float volume = 1;
    private float pitch = 1;

    public SoundEffect(Spell spell, String key, Object target, Entity origin, int level, ConfigurationSection section) {
        super(spell, key, target, origin, level, section);
        this.soundName = section.getString("sound", "EXPLODE").toUpperCase();
        this.volume = (float) section.getDouble("volume", 1);
        this.pitch = (float) section.getDouble("pitch", 1);
        String tempTarget = section.getString("target", "not-a-string");
        if (!tempTarget.equals("not-a-string")) {
            this.target = tempTarget;
        } else {
            this.target = "self";
        }
    }

    public SoundEffect(Spell spell, String key, Object target, Entity origin, int level, String value) {
        super(spell, key, target, origin, level, value);
        this.target = "self";
        this.soundName = value.toUpperCase();
        this.volume = 1;
        this.pitch = 1;
    }

    public boolean meetsRequirement() {
        return true;
    }
    public void apply() {
        Object target = getTarget();
        Location location = null;
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

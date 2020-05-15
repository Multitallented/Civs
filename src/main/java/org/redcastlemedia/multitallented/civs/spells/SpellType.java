package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.spells.effects.CancelEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.CivPotionEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.CooldownEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.DamageEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.Effect;
import org.redcastlemedia.multitallented.civs.spells.effects.FallEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.HealEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.IgniteEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.ManaEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.ParticleEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.SoundEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.SpellEffectConstants;
import org.redcastlemedia.multitallented.civs.spells.effects.StaminaEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.TeleportEffect;
import org.redcastlemedia.multitallented.civs.spells.effects.VelocityEffect;
import org.redcastlemedia.multitallented.civs.spells.targets.AreaTarget;
import org.redcastlemedia.multitallented.civs.spells.targets.BlockTarget;
import org.redcastlemedia.multitallented.civs.spells.targets.Target;
import org.redcastlemedia.multitallented.civs.spells.targets.VectorTarget;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellType extends CivItem {


    public SpellType(List<String> reqs,
                     String name,
                     Material material,
                     CVItem shopIcon,
                     int qty,
                     int min,
                     int max,
                     double price,
                     String permission,
                     List<String> groups,
                     FileConfiguration config,
                     boolean isInShop,
                     int level) {
        super(reqs,
                false,
                ItemType.SPELL,
                name,
                material,
                shopIcon,
                qty,
                min,
                max,
                price,
                permission,
                groups,
                isInShop,
                level);
        this.config = config;
        this.components = new HashMap<>();
        ConfigurationSection componentSection = config.getConfigurationSection("components");
        if (componentSection == null) {
            Civs.logger.severe("Failed to load spell type " + name + " no components");
            return;
        }
        for (String key : componentSection.getKeys(false)) {
            ConfigurationSection currentSection = componentSection.getConfigurationSection(key);
            if (currentSection != null) {
                components.put(key, currentSection);
            }
        }
    }

    private final FileConfiguration config;
    private final HashMap<String, ConfigurationSection> components;

    public FileConfiguration getConfig() {
        return config;
    }
    public Map<String, ConfigurationSection> getComponents() {
        return components;
    }

    public static Target getTarget(String type,
                                   String key,
                                   ConfigurationSection config,
                                   int level,
                                   Player caster,
                                   Spell spell) {
        if (type.equals("vector")) {
            return new VectorTarget(spell, key, caster, level, config);
        } else if (type.equals("area")) {
            return new AreaTarget(spell, key, caster, level, config);
        } else if (type.equals("block")) {
            return new BlockTarget(spell, key, caster, level, config);
        }
        return null;
    }

    public static Effect getEffect(String type,
                                   String key,
                                   Object config,
                                   int level,
                                   Object target,
                                   Player caster,
                                   Spell spell) {
        if (type.equals(SpellEffectConstants.DAMAGE)) {
            return new DamageEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.COOLDOWN)) {
            return new CooldownEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.POTION)) {
            return new CivPotionEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.STAMINA)) {
            return new StaminaEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.VELOCITY)) {
            return new VelocityEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.CANCEL)) {
            return new CancelEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.SOUND)) {
            return new SoundEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.PARTICLE)) {
            return new ParticleEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.FALL)) {
            return new FallEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.HEAL)) {
            return new HealEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.IGNITE)) {
            return new IgniteEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.TELEPORT)) {
            return new TeleportEffect(spell, key, target, caster, level, config);
        } else if (type.equals(SpellEffectConstants.MANA)) {
            return new ManaEffect(spell, key, target, caster, level, config);
        }
        return null;
    }
}

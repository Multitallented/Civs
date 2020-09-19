package org.redcastlemedia.multitallented.civs.spells.effects;

import java.util.HashSet;
import java.util.Optional;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.redcastlemedia.multitallented.civs.spells.Spell;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitEntity;
import io.lumine.xikage.mythicmobs.mobs.GenericCaster;
import io.lumine.xikage.mythicmobs.skills.Skill;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;

public class MythicMobSpellEffect extends Effect {

    private String skillName;
    private float power;

    public MythicMobSpellEffect(Spell spell, String key, Object target, Entity origin, int level, Object value) {
        super(spell, key, target, origin, level);
        if (value instanceof ConfigurationSection) {
            ConfigurationSection section = (ConfigurationSection) value;
            this.skillName = section.getString("name", "default");
            this.power = Math.round(Spell.getLevelAdjustedValue(section.getString("power", "1"), level, target, spell));
        } else if (value instanceof String) {
            this.skillName = (String) value;
            power = 1f;
        }
    }

    @Override
    public void apply() {
        AbstractLocation targetLocation = null;
        Object targetObj = getTarget();
        HashSet<AbstractEntity> targetEntities = new HashSet<>();
        HashSet<AbstractLocation> targetLocations = new HashSet<>();
        if (targetObj instanceof Block) {
            Block block = (Block) targetObj;
            targetLocation = BukkitAdapter.adapt(block.getLocation());
            targetLocations.add(targetLocation);
        } else {
            LivingEntity entityTarget = (LivingEntity) getTarget();
            targetEntities.add(new BukkitEntity(entityTarget));
            targetLocation = BukkitAdapter.adapt(entityTarget.getLocation());
        }
        GenericCaster genericCaster = new GenericCaster(new BukkitEntity(getOrigin()));
        Optional<Skill> skill = MythicMobs.inst().getSkillManager().getSkill(this.skillName);
        final AbstractLocation finalTargetLocation = targetLocation;
        skill.ifPresent(value ->
                value.execute(SkillTrigger.API, genericCaster, genericCaster.getEntity(), finalTargetLocation, targetEntities, targetLocations, this.power));
    }

    @Override
    public boolean meetsRequirement() {
        return true;
    }
}

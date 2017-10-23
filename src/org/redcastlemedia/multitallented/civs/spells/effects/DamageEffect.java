package org.redcastlemedia.multitallented.civs.spells.effects;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.spells.Spell;

/**
 *
 * @author Multitallented
 */
public class DamageEffect extends Effect {
    public DamageEffect(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void execute(Spell spell, Player player) {
        damageEntity(player, spell.getCaster());
    }

    @Override
    public void execute(Spell spell, Entity target) {
        damageEntity(target, spell.getCaster());
    }

    @Override
    public void execute(Spell spell, Block target) {
        int radius = node.getInt("radius",1);
        Chunk chunk = target.getChunk();
        if (!chunk.isLoaded()) {
            return;
        }
        for (Entity e : chunk.getEntities()) {
            if (target.getLocation().distance(e.getLocation()) > radius) {
                continue;
            }
            if (e instanceof LivingEntity && damageCheck(spell.getCaster(), e)) {
                damageEntity(e, spell.getCaster());
                break;
            }
        }
    }

    private void damageEntity(Entity target, Player caster) {
        if (!(target instanceof LivingEntity)) {
            return;
        }
        int amount = node.getInt("damage", 1);
//        boolean knockback = node.getBoolean("knockback", true);
        LivingEntity livingEntity = (LivingEntity) target;
        livingEntity.damage(amount, caster);
    }
}
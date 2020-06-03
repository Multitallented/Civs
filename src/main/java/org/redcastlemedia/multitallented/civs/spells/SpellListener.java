package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.BuiltInCivState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@CivsSingleton
public class SpellListener implements Listener {

    private final HashMap<LivingEntity, AbilityListen> damageListeners = new HashMap<LivingEntity, AbilityListen>();
    private final HashMap<Projectile, AbilityListen> projectileListeners = new HashMap<Projectile, AbilityListen>();
    public static SpellListener spellListener = null;

    public SpellListener() {
        spellListener = this;
        Bukkit.getPluginManager().registerEvents(this, Civs.getInstance());
    }

    public static SpellListener getInstance() {
        if (spellListener == null) {
            new SpellListener();
        }
        return spellListener;
    }

    @EventHandler
    public void onSpellUse(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (!ConfigManager.getInstance().getUseClassesAndSpells() || !CivItem.isCivsItem(itemStack)) {
            return;
        }
        CivItem civItem = CivItem.getFromItemStack(itemStack);

        if (!(civItem instanceof SpellType)) {
            return;
        }
        SpellType spellType = (SpellType) civItem;
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        if (civilian.hasBuiltInState(BuiltInCivState.NO_OUTGOING_SPELLS)) {
            event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(
                    event.getPlayer(), "spell-block"));
            return;
        }

        Spell spell = new Spell(civItem.getProcessedName(), event.getPlayer(), civilian.getLevel(spellType));
        if (spell.useAbility()) {
            if (spellType.getExpPerUse() > 0) {
                civilian.addExp(spellType, spellType.getExpPerUse());
            }
        }
    }

    @EventHandler
    public void onListenerDamage(EntityDamageEvent event) {
        if (!ConfigManager.getInstance().getUseClassesAndSpells() || event.isCancelled() ||
                event.getDamage() < 1 || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        if (!damageListeners.containsKey(livingEntity)) {
            return;
        }
        AbilityListen abilityListener = damageListeners.get(livingEntity);
        if (abilityListener.spell.useAbilityFromListener(abilityListener.getCaster(), abilityListener.getConfig(), event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onListenerProjectile(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }
        for (Map.Entry<Projectile, AbilityListen> entry : new HashMap<>(projectileListeners).entrySet()) {
            if (!entry.getKey().isValid() || entry.getKey().isDead()) {
                projectileListeners.remove(entry.getKey());
            }
            if (!entry.getKey().equals(event.getDamager())) {
                continue;
            }
            if (entry.getValue().spell.useAbilityFromListener(entry.getValue().getCaster(), entry.getValue().getConfig(), event.getEntity())) {
                event.setCancelled(true);
                event.getDamager().remove();
            }
        }
    }

    public void addDamageListener(LivingEntity livingEntity, int level, ConfigurationSection section, Spell spell, Player caster, String key) {
        damageListeners.put(livingEntity, new AbilityListen(livingEntity, level, section, caster, spell, key));
    }
    public void removeDamageListener(LivingEntity livingEntity) {
        damageListeners.remove(livingEntity);
    }
    public void addProjectileListener(Projectile livingEntity, int level, ConfigurationSection section, Spell spell, String key, Player caster) {
        projectileListeners.put(livingEntity, new AbilityListen(livingEntity, level, section, caster, spell, key));
    }

    @AllArgsConstructor @Getter
    private static class AbilityListen {
        private final Object target;
        private final int level;
        private final ConfigurationSection config;
        private final Player caster;
        private final Spell spell;
        private final String key;
    }
}

package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
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
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@CivsSingleton
public class SpellListener implements Listener {

    private final HashMap<LivingEntity, AbilityListen> damageListeners = new HashMap<>();
    private final HashMap<Projectile, AbilityListen> projectileListeners = new HashMap<>();
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
            event.getPlayer().sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
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

    @EventHandler(ignoreCancelled = true)
    public void onListenerDamage(EntityDamageByEntityEvent event) {
        if (!ConfigManager.getInstance().getUseClassesAndSpells() ||
                event.getDamage() < 1 || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        if (event.getDamager() instanceof Projectile) {
            handleProjectile(event);
        } else if (event.getDamager() instanceof LivingEntity) {
            LivingEntity damage = (LivingEntity) event.getDamager();

            if (!damageListeners.containsKey(damage)) {
                return;
            }
            AbilityListen abilityListener = damageListeners.get(damage);
            if (abilityListener.getConfig().isSet("projectile")) {
                return;
            }
            if (abilityListener.spell.useAbilityFromListener(abilityListener.getConfig(), event.getEntity(),
                    abilityListener.getKey(), abilityListener.getMappedTargets())) {
                event.setCancelled(true);
            } else if (abilityListener.getConfig().isSet("damage-mod")) {
                double damageMod = Spell.getLevelAdjustedValue(abilityListener.getConfig().getString("damage-mod", "0"),
                        abilityListener.getLevel(), abilityListener.getCaster(), abilityListener.getSpell());
                event.setDamage(event.getDamage() + damageMod);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectileListeners.containsKey(projectile) && projectile instanceof Arrow) {
            if (event.getEntity().isValid() && !event.getEntity().isDead()) {
                event.getEntity().remove();
            }
        }
    }

    private void handleProjectile(EntityDamageByEntityEvent event) {
        Projectile projectile = (Projectile) event.getDamager();

        for (Map.Entry<Projectile, AbilityListen> entry : new HashMap<>(projectileListeners).entrySet()) {
            if (!entry.getKey().isValid() || entry.getKey().isDead()) {
                projectileListeners.remove(entry.getKey());
            }
            AbilityListen abilityListen = entry.getValue();
            if (!entry.getKey().equals(projectile)) {
                continue;
            }
            if (abilityListen.getConfig().isSet("projectile")) {
                EntityType entityType = EntityType.valueOf(abilityListen.getConfig().getString("projectile", "ARROW"));
                if (projectile.getType() != entityType) {
                    continue;
                }
            }

            if (abilityListen.spell.useAbilityFromListener(abilityListen.getConfig(), event.getEntity(),
                    entry.getValue().getKey(), abilityListen.getMappedTargets())) {
                event.setCancelled(true);
                event.getDamager().remove();
            } else if (abilityListen.getConfig().isSet("damage-mod")) {
                double damageMod = Spell.getLevelAdjustedValue(abilityListen.getConfig().getString("damage-mod", "0"),
                        abilityListen.getLevel(), abilityListen.getCaster(), abilityListen.getSpell());
                event.setDamage(event.getDamage() + damageMod);
            }
        }
    }

    public void addDamageListener(LivingEntity livingEntity, int level, ConfigurationSection section, Spell spell,
                                  Player caster, String key, Map<String, Set<?>> mappedTargets) {
        damageListeners.put(livingEntity, new AbilityListen(livingEntity, level, section, caster, spell, key, mappedTargets));
    }
    public void removeDamageListener(LivingEntity livingEntity) {
        damageListeners.remove(livingEntity);
    }

    public void addProjectileListener(Projectile livingEntity, int level, ConfigurationSection section, Spell spell,
                                      String key, Player caster, Map<String, Set<?>> mappedTargets) {
        projectileListeners.put(livingEntity, new AbilityListen(livingEntity, level, section, caster, spell, key, mappedTargets));
    }

    @AllArgsConstructor @Getter
    private static class AbilityListen {
        private final Object target;
        private final int level;
        private final ConfigurationSection config;
        private final Player caster;
        private final Spell spell;
        private final String key;
        private final Map<String, Set<?>> mappedTargets;
    }
}

package org.redcastlemedia.multitallented.civs.spells;

import com.avaje.ebean.annotation.EnumValue;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.ConditionEvent;
import org.redcastlemedia.multitallented.civs.events.GainExpEvent;
import org.redcastlemedia.multitallented.civs.events.ManaChangeEvent;
import org.redcastlemedia.multitallented.civs.events.SpellPreCastEvent;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.BuiltInCivStates;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

import java.util.HashMap;

/**
 *
 * @author Multitallented
 */
public class SpellListener implements Listener {

    private final HashMap<LivingEntity, AbilityListen> damageListeners = new HashMap<LivingEntity, AbilityListen>();
    private final HashMap<Projectile, AbilityListen> projectileListeners = new HashMap<Projectile, AbilityListen>();
    public static SpellListener spellListener = null;

    public SpellListener() {
        spellListener = this;
    }

    public static SpellListener getInstance() {
        return spellListener;
    }

    @EventHandler
    public void onSpellUse(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        if (itemStack == null || !CivItem.isCivsItem(itemStack)) {
            return;
        }
        String itemName = CivItem.processItemName(itemStack.getItemMeta().getDisplayName());
        CivItem civItem = ItemManager.getInstance().getItemType(itemName);

        if (!(civItem instanceof SpellType)) {
            return;
        }
        SpellType spellType = (SpellType) civItem;
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        Spell spell = new Spell(itemName, event.getPlayer(), civilian.getLevel(spellType));
        //TODO should I add this to a spell manager? Spells dont persist
        spell.useAbility();
    }

    @EventHandler
    public void onListenerDamage(EntityDamageEvent event) {
        if (event.isCancelled() || event.getDamage() < 1 || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity) event.getEntity();
        if (!damageListeners.containsKey(livingEntity)) {
            return;
        }
        AbilityListen abilityListener = damageListeners.get(livingEntity);
        if (abilityListener.spell.useAbilityFromListener(abilityListener.getCaster(), abilityListener.getLevel(), abilityListener.getConfig(), event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onListenerProjectile(EntityDamageByEntityEvent event) {
        AbilityListen abilityListener;
        if (projectileListeners.containsKey(event.getDamager())) {
            abilityListener = projectileListeners.get(event.getDamager());
        } else {
            return;
        }
        if (abilityListener.spell.useAbilityFromListener(abilityListener.getCaster(), abilityListener.getLevel(), abilityListener.getConfig(), event.getEntity())) {
            event.setCancelled(true);
        }
    }

    public void addDamageListener(LivingEntity livingEntity, int level, ConfigurationSection section, Spell spell) {
        damageListeners.put(livingEntity, new AbilityListen(spell,null, livingEntity, level, section));
    }
    public boolean removeDamageListener(LivingEntity livingEntity) {
        return damageListeners.remove(livingEntity) != null;
    }
    public void addProjectileListener(Projectile livingEntity, int level, ConfigurationSection section, Spell spell) {
        projectileListeners.put(livingEntity, new AbilityListen(spell,null, livingEntity, level, section));
    }
    public boolean removeProjectileListener(Projectile livingEntity) {
        return projectileListeners.remove(livingEntity) != null;
    }

    private class AbilityListen {
        private final Object target;
        private final int level;
        private final ConfigurationSection config;
        private final Player caster;
        private final Spell spell;

        public AbilityListen(Spell spell, Player caster, Object target, int level, ConfigurationSection config) {
            this.caster = caster;
            this.target = target;
            this.level = level;
            this.config = config;
            this.spell = spell;
        }

        public Player getCaster() {
            return caster;
        }
        public Object getTarget() {
            return target;
        }
        public int getLevel() {
            return level;
        }
        public ConfigurationSection getConfig() {
            return config;
        }
        public Spell getSpell() { return spell; }
    }
}

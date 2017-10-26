package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.BuiltInCivStates;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

/**
 *
 * @author Multitallented
 */
public class SpellListener implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity e = event.getEntity();
        if (!(e instanceof LivingEntity)) {
            return;
        }
        LivingEntity liv = (LivingEntity) e;
        String creatureTypeName = liv.getType().toString();
        int health = ConfigManager.getInstance().getCreatureHealth(creatureTypeName);
        if (health > 0) {
            liv.setHealth(health);
//            liv.setMaxHealth(health);
        }
        //TODO damage? probably going to have to check the damage events instead
    }
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        for (CivState us : civilian.getStates().values()) {
            if (us.getDefaultStates().contains(BuiltInCivStates.NO_COMMANDS)) {
                us.sendCancelledMessage(event.getPlayer().getPlayer(), CivState.CancelledMessageTypes.COMMAND);
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Civilian user = CivilianManager.getInstance().getCivilian(event.getPlayer().getUniqueId());
        for (CivState us : user.getStates().values()) {
            if (us.getDefaultStates().contains(BuiltInCivStates.MUTE)) {
                us.sendCancelledMessage(event.getPlayer(), CivState.CancelledMessageTypes.CHAT);
                event.setCancelled(true);
                return;
            }
        }
    }

    ////////////VANILLA EVENTS////////////////

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getDamage() < 1) {
            return;
        }
        Civilian user = null;
        Civilian dUser = null;
        Player damagee = null;
        Player damager = null;
        CivilianManager civilianManager = CivilianManager.getInstance();
        boolean byEntity = event instanceof EntityDamageByEntityEvent;
        if (byEntity && ((EntityDamageByEntityEvent) event).getDamager() instanceof Player) {
            damager = (Player) ((EntityDamageByEntityEvent) event).getDamager();
            dUser = civilianManager.getCivilian(damager.getUniqueId());
        }
        if (event.getEntity() instanceof Player) {
            damagee = (Player) event.getEntity();
            user = civilianManager.getCivilian(damagee.getUniqueId());
        }
        if (user != null) {
            for (CivState us : user.getStates().values()) {
                if (us.getDefaultStates().contains(BuiltInCivStates.NO_DAMAGE) ||
                        us.getDefaultStates().contains(BuiltInCivStates.NO_INCOMING_DAMAGE) ||
                        (dUser != null && (us.getDefaultStates().contains(BuiltInCivStates.NO_PVP) ||
                                us.getDefaultStates().contains(BuiltInCivStates.NO_INCOMING_PVP))) ||
                        (dUser == null && (us.getDefaultStates().contains(BuiltInCivStates.NO_INCOMING_PVE) ||
                                us.getDefaultStates().contains(BuiltInCivStates.NO_PVE)))) {
                    if (dUser != null) {
                        us.sendCancelledMessage(damager, CivState.CancelledMessageTypes.DAMAGE);
                    }
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (dUser != null) {
            for (CivState us : dUser.getStates().values()) {
                if (us.getDefaultStates().contains(BuiltInCivStates.NO_OUTGOING_DAMAGE) ||
                        us.getDefaultStates().contains(BuiltInCivStates.NO_DAMAGE) ||
                        (user != null && (us.getDefaultStates().contains(BuiltInCivStates.NO_OUTGOING_PVP) ||
                                us.getDefaultStates().contains(BuiltInCivStates.NO_PVP))) ||
                        (user == null && (us.getDefaultStates().contains(BuiltInCivStates.NO_PVE) ||
                                us.getDefaultStates().contains(BuiltInCivStates.NO_OUTGOING_PVE)))) {
                    us.sendCancelledMessage(damager, CivState.CancelledMessageTypes.DAMAGE);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onHealthChange(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Civilian user = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (CivState us : user.getStates().values()) {
            if (us.getDefaultStates().contains(BuiltInCivStates.NO_HEAL)) {
                us.sendCancelledMessage(player, CivState.CancelledMessageTypes.HEAL);
                event.setCancelled(true);
                return;
            }
        }
    }

    //TODO implement these
    ////////////PROXIS EVENTS/////////////////
    /*@EventHandler
    public void onUserManaChangeEvent(UserManaChangeEvent event) {
        Civilian user = proxis.getUserManager().getUser(event.getUsername());
        boolean natural = event.getReason() == ManaChangeReason.NATURAL_REGEN;
        boolean increase = event.getManaChange() > 0;
        boolean decrease = !increase;
        for (CivState us : user.getStates().values()) {
            if (us.getDefaultStates().contains(BuiltInCivStates.MANA_FREEZE) ||
                    (natural && us.getDefaultStates().contains(BuiltInCivStates.MANA_FREEZE_NATURAL)) ||
                    (increase && us.getDefaultStates().contains(BuiltInCivStates.MANA_FREEZE_GAIN)) ||
                    (decrease && us.getDefaultStates().contains(BuiltInCivStates.MANA_FREEZE))) {
                us.sendCancelledMessage(user.NAME, CivState.CancelledMessageTypes.MANA);
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onSkillPreCastEvent(SkillPreCastEvent event) {
        Civilian user = proxis.getUserManager().getUser(event.getUsername());
        for (CivState us : user.getStates().values()) {
            if (us.getDefaultStates().contains(BuiltInCivStates.NO_SKILLS) ||
                    us.getDefaultStates().contains(BuiltInCivStates.NO_OUTGOING_SKILLS)) {
                us.sendCancelledMessage(user.NAME, CivState.CancelledMessageTypes.SKILL);
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onSkillCondition(SkillConditionEvent event) {
        event.CAST_SKILL.checkInCondition(event.INDEX, event.getResult());
    }
    @EventHandler(order = Order.LATEST)
    public void onUserGainExp(UserGainExpEvent event) {
        proxis.getTypeManager().gainExp(event.getUsername(), event.getType(), event.getExp());
    }*/
    //TODO addFavoriteSkill from listener
}
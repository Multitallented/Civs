package org.redcastlemedia.multitallented.civs.spells.civstate;

import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellComponent;

/**
 *
 * @author Multitallented
 */
public abstract class CivState extends SpellComponent {
    private final long period;
    private final long duration;
    private final HashSet<Integer> currentTasks = new HashSet<>();
    public CivState(String name, long duration, long period) {
        this.duration = duration;
        this.period = period;
    }

    public HashSet<Integer> getCurrentTasks() {
        return currentTasks;
    }

    public abstract HashSet<BuiltInCivStates> getDefaultStates();

    public abstract HashSet<CivState> getInternalStates();

    public void apply(final Civilian civilian) {
        for (final CivState u : getInternalStates()) {
            u.apply(civilian);
        }
        if (duration > 0) {
            int idDuration = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    remove(civilian);
                    for (int i : getCurrentTasks()) {
                        Bukkit.getScheduler().cancelTask(i);
                    }
                }
            }, getDuration());
            getCurrentTasks().add(idDuration);
        }
        if (period > 0) {
            int idPeriod = Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    tick(civilian);
                }
            }, period, period);
            getCurrentTasks().add(idPeriod);
        }
    }
    public void apply(final Block block) {
        for (final CivState u : getInternalStates()) {
            u.apply(block);
        }
        if (duration > 0) {
            int idDuration = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    remove(block);
                    for (int i : getCurrentTasks()) {
                        Bukkit.getScheduler().cancelTask(i);
                    }
                }
            }, getDuration());
            getCurrentTasks().add(idDuration);
        }
        if (period > 0) {
            int idPeriod = Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    tick(block);
                }
            }, period, period);
            getCurrentTasks().add(idPeriod);
        }
    }
    public void apply(final Entity e) {
        for (final CivState u : getInternalStates()) {
            u.apply(e);
        }
        if (duration > 0) {
            int idDuration = Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    remove(e);
                    for (int i : getCurrentTasks()) {
                        Bukkit.getScheduler().cancelTask(i);
                    }
                }
            }, getDuration());
            getCurrentTasks().add(idDuration);
        }
        if (period > 0) {
            int idPeriod = Bukkit.getScheduler().scheduleSyncRepeatingTask(Civs.getInstance(), new Runnable() {
                @Override
                public void run() {
                    tick(e);
                }
            }, period, period);
            getCurrentTasks().add(idPeriod);
        }
    }

    public void tick(Civilian civilian) {

    }
    public void tick(Block block) {

    }
    public void tick(Entity e) {

    }

    public void remove(Civilian user) {
        for (int i : getCurrentTasks()) {
            Bukkit.getScheduler().cancelTask(i);
        }
        for (CivState us : getInternalStates()) {
            us.remove(user);
        }
    }
    public void remove(Block block) {
        for (int i : getCurrentTasks()) {
            Bukkit.getScheduler().cancelTask(i);
        }
        for (CivState us : getInternalStates()) {
            us.remove(block);
        }
    }
    public void remove(Entity e) {
        for (int i : getCurrentTasks()) {
            Bukkit.getScheduler().cancelTask(i);
        }
        for (CivState us : getInternalStates()) {
            us.remove(e);
        }
    }

//    @Override
//    public void execute(Spell cs, Civilian target, HashMap<String, Object> node) {
//        apply(target);
//    }
//    @Override
//    public void execute(Spell cs, Entity target, HashMap<String, Object> node) {
//        apply(target);
//    }
//    @Override
//    public void execute(Spell cs, Block target, HashMap<String, Object> node) {
//        apply(target);
//    }

    public abstract void sendCancelledMessage(Player player, CancelledMessageTypes type);

    public enum CancelledMessageTypes {
        CHAT,
        COMMAND,
        HEAL,
        DAMAGE,
        SKILL,
        MANA
    }

    public long getPeriod() {
        return period;
    }
    public long getDuration() {
        return duration;
    }
}
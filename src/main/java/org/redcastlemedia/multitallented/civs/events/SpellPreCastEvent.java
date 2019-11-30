package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.spells.Spell;

import java.util.UUID;

/**
 *
 * @author Multitallented
 */
public class SpellPreCastEvent extends Event implements Cancellable {
    private static final HandlerList hList = new HandlerList();
    private UUID uuid;
    private Spell spell;
    private boolean cancelled = false;
    public SpellPreCastEvent(UUID uuid, Spell skill) {
        this.uuid = uuid;
        this.spell = skill;
    }
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    public UUID getUuid() {
        return uuid;
    }

    public Spell getSpell() {
        return spell;
    }
    public void setSpell(Spell skill) {
        this.spell = skill;
    }

    @Override
    public void setCancelled(boolean bln) {
        this.cancelled = bln;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    public static HandlerList getHandlerList() {
        return hList;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}
package org.redcastlemedia.multitallented.civs.events;


import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 *
 * @author Multitallented
 */
public class ManaChangeEvent extends Event implements Cancellable {
    private static final HandlerList hList = new HandlerList();
    private UUID uuid;
    private int mana;
    private boolean cancelled = false;
    private ManaChangeReason reason;
    public ManaChangeEvent(UUID uuid, int mana) {
        this.uuid = uuid;
        this.mana = mana;
        this.reason = ManaChangeReason.SKILL;
    }
    public ManaChangeEvent(UUID uuid, int mana, ManaChangeReason reason) {
        this.uuid = uuid;
        this.mana = mana;
        this.reason = reason;
    }
    public UUID getUUID() {
        return uuid;
    }
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
    public double getManaChange() {
        return mana;
    }
    public void setManaChange(int mana) {
        this.mana = mana;
    }
    public ManaChangeReason getReason() {
        return reason;
    }
    public void setManaChangeReason(ManaChangeReason reason) {
        this.reason = reason;
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

    public enum ManaChangeReason {
        NATURAL_REGEN,
        SKILL,
        COMMAND,
    }
}
package org.redcastlemedia.multitallented.civs.events;


import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 *
 * @author Multitallented
 */
public class GainExpEvent extends Event implements Cancellable {
    private static final HandlerList hList = new HandlerList();
    private UUID uuid;
    private String type;
    private double exp;
    private boolean cancelled;

    public GainExpEvent(UUID uuid, String type, double exp) {
        this.uuid = uuid;
        this.type = type;
        this.exp = exp;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public double getExp() {
        return exp;
    }
    public void setExp(double exp) {
        this.exp = exp;
    }

    public static HandlerList getHandlerList() {
        return hList;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
    @Override
    public void setCancelled(boolean bln) {
        this.cancelled = bln;
    }
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
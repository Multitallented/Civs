package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.towns.Town;

import java.util.UUID;

public class PlayerLeaveTownEvent extends Event implements Cancellable {
    private static final HandlerList hList = new HandlerList();

    private boolean cancelled;
    private final UUID uuid;
    private final Town town;

    public PlayerLeaveTownEvent(UUID uuid, Town town) {
        this.uuid = uuid;
        this.town = town;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Town getTown() {
        return town;
    }


    @Override
    public HandlerList getHandlers() {
        return hList;
    }
    public static HandlerList getHandlerList() {
        return hList;
    }
}

package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.towns.Town;

import java.util.UUID;

public class TownInvitesPlayerEvent extends Event implements Cancellable {
    private boolean cancelled;

    private final UUID player;
    private final Town town;

    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    public TownInvitesPlayerEvent(UUID player, Town town) {
        this.player = player;
        this.town = town;
    }

    public UUID getPlayer() {
        return player;
    }

    public Town getTown() {
        return town;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

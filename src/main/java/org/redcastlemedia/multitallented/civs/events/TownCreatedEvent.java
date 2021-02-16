package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import lombok.Getter;

@Getter
public class TownCreatedEvent extends Event implements Cancellable {
    private final Town town;
    private final TownType townType;
    private boolean cancelled;

    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    public TownCreatedEvent(Town town, TownType townType) {
        this.town = town;
        this.townType = townType;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = cancelled;
    }
}

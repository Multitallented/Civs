package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import lombok.Getter;

@Getter
public class TownCreatedEvent extends Event {
    private final Town town;
    private final TownType townType;
    private static final HandlerList hList = new HandlerList();

    public TownCreatedEvent(Town town, TownType townType) {
        this.town = town;
        this.townType = townType;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

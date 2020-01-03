package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import lombok.Getter;

@Getter
public class TownDevolveEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    private final Town town;
    private final TownType townType;

    public TownDevolveEvent(Town town, TownType townType) {
        this.town = town;
        this.townType = townType;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

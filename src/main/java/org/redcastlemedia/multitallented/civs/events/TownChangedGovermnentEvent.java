package org.redcastlemedia.multitallented.civs.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.towns.Government;
import org.redcastlemedia.multitallented.civs.towns.Town;

@Getter
public class TownChangedGovermnentEvent extends Event {
    private final Town town;
    private final Government previousGoverment;

    private static final HandlerList hList = new HandlerList();

    public TownChangedGovermnentEvent(Town town, Government previousGoverment) {
        this.town = town;
        this.previousGoverment = previousGoverment;
    }

    public static HandlerList getHandlerList() {
        return hList;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

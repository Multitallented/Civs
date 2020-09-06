package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.nations.Nation;

import lombok.Getter;

@Getter
public class NationDestroyedEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    private Nation nation;

    public NationDestroyedEvent(Nation nation) {
        this.nation = nation;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

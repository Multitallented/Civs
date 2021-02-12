package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TwoSecondEvent extends Event {
    private static final HandlerList hList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
    public static HandlerList getHandlerList() {
        return hList;
    }
    public TwoSecondEvent() {
    }
}

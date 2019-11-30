package org.redcastlemedia.multitallented.civs.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.regions.Region;

public class RegionDestroyedEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }

    @Getter
    private final Region region;

    public RegionDestroyedEvent(Region region) {
        this.region = region;
    }
}

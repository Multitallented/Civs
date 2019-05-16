package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import lombok.Getter;


public class RegionCreatedEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    @Getter
    private final RegionType regionType;
    @Getter
    private final Region region;

    public RegionCreatedEvent(Region region, RegionType regionType) {
        this.region = region;
        this.regionType = regionType;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

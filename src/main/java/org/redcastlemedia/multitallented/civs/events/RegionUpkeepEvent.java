package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

public class RegionUpkeepEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    private final Region region;
    private final int upkeepIndex;

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
    public static HandlerList getHandlerList() {
        return hList;
    }
    public RegionUpkeepEvent(Region region, int upkeepIndex) {
        this.region = region;
        this.upkeepIndex = upkeepIndex;
    }

    public Region getRegion() {
        return region;
    }

    public int getUpkeepIndex() {
        return upkeepIndex;
    }
}

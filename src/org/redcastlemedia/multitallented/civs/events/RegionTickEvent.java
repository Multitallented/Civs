package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import java.util.UUID;

public class RegionTickEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    private final Region region;
    private final RegionType regionType;
    private final boolean hasUpkeep;
    private final boolean shouldTick;
    private boolean shouldDestroy = false;

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
    public static HandlerList getHandlerList() {
        return hList;
    }
    public RegionTickEvent(Region region, RegionType regionType, boolean hasUpkeep, boolean shouldTick) {
        this.shouldTick = shouldTick;
        this.region = region;
        this.regionType = regionType;
        this.hasUpkeep = hasUpkeep;
    }

    public void setShouldDestroy(boolean shouldDestroy) { this.shouldDestroy = shouldDestroy; }

    public boolean getShouldDestroy() { return shouldDestroy; }

    public boolean isShouldTick() {
        return shouldTick;
    }

    public boolean isHasUpkeep() {
        return hasUpkeep;
    }

    public Region getRegion() {
        return region;
    }

    public RegionType getRegionType() {
        return regionType;
    }
}

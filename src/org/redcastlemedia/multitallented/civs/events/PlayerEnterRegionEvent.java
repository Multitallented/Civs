package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import java.util.UUID;

public class PlayerEnterRegionEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    private final UUID uuid;
    private final Region region;
    private final RegionType regionType;

    @Override
    public HandlerList getHandlers() {
        return hList;
    }

    public PlayerEnterRegionEvent(UUID uuid, Region region, RegionType regionType) {
        this.uuid = uuid;
        this.region = region;
        this.regionType = regionType;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Region getRegion() {
        return region;
    }

    public RegionType getRegionType() {
        return regionType;
    }
}

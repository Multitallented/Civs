package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;

import lombok.Getter;


public class RegionCreatedEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }
    @Getter
    private final RegionType regionType;
    @Getter
    private final Region region;
    @Getter
    private final Player player;

    public RegionCreatedEvent(Region region, RegionType regionType, Player player) {
        this.region = region;
        this.regionType = regionType;
        this.player = player;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

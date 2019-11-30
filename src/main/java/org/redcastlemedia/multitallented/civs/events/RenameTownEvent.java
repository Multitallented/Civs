package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;

import java.util.UUID;

public class RenameTownEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    private final String oldName;
    private final String newName;
    private final Town town;

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
    public static HandlerList getHandlerList() {
        return hList;
    }
    public RenameTownEvent(String oldName, String newName, Town town) {
        this.oldName = oldName;
        this.newName = newName;
        this.town = town;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public Town getTown() {
        return town;
    }
}

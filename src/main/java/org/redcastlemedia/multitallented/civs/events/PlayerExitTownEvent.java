package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import java.util.UUID;

public class PlayerExitTownEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    private final UUID uuid;
    private final Town town;
    private final TownType townType;
    private boolean notify;

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
    public static HandlerList getHandlerList() {
        return hList;
    }
    public PlayerExitTownEvent(UUID uuid, Town town, TownType townType) {
        this.uuid = uuid;
        this.town = town;
        this.townType = townType;
        this.notify = true;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Town getTown() {
        return town;
    }

    public TownType getTownType() {
        return townType;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}

package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;

import lombok.Getter;

@Getter
public class RenameAllianceEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    private Alliance alliance;
    private String oldName;
    private String newName;

    public RenameAllianceEvent(Alliance alliance, String oldName, String newName) {
        this.alliance = alliance;
        this.oldName = oldName;
        this.newName = newName;
    }


    @Override
    public HandlerList getHandlers() {
        return null;
    }
}

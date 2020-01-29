package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.towns.Town;

import lombok.Getter;

@Getter
public class AllianceFormedEvent extends Event {
    private Alliance alliance;
    private Town newMember;
    private boolean newAlliance;
    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    public AllianceFormedEvent(Alliance alliance, Town newMember, boolean newAlliance) {
        this.alliance = alliance;
        this.newMember = newMember;
        this.newAlliance = newAlliance;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

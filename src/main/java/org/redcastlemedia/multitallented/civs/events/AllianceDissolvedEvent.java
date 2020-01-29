package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.towns.Town;

import lombok.Getter;

@Getter
public class AllianceDissolvedEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    private Alliance oldAlliance;
    private Town townExitingAlliance;
    private boolean allianceDissolved;

    public AllianceDissolvedEvent(Alliance oldAlliance, Town townExitingAlliance, boolean allianceDissolved) {
        this.oldAlliance = oldAlliance;
        this.townExitingAlliance = townExitingAlliance;
        this.allianceDissolved = allianceDissolved;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}

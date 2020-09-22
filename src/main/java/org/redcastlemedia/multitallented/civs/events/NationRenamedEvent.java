package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.nations.Nation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class NationRenamedEvent extends Event {
    private static final HandlerList hList = new HandlerList();
    public static HandlerList getHandlerList() {
        return hList;
    }

    private Nation nation;
    private String newName;

    @Override
    public HandlerList getHandlers() {
        return hList;
    }

}

package org.redcastlemedia.multitallented.civs.events;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.spells.SpellResult;

public class EnterCombatEvent extends Event {
    private SpellResult result = SpellResult.NORMAL;
    private static final HandlerList hList = new HandlerList();
    public final UUID UUID;

    public EnterCombatEvent(UUID uuid) {
        this.UUID = uuid;
    }

    public void setResult(SpellResult result) {
        this.result = result;
    }

    public SpellResult getResult() {
        return result;
    }

    public static HandlerList getHandlerList() {
        return hList;
    }

    @Override
    public HandlerList getHandlers() {
        return hList;
    }
}


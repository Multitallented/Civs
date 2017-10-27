package org.redcastlemedia.multitallented.civs.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.redcastlemedia.multitallented.civs.spells.Spell;
import org.redcastlemedia.multitallented.civs.spells.SpellResult;

/**
 * This is thrown whenever a condition is being tested
 * @author Multitallented
 */
public class ConditionEvent extends Event {
    private SpellResult result = SpellResult.NORMAL;
    private static final HandlerList hList = new HandlerList();
    public final Spell SPELL;
    public final int INDEX;

    public ConditionEvent(Spell cs, int index) {
        this.SPELL = cs;
        this.INDEX = index;
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
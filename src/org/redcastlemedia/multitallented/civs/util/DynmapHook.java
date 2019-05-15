package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.dynmap.DynmapCommonAPI;
import org.redcastlemedia.multitallented.civs.events.TownCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDevolveEvent;
import org.redcastlemedia.multitallented.civs.events.TownEvolveEvent;

public class DynmapHook implements Listener {
    public static DynmapCommonAPI dynmapCommonAPI = null;

    public static boolean isMarkerAPIReady() {
        if (dynmapCommonAPI == null) {
            return true;
        }
        return dynmapCommonAPI.markerAPIInitialized();
    }

    @EventHandler
    public void onTownCreation(TownCreatedEvent event) {

    }

    @EventHandler
    public void onTownEvolve(TownEvolveEvent event) {

    }

    @EventHandler
    public void onTownDevolve(TownDevolveEvent event) {

    }

    @EventHandler
    public void onTownDestroyedEvent(TownDestroyedEvent event) {}

}

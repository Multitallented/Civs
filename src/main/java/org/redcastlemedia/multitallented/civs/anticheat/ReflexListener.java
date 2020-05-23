package org.redcastlemedia.multitallented.civs.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

import rip.reflex.api.event.ReflexCheckEvent;

public class ReflexListener implements Listener {

    public ReflexListener() {
        Bukkit.getPluginManager().registerEvents(this, Civs.getInstance());
    }


    @EventHandler(ignoreCancelled = true)
    public void onReflexCheckEvent(ReflexCheckEvent event) {
        if (event.getResult().shouldCancel()) {
            return;
        }
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (ExemptionType exemptionType : civilian.getExemptions()) {
            if (ReflexExemptionAssembler.mapExemptionTypeToCheats(exemptionType).contains(event.getCheat())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}

package org.redcastlemedia.multitallented.civs.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

import me.vagdedes.spartan.api.PlayerViolationEvent;

public class SpartanListener implements Listener {

    public SpartanListener() {
        Bukkit.getPluginManager().registerEvents(this, Civs.getInstance());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpartanViolation(PlayerViolationEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (ExemptionType exemptionType : civilian.getExemptions()) {
            if (SpartanExemptionAssembler.mapExemptionTypeToHackType(exemptionType).contains(event.getHackType())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}

package org.redcastlemedia.multitallented.civs.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

import com.gmail.olexorus.witherac.api.ViolationEvent;

public class WitherACListener implements Listener {

    public WitherACListener() {
        Bukkit.getPluginManager().registerEvents(this, Civs.getInstance());
    }


    @EventHandler(ignoreCancelled = true)
    public void onWitherViolationEvent(ViolationEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (ExemptionType exemptionType : civilian.getExemptions()) {
            if (WitherACExemptionAssembler.mapExemptionTypeToCheckType(exemptionType).contains(event.getType())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}

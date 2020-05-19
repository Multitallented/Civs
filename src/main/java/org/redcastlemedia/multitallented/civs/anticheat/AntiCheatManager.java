package org.redcastlemedia.multitallented.civs.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

import me.vagdedes.spartan.api.PlayerViolationEvent;

@CivsSingleton
public class AntiCheatManager implements Listener {


    private static AntiCheatManager instance = null;

    public static AntiCheatManager getInstance() {
        if (instance == null) {
            instance = new AntiCheatManager();
        }
        return instance;
    }

    public AntiCheatManager() {
        setupDependencies();
    }

    private void setupDependencies() {
        // TODO hook into anti-cheat plugins
    }

    @EventHandler
    public void onPlayerViolationEvent(PlayerViolationEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (ExemptionType exemptionType : civilian.getExemptions()) {
            if (SpartanExemptionAssembler.mapExemptionTypeToHackType(exemptionType).contains(event.getHackType())) {
                event.setCancelled(true);
            }
        }
    }
}

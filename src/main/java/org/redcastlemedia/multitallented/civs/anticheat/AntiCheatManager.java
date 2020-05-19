package org.redcastlemedia.multitallented.civs.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

import me.jinky.BAC;
import me.vagdedes.spartan.api.PlayerViolationEvent;

@CivsSingleton
public class AntiCheatManager implements Listener {

    private static AntiCheatManager instance = null;
    private static BAC basicAntiCheat = null;

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
        if (Bukkit.getPluginManager().isPluginEnabled("BasicAntiCheat")) {
            basicAntiCheat = (BAC) Bukkit.getPluginManager().getPlugin("BasicAntiCheat");
        } else if (Bukkit.getPluginManager().isPluginEnabled("BAC")) {
            basicAntiCheat = (BAC) Bukkit.getPluginManager().getPlugin("BAC");
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("BasicAntiCheat") || event.getPlugin().getName().equals("BAC")) {
            basicAntiCheat = (BAC) event.getPlugin();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equals("BasicAntiCheat") || event.getPlugin().getName().equals("BAC")) {
            basicAntiCheat = null;
        }
    }

    public void addExemption(Player player, ExemptionType exemptionType, long duration) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (basicAntiCheat != null) {
            basicAntiCheat.EXEMPTHANDLER.addExemptionBlock(player, (int) duration);
        }

        civilian.getExemptions().add(exemptionType);
        Bukkit.getScheduler().runTaskLater(Civs.getInstance(), () -> civilian.getExemptions().remove(exemptionType), duration / 50);
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

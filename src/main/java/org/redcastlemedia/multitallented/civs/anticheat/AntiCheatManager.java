package org.redcastlemedia.multitallented.civs.anticheat;

import java.util.Set;

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

import com.gmail.olexorus.witherac.api.ViolationEvent;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import me.jinky.BAC;
import me.vagdedes.spartan.api.PlayerViolationEvent;
import rip.reflex.api.event.ReflexCheckEvent;

@CivsSingleton
public class AntiCheatManager implements Listener {

    private static AntiCheatManager instance = null;
    private static BAC basicAntiCheat = null;
    private static NoCheatPlus noCheatPlus = null;

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
        } else if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            noCheatPlus = (NoCheatPlus) Bukkit.getPluginManager().getPlugin("NoCheatPlus");
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("BasicAntiCheat") || event.getPlugin().getName().equals("BAC")) {
            basicAntiCheat = (BAC) event.getPlugin();
        } else if (event.getPlugin().getName().equals("NoCheatPlus")) {
            noCheatPlus = (NoCheatPlus) event.getPlugin();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equals("BasicAntiCheat") || event.getPlugin().getName().equals("BAC")) {
            basicAntiCheat = null;
        } else if (event.getPlugin().getName().equals("NoCheatPlus")) {
            noCheatPlus = null;
        }
    }

    public void addExemption(Player player, ExemptionType exemptionType, long duration) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (basicAntiCheat != null) {
            basicAntiCheat.EXEMPTHANDLER.addExemptionBlock(player, (int) duration);
        }
        if (noCheatPlus != null) {
            Set<CheckType> ncpChecks = NCPExemptionAssembler.mapExemptionTypeToCheatTypes(exemptionType);
            for (CheckType checkType : ncpChecks) {
                NCPExemptionManager.exemptPermanently(player, checkType);
            }
            Bukkit.getScheduler().runTaskLater(Civs.getInstance(), () -> {
                if (noCheatPlus != null) {
                    for (CheckType checkType : ncpChecks) {
                        NCPExemptionManager.unexempt(player, checkType);
                    }
                }
            }, duration / 50);
        }

        civilian.getExemptions().add(exemptionType);
        Bukkit.getScheduler().runTaskLater(Civs.getInstance(), () -> civilian.getExemptions().remove(exemptionType), duration / 50);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBACPlayerViolationEvent(PlayerViolationEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (ExemptionType exemptionType : civilian.getExemptions()) {
            if (SpartanExemptionAssembler.mapExemptionTypeToHackType(exemptionType).contains(event.getHackType())) {
                event.setCancelled(true);
                return;
            }
        }
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerViolation(me.konsolas.aac.api.PlayerViolationEvent event) {
        Player player = event.getPlayer();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        for (ExemptionType exemptionType : civilian.getExemptions()) {
            if (AACExemptionAssembler.mapExemptionTypeToHackTypes(exemptionType).contains(event.getHackType())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}

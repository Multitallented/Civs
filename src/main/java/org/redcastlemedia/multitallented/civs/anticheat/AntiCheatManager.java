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

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import me.jinky.BAC;

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
        } else if (Bukkit.getPluginManager().isPluginEnabled("Reflex")) {
            new ReflexListener();
        } else if (Bukkit.getPluginManager().isPluginEnabled("Spartan")) {
            new SpartanListener();
        } else if (Bukkit.getPluginManager().isPluginEnabled("WitherAC")) {
            new WitherACListener();
        } else if (Bukkit.getPluginManager().isPluginEnabled("AAC")) {
            new AACListener();
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("BasicAntiCheat") ||
                event.getPlugin().getName().equalsIgnoreCase("BAC")) {
            basicAntiCheat = (BAC) event.getPlugin();
        } else if (event.getPlugin().getName().equalsIgnoreCase("NoCheatPlus")) {
            noCheatPlus = (NoCheatPlus) event.getPlugin();
        } else if (event.getPlugin().getName().equalsIgnoreCase("Reflex")) {
            new ReflexListener();
        } else if (event.getPlugin().getName().equalsIgnoreCase("Spartan")) {
            new SpartanListener();
        } else if (event.getPlugin().getName().equalsIgnoreCase("WitherAC")) {
            new WitherACListener();
        } else if (event.getPlugin().getName().equalsIgnoreCase("AAC")) {
            new AACListener();
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
}

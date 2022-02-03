package org.redcastlemedia.multitallented.civs.plugins;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.ServicePriority;
import org.dynmap.DynmapCommonAPI;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.dynmaphook.DynmapHook;
import org.redcastlemedia.multitallented.civs.placeholderexpansion.PlaceHook;
import org.redcastlemedia.multitallented.civs.util.Constants;

import github.scarsz.discordsrv.DiscordSRV;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.Indyuce.mmoitems.MMOItems;
import ru.endlesscode.mimic.classes.BukkitClassSystem;

public class PluginListener implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if ("dynmap".equalsIgnoreCase(event.getPlugin().getName())) {
            DynmapHook.dynmapCommonAPI = (DynmapCommonAPI) event.getPlugin();
            DynmapHook.initMarkerSet();
        } else if ("Mimic".equalsIgnoreCase(event.getPlugin().getName())) {
            Civs.mimic = true;
            Civs.getInstance().getServer().getServicesManager().register(BukkitClassSystem.Provider.class,
                    new MimicClassProvider.Provider(), Civs.getInstance(), ServicePriority.Normal);
        } else if (Constants.PLACEHOLDER_API.equals(event.getPlugin().getName()) &&
                Bukkit.getPluginManager().isPluginEnabled(Constants.PLACEHOLDER_API)) {
            new PlaceHook().register();
            Civs.placeholderAPI = (PlaceholderAPIPlugin) event.getPlugin();
        } else if ("MMOItems".equals(event.getPlugin().getName()) &&
                Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            Civs.mmoItems = MMOItems.plugin;
        } else if ("DiscordSRV".equals(event.getPlugin().getName()) &&
                Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            Civs.discordSRV = DiscordSRV.getPlugin();
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if ("dynmap".equalsIgnoreCase(event.getPlugin().getName())) {
            DynmapHook.dynmapCommonAPI = null;
        } else if ("Mimic".equalsIgnoreCase(event.getPlugin().getName())) {
            Civs.mimic = null;
        } else if ("MMOItems".equals(event.getPlugin().getName()) &&
                !Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            Civs.mmoItems = null;
        } else if ("DiscordSRV".equals(event.getPlugin().getName()) &&
                !Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            Civs.discordSRV = null;
        } else if (Constants.PLACEHOLDER_API.equals(event.getPlugin().getName())) {
            Civs.placeholderAPI = null;
        }
    }
}

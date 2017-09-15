package org.redcastlemedia.multitallented.civs;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.redcastlemedia.multitallented.civs.commands.CivCommand;
import org.redcastlemedia.multitallented.civs.commands.MenuCommand;
import org.redcastlemedia.multitallented.civs.menus.MainMenu;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.regions.RegionListener;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

import java.util.HashMap;

public class Civs extends JavaPlugin {

    private HashMap<String, CivCommand> commandList = new HashMap<>();
    public static String NAME = "Civs";
    public static String VERSION = "0.0.1";
    private static Economy econ;
    private static Permission perm;
    private static Chat chat;
    private static Civs civs;

    @Override
    public void onEnable() {
        setupChat();
        setupEconomy();
        setupPermissions();
        new RegionManager();
        initCommands();
        initListeners();
        civs = this;
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String message, String[] args) {
        if (args.length < 1) {
            args = new String[1];
            args[0] = "menu";
        }
        CivCommand civCommand = commandList.get(args[0]);
        if (civCommand == null) {
            commandSender.sendMessage(getPrefix() + "Invalid command " + args[0]);
            return true;
        }
        return civCommand.runCommand(commandSender, command, message, args);
    }

    private void initCommands() {
        commandList.put("menu", new MenuCommand());
    }

    private void initListeners() {
        Bukkit.getPluginManager().registerEvents(new MainMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionHandler(), this);
        Bukkit.getPluginManager().registerEvents(new RegionListener(), this);
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
            if (econ != null)
                System.out.println("[Townships] Hooked into " + econ.getName());
        }
        return econ != null;
    }
    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perm = permissionProvider.getProvider();
            if (perm != null)
                System.out.println("[Townships] Hooked into " + perm.getName());
        }
        return (perm != null);
    }
    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
        return (chat != null);
    }

    public static Economy getEcon() {
        return econ;
    }
    public static Permission getPerm() {
        return perm;
    }
    public static String getPrefix() {
        return "[" + NAME + "] ";
    }
    public static synchronized Civs getInstance() {
        return civs;
    }
}

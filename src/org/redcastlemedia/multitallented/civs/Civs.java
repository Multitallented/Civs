package org.redcastlemedia.multitallented.civs;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.commands.CivCommand;
import org.redcastlemedia.multitallented.civs.commands.MenuCommand;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.*;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.regions.RegionListener;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.scheduler.DailyScheduler;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

public class Civs extends JavaPlugin {

    private HashMap<String, CivCommand> commandList = new HashMap<>();
    public static String NAME = "Civs";
    public static String VERSION = "0.0.1";
    private static Economy econ;
    private static Permission perm;
    private static Chat chat;
    private static Civs civs;
    public static Logger logger;

    @Override
    public void onEnable() {
        civs = this;
        logger = getLogger();
        setupChat();
        setupEconomy();
        setupPermissions();

        new ConfigManager(new File(getDataFolder(), "config.yml"));
        new LocaleManager(new File(getDataFolder(), "locale.yml"));
        new ItemManager();
        RegionManager regionManager = new RegionManager();
        regionManager.loadAllRegions();
        new CivilianManager();

        initCommands();
        initListeners();

        initScheduler();
        civs = this;
        getLogger().info("v" + VERSION + " is now enabled!");
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

    private void initScheduler() {
        Date date = new Date();
        date.setSeconds(0);
        date.setMinutes(0);
        date.setHours(0);
        long timeUntilDay = (86400000 + date.getTime() - System.currentTimeMillis()) / 50;
        System.out.println(getPrefix() + timeUntilDay + " ticks until 00:00");
        DailyScheduler dailyScheduler = new DailyScheduler();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, dailyScheduler, timeUntilDay, 1728000);

        CommonScheduler commonScheduler = new CommonScheduler();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, commonScheduler, 4L, 4L);
    }

    private void initCommands() {
        commandList.put("menu", new MenuCommand());
    }

    private void initListeners() {
        Bukkit.getPluginManager().registerEvents(new MainMenu(), this);
        Bukkit.getPluginManager().registerEvents(new LanguageMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ItemsMenu(), this);
        Bukkit.getPluginManager().registerEvents(new CommunityMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ShopMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionHandler(), this);
        Bukkit.getPluginManager().registerEvents(new RegionListener(), this);
        Bukkit.getPluginManager().registerEvents(new CivilianListener(), this);
    }

    public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
            if (econ != null)
                System.out.println(Civs.getPrefix() + "Hooked into " + econ.getName());
        }
        return econ != null;
    }
    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perm = permissionProvider.getProvider();
            if (perm != null)
                System.out.println(Civs.getPrefix() + "Hooked into " + perm.getName());
        }
        return (perm != null);
    }
    private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
            if (chat != null)
                System.out.println(Civs.getPrefix() + "Hooked into " + chat.getName());
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
        return ChatColor.GREEN + "[" + NAME + "] ";
    }
    public static synchronized Civs getInstance() {
        return civs;
    }
}

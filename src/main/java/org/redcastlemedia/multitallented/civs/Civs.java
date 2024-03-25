package org.redcastlemedia.multitallented.civs;

import github.scarsz.discordsrv.DiscordSRV;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.Indyuce.mmoitems.MMOItems;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redcastlemedia.multitallented.civs.chat.ChatManager;
import org.redcastlemedia.multitallented.civs.civilians.allowedactions.AllowedActionsListener;
import org.redcastlemedia.multitallented.civs.commands.CivCommand;
import org.redcastlemedia.multitallented.civs.commands.CivsCommand;
import org.redcastlemedia.multitallented.civs.commands.TabComplete;
import org.redcastlemedia.multitallented.civs.dynmaphook.DynmapHook;
import org.redcastlemedia.multitallented.civs.pl3xmap.Pl3xMapHook;
import org.redcastlemedia.multitallented.civs.placeholderexpansion.PlaceHook;
import org.redcastlemedia.multitallented.civs.plugins.PluginListener;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.StructureUtil;
import org.redcastlemedia.multitallented.civs.regions.effects.ConveyorEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.FlyEffect;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.scheduler.DailyScheduler;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.DebugLogger;
import org.redcastlemedia.multitallented.civs.util.LogInfo;
import org.redcastlemedia.multitallented.civs.util.Util;
import org.redcastlemedia.multitallented.civs.worldedit.WorldEditSessionListener;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Civs extends JavaPlugin {

    public static File dataLocation;
    public static Boolean mimic = null;
    private HashMap<String, CivCommand> commandList = new HashMap<>();
    public static final String NAME = "Civs";
    public static Economy econ;
    public static Permission perm;
    public static MMOItems mmoItems;
    public static DiscordSRV discordSRV;
    public static PlaceholderAPIPlugin placeholderAPI;
    protected static Civs civs;
    public static Logger logger;
    private TabComplete tabComplete;

    @Override
    public void onEnable() {
        civs = this;
        dataLocation = getDataFolder();
        logger = Logger.getLogger("Minecraft");
        setupDependencies();
        setupEconomy();
        setupPermissions();

        instantiateSingletons();
        TownManager.getInstance().checkAllTownsForWarEnabled();

        initCommands();

        initScheduler();
        fancyPrintLog();
    }

    @Override
    public void onDisable() {
//        BlockLogger.getInstance().saveBlocks();
        FlyEffect.removeFlyFromAllPlayers();
        StructureUtil.removeAllBoundingBoxes();
        RegionManager.getInstance().saveAllUnsavedRegions();
        TownManager.getInstance().saveAllUnsavedTowns();
        ConveyorEffect.getInstance().onDisable();
        getLogger().info(LogInfo.DISABLED);
        Bukkit.getScheduler().cancelTasks(this);
        AllowedActionsListener.getInstance().onDisable();
        ChatManager.getInstance().onDisable();
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String message, String[] args) {
        if (args.length < 1) {
            args = new String[1];
            args[0] = "menu";
        }
        if (commandSender instanceof Player &&
                Util.isDisallowedByWorld(((Player) commandSender).getWorld().getName())) {
            return true;
        }
        CivCommand civCommand = commandList.get(args[0]);
        if (civCommand == null) {
            commandSender.sendMessage(getPrefix() + "Invalid command " + args[0]);
            return true;
        }
        return civCommand.runCommand(commandSender, command, message, args);
    }

    private void fancyPrintLog() {
        logger.info(LogInfo.INFO);
        logger.info(LogInfo.PH_VOID);

        logger.info(LogInfo.PH_INFO);
        if (econ != null) {
            logger.log(Level.INFO, "{0}", LogInfo.HOOKECON + econ.getName());
        }
        if (perm != null) {
            logger.log(Level.INFO, "{0}", LogInfo.HOOKPERM + perm.getName());
        }
        if (Bukkit.getPluginManager().isPluginEnabled(Constants.PLACEHOLDER_API)) {
            logger.log(Level.INFO, "{0}", LogInfo.HOOKCHAT + Constants.PLACEHOLDER_API);
        }
        if (mmoItems != null) {
            logger.log(Level.INFO, "{0} MMOItems", LogInfo.HOOKITEMS);
        }
        if (discordSRV != null) {
            logger.log(Level.INFO, "{0} DiscordSRV", LogInfo.HOOKCHAT);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("WorldEdit") && ConfigManager.getInstance().isSafeWE()) {
            WorldEditSessionListener.init();
            logger.log(Level.INFO, "{0}", LogInfo.HOOKWE);
        }
        logger.info(LogInfo.PH_INFO);

        logger.info(LogInfo.PH_VOID);

        logger.info(LogInfo.ENABLED);
    }

    private void initScheduler() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        long timeUntilDay = (86400000 + calendar.getTimeInMillis() - System.currentTimeMillis()) / 50;
        Civs.logger.log(Level.INFO, "{0} ticks until 00:00", timeUntilDay);
        DailyScheduler dailyScheduler = new DailyScheduler();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, dailyScheduler, timeUntilDay, 1728000);

        if (ConfigManager.getInstance().isDebugLog()) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, DebugLogger.timedDebugTask(), 600L, 600L);
        }
        CommonScheduler commonScheduler = new CommonScheduler();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, commonScheduler, 4L, 4L);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

            @Override
            public void run() {
                RegionManager.getInstance().saveNextRegion();
                TownManager.getInstance().saveNextTown();
            }
        }, 20L, 20L);
    }

    private void initCommands() {
        Reflections reflections = new Reflections("org.redcastlemedia.multitallented.civs.commands");
        Set<Class<? extends CivCommand>> commands = reflections.getSubTypesOf(CivCommand.class);
        for (Class<? extends CivCommand> currentCommandClass : commands) {
            try {
                if (Modifier.isAbstract(currentCommandClass.getModifiers())) {
                    continue;
                }
                CivCommand currentCommand = currentCommandClass.newInstance();
                for (String key : currentCommandClass.getAnnotation(CivsCommand.class).keys()) {
                    commandList.put(key, currentCommand);
                }
            } catch (Exception e) {
                Civs.logger.log(Level.SEVERE, "Unable to load {0} class", currentCommandClass.getName());
                Civs.logger.log(Level.SEVERE, "Exception generated", e);
            }
        }
        tabComplete = new TabComplete(commandList);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return tabComplete.onTabComplete(sender, command, alias, args);
    }

    //    private void initListeners() {
//        Bukkit.getPluginManager().registerEvents(new SpellListener(), this);
//        Bukkit.getPluginManager().registerEvents(new AIListener(), this);
//    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            econ = rsp.getProvider();
        }
    }
    private void setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            perm = permissionProvider.getProvider();
        }
    }
    private void setupDependencies() {
        if (Bukkit.getPluginManager().isPluginEnabled(Constants.PLACEHOLDER_API)) {
            new PlaceHook().register();
            placeholderAPI = (PlaceholderAPIPlugin) Bukkit.getPluginManager().getPlugin(Constants.PLACEHOLDER_API);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            mmoItems = MMOItems.plugin;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            discordSRV = DiscordSRV.getPlugin();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            Bukkit.getPluginManager().registerEvents(new DynmapHook(), this);
            DynmapHook.dynmapCommonAPI = (DynmapCommonAPI) Bukkit.getPluginManager().getPlugin("dynmap");
            DynmapHook.initMarkerSet();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Pl3xMap")) {
            Pl3xMapHook pl3xMapHook = new Pl3xMapHook();
            Bukkit.getPluginManager().registerEvents(pl3xMapHook, this);
            pl3xMapHook.initMarkerSet();
        }
        Bukkit.getPluginManager().registerEvents(new PluginListener(), this);
    }

    private void instantiateSingletons() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        FilterBuilder filterBuilder = new FilterBuilder();
        configurationBuilder.addUrls(ClasspathHelper.forPackage("org.redcastlemedia.multitallented.civs"));
        filterBuilder.includePackage("org.redcastlemedia.multitallented.civs")
                .excludePackage("org.redcastlemedia.multitallented.civs.dynmaphook")
                .excludePackage("ru.endlesscode.mimic")
                .excludePackage("org.redcastlemedia.multitallented.civs.placeholderexpansion")
                .excludePackage("org.redcastlemedia.multitallented.civs.worldedit");
        configurationBuilder.filterInputsBy(filterBuilder);
        Reflections reflections = new Reflections(configurationBuilder);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(CivsSingleton.class);
        List<Class<?>> classList = new ArrayList<>(classes);
        classList.sort(Comparator.comparing(o -> o.getAnnotation(CivsSingleton.class).priority()));
        for (Class<?> currentSingleton : classList) {
            try {
                Method method = currentSingleton.getMethod("getInstance");
                method.invoke(currentSingleton);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "There was an error when calling  " + currentSingleton+".getInstance()", e);
            }
        }
    }

    public static Economy getEcon() {
        return econ;
    }
    public static Permission getPerm() {
        return perm;
    }
    public static String getPrefix() {
        return ConfigManager.getInstance().getCivsChatPrefix();
    }
    public static String getRawPrefix() { return ConfigManager.getInstance().civsChatPrefix;}
    public static synchronized Civs getInstance() {
        return civs;
    }
}

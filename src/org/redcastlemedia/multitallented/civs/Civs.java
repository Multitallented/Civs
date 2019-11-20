package org.redcastlemedia.multitallented.civs;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.commands.CivCommand;
import org.redcastlemedia.multitallented.civs.commands.CivsCommand;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.protections.DeathListener;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.regions.RegionListener;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.effects.ActiveEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.AntiCampEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.ArrowTurret;
import org.redcastlemedia.multitallented.civs.regions.effects.BedEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.CommandEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.ConveyorEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.EvolveEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.ForSaleEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.HousingEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.HuntEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.IntruderEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.JammerEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.PermissionEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.PotionAreaEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.RaidPortEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.RepairEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.SiegeEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.SpawnEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.TNTCannon;
import org.redcastlemedia.multitallented.civs.regions.effects.TeleportEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.TemporaryEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.VillagerEffect;
import org.redcastlemedia.multitallented.civs.regions.effects.WarehouseEffect;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.scheduler.DailyScheduler;
import org.redcastlemedia.multitallented.civs.spells.SpellListener;
import org.redcastlemedia.multitallented.civs.towns.GovernmentManager;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.update.UpdateUtil;
import org.redcastlemedia.multitallented.civs.util.DebugLogger;
import org.redcastlemedia.multitallented.civs.util.LogInfo;
import org.redcastlemedia.multitallented.civs.util.PlaceHook;
import org.redcastlemedia.multitallented.civs.util.StructureUtil;
import org.reflections.Reflections;

import github.scarsz.discordsrv.DiscordSRV;
import net.Indyuce.mmoitems.MMOItems;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class Civs extends JavaPlugin {

    private HashMap<String, CivCommand> commandList = new HashMap<>();
    public static String NAME = "Civs";
    public static Economy econ;
    public static Permission perm;
    public static MMOItems mmoItems;
    public static DiscordSRV discordSRV;
    private static Civs civs;
    public static Logger logger;

    @Override
    public void onEnable() {
        civs = this;
        logger = Logger.getLogger("Minecraft");
        UpdateUtil.checkUpdate();
        setupDependencies();
        setupEconomy();
        setupPermissions();

        ConfigManager.getInstance();
        LocaleManager.getInstance();
        new ItemManager();
        TutorialManager.getInstance();
        GovernmentManager.getInstance();
        new BlockLogger();
        MenuManager.getInstance();
        RegionManager regionManager = new RegionManager();
        regionManager.loadAllRegions();
        TownManager townManager = new TownManager();
        townManager.loadAllTowns();
        AllianceManager allianceManager = new AllianceManager();
        allianceManager.loadAllAlliances();
        new CivilianManager();

        initCommands();
        initListeners();

        initScheduler();
        civs = this;
        fancyPrintLog();
    }

    @Override
    public void onDisable() {
//        BlockLogger.getInstance().saveBlocks();
        StructureUtil.removeAllBoundingBoxes();
        RegionManager.getInstance().saveAllUnsavedRegions();
        TownManager.getInstance().saveAllUnsavedTowns();
        ConveyorEffect.getInstance().onDisable();
        getLogger().info(LogInfo.DISABLED);
        Bukkit.getScheduler().cancelTasks(this);
    }


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String message, String[] args) {
        if (args.length < 1) {
            args = new String[1];
            args[0] = "menu";
        }
        if (commandSender instanceof Player && ConfigManager.getInstance().getBlackListWorlds()
                .contains(((Player) commandSender).getWorld().getName())) {
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
            logger.info(LogInfo.HOOKECON + econ.getName());
        }
        if (perm != null) {
            logger.info(LogInfo.HOOKPERM + perm.getName());
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.info(LogInfo.HOOKCHAT + "PlaceholderAPI");
        }
        if (mmoItems != null) {
            logger.info(LogInfo.HOOKCHAT + "MMOItems");
        }
        if (discordSRV != null) {
            logger.info(LogInfo.HOOKCHAT + "DiscordSRV");
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
        Civs.logger.info(timeUntilDay + " ticks until 00:00");
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
                CivCommand currentCommand = currentCommandClass.newInstance();
                for (String key : currentCommandClass.getAnnotation(CivsCommand.class).keys()) {
                    commandList.put(key, currentCommand);
                }
            } catch (Exception e) {

            }
        }
    }

    private void initListeners() {
        Bukkit.getPluginManager().registerEvents(new ProtectionHandler(), this);
        Bukkit.getPluginManager().registerEvents(new RegionListener(), this);
        Bukkit.getPluginManager().registerEvents(new CivilianListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpellListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new ArrowTurret(), this);
        Bukkit.getPluginManager().registerEvents(new TNTCannon(), this);
        Bukkit.getPluginManager().registerEvents(new VillagerEffect(), this);
        Bukkit.getPluginManager().registerEvents(new ConveyorEffect(), this);
        Bukkit.getPluginManager().registerEvents(new RaidPortEffect(), this);
        Bukkit.getPluginManager().registerEvents(new EvolveEffect(), this);
        Bukkit.getPluginManager().registerEvents(new AntiCampEffect(), this);
        Bukkit.getPluginManager().registerEvents(new SiegeEffect(), this);
        Bukkit.getPluginManager().registerEvents(new IntruderEffect(), this);
        Bukkit.getPluginManager().registerEvents(new TemporaryEffect(), this);
        Bukkit.getPluginManager().registerEvents(new SpawnEffect(), this);
        Bukkit.getPluginManager().registerEvents(new RepairEffect(), this);
        Bukkit.getPluginManager().registerEvents(new PotionAreaEffect(), this);
        Bukkit.getPluginManager().registerEvents(new WarehouseEffect(), this);
        Bukkit.getPluginManager().registerEvents(new ForSaleEffect(), this);
        Bukkit.getPluginManager().registerEvents(new PermissionEffect(), this);
        Bukkit.getPluginManager().registerEvents(new CommandEffect(), this);
        Bukkit.getPluginManager().registerEvents(new HuntEffect(), this);
        Bukkit.getPluginManager().registerEvents(new ActiveEffect(), this);
        Bukkit.getPluginManager().registerEvents(new TeleportEffect(), this);
        Bukkit.getPluginManager().registerEvents(new JammerEffect(), this);
//        Bukkit.getPluginManager().registerEvents(new AIListener(), this);

        new HousingEffect();
        new BedEffect();
    }

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
    public void setupDependencies() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHook().register();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("MMOItems")) {
            mmoItems = MMOItems.plugin;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("DiscordSRV")) {
            discordSRV = DiscordSRV.getPlugin();
        }
//        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
//        if (chatProvider != null) {
//            chat = chatProvider.getProvider();
//            if (chat != null)
//                System.out.println(Civs.getPrefix() + "Hooked into chat plugin " + chat.getName());
//        }
//        return (chat != null);
    }

    public static Economy getEcon() {
        return econ;
    }
    public static Permission getPerm() {
        return perm;
    }
    public static String getPrefix() {
        return ConfigManager.getInstance().getCivsChatPrefix() + " ";
    }
    public static String getRawPrefix() { return ConfigManager.getInstance().civsChatPrefix + " ";}
    public static synchronized Civs getInstance() {
        return civs;
    }
}

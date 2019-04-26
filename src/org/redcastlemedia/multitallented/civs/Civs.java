package org.redcastlemedia.multitallented.civs;

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
import org.redcastlemedia.multitallented.civs.civilians.TutorialManager;
import org.redcastlemedia.multitallented.civs.commands.*;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.*;
import org.redcastlemedia.multitallented.civs.protections.DeathListener;
import org.redcastlemedia.multitallented.civs.protections.ProtectionHandler;
import org.redcastlemedia.multitallented.civs.regions.RegionListener;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.effects.*;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.scheduler.DailyScheduler;
import org.redcastlemedia.multitallented.civs.spells.SpellListener;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.PlaceHook;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Logger;

public class Civs extends JavaPlugin {

    private HashMap<String, CivCommand> commandList = new HashMap<>();
    public static String NAME = "Civs";
    public static String VERSION = "0.0.1";
    public static Economy econ;
    public static Permission perm;
    private static Civs civs;
    public static Logger logger;

    @Override
    public void onEnable() {
        civs = this;
        logger = Logger.getLogger("Minecraft");
        setupPlaceholders();
        setupEconomy();
        setupPermissions();

        new ConfigManager(new File(getDataFolder(), "config.yml"));
        new LocaleManager(new File(getDataFolder(), "locale.yml"));
        new ItemManager();
        new TutorialManager();
        new BlockLogger();
        RegionManager regionManager = new RegionManager();
        regionManager.loadAllRegions();
        TownManager townManager = new TownManager();
        townManager.loadAllTowns();
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
        getLogger().info(LogInfo.DISABLED);
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

    private void fancyPrintLog() {
        logger.info(LogInfo.INFO);
        logger.info(LogInfo.PH_VOID);

        logger.info(LogInfo.PH_INFO);
        if (econ != null)
            logger.info(LogInfo.HOOKECON + econ.getName());
        if (perm != null)
            logger.info(LogInfo.HOOKPERM + perm.getName());
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            logger.info(LogInfo.HOOKCHAT + "PlaceholderAPI");
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

        CommonScheduler commonScheduler = new CommonScheduler();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, commonScheduler, 4L, 4L);

        //sync repeating task for war
    }

    private void initCommands() {
        commandList.put("menu", new MenuCommand());
        commandList.put("invite", new InviteTownCommand());
        commandList.put("accept", new AcceptInviteCommand());
        commandList.put("setmember", new SetMemberCommand());
        commandList.put("setowner", new SetOwnerCommand());
        commandList.put("setguest", new SetGuestCommand());
        commandList.put("removemember", new RemoveMemberCommand());
        commandList.put("add", new AddMemberCommand());
        commandList.put("town", new TownCommand());
        PortCommand portCommand = new PortCommand();
        commandList.put("port", portCommand);
        commandList.put("spawn", portCommand);
        commandList.put("home", portCommand);
        commandList.put("rename", new RenameCommand());
        commandList.put("bounty", new BountyCommand());
    }

    private void initListeners() {
        Bukkit.getPluginManager().registerEvents(new MainMenu(), this);
        Bukkit.getPluginManager().registerEvents(new LanguageMenu(), this);
        Bukkit.getPluginManager().registerEvents(new BlueprintsMenu(), this);
        Bukkit.getPluginManager().registerEvents(new CommunityMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ShopMenu(), this);
        Bukkit.getPluginManager().registerEvents(new RecipeMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ConfirmationMenu(), this);
        Bukkit.getPluginManager().registerEvents(new RegionTypeInfoMenu(), this);
        Bukkit.getPluginManager().registerEvents(new BuiltRegionMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ClassTypeInfoMenu(), this);
        Bukkit.getPluginManager().registerEvents(new MemberActionMenu(), this);
        Bukkit.getPluginManager().registerEvents(new RegionActionMenu(), this);
        Bukkit.getPluginManager().registerEvents(new SpellsMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ViewMembersMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ListAllPlayersMenu(), this);
        Bukkit.getPluginManager().registerEvents(new ProtectionHandler(), this);
        Bukkit.getPluginManager().registerEvents(new RegionListener(), this);
        Bukkit.getPluginManager().registerEvents(new CivilianListener(), this);
        Bukkit.getPluginManager().registerEvents(new TownListMenu(), this);
        Bukkit.getPluginManager().registerEvents(new TownActionMenu(), this);
        Bukkit.getPluginManager().registerEvents(new TownInviteMenu(), this);
        Bukkit.getPluginManager().registerEvents(new TownInviteConfirmationMenu(), this);
        Bukkit.getPluginManager().registerEvents(new SpellListener(), this);
        Bukkit.getPluginManager().registerEvents(new TownTypeInfoMenu(), this);
        Bukkit.getPluginManager().registerEvents(new DestroyConfirmationMenu(), this);
        Bukkit.getPluginManager().registerEvents(new LeaderboardMenu(), this);
        Bukkit.getPluginManager().registerEvents(new LeaveConfirmationMenu(), this);
        Bukkit.getPluginManager().registerEvents(new PortMenu(), this);
        Bukkit.getPluginManager().registerEvents(new RegionListMenu(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerProfileMenu(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new SpellTypeInfoMenu(), this);
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
        Bukkit.getPluginManager().registerEvents(new WarehouseEffect(), this);
        Bukkit.getPluginManager().registerEvents(new StartTutorialMenu(), this);

        new HousingEffect();
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
    public void setupPlaceholders() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceHook().register();
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
    public static String getPrefix() { return ChatColor.GREEN + "[" + NAME + "] ";
    }
    public static synchronized Civs getInstance() {
        return civs;
    }
}

package org.redcastlemedia.multitallented.civs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.FallbackConfigUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import lombok.Getter;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.CRITICAL)
public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "config.yml";

    private static ConfigManager configManager;
    List<String> blackListWorlds = new ArrayList<>();
    String defaultLanguage;
    boolean allowCivItemDropping;
    boolean explosionOverride;
    double priceMultiplier;
    double priceBase;
    double expModifier;
    int expBase;
    HashMap<String, String> itemGroups;
    String defaultClass;
    HashMap<String, Integer> groups;
    HashMap<String, CVItem> folderIcons;
    @Getter Map<String, List<String>> folderReqs = new HashMap<>();
    HashMap<String, Integer> creatureHealth = new HashMap<>();
    boolean useStarterBook;
    long jailTime;
    long deathGracePeriod;
    double pointsPerKillStreak;
    double moneyPerKillStreak;
    double pointsPerKillJoy;
    double moneyPerKillJoy;
    double pointsPerKill;
    double moneyPerKill;
    double pointsPerDeath;
    double pointsPerHalfHealth;
    double pointsPerQuarterHealth;
    double moneyPerKarma;
    int karmaPerKill;
    int karmaPerKillStreak;
    int powerPerKill;
    int powerPerNPCKill;
    long villagerCooldown;
    boolean denyArrowTurretShootAtMobs;
    int portMana;
    int portWarmup;
    int portCooldown;
    double portMoney;
    int portDamage;
    int portStamina;
    List<String> portReagents;
    boolean portSlowWarmup;
    int combatTagDuration;
    boolean portDuringCombat;
    boolean townRings;
    long karmaDepreciatePeriod;
    int combatLogPenalty;
    boolean destroyTownsAtZero;
    boolean allowFoodHealInCombat;
    long townGracePeriod;
    boolean useClassesAndSpells;
    Map<String, List<String>> customItemDescriptions;
    @Getter
    Material townRingMat;
    @Getter
    boolean checkWaterSpread;
    @Getter
    boolean useTutorial;
    @Getter
    boolean useGuide;
    @Getter
    List<String> levelList;
    @Getter
    boolean allowTeleportInCombat;
    @Getter
    boolean useParticleBoundingBoxes;
    @Getter
    String defaultGovernmentType;
    @Getter
    boolean allowChangingOfGovType;
    @Getter
    double maxTax;
    @Getter
    int daysBetweenVotes;
    @Getter
    double capitalismVotingCost;
    @Getter
    String topGuideSpacer;
    @Getter
    String bottomGuideSpacer;
    String civsChatPrefix;
    @Getter
    String prefixAllText;
    String civsItemPrefix;
    @Getter
    boolean useAnnouncements;
    @Getter
    long announcementPeriod;
    @Getter
    String revoltCost;
    @Getter
    boolean useBoundingBox;
    @Getter
    boolean mobsDropItemsWhenKilledInDenyDamage;
    @Getter
    boolean debugLog;
    @Getter
    double maxBankDeposit;
    @Getter
    double antiCampCost;
    @Getter
    boolean allowOfflineRaiding;
    @Getter
    boolean allowTeleportingOutOfHostileTowns;
    @Getter
    boolean townRingsCrumbleToGravel;
    @Getter
    boolean enterExitMessagesUseTitles;
    @Getter
    boolean dropMoneyIfZeroBalance;
    @Getter
    int minDistanceBetweenTowns;
    @Getter
    boolean disableRegionsInUnloadedChunks;
    @Getter
    String defaultConfigSet;
    @Getter
    int minPopulationForGovTransition;
    @Getter
    int lineBreakLength;
    Map<String, Integer> lineLengthMap;
    @Getter
    EnumMap<ChatChannel.ChatChannelType, String> chatChannels;
    @Getter
    long unloadedChestRefreshRate;
    @Getter
    int hardshipDepreciationPeriod;
    @Getter
    double huntKarma;
    @Getter
    boolean allowHuntNewPlayers;
    @Getter
    double hardshipPerKill;
    @Getter
    boolean useHardshipSystem;
    @Getter
    boolean keepRegionChunksLoaded;
    @Getter boolean useSkills;
    @Getter boolean huntCrossWorld;
    @Getter boolean silentExp;
    @Getter boolean deleteInvalidRegions;
    @Getter boolean skinsInMenu;
    @Getter boolean useBounties;
    @Getter boolean warningLogger;
    @Getter double percentPowerForUpgrade;
    @Getter boolean showKillStreakMessages;

    @Getter
    String chatChannelFormat;
    @Getter
    private int residenciesCount;
    @Getter
    private NavigableMap<Integer, String> residenciesCountOverride;

    @Getter boolean warnOnEmptyChatChannel;

    public ConfigManager() {
        loadDefaults();
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }
    public List<String> getBlackListWorlds() {
        return blackListWorlds;
    }
    public boolean getAllowSharingCivsItems() { return allowCivItemDropping; }
    public boolean getExplosionOverride() { return explosionOverride; }
    public boolean getUseStarterBook() { return useStarterBook; }
    public double getPriceMultiplier() { return priceMultiplier; }
    public double getPriceBase() { return priceBase; }
    public double getExpModifier() { return expModifier; }
    public int getExpBase() { return expBase; }
    public long getJailTime() { return jailTime; }
    public String getDefaultClass() { return defaultClass; }
    public Map<String, String> getItemGroups() { return itemGroups; }
    public Map<String, Integer> getGroups() { return groups; }
    public long getDeathGracePeriod() { return deathGracePeriod; }
    public double getPointsPerKillStreak() { return pointsPerKillStreak; }
    public double getMoneyPerKillStreak() { return moneyPerKillStreak; }
    public double getPointsPerKillJoy() { return pointsPerKillJoy; }
    public double getMoneyPerKillJoy() { return moneyPerKillJoy; }
    public double getPointsPerKill() { return pointsPerKill; }
    public double getMoneyPerKill() { return moneyPerKill; }
    public double getPointsPerDeath() { return pointsPerDeath; }
    public double getPointsPerHalfHealth() { return pointsPerHalfHealth; }
    public double getPointsPerQuarterHealth() { return pointsPerQuarterHealth; }
    public double getMoneyPerKarma() { return moneyPerKarma; }
    public int getKarmaPerKill() { return karmaPerKill; }
    public int getKarmaPerKillStreak() { return karmaPerKillStreak; }
    public int getPowerPerKill() { return powerPerKill; }
    public int getPowerPerNPCKill() { return powerPerNPCKill; }
    public long getVillagerCooldown() { return villagerCooldown; }
    public boolean getDenyArrowTurretShootAtMobs() { return denyArrowTurretShootAtMobs; }
    public int getPortMana() { return portMana; }
    public int getPortWarmup() {
        return portWarmup;
    }
    public int getPortCooldown() {
        return portCooldown;
    }
    public double getPortMoney() {
        return portMoney;
    }
    public int getPortDamage() {
        return portDamage;
    }
    public int getPortStamina() {
        return portStamina;
    }
    public List<String> getPortReagents() {
        return portReagents;
    }
    public boolean isPortSlowWarmup() {
        return portSlowWarmup;
    }
    public int getCombatTagDuration() { return combatTagDuration; }
    public boolean getPortDuringCombat() { return portDuringCombat; }
    public boolean getTownRings() { return townRings; }
    public long getKarmaDepreciatePeriod() {
        return karmaDepreciatePeriod;
    }
    public int getCombatLogPenalty() { return combatLogPenalty; }
    public boolean getDestroyTownsAtZero() { return destroyTownsAtZero; }
    public boolean getFoodHealInCombat() { return allowFoodHealInCombat; }
    public long getTownGracePeriod() { return townGracePeriod; }
    public boolean getUseClassesAndSpells() { return useClassesAndSpells; }
    public int getLineBreakLength(String locale) {
        return lineLengthMap.getOrDefault(locale, lineBreakLength);
    }

    public String getCivsChatPrefix() {
        return Util.parseColors(civsChatPrefix);
    }
    public String getCivsItemPrefix() {
        return Util.parseColors(civsItemPrefix + " ");
    }

    public int getCreatureHealth(String type) {
        if (type == null || creatureHealth == null) {
            return -1;
        }
        Integer integer = creatureHealth.get(type);
        return integer == null ? -1 : integer;
    }
    public CVItem getFolderIcon(String folderName) {
        CVItem cvItem = folderIcons.get(folderName);
        if (cvItem == null) {
            cvItem = CVItem.createCVItemFromString("CHEST");
        }
        return cvItem;
    }

    public void reload() {
        File config = new File(Civs.dataLocation, CONFIG_FILE_NAME);
        loadFile(config);
    }

    private void loadFile(File configFile) {
        FileConfiguration config = FallbackConfigUtil.getConfig(configFile, CONFIG_FILE_NAME);
        try {

            blackListWorlds = config.getStringList("black-list-worlds");
            defaultLanguage = config.getString("default-language", "en");
            allowCivItemDropping = config.getBoolean("allow-civ-item-sharing", false);
            explosionOverride = config.getBoolean("explosion-override", false);
            useStarterBook = config.getBoolean("use-starter-book", true);
            priceMultiplier = config.getDouble("price-multiplier", 1);
            priceBase = config.getDouble("price-base", 0);
            expModifier = config.getDouble("exp-modifier", 0.2);
            expBase = config.getInt("exp-base", 100);
            defaultClass = config.getString("default-class", "default");
            showKillStreakMessages = config.getBoolean("show-killstreak-messages", true);
            folderIcons = new HashMap<>();
            ConfigurationSection section2 = config.getConfigurationSection("folders");
            if (section2 != null) {
                for (String key : section2.getKeys(false)) {
                    String iconString = "CHEST";
                    if (config.isSet("folders." + key + ".icon")) {
                        iconString = config.getString("folders." + key + ".icon", "CHEST");
                    } else {
                        iconString = config.getString("folders." + key, "CHEST");
                    }
                    if (config.isSet("folders." + key + ".pre-reqs")) {
                        List<String> preReqs = config.getStringList("folders." + key + ".pre-reqs");
                        folderReqs.put(key, preReqs);
                    }
                    folderIcons.put(key, CVItem.createCVItemFromString(iconString));
                }
            }
            itemGroups = new HashMap<>();
            ConfigurationSection section1 = config.getConfigurationSection("item-groups");
            if (section1 != null) {
                for (String key : section1.getKeys(false)) {
                    itemGroups.put(key, config.getString("item-groups." + key));
                }
            }
            groups = new HashMap<>();
            ConfigurationSection section = config.getConfigurationSection("groups");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    groups.put(key, config.getInt("groups." + key, -1));
                }
            }
            creatureHealth = new HashMap<>();
            ConfigurationSection section3 = config.getConfigurationSection("creature-health");
            if (section3 != null) {
                for (String key : section3.getKeys(false)) {
                    creatureHealth.put(key, config.getInt("creature-health." + key, -1));
                }
            }
            jailTime = config.getLong("jail-time-seconds", 300) * 1000;
            deathGracePeriod = config.getLong("death-grace-period-seconds", 60) * 1000;
            pointsPerKillStreak = config.getDouble("points.killstreak", 0.1);
            moneyPerKillStreak = config.getDouble("money.killstreak", 1);
            pointsPerKillJoy = config.getDouble("points.killjoy", 0.2);
            moneyPerKillJoy = config.getDouble("money.killjoy", 1);
            pointsPerKill = config.getDouble("points.kill", 1);
            moneyPerKill = config.getDouble("money.kill", 1);
            pointsPerDeath = config.getDouble("points.death", -1);
            pointsPerHalfHealth = config.getDouble("points.half-health", 0.5);
            pointsPerQuarterHealth = config.getDouble("points.quarter-health", 1);
            moneyPerKarma = config.getDouble("money.karma", 0.1);
            karmaPerKill = config.getInt("karma-per-kill", 1);
            karmaPerKillStreak = config.getInt("karma-per-kill-streak", 1);
            powerPerKill = config.getInt("power-per-kill", 30);
            powerPerNPCKill = config.getInt("power-per-npc-kill", 5);
            villagerCooldown = config.getLong("villager-cooldown", 300);
            denyArrowTurretShootAtMobs = config.getBoolean("disable-arrow-turret-shooting-at-mobs", false);
            portMana = config.getInt("port.mana", 0);
            portWarmup = config.getInt("port.warmup", 5);
            portCooldown = config.getInt("port.cooldown", 60);
            portMoney = config.getDouble("port.money", 0);
            portDamage = config.getInt("port.damage", 0);
            portStamina = config.getInt("port.stamina", 0);
            portSlowWarmup = config.getBoolean("port.slow-warmup", true);
            portReagents = config.getStringList("port.reagents");
            combatTagDuration = config.getInt("combat-tag-duration", 60);
            huntCrossWorld = config.getBoolean("allow-hunt-cross-world", false);
            portDuringCombat = config.getBoolean("port.port-during-combat", false);
            getTownRingSettings(config);
            karmaDepreciatePeriod = config.getLong("karma-depreciate-period", 43200);
            combatLogPenalty = config.getInt("combat-log-out-percent-damage", 80);
            destroyTownsAtZero = config.getBoolean("destroy-towns-at-zero", false);
            allowFoodHealInCombat = config.getBoolean("allow-food-heal-in-combat", true);
            allowTeleportInCombat = config.getBoolean("allow-teleporting-during-combat", false);
            townGracePeriod = config.getLong("town-grace-period", 43200); //12 hours
            useTutorial = config.getBoolean("tutorial.use-tutorial", true);
            useGuide = config.getBoolean("tutorial.use-guide", true);
            checkWaterSpread = config.getBoolean("check-water-spread", true);
            customItemDescriptions = processMap(config.getConfigurationSection("custom-items"));
            levelList = config.getStringList("levels");
            useParticleBoundingBoxes = config.getBoolean("use-particle-bounding-boxes", false);
            getGovSettings(config);
            maxTax = config.getDouble("max-town-tax", 50);
            daysBetweenVotes = config.getInt("days-between-elections", 7);
            capitalismVotingCost = config.getDouble("capitalism-voting-cost", 200);
            topGuideSpacer = config.getString("top-guide-spacer", "-----------------Civs-----------------");
            bottomGuideSpacer = config.getString("bottom-guide-spacer", "--------------------------------------");
            civsChatPrefix = config.getString("civs-chat-prefix", "@{GREEN}[Civs] ");
            prefixAllText = Util.parseColors(config.getString("prefix-all-text", ""));
            civsItemPrefix = config.getString("civs-item-prefix", "Civs");
            skinsInMenu = config.getBoolean("show-player-skins-in-menus", true);
            if ("".equals(civsItemPrefix)) {
                civsItemPrefix = "Civs";
            }
            revoltCost = config.getString("revolt-cost", "GUNPOWDER*64");
            useAnnouncements = config.getBoolean("use-announcements", true);
            announcementPeriod = config.getLong("announcement-period", 240);
            useBoundingBox = config.getBoolean("use-region-bounding-box", true);
            mobsDropItemsWhenKilledInDenyDamage = config.getBoolean("stop-mobs-from-dropping-items-in-safe-zones", false);
            debugLog = config.getBoolean("debug-log", false);
            maxBankDeposit = config.getDouble("max-bank-deposit", -1);
            allowOfflineRaiding = config.getBoolean("allow-offline-raiding", true);
            allowTeleportingOutOfHostileTowns = config.getBoolean("allow-teleporting-out-of-hostile-towns", true);
            townRingsCrumbleToGravel = config.getBoolean("town-rings-crumble-to-gravel", true);
            enterExitMessagesUseTitles = config.getBoolean("enter-exit-messages-use-titles", true);
            dropMoneyIfZeroBalance = config.getBoolean("always-drop-money-if-no-balance", false);
            minDistanceBetweenTowns = config.getInt("min-distance-between-towns", 10);
            disableRegionsInUnloadedChunks = config.getBoolean("disable-regions-in-unloaded-chunks", false);
            defaultConfigSet = config.getString("default-config-set", "hybrid");
            minPopulationForGovTransition = config.getInt("min-population-for-auto-gov-transition", 4);
            lineBreakLength = config.getInt("line-break-length", 40);
            unloadedChestRefreshRate = config.getLong("unloaded-chest-refresh-rate", 10) * 60000;
            hardshipDepreciationPeriod = config.getInt("hardship-depreciation-period-in-days", 7);
            huntKarma = config.getDouble("hunt-karma", -250.0);
            allowHuntNewPlayers = config.getBoolean("hunt-new-players", true);
            hardshipPerKill = config.getDouble("hardship-per-kill", 0);
            useHardshipSystem = config.getBoolean("hardship-should-pay-damages", false);
            keepRegionChunksLoaded = config.getBoolean("keep-region-chunks-loaded", true);
            silentExp = config.getBoolean("no-exp-chat-messages", false);
            deleteInvalidRegions = config.getBoolean("delete-invalid-regions", false);
            lineLengthMap = new HashMap<>();
            useBounties = config.getBoolean("use-bounties", true);
            useSkills = config.getBoolean("use-skills", true);
            warningLogger = config.getBoolean("show-warning-logs", false);
            percentPowerForUpgrade = config.getDouble("percent-power-for-town-upgrade", 0.1);
            if (config.isSet("line-break-length-per-language")) {
                for (String key : config.getConfigurationSection("line-break-length-per-language").getKeys(false)) {
                    lineLengthMap.put(key, config.getInt("line-break-length-per-language." + key, lineBreakLength));
                }
            }
            chatChannels = new EnumMap<>(ChatChannel.ChatChannelType.class);
            if (config.isSet("chat-channels")) {
                for (String chatChannel : config.getConfigurationSection("chat-channels").getKeys(false)) {
                    try {
                        if (config.getBoolean("chat-channels." + chatChannel + ".enabled", false)) {
                            chatChannels.put(ChatChannel.ChatChannelType.valueOf(chatChannel.toUpperCase()),
                                    config.getString("chat-channels." + chatChannel + ".icon", Material.GRASS.name()));
                        }
                    } catch (Exception e) {
                        Civs.logger.log(Level.WARNING, "Invalid chat channel type {0}", chatChannel);
                    }
                }
            }
            if (chatChannels.isEmpty()) {
                chatChannels.put(ChatChannel.ChatChannelType.GLOBAL, Material.GRASS.name());
            }
            chatChannelFormat = config.getString("chat-channel-format", "[$channel$]$player$: $message$");

            if (config.isSet("player-residencies-count")) {
                residenciesCount = config.getInt("player-residencies-count");
            }

            if (config.isSet("player-residencies-count-override")) {
                for (String count : config.getConfigurationSection("player-residencies-count-override").getKeys(false)) {
                    String perm = config.getString("player-residencies-count-override." + count);
                    residenciesCountOverride.put(Integer.parseInt(count), perm);
                }
            }

            warnOnEmptyChatChannel = config.getBoolean("warn-on-empty-chat-channel", true);
        } catch (Exception e) {
            Civs.logger.log(Level.SEVERE, "Unable to read from config.yml", e);
        }
    }

    private void getGovSettings(FileConfiguration config) {
        String defaultGovTypeString = config.getString("default-gov-type", "DICTATORSHIP");
        if (defaultGovTypeString != null) {
            defaultGovernmentType = defaultGovTypeString.toUpperCase();
        } else {
            defaultGovernmentType = GovernmentType.DICTATORSHIP.name();
        }
        allowChangingOfGovType = config.getBoolean("allow-changing-gov-type", false);
    }

    private void getTownRingSettings(FileConfiguration config) {
        townRings = config.getBoolean("town-rings", true);
        try {
            townRingMat = Material.valueOf(config.getString("town-ring-material", "GLOWSTONE"));
        } catch (Exception e) {
            townRingMat = Material.GLOWSTONE;
            Civs.logger.severe("Unable to read town-ring-material");
        }
    }

    private Map<String, List<String>> processMap(ConfigurationSection section) {
        HashMap<String, List<String>> returnMap = new HashMap<>();
        if (section == null) {
            return returnMap;
        }
        for (String key : section.getKeys(false)) {
            List<String> returnList = section.getStringList(key);
            if (returnList.isEmpty()) {
                returnList.add(key);
            } else if (returnList.size() == 1) {
                returnMap.put(key, Util.textWrap(Util.parseColors(returnList.get(0))));
            }
            returnMap.put(key, returnList);
        }
        return returnMap;
    }

    private void loadDefaults() {
        warningLogger = false;
        percentPowerForUpgrade = 0.1;
        huntCrossWorld = false;
        skinsInMenu = true;
        useBounties = true;
        deleteInvalidRegions = false;
        defaultGovernmentType = GovernmentType.DICTATORSHIP.name();
        silentExp = false;
        useSkills = true;
        keepRegionChunksLoaded = true;
        hardshipPerKill = 0;
        allowHuntNewPlayers = false;
        hardshipDepreciationPeriod = 7;
        huntKarma = -250.0;
        lineLengthMap = new HashMap<>();
        unloadedChestRefreshRate = 600000;
        chatChannels = new EnumMap<>(ChatChannel.ChatChannelType.class);
        chatChannels.put(ChatChannel.ChatChannelType.GLOBAL, Material.GRASS.name());
        lineBreakLength = 40;
        minPopulationForGovTransition = 4;
        defaultConfigSet = "hybrid";
        disableRegionsInUnloadedChunks = false;
        minDistanceBetweenTowns = 10;
        dropMoneyIfZeroBalance = false;
        enterExitMessagesUseTitles = true;
        townRingsCrumbleToGravel = true;
        allowTeleportingOutOfHostileTowns = true;
        allowOfflineRaiding = true;
        maxBankDeposit = -1;
        debugLog = false;
        mobsDropItemsWhenKilledInDenyDamage = false;
        useBoundingBox = true;
        revoltCost = "GUNPOWDER*64";
        announcementPeriod = 240;
        useAnnouncements = true;
        prefixAllText = "";
        civsChatPrefix = "@{GREEN}[Civs] ";
        civsItemPrefix = "Civs";
        capitalismVotingCost = 200;
        daysBetweenVotes = 7;
        defaultLanguage = "en";
        allowCivItemDropping = false;
        useParticleBoundingBoxes = false;
        maxTax = 50;
        explosionOverride = false;
        useStarterBook = true;
        priceMultiplier = 1;
        priceBase = 0;
        expModifier = 0.2;
        expBase = 100;
        itemGroups = new HashMap<>();
        defaultClass = "default";
        groups = new HashMap<>();
        folderIcons = new HashMap<>();
        jailTime = 300000;
        deathGracePeriod = 60000;
        pointsPerKillStreak = 0.1;
        moneyPerKillStreak = 1;
        pointsPerKillJoy = 0.2;
        moneyPerKillJoy = 1;
        pointsPerKill = 1;
        moneyPerKill = 1;
        pointsPerDeath = -1;
        pointsPerHalfHealth = 0.5;
        pointsPerQuarterHealth = 1;
        moneyPerKarma = 0.1;
        karmaPerKillStreak = 1;
        karmaPerKill = 1;
        powerPerKill = 30;
        powerPerNPCKill = 5;
        villagerCooldown = 300;
        denyArrowTurretShootAtMobs = false;
        portMana = 0;
        portWarmup = 5;
        portCooldown = 60;
        portMoney = 0;
        portDamage = 0;
        portStamina = 0;
        portReagents = new ArrayList<>();
        portSlowWarmup = true;
        combatTagDuration = 60;
        portDuringCombat = false;
        showKillStreakMessages = false;
        townRings = true;
        karmaDepreciatePeriod = 43200;
        combatLogPenalty = 80;
        destroyTownsAtZero = false;
        allowFoodHealInCombat = true;
        allowTeleportInCombat = false;
        townGracePeriod = 43200; //12 hours
        useClassesAndSpells = false;
        useTutorial = true;
        useGuide = true;
        checkWaterSpread = true;
        customItemDescriptions = new HashMap<>();
        levelList = new ArrayList<>();
        defaultGovernmentType = GovernmentType.DICTATORSHIP.name();
        allowChangingOfGovType = false;
        residenciesCount = -1;
        residenciesCountOverride = new TreeMap<>();
        warnOnEmptyChatChannel = true;
    }

    public static ConfigManager getInstance() {
        if (configManager == null) {
            configManager = new ConfigManager();
            configManager.loadFile(new File(Civs.dataLocation, CONFIG_FILE_NAME));
        }
        return configManager;
    }
}

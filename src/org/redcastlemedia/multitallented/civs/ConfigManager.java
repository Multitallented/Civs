package org.redcastlemedia.multitallented.civs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.towns.GovernmentType;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

public class ConfigManager {


    public static ConfigManager configManager;
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
    String tutorialUrl;

    @Getter
    List<String> levelList;

    @Getter
    boolean allowTeleportInCombat;

    @Getter
    GovernmentType defaultGovernmentType;

    @Getter
    boolean allowChangingOfGovType;
    @Getter
    double maxTax;
    @Getter
    int daysBetweenVotes;
    @Getter
    double capitalismVotingCost;
    @Getter
    long allianceClaimCaptureTime;
    @Getter
    String claimMaterial;
    @Getter
    String topGuideSpacer;
    @Getter
    String bottomGuideSpacer;
    String civsChatPrefix;
    String civsItemPrefix;
    @Getter
    List<String> allianceClaimEffects;

    public ConfigManager() {
        loadDefaults();
        configManager = this;
    }

    public ConfigManager(File configFile) {
        configManager = this;
        loadFile(configFile);
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
    public HashMap<String, String> getItemGroups() { return itemGroups; }
    public HashMap<String, Integer> getGroups() { return groups; }
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

    public String getCivsChatPrefix() {
        return Util.parseColors(civsChatPrefix);
    }
    public String getCivsItemPrefix() {
        return Util.parseColors(civsItemPrefix + " ");
    }

    public List<String> getCustomItemDescription(String key) {
        List<String> returnDescription = customItemDescriptions.get(key.toLowerCase());
        if (returnDescription == null) {
            ArrayList<String> returnLore = new ArrayList<>();
            returnLore.add(key);
            return returnLore;
        }
        return returnDescription;
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

    private void loadFile(File configFile) {
        FileConfiguration config = new YamlConfiguration();
        try {
            if (!configFile.exists()) {
                Civs.logger.severe("No config.yml found");
                loadDefaults();
                return;
            }
            config.load(configFile);

            blackListWorlds = config.getStringList("blacklist-worlds");
            defaultLanguage = config.getString("default-language", "en");
            allowCivItemDropping = config.getBoolean("allow-civ-item-sharing", false);
            explosionOverride = config.getBoolean("explosion-override", false);
            useStarterBook = config.getBoolean("use-starter-book", true);
            priceMultiplier = config.getDouble("price-multiplier", 1);
            priceBase = config.getDouble("price-base", 0);
            expModifier = config.getDouble("exp-modifier", 0.2);
            expBase = config.getInt("exp-base", 100);
            defaultClass = config.getString("default-class", "default");
            folderIcons = new HashMap<>();
            ConfigurationSection section2 = config.getConfigurationSection("folders");
            if (section2 != null) {
                for (String key : section2.getKeys(false)) {
                    folderIcons.put(key, CVItem.createCVItemFromString(config.getString("folders." + key, "CHEST")));
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
            powerPerKill = config.getInt("power-per-kill", 1);
            powerPerNPCKill = config.getInt("power-per-npc-kill", 1);
            villagerCooldown = config.getLong("villager-cooldown", 300);
            denyArrowTurretShootAtMobs = config.getBoolean("disable-arrow-turret-shooting-at-mobs", false);
            portMana = config.getInt("port.mana", 0);
            portWarmup = config.getInt("port.warmpup", 5);
            portCooldown = config.getInt("port.cooldown", 60);
            portMoney = config.getDouble("port.money", 0);
            portDamage = config.getInt("port.damage", 0);
            portStamina = config.getInt("port.stamina", 0);
            portSlowWarmup = config.getBoolean("port.slow-warmup", true);
            portReagents = config.getStringList("port.reagents");
            combatTagDuration = config.getInt("combat-tag-duration", 60);
            portDuringCombat = config.getBoolean("port.port-during-combat", false);
            townRings = config.getBoolean("town-rings", true);
            try {
                townRingMat = Material.valueOf(config.getString("town-ring-material", "GLOWSTONE"));
            } catch (Exception e) {
                townRingMat = Material.GLOWSTONE;
                Civs.logger.severe("Unable to read town-ring-material");
            }
            karmaDepreciatePeriod = config.getLong("karma-depreciate-period", 43200);
            combatLogPenalty = config.getInt("combat-log-out-percent-damage", 80);
            destroyTownsAtZero = config.getBoolean("destroy-towns-at-zero", false);
            allowFoodHealInCombat = config.getBoolean("allow-food-heal-in-combat", true);
            allowTeleportInCombat = config.getBoolean("allow-teleporting-during-combat", false);
            townGracePeriod = config.getLong("town-grace-period", 43200); //12 hours
            useClassesAndSpells = config.getBoolean("use-classes-and-spells", false);
            useTutorial = config.getBoolean("tutorial.use-tutorial", true);
            useGuide = config.getBoolean("tutorial.use-guide", true);
            tutorialUrl = config.getString("tutorial.url");
            checkWaterSpread = config.getBoolean("check-water-spread", true);
            customItemDescriptions = processMap(config.getConfigurationSection("custom-items"));
            levelList = config.getStringList("levels");
            String defaultGovTypeString = config.getString("default-gov-type", "DICTATORSHIP");
            if (defaultGovTypeString != null) {
                defaultGovernmentType = GovernmentType.valueOf(defaultGovTypeString.toUpperCase());
            } else {
                defaultGovernmentType = GovernmentType.DICTATORSHIP;
            }
            allowChangingOfGovType = config.getBoolean("allow-changing-gov-type", false);
            maxTax = config.getDouble("max-town-tax", 50);
            daysBetweenVotes = config.getInt("days-between-elections", 7);
            capitalismVotingCost = config.getDouble("capitalism-voting-cost", 200);
            allianceClaimCaptureTime = config.getLong("alliance-claim-capture-seconds", 180);
            topGuideSpacer = config.getString("top-guide-spacer", "-----------------Civs-----------------");
            bottomGuideSpacer = config.getString("bottom-guide-spacer", "--------------------------------------");
            civsChatPrefix = config.getString("civs-chat-prefix", "@{GREEN}[Civs]");
            civsItemPrefix = config.getString("civs-item-prefix", "Civs");
            if ("".equals(civsItemPrefix)) {
                civsItemPrefix = "Civs";
            }
            claimMaterial = config.getString("claim-material", "REDSTONE*64");
            allianceClaimEffects = config.getStringList("claim-effects");
            if (allianceClaimEffects.isEmpty()) {
                allianceClaimEffects = getDefaultAllianceEffects();
            }

        } catch (Exception e) {
            Civs.logger.severe("Unable to read from config.yml");
            e.printStackTrace();
        }
    }

    private List<String> getDefaultAllianceEffects() {
        List<String> returnList = new ArrayList<>();
        returnList.add("block_break");
        returnList.add("block_build");
        return returnList;
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
                returnMap.put(key, Util.textWrap("", Util.parseColors(returnList.get(0))));
            }
            returnMap.put(key, returnList);
        }
        return returnMap;
    }

    private void loadDefaults() {
        claimMaterial = "REDSTONE*64";
        civsChatPrefix = "@{GREEN}[Civs]";
        civsItemPrefix = "Civs";
        capitalismVotingCost = 200;
        allianceClaimCaptureTime = 180;
        daysBetweenVotes = 7;
        defaultLanguage = "en";
        allowCivItemDropping = false;
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
        powerPerKill = 1;
        powerPerNPCKill = 1;
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
        defaultGovernmentType = GovernmentType.DICTATORSHIP;
        allowChangingOfGovType = false;
    }

    public static ConfigManager getInstance() {
        if (configManager == null) {
            if (Civs.getInstance() != null) {
                configManager = new ConfigManager(new File(Civs.getInstance().getDataFolder(), "config.yml"));
            } else {
                new ConfigManager();
            }
            return configManager;
        } else {
            return configManager;
        }
    }
}

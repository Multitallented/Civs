package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ai.AIManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.io.File;
import java.util.*;

public class GovernmentManager {
    private static GovernmentManager instance = null;
    private HashMap<GovernmentType, Government> governments = new HashMap<>();

    public static GovernmentManager getInstance() {
        if (instance == null) {
            new GovernmentManager();
        }
        return instance;
    }

    public GovernmentManager() {
        instance = this;
        if (Civs.getInstance() != null) {
            loadAllGovTypes();
        }
    }

    private void loadAllGovTypes() {
        File govTypeFolder = new File(Civs.getInstance().getDataFolder(), "gov-types");
        if (!govTypeFolder.exists()) {
            Civs.logger.info("No gov-types folder found");
            return;
        }
        try {
            for (File govTypeFile : govTypeFolder.listFiles()) {
                loadGovType(govTypeFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadGovType(File govTypeFile) {
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(govTypeFile);
            if (!config.getBoolean("enabled", false)) {
                return;
            }
            String govTypeString = govTypeFile.getName().replace(".yml", "");
            GovernmentType governmentType = GovernmentType.valueOf(govTypeString.toUpperCase());
            if (governmentType == GovernmentType.CYBERSYNACY) {
                new AIManager();
            }
            CVItem cvItem = CVItem.createCVItemFromString(config.getString("icon", "STONE"));

            ArrayList<GovTransition> transitions = processTransitionList(config.getConfigurationSection("transition"));
            Government government = new Government(governmentType,
                    getBuffs(config.getConfigurationSection("buffs")), cvItem, transitions);
            governments.put(governmentType, government);
        } catch (Exception e) {
            Civs.logger.severe("Unable to load " + govTypeFile.getName());
        }
    }

    private ArrayList<GovTransition> processTransitionList(ConfigurationSection section) {
        ArrayList<GovTransition> transitions = new ArrayList<>();
        if (section == null) {
            return transitions;
        }
        for (String index : section.getKeys(false)) {
            int power = -1;
            int moneyGap = -1;
            int revolt = -1;
            long inactive = -1;
            GovernmentType governmentType = null;
            for (String key : section.getConfigurationSection(index).getKeys(false)) {
                if ("power".equals(key)) {
                    power = section.getInt(index + "." + key);
                } else if ("money-gap".equals(key)) {
                    moneyGap = section.getInt(index + "." + key);
                } else if ("revolt".equals(key)) {
                    revolt = section.getInt(index + "." + key);
                } else if ("inactive".equals(key)) {
                    inactive = section.getLong(index + "." + key);
                } else {
                    governmentType = GovernmentType.valueOf(section.getString(key));
                }
            }
            if (governmentType == null) {
                continue;
            }
            transitions.add(new GovTransition(revolt, moneyGap, power, inactive, governmentType));
        }
        return transitions;
    }

    private HashSet<GovTypeBuff> getBuffs(ConfigurationSection section) {
        HashSet<GovTypeBuff> buffs = new HashSet<>();

        for (String key : section.getKeys(false)) {
            GovTypeBuff.BuffType buffType = GovTypeBuff.BuffType.valueOf(key.toUpperCase());
            List<String> groups = section.getStringList(key + ".groups");
            List<String> regions = section.getStringList(key + ".regions");
            HashSet<String> groupSet;
            if (groups.isEmpty()) {
                groupSet = new HashSet<>();
            } else {
                groupSet = new HashSet<>(groups);
            }
            HashSet<String> regionSet;
            if (regions.isEmpty()) {
                regionSet = new HashSet<>();
            } else {
                regionSet = new HashSet<>(regions);
            }

            GovTypeBuff govTypeBuff = new GovTypeBuff(buffType,
                    section.getInt(key + ".percent", 10),
                    groupSet,
                    regionSet);
            buffs.add(govTypeBuff);
        }

        return buffs;
    }

    public Government getGovernment(GovernmentType governmentType) {
        return governments.get(governmentType);
    }

    public Set<GovernmentType> getGovermentTypes() {
        return governments.keySet();
    }
}

package org.redcastlemedia.multitallented.civs.towns;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.ai.AIManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.FallbackConfigUtil;
import org.redcastlemedia.multitallented.civs.util.Util;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class GovernmentManager {
    private static GovernmentManager instance = null;
    private HashMap<String, Government> governments = new HashMap<>();

    public static GovernmentManager getInstance() {
        if (instance == null) {
            instance = new GovernmentManager();
            instance.loadAllGovTypes();
        }
        return instance;
    }

    public void reload() {
        governments.clear();
        loadAllGovTypes();
    }

    private void loadAllGovTypes() {
        final String GOV_TYPE_FOLDER_NAME = "gov-types";
        File govTypeFolder = new File(Civs.dataLocation, GOV_TYPE_FOLDER_NAME);
        boolean govTypeFolderExists = govTypeFolder.exists();
        String path = "resources." + ConfigManager.getInstance().getDefaultConfigSet() + "." + GOV_TYPE_FOLDER_NAME;
        Reflections reflections = new Reflections(path , new ResourcesScanner());
        for (String fileName : reflections.getResources(Pattern.compile(".*\\.yml"))) {
            FileConfiguration config;
            if (govTypeFolderExists) {
                System.out.println(fileName);
                config = FallbackConfigUtil.getConfigFullPath(
                        new File(govTypeFolder, fileName), "/" + fileName);
            } else {
                config = FallbackConfigUtil.getConfigFullPath(null, "/" + fileName);
            }
            loadGovType(config, fileName.substring(fileName.lastIndexOf("/") + 1).replace(".yml", ""));
        }
        if (govTypeFolderExists) {
            for (File file : govTypeFolder.listFiles()) {
                String govName = file.getName().replace(".yml", "");
                if (governments.containsKey(govName)) {
                    continue;
                }
                FileConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);
                } catch (Exception e) {
                    Civs.logger.severe("Unable to load " + file.getName());
                    continue;
                }
                loadGovType(config, govName);
            }
        }
    }

    private void loadGovType(FileConfiguration config, String name) {
        if (!config.getBoolean("enabled", false)) {
            return;
        }
        String govTypeString = config.getString("inherit", name);
        GovernmentType governmentType = GovernmentType.valueOf(govTypeString.toUpperCase());
        if (governmentType == GovernmentType.CYBERSYNACY) {
            new AIManager();
        }
        CVItem cvItem = CVItem.createCVItemFromString(config.getString("icon", "STONE"));

        ArrayList<GovTransition> transitions = processTransitionList(config.getConfigurationSection("transition"));
        Government government = new Government(name, governmentType,
                getBuffs(config.getConfigurationSection("buffs")), cvItem, transitions);
        governments.put(name.toUpperCase(), government);
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
            String governmentType = null;
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
                    governmentType = section.getString(index + "." + key);
                }
            }
            if (governmentType == null) {
                continue;
            }
            transitions.add(new GovTransition(revolt, moneyGap, power, inactive, governmentType));
        }
        return transitions;
    }

    void addGovernment(Government government) {
        governments.put(government.getName().toUpperCase(), government);
    }

    public void transitionGovernment(Town town, String governmentType, boolean save) {
        Government government = GovernmentManager.getInstance().getGovernment(governmentType);
        if (government == null) {
            return;
        }
        for (UUID uuid : town.getPeople().keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                continue;
            }
            Civilian civilian1 = CivilianManager.getInstance().getCivilian(uuid);
            String oldGovName = LocaleManager.getInstance().getTranslation(civilian1.getLocale(),
                    town.getGovernmentType().toLowerCase() + "-name");
            String newGovName = LocaleManager.getInstance().getTranslation(civilian1.getLocale(),
                    governmentType.toLowerCase() + "-name");
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslation(civilian1.getLocale(), "gov-type-change")
                    .replace("$1", town.getName())
                    .replace("$2", oldGovName).replace("$3", newGovName));
        }

        // TODO any other changes that need to be made

        Government prevGovernment = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
        if (prevGovernment != null) {
            for (GovTypeBuff buff : prevGovernment.getBuffs()) {
                if (buff.getBuffType() != GovTypeBuff.BuffType.MAX_POWER) {
                    continue;
                }
                town.setMaxPower((int) Math.round((double) town.getMaxPower() * (1 - (double) buff.getAmount() / 100)));
                break;
            }
        }

        for (GovTypeBuff buff : government.getBuffs()) {
            if (buff.getBuffType() != GovTypeBuff.BuffType.MAX_POWER) {
                continue;
            }
            town.setMaxPower((int) Math.round((double) town.getMaxPower() * (1 + (double) buff.getAmount() / 100)));
            break;
        }



        if (government.getGovernmentType() == GovernmentType.MERITOCRACY) {
            Util.promoteWhoeverHasMostMerit(town, false);
        }

        if (government.getGovernmentType() == GovernmentType.IDIOCRACY) {
            Util.promoteWhoeverHasMostNoise(town, false);
        }

        if (government.getGovernmentType() == GovernmentType.COMMUNISM) {
            HashSet<UUID> setThesePeople = new HashSet<>(town.getRawPeople().keySet());
            for (UUID uuid : setThesePeople) {
                town.setPeople(uuid, "owner");
            }
        }

        if (government.getGovernmentType() == GovernmentType.LIBERTARIAN ||
                government.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                government.getGovernmentType() == GovernmentType.CYBERSYNACY) {
            HashSet<UUID> setThesePeople = new HashSet<>(town.getRawPeople().keySet());
            for (UUID uuid : setThesePeople) {
                town.setPeople(uuid, "member");
            }
        }
        if (town.getBankAccount() > 0 && Civs.econ != null &&
                (government.getGovernmentType() == GovernmentType.COMMUNISM ||
                        government.getGovernmentType() == GovernmentType.ANARCHY ||
                        government.getGovernmentType() == GovernmentType.LIBERTARIAN_SOCIALISM ||
                        government.getGovernmentType() == GovernmentType.LIBERTARIAN)) {
            double size = town.getRawPeople().size();
            for (UUID uuid : town.getRawPeople().keySet()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer != null) {
                    Civs.econ.depositPlayer(offlinePlayer, town.getBankAccount() / size);
                }
            }
            town.setBankAccount(0);
        }

        if (government.getGovernmentType() == GovernmentType.COOPERATIVE ||
                government.getGovernmentType() == GovernmentType.CAPITALISM ||
                government.getGovernmentType() == GovernmentType.DEMOCRACY ||
                government.getGovernmentType() == GovernmentType.DEMOCRATIC_SOCIALISM) {
            town.setLastVote(System.currentTimeMillis());
        }

        town.getRevolt().clear();
        town.getVotes().clear();
        town.setTaxes(0);
        town.setColonialTown(null);
        town.setGovTypeChangedToday(true);
        town.setGovernmentType(governmentType);
        if (save) {
            TownManager.getInstance().saveTown(town);
        }
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

    public Government getGovernment(String governmentType) {
        return governments.get(governmentType.toUpperCase());
    }

    public Set<String> getGovermentTypes() {
        return governments.keySet();
    }
}

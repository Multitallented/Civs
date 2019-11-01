package org.redcastlemedia.multitallented.civs.update;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.redcastlemedia.multitallented.civs.Civs;

public final class Update1d5d6 {
    private Update1d5d6() {

    }

    public static String update() {
        updateConfig();
        updateItemTypes();
        return "1.5.6";
    }

    private static void updateItemTypes() {
        File itemTypesFolder = new File(Civs.getInstance().getDataFolder(), "item-types");
        if (!itemTypesFolder.exists()) {
            return;
        }
        upgradeTowns(itemTypesFolder);
        addJammerTrap(itemTypesFolder);
        updateEmbassy(itemTypesFolder);
        updateDefenses(itemTypesFolder);
        updateTranslations();

        File adminFolder = new File(itemTypesFolder, "admin-invisible");
        if (adminFolder.exists()) {
            File shelterFile = new File(adminFolder, "shelter.yml");
            if (shelterFile.exists()) {
                try {
                    FileConfiguration config = new YamlConfiguration();
                    config.load(shelterFile);
                    List<String> effects = config.getStringList("effects");
                    effects.add("bed");
                    config.set("effects", effects);
                    config.save(shelterFile);
                } catch (Exception e) {

                }
            }
        }

    }

    private static void updateTranslations() {
        File translationsFolder = new File(Civs.getInstance().getDataFolder(), "translations");
        if (!translationsFolder.exists()) {
            return;
        }
        File enFile = new File(translationsFolder, "en.yml");
        if (enFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(enFile);
                config.set("siege-built", "WARNING! $1 has created a $2 targeting $3");
                config.set("jammer_trap-name", "Jammer Trap");
                config.set("jammer_trap-desc", "A building that intercepts teleports that would travel within 100. Redirects the destination to nearby the jammer.");
                config.set("jammer-redirect", "@{RED}[ALERT] Your teleport was intercepted by a $1");
                config.set("no-tp-out-of-town", "You can't teleport out of a non-allied town");
                config.set("intruder-enter", "@{RED}[WARNING] $1 has entered $2");
                config.set("intruder-exit", "$1 has exited $2");
                config.set("raid-porter-offline", "You cant raid $1 when none of their members are online.");
                config.set("no-blocks-above-chest", "There must be no blocks above the center chest of a $1");
                config.set("activate-anticamp-question", "$1 has died in $2. Would you like to activate anti-camping defenses for $3?");
                config.save(enFile);
            } catch (Exception e) {

            }
        }
    }

    private static void updateEmbassy(File itemTypesFolder) {
        File utilitiesFolder = new File(itemTypesFolder, "utilities");
        if (!utilitiesFolder.exists()) {
            return;
        }
        File embassyFile = new File(utilitiesFolder, "embassy.yml");
        if (embassyFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(embassyFile);
                List<String> townsList = config.getStringList("towns");
                townsList.add("settlement");
                townsList.add("hamlet");
                townsList.add("village");
                config.set("towns", townsList);
                List<String> preReqs = config.getStringList("pre-reqs");
                if (!preReqs.isEmpty() && preReqs.get(0).startsWith("member=")) {
                    preReqs.remove(0);
                    preReqs.add("member=settlement:hamlet:village:town:city:metropolis");
                }
                config.set("pre-reqs", preReqs);
                config.save(embassyFile);
            } catch (Exception e) {

            }
        }
        // TODO update council room, town hall, city hall, and capitol

    }
    private static void addJammerTrap(File itemTypesFolder) {
        File offenseFolder = new File(itemTypesFolder, "offense");
        if (!offenseFolder.exists()) {
            return;
        }
        File blindnessTrapFile = new File(offenseFolder, "blindness_trap.yml");
        if (blindnessTrapFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(blindnessTrapFile);
                List<String> effects = config.getStringList("effects");
                fixPotionDuration(effects);
                config.set("effects", effects);
                config.save(blindnessTrapFile);
            } catch (Exception e) {

            }
        }

        File jammerFile = new File(offenseFolder, "jammer_trap.yml");
        if (jammerFile.exists()) {
            return;
        }
        try {
            jammerFile.createNewFile();
            FileConfiguration config = new YamlConfiguration();
            config.set("type", "region");
            config.set("icon", "SOUL_SAND");
            config.set("name", "Jammer_Trap");
            config.set("max", 1);
            config.set("price", 400);
            ArrayList<String> groups = new ArrayList<>();
            groups.add("offense");
            config.set("groups", groups);
            config.set("level", 1);
            ArrayList<String> buildReqs = new ArrayList<>();
            buildReqs.add("TNT*4");
            buildReqs.add("OBSIDIAN*2");
            buildReqs.add("g:fence*12");
            config.set("build-reqs", buildReqs);
            config.set("build-radius", 3);
            ArrayList<String> effects = new ArrayList<>();
            effects.add("block_break");
            effects.add("block_build");
            effects.add("jammer:100.30.30");
            effects.add("temporary:1800");
            effects.add("port:member");
            config.set("effects", effects);
            config.save(jammerFile);
        } catch (Exception e) {

        }
    }
    private static void updateDefenses(File itemTypesFolder) {
        File defenseFolder = new File(itemTypesFolder, "defense");
        if (!defenseFolder.exists()) {
            return;
        }
        File idolFile = new File(defenseFolder, "idol.yml");
        if (idolFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(idolFile);
                List<String> effects = config.getStringList("effects");
                fixPotionDuration(effects);
                config.set("effects", effects);
                config.save(idolFile);
            } catch (Exception e) {

            }
        }
        File monumentFile = new File(defenseFolder, "monument.yml");
        if (monumentFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(monumentFile);
                List<String> effects = config.getStringList("effects");
                fixPotionDuration(effects);
                config.set("effects", effects);
                config.save(monumentFile);
            } catch (Exception e) {

            }
        }
        File statueFile = new File(defenseFolder, "statue.yml");
        if (statueFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(statueFile);
                List<String> effects = config.getStringList("effects");
                fixPotionDuration(effects);
                config.set("effects", effects);
                config.save(statueFile);
            } catch (Exception e) {

            }
        }
        File hospitalFile = new File(defenseFolder, "hospital.yml");
        if (hospitalFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(hospitalFile);
                List<String> effects = config.getStringList("effects");
                fixPotionDuration(effects);
                config.set("effects", effects);
                config.save(hospitalFile);
            } catch (Exception e) {

            }
        }
    }
    private static void upgradeTowns(File itemTypesFolder) {
        File townsFolder = new File(itemTypesFolder, "towns");
        if (!townsFolder.exists()) {
            return;
        }
        File settlementFile = new File(townsFolder, "settlement.yml");
        if (settlementFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(settlementFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 1) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:1");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(settlementFile);
            } catch (Exception e) {

            }
        }

        File hamletFile = new File(townsFolder, "hamlet.yml");
        if (hamletFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(hamletFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 2) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:2");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(hamletFile);
            } catch (Exception e) {

            }
        }

        File villageFile = new File(townsFolder, "village.yml");
        if (villageFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(villageFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 3) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:3");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(villageFile);
            } catch (Exception e) {

            }
        }

        File townFile = new File(townsFolder, "town.yml");
        if (townFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(townFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 5) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:5");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(townFile);
            } catch (Exception e) {

            }
        }

        File cityFile = new File(townsFolder, "city.yml");
        if (cityFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(cityFile);
                List<String> limitList =  config.getStringList("limits");
                int embassyLimit = removeEmbassy(limitList);
                if (embassyLimit > 8) {
                    limitList.add("embassy:" + embassyLimit);
                } else {
                    limitList.add("embassy:8");
                }
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(cityFile);
            } catch (Exception e) {

            }
        }

        File metropolisFile = new File(townsFolder, "metropolis.yml");
        if (metropolisFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(metropolisFile);
                List<String> limitList =  config.getStringList("limits");
                limitList.add("jammer_trap:0");
                config.set("limits", limitList);
                config.save(metropolisFile);
            } catch (Exception e) {

            }
        }
        File miningColonyFile = new File(townsFolder, "mining-colony.yml");
        if (miningColonyFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(miningColonyFile);
                List<String> effects = config.getStringList("effects");
                fixPotionDuration(effects);
                config.set("effects", effects);
                config.save(miningColonyFile);
            } catch (Exception e) {

            }
        }
        File keepFile = new File(townsFolder, "keep.yml");
        if (keepFile.exists()) {
            try {
                FileConfiguration config = new YamlConfiguration();
                config.load(keepFile);
                List<String> effects = config.getStringList("effects");
                fixPotionDuration(effects);
                config.set("effects", effects);
                config.save(keepFile);
            } catch (Exception e) {

            }
        }

    }

    static void fixPotionDuration(List<String> effectList) {
        for (String effect : new HashSet<>(effectList)) {
            if (effect.startsWith("potion:")) {
                effectList.remove(effect);
                String potionString = "potion:";
                String[] potionSplit = effect.split(":")[1].split(",");
                for (String durationString : potionSplit) {
                    if (!"potion:".equals(potionString)) {
                        potionString += ",";
                    }
                    String[] currentSplit = durationString.split("\\.");
                    int duration = Integer.parseInt(currentSplit[1]);
                    duration = duration / 20;
                    currentSplit[1] = "" + duration;
                    for (String thisSplit : currentSplit) {
                        potionString += thisSplit + ".";
                    }
                    potionString = potionString.substring(0, potionString.length() - 1);
                }
                effectList.add(potionString);
            }
        }
    }
    private static int removeEmbassy(List<String> limitList) {
        for (String limit : new ArrayList<>(limitList)) {
            if (limit.contains("embassy")) {
                int limitCount = Integer.parseInt(limit.split(":")[1]);
                limitList.remove(limit);
                return limitCount;
            }
        }
        return -1;
    }

    private static void updateConfig() {
        File configFile = new File(Civs.getInstance().getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            return;
        }
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(configFile);
            config.set("town-rings-crumble-to-gravel", true);
            config.set("allow-teleporting-out-of-hostile-towns", true);
            config.set("allow-offline-raiding", true);
            config.save(configFile);
        } catch (Exception exception) {
            return;
        }
    }
}

package org.redcastlemedia.multitallented.civs.civilians;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.skills.Skill;
import org.redcastlemedia.multitallented.civs.skills.SkillManager;
import org.redcastlemedia.multitallented.civs.skills.SkillType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialPath;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialStep;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class CivilianManager {

    private final HashMap<UUID, Civilian> civilians = new HashMap<>();
    private final ArrayList<Civilian> sortedCivilians = new ArrayList<>();
    @Getter
    @Setter
    private boolean listNeedsToBeSorted = false;

    private static CivilianManager civilianManager = null;

    public void reload() {
        civilians.clear();
        sortedCivilians.clear();
        listNeedsToBeSorted = true;
        loadAllCivilians();
    }

    public Collection<Civilian> getCivilians() {
        return civilians.values();
    }

    private void loadAllCivilians() {
        File civilianFolder = new File(Civs.dataLocation, "players");
        if (!civilianFolder.exists()) {
            return;
        }
        for (File currentFile : civilianFolder.listFiles()) {
            try {
                UUID uuid = UUID.fromString(currentFile.getName().replace(".yml",""));
                Civilian civilian = loadFromFileCivilian(uuid);
                civilians.put(uuid, civilian);
                sortedCivilians.add(civilian);
            } catch (Exception npe) {
                Civs.logger.log(Level.SEVERE, "Unable to load civilian", npe);
            }
        }
        listNeedsToBeSorted = true;
        sortCivilians();
    }

    public static CivilianManager getInstance() {
        if (civilianManager == null) {
            civilianManager = new CivilianManager();
            if (Civs.getInstance() != null) {
                civilianManager.loadAllCivilians();
            }
        }
        return civilianManager;
    }

    void loadCivilian(Player player) {
        Civilian civilian = loadFromFileCivilian(player.getUniqueId());
        civilians.put(player.getUniqueId(), civilian);
        ClassManager.getInstance().loadPlayer(player, civilian);
    }
    public void createDefaultCivilian(Player player) {
        Civilian civilian = createDefaultCivilian(player.getUniqueId());
        civilians.put(player.getUniqueId(), civilian);
        sortedCivilians.add(civilian);
        listNeedsToBeSorted = true;
    }
    public void sortCivilians() {
        if (!listNeedsToBeSorted) {
            return;
        }
        listNeedsToBeSorted = false;
        sortedCivilians.sort((o1, o2) -> {
            if (o1.getPoints() == o2.getPoints()) {
                return 0;
            }
            return o1.getPoints() < o2.getPoints() ? 1 : -1;
        });
    }
    void unloadCivilian(Player player) {
        ClassManager.getInstance().unloadPlayer(player);
        Civilian civilian = getCivilian(player.getUniqueId());
        saveCivilian(civilian);
//        civilians.remove(player.getUniqueId());
    }
    public Civilian getCivilian(UUID uuid) {
        Civilian civilian = civilians.get(uuid);
        if (civilian == null) {
            civilian = loadFromFileCivilian(uuid);
        }
        return civilian;
    }
    private Civilian loadFromFileCivilian(UUID uuid) {
        if (Civs.getInstance() == null) {
            Civilian civilian = createDefaultCivilian(uuid);
            saveCivilian(civilian);
            return civilian;
        }
        File civilianFolder = new File(Civs.dataLocation, "players");
        Player player = Bukkit.getPlayer(uuid);
        if (!civilianFolder.exists()) {
            Civilian civilian = createDefaultCivilian(uuid);
            if (player != null) {
                saveCivilian(civilian);
            }
            return civilian;
        }
        File civilianFile = new File(civilianFolder, uuid + ".yml");
        if (!civilianFile.exists()) {
            Civilian civilian = createDefaultCivilian(uuid);
            if (player != null) {
                saveCivilian(civilian);
            }
            return civilian;
        }
        FileConfiguration civConfig = new YamlConfiguration();
        try {
            civConfig.load(civilianFile);

            ItemManager itemManager = ItemManager.getInstance();
            Map<String, Integer> items = itemManager.loadCivItems(civConfig);
            HashMap<CivItem, Integer> exp = new HashMap<>();
            ConfigurationSection section = civConfig.getConfigurationSection("exp");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    CivItem item = itemManager.getItemType(key);
                    if (item == null) {
                        continue;
                    }
                    exp.put(item, civConfig.getInt("exp." + key, 0));
                }
            }

            int tutorialIndex = civConfig.getInt("tutorial-index", 0);
            int tutorialProgress = civConfig.getInt("tutorial-progress", 0);
            String tutorialPathName = civConfig.getString("tutorial-path", "default");

            Civilian civilian = new Civilian(uuid, civConfig.getString("locale"), items, exp,
                    civConfig.getInt("kills", 0), civConfig.getInt("kill-streak", 0),
                    civConfig.getInt("deaths", 0), civConfig.getInt("highest-kill-streak", 0),
                    civConfig.getDouble("points", 0), civConfig.getInt("karma", 0));
            civilian.setTutorialIndex(tutorialIndex);
            civilian.setTutorialPath(tutorialPathName);
            civilian.setTutorialProgress(tutorialProgress);
            civilian.setUseAnnouncements(civConfig.getBoolean("use-announcements", true));
            civilian.setDaysSinceLastHardshipDepreciation(civConfig.getInt("days-since-hardship-depreciation", 0));
            civilian.setHardship(civConfig.getDouble("hardship", 0));
            String stringRespawn = civConfig.getString("respawn");
            if (civConfig.isSet("skills")) {
                for (String skillName : civConfig.getConfigurationSection("skills").getKeys(false)) {
                    SkillType skillType = SkillManager.getInstance().getSkillType(skillName);
                    if (skillType == null) {
                        continue;
                    }
                    Skill skill = new Skill(skillName);
                    for (String accomplishment : civConfig.getConfigurationSection("skills." + skillName).getKeys(false)) {
                        int level = civConfig.getInt("skills." + skillName + "." + accomplishment);
                        skill.getAccomplishments().put(accomplishment, level);
                    }
                    civilian.getSkills().put(skillName, skill);
                }
            }
            for (String skillName : SkillManager.getInstance().getSkills().keySet()) {
                if (!civilian.getSkills().containsKey(skillName)) {
                    civilian.getSkills().put(skillName, new Skill(skillName));
                }
            }

            if (stringRespawn != null) {
                civilian.setRespawnPoint(Region.idToLocation(stringRespawn));
            }
            if (civConfig.isSet("bounties")) {
                civilian.setBounties(Util.readBountyList(civConfig));
            }
            if (civConfig.isSet("last-karma-depreciation")) {
                civilian.setLastKarmaDepreciation(civConfig.getLong("last-karma-depreciation", -1));
            }
            if (civConfig.isSet("friends")) {
                HashSet<UUID> friendSet = new HashSet<>();
                for (String uuidString : civConfig.getStringList("friends")) {
                    friendSet.add(UUID.fromString(uuidString));
                }
                civilian.setFriends(friendSet);
            }
            civilian.setMana(civConfig.getInt("mana", 0), false);

            ItemManager.getInstance().addMinItems(civilian);

            if (civilian.getCompletedTutorialSteps().isEmpty() && civilian.getTutorialIndex() > 0) {
                TutorialPath tutorialPath = TutorialManager.getInstance().getPathByName(tutorialPathName);
                for (int i = 0; i < civilian.getTutorialIndex() && i < tutorialPath.getSteps().size(); i++) {
                    TutorialStep tutorialStep = tutorialPath.getSteps().get(i);
                    String key = TutorialManager.getInstance().getKey(tutorialPathName,
                            TutorialManager.TutorialType.valueOf(tutorialStep.getType().toUpperCase()), tutorialStep);
                    civilian.getCompletedTutorialSteps().add(key);
                }
            }

            return civilian;
        } catch (Exception ex) {
            Civs.logger.log(Level.SEVERE, "Unable to read " + uuid + ".yml", ex);
            if (civilianFile.exists()) {
                civilianFile.delete();
            }
            return createDefaultCivilian(uuid);
        }
    }
    Civilian createDefaultCivilian(UUID uuid) {
        ConfigManager configManager = ConfigManager.getInstance();
        Civilian civilian = new Civilian(uuid,
                configManager.getDefaultLanguage(),
                new HashMap<>(),
                new HashMap<>(), 0, 0, 0, 0, 0, 0);
        civilian.getStashItems().putAll(ItemManager.getInstance().getNewItems(civilian));
        civilian.setTutorialPath("default");
        civilian.setTutorialIndex(0);
        civilian.setUseAnnouncements(true);
        civilian.setTutorialProgress(0);

        for (String skillName : SkillManager.getInstance().getSkills().keySet()) {
            civilian.getSkills().put(skillName, new Skill(skillName));
        }
        return civilian;
    }
    public void saveCivilian(Civilian civilian) {
        if (Civs.getInstance() == null) {
            return;
        }
        File civilianFolder = new File(Civs.dataLocation, "players");
        if (!civilianFolder.exists()) {
            if (!civilianFolder.mkdir()) {
                Civs.logger.severe("Unable to create players folder");
                return;
            }
        }
        File civilianFile = new File(civilianFolder, civilian.getUuid() + ".yml");
        if (!civilianFile.exists()) {
            try {
                civilianFile.createNewFile();
            } catch (IOException ioexception) {
                Civs.logger.severe("Unable to create " + civilian.getUuid() + ".yml");
                return;
            }
        }
        FileConfiguration civConfig = new YamlConfiguration();
        try {
            civConfig.load(civilianFile);

            civConfig.set("locale", civilian.getLocale());
            civConfig.set("hardship", civilian.getHardship());
            civConfig.set("mana", civilian.getMana());
            civConfig.set("days-since-hardship-depreciation", civilian.getDaysSinceLastHardshipDepreciation());
            civConfig.set("tutorial-index", civilian.getTutorialIndex());
            civConfig.set("tutorial-path", civilian.getTutorialPath());
            civConfig.set("tutorial-progress", civilian.getTutorialProgress());
            civConfig.set("use-announcements", civilian.isUseAnnouncements());

            civConfig.set("items", null);
            for (String currentName : civilian.getStashItems().keySet()) {
                CivItem civItem = ItemManager.getInstance().getItemType(currentName);
                if (civItem == null) {
                    continue;
                }
                if (civItem.getItemType() == CivItem.ItemType.FOLDER) {
                    continue;
                }
                civConfig.set("items." + civItem.getProcessedName(), civItem.getQty());
            }
            List<String> classes = new ArrayList<>();
            for (CivClass civClass : civilian.getCivClasses()) {
                if (civClass == null) {
                    continue;
                }
                classes.add(civClass.getId().toString());
            }
            for (Skill skill : civilian.getSkills().values()) {
                for (Map.Entry<String, Integer> accomplishment : skill.getAccomplishments().entrySet()) {
                    civConfig.set("skills." + skill.getType() + "." + accomplishment.getKey(), accomplishment.getValue());
                }
            }
            civConfig.set("kills", civilian.getKills());
            civConfig.set("kill-streak", civilian.getKillStreak());
            civConfig.set("deaths", civilian.getDeaths());
            civConfig.set("highest-kill-streak", civilian.getHighestKillStreak());
            civConfig.set("points", civilian.getPoints());
            civConfig.set("karma", civilian.getKarma());
            civConfig.set("classes", classes);
            civConfig.set("locale", civilian.getLocale());
            for (Skill skill : civilian.getSkills().values()) {
                for (Map.Entry<String, Integer> entry : skill.getAccomplishments().entrySet()) {
                    civConfig.set("skills." + skill.getType() + "." + entry.getKey(), entry.getValue());
                }
            }

            if (civilian.getBounties() != null && !civilian.getBounties().isEmpty()) {
                for (int i = 0; i < civilian.getBounties().size(); i++) {
                    if (civilian.getBounties().get(i).getIssuer() != null) {
                        civConfig.set("bounties." + i + ".issuer", civilian.getBounties().get(i).getIssuer().toString());
                    }
                    civConfig.set("bounties." + i + ".amount", civilian.getBounties().get(i).getAmount());
                }
            } else {
                civConfig.set("bounties", null);
            }
            if (civilian.getFriends() != null && !civilian.getFriends().isEmpty()) {
                ArrayList<String> friendList = new ArrayList<>();
                for (UUID uuid : civilian.getFriends()) {
                    friendList.add(uuid.toString());
                }
                civConfig.set("friends", friendList);
            } else {
                civConfig.set("friends", null);
            }

            for (CivItem item : civilian.getExp().keySet()) {
                int exp = civilian.getExp().get(item);
                if (exp < 1) {
                    continue;
                }
                civConfig.set("exp." + item.getProcessedName(), exp);
            }
            if (civilian.getRespawnPoint() != null) {
                civConfig.set("respawn", Region.locationToString(civilian.getRespawnPoint()));
            }
            civConfig.set("last-karma-depreciation", civilian.getLastKarmaDepreciation());

            civConfig.save(civilianFile);
        } catch (Exception ex) {
            Civs.logger.severe("Unable to write " + civilian.getUuid() + ".yml");
            ex.printStackTrace();
        }
    }

    public void deleteCivilian(Civilian civilian) {
        civilians.remove(civilian.getUuid());
        File playerFolder = new File(Civs.dataLocation, "players");
        File playerFile = new File(playerFolder, civilian.getUuid() + ".yml");
        if (!playerFile.exists()) {
            return;
        }
        playerFile.delete();
    }

    public void exchangeHardship(UUID attacker, UUID defender, double amount) {
        if (attacker != null) {
            Civilian attackerCiv = CivilianManager.getInstance().getCivilian(attacker);
            attackerCiv.setHardship(attackerCiv.getHardship() - amount);
            CivilianManager.getInstance().saveCivilian(attackerCiv);
        }

        if (defender != null) {
            Civilian defenderCiv = CivilianManager.getInstance().getCivilian(defender);

            if (ConfigManager.getInstance().isUseHardshipSystem() &&
                    Civs.econ != null && defenderCiv.getHardship() > 0) {
                if (attacker != null) {
                    Civs.econ.withdrawPlayer(Bukkit.getOfflinePlayer(attacker), amount);
                }
                Civs.econ.depositPlayer(Bukkit.getOfflinePlayer(defender), amount);
            }

            defenderCiv.setHardship(defenderCiv.getHardship() + amount);
            CivilianManager.getInstance().saveCivilian(defenderCiv);
        }
    }

    public void exchangeHardship(Region region, UUID attacker, double amount) {
        Set<UUID> defenders = region.getOwners();
        for (UUID defender : defenders) {
            exchangeHardship(attacker, defender, amount / (double) defenders.size());
        }
    }

    public void exchangeHardship(Town town, UUID attacker, double amount) {
        Set<UUID> defenders = town.getRawPeople().keySet();
        for (UUID defender : defenders) {
            exchangeHardship(attacker, defender, amount / (double) defenders.size());
        }
    }
}

package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import lombok.Getter;
import lombok.Setter;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class CivilianManager {

    private HashMap<UUID, Civilian> civilians = new HashMap<>();
    private ArrayList<Civilian> sortedCivilians = new ArrayList<>();
    @Getter
    @Setter
    private boolean listNeedsToBeSorted = false;

    private static CivilianManager civilianManager = null;

    public void reload() {
        civilians.clear();
        sortedCivilians.clear();
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
        try {
            for (File currentFile : civilianFolder.listFiles()) {
                UUID uuid = UUID.fromString(currentFile.getName().replace(".yml",""));
                Civilian civilian = loadFromFileCivilian(uuid);
                civilians.put(uuid, civilian);
                sortedCivilians.add(civilian);
            }
        } catch (NullPointerException npe) {

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
        sortedCivilians.sort(new Comparator<Civilian>() {
            @Override
            public int compare(Civilian o1, Civilian o2) {
                if (o1.getPoints() == o2.getPoints()) {
                    return 0;
                }
                return o1.getPoints() < o2.getPoints() ? 1 : -1;
            }
        });
    }
    void unloadCivilian(Player player) {
        Civilian civilian = getCivilian(player.getUniqueId());
        saveCivilian(civilian);
//        civilian.setMana(100);
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
            Set<CivClass> classes = new HashSet<>();
            ClassManager classManager = ClassManager.getInstance();
            for (int id : civConfig.getIntegerList("classes")) {
                classes.add(classManager.getCivClass(uuid, id));
            }
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

            int tutorialIndex = civConfig.getInt("tutorial-index", -1);
            int tutorialProgress = civConfig.getInt("tutorial-progress", 0);
            String tutorialPath = civConfig.getString("tutorial-path", "default");

            Civilian civilian = new Civilian(uuid, civConfig.getString("locale"), items, classes, exp,
                    civConfig.getInt("kills", 0), civConfig.getInt("kill-streak", 0),
                    civConfig.getInt("deaths", 0), civConfig.getInt("highest-kill-streak", 0),
                    civConfig.getDouble("points", 0), civConfig.getInt("karma", 0));
            civilian.setTutorialIndex(tutorialIndex);
            civilian.setTutorialPath(tutorialPath);
            civilian.setTutorialProgress(tutorialProgress);
            civilian.setUseAnnouncements(civConfig.getBoolean("use-announcements", true));
            civilian.setDaysSinceLastHardshipDepreciation(civConfig.getInt("days-since-hardship-depreciation", 0));
            civilian.setHardship(civConfig.getDouble("hardship", 0));
            String stringRespawn = civConfig.getString("respawn");
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

            ItemManager.getInstance().addMinItems(civilian);

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
        CivClass defaultClass = ClassManager.getInstance().createDefaultClass(uuid);
        Set<CivClass> classes = new HashSet<>();
        classes.add(defaultClass);
        Civilian civilian = new Civilian(uuid,
                configManager.getDefaultLanguage(),
                new HashMap<>(),
                classes,
                new HashMap<>(), 0, 0, 0, 0, 0, 0);
        civilian.getStashItems().putAll(ItemManager.getInstance().getNewItems(civilian));
        civilian.setTutorialPath("default");
        civilian.setTutorialIndex(-1);
        civilian.setUseAnnouncements(true);
        civilian.setTutorialProgress(0);
        return civilian;
    }
    public void saveCivilian(Civilian civilian) {
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
            List<Integer> classes = new ArrayList<>();
            if (civilian.getCivClasses() != null) {
                for (CivClass civClass : civilian.getCivClasses()) {
                    if (civClass == null) {
                        continue;
                    }
                    classes.add(civClass.getId());
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
            return;
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

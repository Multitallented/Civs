package org.redcastlemedia.multitallented.civs.alliances;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.AllianceDissolvedEvent;
import org.redcastlemedia.multitallented.civs.events.AllianceFormedEvent;
import org.redcastlemedia.multitallented.civs.events.RenameAllianceEvent;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class AllianceManager implements Listener {
    private static AllianceManager instance = null;
    private final HashMap<String, Alliance> alliances = new HashMap<>();
    public static AllianceManager getInstance() {
        if (instance == null) {
            instance = new AllianceManager();
            if (Civs.getInstance() != null) {
                instance.loadAllAlliances();
            }
            Bukkit.getPluginManager().registerEvents(instance, Civs.getInstance());
        }
        return instance;
    }

    public void reload() {
        alliances.clear();
        if (Civs.getInstance() != null) {
            loadAllAlliances();
        }
    }

    public void loadAllAlliances() {
        File allianceFolder = new File(Civs.dataLocation, "alliances");
        if (!allianceFolder.exists()) {
            allianceFolder.mkdir();
            return;
        }
        try {
            for (File allianceFile : allianceFolder.listFiles()) {
                loadAlliance(allianceFile);
            }
        } catch (NullPointerException npe) {
            // dont care
        }
    }

    public boolean isInAlliance(UUID uuid, Alliance alliance) {
        for (String townName : alliance.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            if (town.getPeople().containsKey(uuid)) {
                return true;
            }
        }
        return false;
    }

    private void loadAlliance(File allianceFile) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(allianceFile);
            Alliance alliance = new Alliance();
            alliance.setName(allianceFile.getName().replace(".yml", ""));
            alliance.setMembers(new HashSet<>(config.getStringList("members")));
            for (String townName : new ArrayList<>(alliance.getMembers())) {
                if (TownManager.getInstance().getTown(townName) == null) {
                    alliance.getMembers().remove(townName);
                }
            }
            String uuidString = config.getString("last-rename", null);
            if (uuidString != null) {
                alliance.setLastRenamedBy(UUID.fromString(uuidString));
            }

            alliance.getEffects().addAll(ConfigManager.getInstance().getNationClaimEffects());

            alliances.put(alliance.getName(), alliance);
        } catch (Exception e) {
            Civs.logger.severe("Unable to load alliance " + allianceFile.getName());
        }
    }

    public boolean renameAlliance(String oldName, String newName) {
        if (alliances.get(newName) != null) {
            return false;
        }
        Alliance alliance = alliances.get(oldName);
        File allianceFolder = new File(Civs.dataLocation, "alliances");
        File allianceFile = new File(allianceFolder, oldName + ".yml");
        if (!allianceFile.delete()) {
            return false;
        }
        alliances.remove(oldName);
        alliance.setName(newName);
        alliances.put(newName, alliance);
        saveAlliance(alliance);
        RenameAllianceEvent renameAllianceEvent = new RenameAllianceEvent(alliance, oldName, newName);
        Bukkit.getPluginManager().callEvent(renameAllianceEvent);
        return true;
    }

    public boolean removeAlliance(Alliance alliance) {
        alliances.remove(alliance.getName());
        if (Civs.getInstance() == null) {
            return true;
        }
        File allianceFolder = new File(Civs.dataLocation, "alliances");
        File allianceFile = new File(allianceFolder, alliance.getName() + ".yml");
        if (!allianceFile.delete()) {
            return false;
        }
        return true;
    }

    public void saveAlliance(Alliance alliance) {
        if (Civs.getInstance() == null) {
            return;
        }
        try {
            File allianceFolder = new File(Civs.dataLocation, "alliances");
            if (!allianceFolder.exists()) {
                allianceFolder.mkdir();
            }
            File allianceFile = new File(allianceFolder, alliance.getName() + ".yml");
            if (!allianceFile.exists()) {
                allianceFile.createNewFile();
            }
            FileConfiguration config = new YamlConfiguration();

            config.set("members", new ArrayList<>(alliance.getMembers()));
            if (alliance.getLastRenamedBy() != null) {
                config.set("last-rename", alliance.getLastRenamedBy().toString());
            }
            config.save(allianceFile);
        } catch (Exception e) {
            e.printStackTrace();
            Civs.logger.severe("Unable to save alliance " + alliance.getName());
        }
    }

    public Alliance getAlliance(String name) {
        return alliances.get(name);
    }

    public Set<Alliance> getAlliances(UUID uuid) {
        HashSet<Alliance> returnAlliances = new HashSet<>();
        for (Alliance alliance : alliances.values()) {
            for (String townName : alliance.getMembers()) {
                Town town = TownManager.getInstance().getTown(townName);
                if (town.getRawPeople().containsKey(uuid)) {
                    returnAlliances.add(alliance);
                }
            }
        }
        return returnAlliances;
    }

    public HashSet<Alliance> getAlliances(Town town) {
        HashSet<Alliance> returnAlliances = new HashSet<>();
        for (Alliance alliance : alliances.values()) {
            if (alliance.getMembers().contains(town.getName())) {
                returnAlliances.add(alliance);
            }
        }
        return returnAlliances;
    }

    public boolean isAllied(Town town1, Town town2) {
        for (Alliance alliance : alliances.values()) {
            if (alliance.getMembers().contains(town1.getName()) &&
                    alliance.getMembers().contains(town2.getName())) {
                return true;
            }
        }
        return false;
    }

    public void sendAllyInvites(Town toTown, Town fromTown, Player player) {
        if (toTown.getAllyInvites().contains(fromTown.getName())) {
            return;
        }
        toTown.getAllyInvites().add(fromTown.getName());
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "town-ally-request-sent").replace("$1", fromTown.getName()));
        for (UUID uuid : toTown.getRawPeople().keySet()) {
            if (uuid.equals(player.getUniqueId())) {
                continue;
            }
            if (toTown.getRawPeople().get(uuid).contains(Constants.OWNER)) {
                Player pSend = Bukkit.getPlayer(uuid);
                if (pSend != null && pSend.isOnline()) {
                    pSend.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(pSend,
                            "town-ally-request-sent").replace("$1", fromTown.getName()));
                }
            }
        }
    }

    public void allyTheseTowns(Town town1, Town town2) {
        HashSet<Alliance> saveThese = new HashSet<>();
        HashSet<Alliance> removeThese = new HashSet<>();

        Alliance mergeAlliance = null;
        outer: for (Alliance alliance : getAlliances(town2)) {
            if (alliance.getMembers().contains(town1.getName())) {
                return;
            }

            for (String townName : alliance.getMembers()) {
                if (!townName.equals(town2.getName()) &&
                        !isAllied(town1, TownManager.getInstance().getTown(townName))) {
                    continue outer;
                }
            }
            alliance.getMembers().add(town1.getName());
            mergeAlliance = alliance;
            AllianceFormedEvent allianceFormedEvent = new AllianceFormedEvent(alliance, town1, false);
            Bukkit.getPluginManager().callEvent(allianceFormedEvent);
            saveThese.add(alliance);
        }
        if (mergeAlliance == null) {
            Alliance alliance = new Alliance();
            alliance.getMembers().add(town1.getName());
            alliance.getMembers().add(town2.getName());
            alliance.setName(town1.getName() + "-" + town2.getName());
            alliances.put(alliance.getName(), alliance);
            AllianceFormedEvent allianceFormedEvent = new AllianceFormedEvent(alliance, town2, true);
            Bukkit.getPluginManager().callEvent(allianceFormedEvent);
            saveAlliance(alliance);
        } else {
            outer: for (Alliance alliance : alliances.values()) {
                if (alliance.equals(mergeAlliance)) {
                    continue;
                }
                for (String townName : alliance.getMembers()) {
                    if (!mergeAlliance.getMembers().contains(townName)) {
                        continue outer;
                    }
                }
                AllianceDissolvedEvent allianceDissolvedEvent = new AllianceDissolvedEvent(alliance, town2, true);
                Bukkit.getPluginManager().callEvent(allianceDissolvedEvent);
                removeThese.add(alliance);
            }
        }

        for (Alliance alliance : removeThese) {
            removeAlliance(alliance);
        }

        for (Alliance alliance : saveThese) {
            saveAlliance(alliance);
        }
    }

    public void unAllyBroadcast(Town townBeingBrokenUpWith, Town allianceBreakerTown) {
        unAlly(townBeingBrokenUpWith, allianceBreakerTown);
        for (Player cPlayer : Bukkit.getOnlinePlayers()) {
            cPlayer.sendMessage(Civs.getPrefix() + ChatColor.RED + LocaleManager.getInstance()
                    .getTranslation(cPlayer, "town-ally-removed")
                    .replace("$1", townBeingBrokenUpWith.getName())
                    .replace("$2", allianceBreakerTown.getName()));
        }
    }

    public void unAlly(Town town1, Town town2) {
        if (!isAllied(town1, town2)) {
            return;
        }
        HashSet<Alliance> saveThese = new HashSet<>();
        HashSet<Alliance> removeThese = new HashSet<>();

        int i=0;
        for (Alliance alliance : alliances.values()) {
            boolean inAlliance = alliance.getMembers().contains(town1.getName()) &&
                    alliance.getMembers().contains(town2.getName());
            if (inAlliance) {
                AllianceDissolvedEvent allianceDissolvedEvent = new AllianceDissolvedEvent(alliance, town2, true);
                Bukkit.getPluginManager().callEvent(allianceDissolvedEvent);
                removeThese.add(alliance);
            } else {
                continue;
            }
            if (alliance.getMembers().size() > 2) {
                Alliance alliance1 = new Alliance();
                alliance1.setName(alliance.getName() + i);
                alliance1.getMembers().add(town1.getName());
                i++;

                Alliance alliance2 = new Alliance();
                alliance2.setName(alliance.getName() + i);
                alliance2.getMembers().add(town2.getName());
                i++;
                for (String townName : alliance.getMembers()) {
                    if (townName.equals(town1.getName()) ||
                            townName.equals(town2.getName())) {
                        continue;
                    }
                    alliance1.getMembers().add(townName);
                    alliance2.getMembers().add(townName);
                }
                if (!alliances.containsKey(alliance1.getName())) {
                    AllianceFormedEvent allianceFormedEvent = new AllianceFormedEvent(alliance1, town1, true);
                    Bukkit.getPluginManager().callEvent(allianceFormedEvent);
                    saveThese.add(alliance1);
                }
                if (!alliances.containsKey(alliance2.getName())) {
                    AllianceFormedEvent allianceFormedEvent = new AllianceFormedEvent(alliance2, town2, true);
                    Bukkit.getPluginManager().callEvent(allianceFormedEvent);
                    saveThese.add(alliance2);
                }
            }
        }

        for (Alliance alliance : removeThese) {
            removeAlliance(alliance);
        }

        for (Alliance alliance : saveThese) {
            alliances.put(alliance.getName(), alliance);
            saveAlliance(alliance);
        }
    }

    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        HashSet<Alliance> saveThese = new HashSet<>();
        for (Alliance alliance : alliances.values()) {
            if (alliance.getMembers().contains(event.getOldName())) {
                alliance.getMembers().remove(event.getOldName());
                alliance.getMembers().add(event.getNewName());
                saveThese.add(alliance);
            }
        }
        for (Alliance alliance : saveThese) {
            saveAlliance(alliance);
        }
    }

    @EventHandler
    public void onTownDestroyed(TownDestroyedEvent event) {
        Town town = event.getTown();
        for (Town currentTown : TownManager.getInstance().getTowns()) {
            if (currentTown.equals(town)) {
                continue;
            }
            if (isAllied(town, currentTown)) {
                unAlly(town, currentTown);
            }
        }
    }

    public ArrayList<Alliance> getAllSortedAlliances() {
        ArrayList<Alliance> returnList = getAllAlliances();
        Comparator<Alliance> comparator = new Comparator<Alliance>() {
            @Override
            public int compare(Alliance o1, Alliance o2) {
                int o1Size = o1.getMembers().size();
                int o2Size = o2.getMembers().size();
                return Integer.compare(o1Size, o2Size);
            }
        };
        returnList.sort(comparator);
        return returnList;
    }

    public ArrayList<Alliance> getAllAlliances() {
        return new ArrayList<>(alliances.values());
    }
}

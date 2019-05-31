package org.redcastlemedia.multitallented.civs.alliances;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class AllianceManager implements Listener {
    private static AllianceManager instance = null;
    private final HashMap<String, Alliance> alliances = new HashMap<>();
    public static AllianceManager getInstance() {
        if (instance == null) {
            new AllianceManager();
        }
        return instance;
    }
    public AllianceManager() {
        instance = this;
        if (Civs.getInstance() != null) {
            Bukkit.getPluginManager().registerEvents(this, Civs.getInstance());
        }
    }

    public void reload() {
        alliances.clear();
        loadAllAlliances();
    }

    public void loadAllAlliances() {
        File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
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

    public ChunkClaim getClaimAt(Location location) {
        Chunk chunk = location.getChunk();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        for (Alliance alliance : alliances.values()) {

            if (alliance.getNationClaims().get(location.getWorld().getUID())
                    .containsKey(chunkKey)) {
                return alliance.getNationClaims().get(location.getWorld().getUID())
                        .get(chunkKey);
            }
        }
        return null;
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
            alliance.setMembers(new HashSet<String>(config.getStringList("members")));
            String uuidString = config.getString("last-rename", null);
            if (uuidString != null) {
                alliance.setLastRenamedBy(UUID.fromString(uuidString));
            }
            if (config.getConfigurationSection("claims") != null) {
                HashMap<UUID, HashMap<String, ChunkClaim>> claims = new HashMap<>();
                for (String worldUUID : config.getConfigurationSection("claims").getKeys(false)) {
                    HashMap<String, ChunkClaim> chunkMap = new HashMap<>();
                    for (String chunkString : config.getStringList("claims." + worldUUID)) {
                        ChunkClaim chunkClaim = ChunkClaim.fromString(chunkString, alliance);
                        chunkMap.put(chunkClaim.getX() + "," + chunkClaim.getZ(), chunkClaim);
                    }
                    claims.put(UUID.fromString(worldUUID), chunkMap);
                }
                alliance.setNationClaims(claims);
            }

            alliance.getEffects().addAll(ConfigManager.getInstance().getAllianceClaimEffects());

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
        File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
        File allianceFile = new File(allianceFolder, oldName + ".yml");
        if (!allianceFile.delete()) {
            return false;
        }
        alliances.remove(oldName);
        alliance.setName(newName);
        alliances.put(newName, alliance);
        saveAlliance(alliance);
        return true;
    }

    public boolean removeAlliance(Alliance alliance) {
        if (Civs.getInstance() != null) {
            File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
            File allianceFile = new File(allianceFolder, alliance.getName() + ".yml");
            if (!allianceFile.delete()) {
                return false;
            }
        }
        alliances.remove(alliance.getName());
        return true;
    }

    public void saveAlliance(Alliance alliance) {
        if (Civs.getInstance() == null) {
            return;
        }
        try {
            File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
            if (!allianceFolder.exists()) {
                allianceFolder.mkdir();
            }
            File allianceFile = new File(allianceFolder, alliance.getName() + ".yml");
            if (!allianceFile.exists()) {
                allianceFile.createNewFile();
            }
            FileConfiguration config = new YamlConfiguration();

            if (alliance.getNationClaims().isEmpty()) {
                config.set("claims", null);
            } else {
                for (UUID uuid : alliance.getNationClaims().keySet()) {
                    ArrayList<String> claimList = new ArrayList<>();
                    for (ChunkClaim chunk : alliance.getNationClaims().get(uuid).values()) {
                        claimList.add(chunk.toString());
                    }
                    config.set("claims." + uuid.toString(), claimList);
                }
            }
            config.set("members", new ArrayList<String>(alliance.getMembers()));
            if (alliance.getLastRenamedBy() != null) {
                config.set("last-rename", alliance.getLastRenamedBy().toString());
            }
            config.save(allianceFile);
        } catch (Exception e) {
            e.printStackTrace();
            Civs.logger.severe("Unable to save alliance " + alliance.getName());
        }
    }

    public HashSet<Chunk> getContainingChunks(Location location,
                                              int xp, int xn,
                                              int zp, int zn) {
        HashSet<Chunk> chunkClaims = new HashSet<>();
        for (int x = (int) location.getX() - xn; x < location.getX() + xp; x += 16) {
            for (int z = (int) location.getZ() - zn; z < location.getZ() + zp; z += 16) {
                chunkClaims.add(location.getWorld().getChunkAt(x, z));
            }
        }
        return chunkClaims;
    }

    public Alliance getAlliance(String name) {
        return alliances.get(name);
    }

    public HashSet<Alliance> getAlliances(UUID uuid) {
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
            saveThese.add(alliance);
        }
        if (mergeAlliance == null) {
            Alliance alliance = new Alliance();
            alliance.getMembers().add(town1.getName());
            alliance.getMembers().add(town2.getName());
            alliance.setName(town1.getName() + "-" + town2.getName());
            // TODO fill claims
            alliances.put(alliance.getName(), alliance);
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
                removeThese.add(alliance);
            }
            mergeClaims(mergeAlliance, removeThese);
            // TODO auto fill claims
        }

        for (Alliance alliance : removeThese) {
            removeAlliance(alliance);
        }

        for (Alliance alliance : saveThese) {
            saveAlliance(alliance);
        }
    }

    private void mergeClaims(Alliance rootAlliance, HashSet<Alliance> merges) {
        for (Alliance merge : merges) {
            for (UUID uuid : merge.getNationClaims().keySet()) {
                if (!rootAlliance.getNationClaims().containsKey(uuid)) {
                    rootAlliance.getNationClaims().put(uuid, new HashMap<>());
                }
                for (ChunkClaim claim : merge.getNationClaims().get(uuid).values()) {
                    claim.setAlliance(rootAlliance);
                    String claimId = claim.getX() + "," + claim.getZ();
                    rootAlliance.getNationClaims().get(uuid).put(claimId, claim);
                }
            }
        }
    }

    public void unAlly(Town town1, Town town2) {
        HashSet<Alliance> saveThese = new HashSet<>();
        HashSet<Alliance> removeThese = new HashSet<>();

        int i=0;
        for (Alliance alliance : alliances.values()) {
            boolean inAlliance = alliance.getMembers().contains(town1.getName()) &&
                    alliance.getMembers().contains(town2.getName());
            if (inAlliance) {
                removeThese.add(alliance);
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
                    saveThese.add(alliance1);
                }
                if (!alliances.containsKey(alliance2.getName())) {
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

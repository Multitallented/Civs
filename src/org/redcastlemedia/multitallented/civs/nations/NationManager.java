package org.redcastlemedia.multitallented.civs.nations;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.alliances.ChunkClaim;
import org.redcastlemedia.multitallented.civs.alliances.ClaimBridge;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

public class NationManager implements Listener {

    public static NationManager instance = null;

    public static NationManager getInstance() {
        if (instance == null) {
            new NationManager();
        }
        return instance;
    }

    private HashMap<String, Nation> nations = new HashMap<>();

    public NationManager() {
        instance = this;
        if (Civs.getInstance() != null) {
            Bukkit.getPluginManager().registerEvents(this, Civs.getInstance());
        }
    }

    public void loadAllNations() {
        File nationFolder = new File(Civs.getInstance().getDataFolder(), "nations");
        if (!nationFolder.exists()) {
            nationFolder.mkdir();
            return;
        }
        try {
            for (File nationFile : nationFolder.listFiles()) {
                loadNation(nationFile);
            }
        } catch (NullPointerException npe) {
            // dont care
        }
    }

    private void loadNation(File nationFile) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(nationFile);
            Nation nation = new Nation();
            nation.setName(nationFile.getName().replace(".yml", ""));
            nation.setMembers(new HashSet<>(config.getStringList("members")));
            for (String townName : new ArrayList<>(nation.getMembers())) {
                if (TownManager.getInstance().getTown(townName) == null) {
                    nation.getMembers().remove(townName);
                }
            }
            String uuidString = config.getString("last-rename", null);
            if (uuidString != null) {
                nation.setLastRenamedBy(UUID.fromString(uuidString));
            }
            if (config.getConfigurationSection("claims") != null) {
                HashMap<UUID, HashMap<String, ChunkClaim>> claims = new HashMap<>();
                for (String worldUUID : config.getConfigurationSection("claims").getKeys(false)) {
                    HashMap<String, ChunkClaim> chunkMap = new HashMap<>();
                    for (String chunkString : config.getStringList("claims." + worldUUID)) {
                        ChunkClaim chunkClaim = ChunkClaim.fromString(chunkString, nation);
                        chunkMap.put(chunkClaim.getX() + "," + chunkClaim.getZ(), chunkClaim);
                    }
                    claims.put(UUID.fromString(worldUUID), chunkMap);
                }
                nation.setNationClaims(claims);
            }

            nation.getEffects().addAll(ConfigManager.getInstance().getNationClaimEffects());

            nations.put(nation.getName(), nation);
        } catch (Exception e) {
            Civs.logger.severe("Unable to load alliance " + nationFile.getName());
        }
    }

    public void saveNation(Nation nation) {
        if (Civs.getInstance() == null) {
            return;
        }
        try {
            File nationFolder = new File(Civs.getInstance().getDataFolder(), "nations");
            if (!nationFolder.exists()) {
                nationFolder.mkdir();
            }
            File nationFile = new File(nationFolder, nation.getName() + ".yml");
            if (!nationFile.exists()) {
                nationFile.createNewFile();
            }
            FileConfiguration config = new YamlConfiguration();

            if (nation.getNationClaims().isEmpty()) {
                config.set("claims", null);
            } else {
                for (UUID uuid : nation.getNationClaims().keySet()) {
                    ArrayList<String> claimList = new ArrayList<>();
                    for (ChunkClaim chunk : nation.getNationClaims().get(uuid).values()) {
                        claimList.add(chunk.toString());
                    }
                    config.set("claims." + uuid.toString(), claimList);
                }
            }
            config.set("members", new ArrayList<>(nation.getMembers()));
            if (nation.getLastRenamedBy() != null) {
                config.set("last-rename", nation.getLastRenamedBy().toString());
            }
            config.save(nationFile);
        } catch (Exception e) {
            e.printStackTrace();
            Civs.logger.severe("Unable to save alliance " + nation.getName());
        }
    }

    public boolean renameNation(String oldName, String newName) {
        if (nations.get(newName) != null) {
            return false;
        }
        Nation nation = nations.get(oldName);
        File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
        File allianceFile = new File(allianceFolder, oldName + ".yml");
        if (!allianceFile.delete()) {
            return false;
        }
        nations.remove(oldName);
        nation.setName(newName);
        nations.put(newName, nation);
        saveNation(nation);
        return true;
    }

    public boolean removeAlliance(Nation nation) {
        if (Civs.getInstance() != null) {
            File allianceFolder = new File(Civs.getInstance().getDataFolder(), "alliances");
            File allianceFile = new File(allianceFolder, nation.getName() + ".yml");
            if (!allianceFile.delete()) {
                return false;
            }
        }
        nations.remove(nation.getName());
        return true;
    }

    public ChunkClaim getClaimAt(Location location) {
        Chunk chunk = location.getChunk();
        String chunkKey = chunk.getX() + "," + chunk.getZ();
        for (Nation nation : nations.values()) {

            if (!nation.getNationClaims().containsKey(location.getWorld().getUID())) {
                continue;
            }
            if (nation.getNationClaims().get(location.getWorld().getUID())
                    .containsKey(chunkKey)) {
                return nation.getNationClaims().get(location.getWorld().getUID())
                        .get(chunkKey);
            }
        }
        return null;
    }


    @EventHandler
    public void onTownDestroyed(TownDestroyedEvent event) {
        Town town = event.getTown();
        for (Nation nation : new HashSet<>(nations.values())) {
            if (nation.getMembers().contains(town.getName())) {
                nation.getMembers().remove(town.getName());
                // TODO check if nation should dissolve
                NationManager.getInstance().saveNation(nation);
            }
        }
    }

    @EventHandler
    public void onTownRename(RenameTownEvent event) {
        HashSet<Nation> saveThese = new HashSet<>();
        for (Nation nation : nations.values()) {
            if (nation.getMembers().contains(event.getOldName())) {
                nation.getMembers().remove(event.getOldName());
                nation.getMembers().add(event.getNewName());
                saveThese.add(nation);
            }
        }
        for (Nation nation : saveThese) {
            saveNation(nation);
        }
    }

    public ArrayList<Nation> getAllNations() {
        return new ArrayList<>(nations.values());
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

    public int getMaxNationClaims(Nation nation) {
        int numberOfClaims = 0;
        for (String townName : nation.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            numberOfClaims += town.getPower();
        }
        return (int) ((double) numberOfClaims / ConfigManager.getInstance().getPowerPerNationClaim());
    }

    public int getNumberOfClaims(Nation nation) {
        int autoFilledClaims = 0;
        for (UUID uuid : nation.getNationClaims().keySet()) {
            autoFilledClaims += nation.getNationClaims().get(uuid).size();
        }
        return autoFilledClaims;
    }

    private void fillClaims(Nation nation) {
        int numberOfClaims = getMaxNationClaims(nation);
        int autoFilledClaims = getNumberOfClaims(nation);

        if (autoFilledClaims >= numberOfClaims) {
            return;
        }

        int claimsAvailable = numberOfClaims - autoFilledClaims;
        claimsAvailable = surroundAllAlliedTowns(nation, claimsAvailable);

        if (claimsAvailable < 1) {
            return;
        }
        HashSet<String> bridges = new HashSet<>();
        for (String town1Name : nation.getMembers()) {
            for (String town2Name : nation.getMembers()) {
                if (town1Name.equals(town2Name)) {
                    continue;
                }

                if (bridges.contains(town1Name + ":" + town2Name) ||
                        bridges.contains(town2Name + ":" + town1Name)) {
                    continue;
                }
                Town town1 = TownManager.getInstance().getTown(town1Name);
                Town town2 = TownManager.getInstance().getTown(town2Name);
                if (town1.getLocation().getWorld().equals(town2.getLocation().getWorld())) {
                    bridges.add(town1Name + ":" + town2Name);
                }
            }
        }
        claimsAvailable = createBridgesBetweenAlliedTowns(nation, bridges, claimsAvailable);

        if (claimsAvailable < 1) {
            return;
        }

        // TODO spiral outwards from connected towns
    }

    private int surroundAllAlliedTowns(Nation nation, int claimsAvailable) {
        int i=0;
        for (;;) {
            int fullTowns = 0;
            for (String townName : nation.getMembers()) {
                Town town = TownManager.getInstance().getTown(townName);
                TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

                int chunkRadius = (int) (Math.ceil((double) townType.getBuildRadius() / 16) + 2);
                if (i > chunkRadius * 8 + 1) {
                    fullTowns++;
                    continue;
                }

                Chunk chunk = getSurroundTownClaim(i, town.getLocation());
                ChunkClaim claim = ChunkClaim.fromChunk(chunk);
                if (claim == null) {
                    claim = new ChunkClaim(chunk.getX(), chunk.getZ(), town.getLocation().getWorld(), nation);
                    if (!nation.getNationClaims().containsKey(town.getLocation().getWorld().getUID())) {
                        nation.getNationClaims().put(town.getLocation().getWorld().getUID(), new HashMap<>());
                    }
                    nation.getNationClaims().get(town.getLocation().getWorld().getUID())
                            .put(claim.getId(), claim);
                    claimsAvailable--;

                    if (claimsAvailable < 1) {
                        return 0;
                    }
                }

            }
            if (fullTowns >= nation.getMembers().size()) {
                break;
            }
            i++;
        }
        return claimsAvailable;
    }

    private int createBridgesBetweenAlliedTowns(Nation nation, HashSet<String> bridges, int claimsAvailable) {
        HashSet<ClaimBridge> claimBridges = new HashSet<>();
        for (String bridgeString : bridges) {
            claimBridges.add(getBridges(bridgeString));
        }

        int i=0;
        while (claimsAvailable > 0 && !claimBridges.isEmpty()) {
            HashSet<ClaimBridge> tempBridges = new HashSet<>(claimBridges);
            for (ClaimBridge claimBridge : tempBridges) {
                Chunk chunk = getBridgeChunk(i, claimBridge);
                if (chunk == null) {
                    claimBridges.remove(claimBridge);
                }
                // TODO add chunk to alliance claim
            }
        }

        return claimsAvailable;
    }

    ClaimBridge getBridges(String bridgeName) {
        Town town1 = TownManager.getInstance().getTown(bridgeName.split(":")[0]);
        Town town2 = TownManager.getInstance().getTown(bridgeName.split(":")[1]);

        double x1 = town1.getLocation().getX();
        double x2 = town2.getLocation().getX();
        double diffX = x2 - x1;

        double z1 = town1.getLocation().getZ();
        double z2 = town2.getLocation().getZ();
        double diffZ = z2 - z1;

        double slope = diffZ / diffX;

        return new ClaimBridge(x1, x2, z1, z2, diffX, diffZ, slope, town1.getLocation().getWorld());
    }

    // TODO this is probably wrong and needs to be fixed and tested
    Chunk getBridgeChunk(int index, ClaimBridge claimBridge) {
        if (claimBridge.getDiffX() > 0 && claimBridge.getX1() + 16 * index > claimBridge.getX2()) {
            return null;
        } else if (claimBridge.getDiffX() < 0 && claimBridge.getX1() + 16 * index < claimBridge.getX2()) {
            return null;
        }
        double x = claimBridge.getDiffX() > 0 ? claimBridge.getX1() + 16 * index : claimBridge.getX1() - 16 * index;
        double z = claimBridge.getZ1() + claimBridge.getSlope() * x;

        Location location = new Location(claimBridge.getWorld(), x, 60, z);
        return location.getChunk();
    }

    Chunk getSurroundTownClaim(int index, Location location) {
        if (0 == index) {
            return location.getChunk();
        }
        int layer = 0;
        int currentStep = 0;
        int layerProgress = 0;
        for (;;) {
            layer++;
            currentStep++;
            layerProgress = index - currentStep;
            currentStep += Math.min(layer * 8, layerProgress);
            if (currentStep >= index) {
                break;
            }
        }
        // 1, 1, 1, 0, -1, -1, -1, 0
        int side = (int) ((double) layer * 2) + 1;
        int x = layer;
        int z = layer;

        if (layerProgress < side) {
            z = z - layerProgress;
        } else if (layerProgress < side * 2 - 2) {
            z = -z;
            x = x - layerProgress - 1 + side;
        } else if (layerProgress < side * 3 - 2) {
            x = -x;
            z = -z + layerProgress + 2 - side * 2;
        } else {
            x = -x + layerProgress + 3 - side * 3;
        }


        Chunk chunk = location.getChunk();
        x = chunk.getX() + x;
        z = chunk.getZ() + z;
        return location.getWorld().getChunkAt(x, z);
    }

    private void mergeClaims(Nation rootNation, HashSet<Nation> merges) {
        for (Nation merge : merges) {
            for (UUID uuid : merge.getNationClaims().keySet()) {
                if (!rootNation.getNationClaims().containsKey(uuid)) {
                    rootNation.getNationClaims().put(uuid, new HashMap<>());
                }
                for (ChunkClaim claim : merge.getNationClaims().get(uuid).values()) {
                    claim.setNation(rootNation);
                    String claimId = claim.getX() + "," + claim.getZ();
                    rootNation.getNationClaims().get(uuid).put(claimId, claim);
                }
            }
        }
    }

    public boolean isInNation(UUID uniqueId, Nation nation) {
        for (String townName : nation.getMembers()) {
            Town town = TownManager.getInstance().getTown(townName);
            if (town == null) {
                continue;
            }
            if (town.getRawPeople().containsKey(uniqueId)) {
                return true;
            }
        }
        return false;
    }

    public Nation getNation(UUID uniqueId) {
        for (Nation nation : nations.values()) {
            for (String townName : nation.getMembers()) {
                Town town = TownManager.getInstance().getTown(townName);
                if (town == null) {
                    continue;
                }
                if (town.getRawPeople().containsKey(uniqueId)) {
                    return nation;
                }
            }
        }
        return null;
    }

    public Nation getNation(String townName) {
        for (Nation nation : nations.values()) {
            if (nation.getMembers().contains(townName)) {
                return nation;
            }
        }
        return null;
    }

    public void createNation(Town newTown) {
        Nation nation = new Nation();
        nation.setName(newTown.getName());
        nation.setCapitol(newTown.getName());
        // TODO create chunk claims
        nation.getMembers().add(newTown.getName());
        saveNation(nation);
    }
}

package org.redcastlemedia.multitallented.civs.towns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.*;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.HousingEffect;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.DebugLogger;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class TownManager {

    private static TownManager townManager = null;
    private HashMap<String, Town> towns = new HashMap<>();
    private List<Town> sortedTowns = new ArrayList<>();
    private HashMap<UUID, Town> invites = new HashMap<>();
    private ArrayList<Town> needsSaving = new ArrayList<>();


    public void reload() {
        towns.clear();
        sortedTowns.clear();
        invites.clear();
        loadAllTowns();
    }

    public void loadAllTowns() {
        if (Civs.getInstance() == null) {
            return;
        }
        File townFolder = new File(Civs.dataLocation, "towns");
        if (!townFolder.exists()) {
            townFolder.mkdir();
        }
        try {
            for (File file : townFolder.listFiles()) {
                FileConfiguration config = new YamlConfiguration();
                try {
                    config.load(file);

                    loadTown(config, file);
                } catch (Exception e) {
                    String message = "Unable to read from towns/" + file.getName();
                    Civs.logger.log(Level.SEVERE, message, e);
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException npe) {
            Civs.logger.log(Level.SEVERE, "Unable to read from town folder!", npe);
        }
    }

    public List<Town> getTowns() { return sortedTowns; }
    public Set<String> getTownNames() { return towns.keySet(); }
    public Town getTown(String name) {
        return towns.get(name);
    }

    public Town getTownAt(Location location) {
        ItemManager itemManager = ItemManager.getInstance();
        for (Town town : sortedTowns) {
            TownType townType = (TownType) itemManager.getItemType(town.getType());
            int radius = townType.getBuildRadius();
            int radiusY = townType.getBuildRadiusY();
            Location townLocation = town.getLocation();

            if (townLocation.getWorld() == null) {
                continue;
            }
            if (!townLocation.getWorld().equals(location.getWorld())) {
                continue;
            }

            if (townLocation.getX() - radius >= location.getX()) {
                break;
            }

            if (townLocation.getX() + radius >= location.getX() &&
                    townLocation.getZ() + radius >= location.getZ() &&
                    townLocation.getZ() - radius <= location.getZ() &&
                    townLocation.getY() - radiusY <= location.getY() &&
                    townLocation.getY() + radiusY >= location.getY()) {
                return town;
            }

        }
        return null;
    }

    public void checkCriticalRequirements(Region region) {
        Town town = getTownAt(region.getLocation());
        if (town == null) {
            return;
        }
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        RegionManager regionManager = RegionManager.getInstance();
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        HashSet<String> critReqs = new HashSet<>();
        if (!townType.getCriticalReqs().contains(region.getType().toLowerCase())) {
            boolean containsReq = false;
            for (String currentReq : regionType.getGroups()) {
                if (townType.getCriticalReqs().contains(currentReq)) {
                    critReqs.add(currentReq);
                    containsReq = true;
                }
            }
            if (!containsReq) {
                return;
            }
        } else {
            critReqs.add(region.getType().toLowerCase());
        }
        for (Region containedRegion :
                regionManager.getContainingRegions(town.getLocation(), townType.getBuildRadius())) {
            if (region.equals(containedRegion)) {
                continue;
            }
            critReqs.remove(region.getType().toLowerCase());
            RegionType containedType = (RegionType) ItemManager.getInstance().getItemType(containedRegion.getType());
            for (String currentReq : containedType.getGroups()) {
                critReqs.remove(currentReq);
            }
        }
        if (!critReqs.isEmpty()) {
            removeTown(town, true);
        }
    }

    public List<Town> checkIntersect(Location location, TownType townType, int modifier) {
        int buildRadius = townType.getBuildRadius() + modifier;
        int buildRadiusY = townType.getBuildRadiusY() + modifier;
        List<Town> townArrayList = new ArrayList<>();
        for (Town town : getTowns()) {
            if (!location.getWorld().equals(town.getLocation().getWorld())) {
                continue;
            }
            TownType currentTownType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            if (location.getX() + buildRadius >= town.getLocation().getX() - currentTownType.getBuildRadius() &&
                    location.getX() - buildRadius <= town.getLocation().getX() + currentTownType.getBuildRadius() &&
                    location.getZ() + buildRadius >= town.getLocation().getZ() - currentTownType.getBuildRadius() &&
                    location.getZ() - buildRadius <= town.getLocation().getZ() + currentTownType.getBuildRadius() &&
                    Math.max(location.getY() - buildRadiusY, 0) <=
                            Math.max(town.getLocation().getY() + currentTownType.getBuildRadiusY(), 0) &&
                    Math.min(location.getY() + buildRadiusY, location.getWorld().getMaxHeight()) >=
                            Math.min(town.getLocation().getY() - currentTownType.getBuildRadiusY(), town.getLocation().getWorld().getMaxHeight())) {
                townArrayList.add(town);
            }
        }
        return townArrayList;
    }

    private void loadTown(FileConfiguration config, File file) {

        Location location = Region.idToLocation(config.getString("location"));
        if (location == null || location.getWorld() == null) {
            Civs.logger.log(Level.SEVERE, "Invalid town attempted to load {0}", file.getName());
            if (ConfigManager.getInstance().isDeleteInvalidRegions()) {
                Civs.logger.log(Level.SEVERE, "Deleteing invalid town file {0}", file.getName());
                file.delete();
            }
            return;
        }
        HashMap<UUID, String> people = new HashMap<>();
        ConfigurationSection peopleSection = config.getConfigurationSection("people");
        if (config.isSet("people") && peopleSection != null && !peopleSection.getKeys(false).isEmpty()) {
            for (String key : peopleSection.getKeys(false)) {
                people.put(UUID.fromString(key), config.getString("people." + key));
            }
        }
        int maxPower = config.getInt("max-power", 500);
        int power = config.getInt("power", maxPower);
        int housing = config.getInt("housing", 0);
        int villagers = config.getInt("villagers", 0);
        long lastDisable = config.getLong("last-disable", -1);
        String governmentType = config.getString("gov-type", GovernmentType.DICTATORSHIP.name());
        Town town = new Town(config.getString("name", "NameNotFound"),
                config.getString("type"),
                location,
                people,
                power,
                maxPower,
                housing,
                villagers,
                lastDisable);
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        town.setEffects(new HashMap<>(townType.getEffects()));
        town.setDevolvedToday(config.getBoolean("devolved-today", false));
        town.setGovernmentType(governmentType);
        if (config.isSet("idiocracy-score")) {
            HashMap<UUID, Integer> idiocracyScores = new HashMap<>();
            for (String uuidString : config.getConfigurationSection("idiocracy-score").getKeys(false)) {
                UUID cUuid = UUID.fromString(uuidString);
                int score = config.getInt("idiocracy-score." + uuidString, 0);
                idiocracyScores.put(cUuid, score);
            }
            town.setIdiocracyScore(idiocracyScores);
        }
        if (config.isSet("gov-type-changed-today")) {
            town.setGovTypeChangedToday(true);
        }
        if (config.isSet("last-active")) {
            town.setLastActive(config.getLong("last-active", -1));
        } else {
            town.setLastActive(-1);
        }
        if (config.isSet("revolt")) {
            loadRevolt(town, config.getStringList("revolt"));
        }
        if (config.isSet("last-vote")) {
            town.setLastVote(config.getLong("last-vote", 0));
        }
        if (config.isSet("votes")) {
            HashMap<UUID, HashMap<UUID, Integer>> votes = new HashMap<>();
            for (String cUuidString : config.getConfigurationSection("votes").getKeys(false)) {
                UUID cUuid = UUID.fromString(cUuidString);
                HashMap<UUID, Integer> theseVotes = new HashMap<>();
                for (String uuidString : config.getConfigurationSection("votes." + cUuidString).getKeys(false)) {
                    UUID uuid = UUID.fromString(uuidString);
                    theseVotes.put(uuid, config.getInt("votes." + cUuidString + "." + uuidString, 0));
                }
                votes.put(cUuid, theseVotes);
            }
            town.setVotes(votes);
        }
        if (config.isSet("bounties")) {
            town.setBounties(Util.readBountyList(config));
        }
        town.setBankAccount(config.getDouble("bank", 0));
        town.setTaxes(config.getDouble("taxes", 0));
        if (config.isSet("colonial-town")) {
            town.setColonialTown(config.getString("colonial-town"));
        }
        if (config.isSet("child-locations")) {
            List<Location> locationList = new ArrayList<>();
            for (String locationString : config.getStringList("child-locations")) {
                locationList.add(Region.idToLocation(locationString));
            }
            town.setChildLocations(locationList);
        }
        addTown(town);
    }

    private void loadRevolt(Town town, List<String> revoltList) {
        for (String uuidString : revoltList) {
            town.getRevolt().add(UUID.fromString(uuidString));
        }
    }

    public Set<Town> getOwnedTowns(Civilian civilian) {
        HashSet<Town> townSet = new HashSet();
        for (Town town : towns.values()) {
            if (!town.getRawPeople().containsKey(civilian.getUuid()) ||
                    !town.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER)) {
                continue;
            }
            townSet.add(town);
        }
        return townSet;
    }

    public void addTown(Town town) {
        towns.put(town.getName(), town);
        sortedTowns.add(town);
        if (sortedTowns.size() > 1) {
            Collections.sort(sortedTowns, new Comparator<Town>() {

                @Override
                public int compare(Town o1, Town o2) {
                    ItemManager itemManager = ItemManager.getInstance();
                    TownType townType1 = (TownType) itemManager.getItemType(o1.getType());
                    TownType townType2 = (TownType) itemManager.getItemType(o2.getType());
                    if (o1.getLocation().getX() - townType1.getBuildRadius() >
                            o2.getLocation().getX() - townType2.getBuildRadius()) {
                        return 1;
                    } else if (o1.getLocation().getX() - townType1.getBuildRadius() <
                            o2.getLocation().getX() - townType2.getBuildRadius()) {
                        return -1;
                    }
                    return 0;
                }
            });
        }
    }
    public void removeTown(Town town, boolean broadcast) {
        removeTown(town, broadcast, true);
    }
    public void removeTown(Town town, boolean broadcast, boolean destroyRing) {
        if (broadcast) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "town-destroyed").replace("$1", town.getName()));
            }
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            TownDestroyedEvent townDestroyedEvent = new TownDestroyedEvent(town, townType);
            Bukkit.getPluginManager().callEvent(townDestroyedEvent);
        }
        towns.remove(town.getName());
        sortedTowns.remove(town);
        needsSaving.removeIf(cTown -> cTown.equals(town));
        if (destroyRing && ConfigManager.getInstance().getTownRings()) {
            town.destroyRing(true, broadcast);
        }
        removeTownFile(town.getName());
    }

    public void setTownPower(Town town, int power) {
        if (power > town.getMaxPower()) {
            town.setPower(town.getMaxPower());
        } else {
            town.setPower(power);
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        if (town.getPower() < 1 && ConfigManager.getInstance().getDestroyTownsAtZero() &&
                townType.getChild() == null) {
            TownManager.getInstance().removeTown(town, true);
        } else {
            if (town.getPower() < 1) {
                if (townType.getChild() != null) {
                    devolveTown(town, townType);
                } else {
                    hasGrace(town, true);
                }
            } else {
                TownManager.getInstance().saveTown(town);
            }
        }
    }

    private void devolveTown(Town town, TownType townType) {
        if (townType.getChild() == null) {
            return;
        }
        TownDevolveEvent townDevolveEvent = new TownDevolveEvent(town, townType);
        Bukkit.getPluginManager().callEvent(townDevolveEvent);
        town.destroyRing(false, true);
        TownType childTownType = (TownType) ItemManager.getInstance().getItemType(townType.getChild());
        town.setType(childTownType.getProcessedName());
        town.setPower(childTownType.getMaxPower() / 2);
        town.setMaxPower(childTownType.getMaxPower());
        town.setDevolvedToday(true);
        TownManager.getInstance().saveTown(town);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(ChatColor.RED + ChatColor.stripColor(Civs.getPrefix()) +
                    LocaleManager.getInstance().getTranslation(player, "devolve-town")
                    .replace("$1", town.getName())
                    .replace("$2", childTownType.getDisplayName(player)));
        }
    }

    private void removeTownFile(String townName) {
        if (Civs.getInstance() == null) {
            return;
        }
        File townFolder = new File(Civs.dataLocation, "towns");
        if (!townFolder.exists()) {
            townFolder.mkdir();
        }
        File townFile = new File(townFolder, townName + ".yml");
        townFile.delete();
        Civs.logger.info(townFile.getName() + " was deleted");
    }

    public boolean hasGrace(Town town, boolean disable) {
        long grace = getRemainingGracePeriod(town);
        if (grace < 0 && disable) {
            long lastDisable = (ConfigManager.getInstance().getTownGracePeriod() * 1000) + System.currentTimeMillis();
            town.setLastDisable(lastDisable);
            saveTown(town);
            return true;
        }
        if (!disable) {
            if (grace > -1) {
                town.setLastDisable(-1);
                saveTown(town);
            }
            return true;
        }
        return grace != 0;
    }

    public long getRemainingGracePeriod(Town town) {
        if (town == null) {
            return 0;
        }
        if (town.getLastDisable() < 1) {
            return -1;
        }
        return Math.max(0, town.getLastDisable() - System.currentTimeMillis());
    }

    public boolean addInvite(UUID uuid, Town town) {
        TownInvitesPlayerEvent event = new TownInvitesPlayerEvent(uuid, town);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        invites.put(uuid, town);
        return true;
    }
    public void clearInvite(UUID uuid) {
        invites.remove(uuid);
    }
    public Town getInviteTown(UUID uuid) {
        return invites.get(uuid);
    }
    public boolean acceptInvite(UUID uuid) {
        if (!invites.containsKey(uuid)) {
            return false;
        }

        Town town = invites.get(uuid);

        PlayerAcceptsTownInviteEvent event = new PlayerAcceptsTownInviteEvent(uuid, town);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        town.setPeople(uuid, Constants.MEMBER);
        saveTown(town);
        invites.remove(uuid);
        return true;
    }

    public Set<Town> getTownsForPlayer(UUID uuid) {
        Set<Town> townSet = new HashSet<>();
        for (Town town : towns.values()) {
            if (town.getRawPeople().containsKey(uuid)) {
                townSet.add(town);
            }
        }
        return townSet;
    }

    public Set<Region> getContainingRegions(String townName) {
        Town town = getTown(townName);
        if (town == null) {
            return new HashSet<>();
        }

        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        return RegionManager.getInstance().getRegionsXYZ(town.getLocation(), townType.getBuildRadius(),
                townType.getBuildRadiusY(), townType.getBuildRadius(), false);
    }

    public void saveTown(Town town) {
        needsSaving.add(town);
    }

    public void saveAllUnsavedTowns() {
        for (Town town : new HashSet<>(needsSaving)) {
            saveTownNow(town);
        }
        needsSaving.clear();
    }

    public int getCountOfPendingSaves() {
        return needsSaving.size();
    }

    public void saveNextTown() {
        Town t = null;
        for (Town town : needsSaving) {
            t = town;
            saveTownNow(t);
            break;
        }
        if (t != null) {
            while (needsSaving.contains(t)) {
                needsSaving.remove(t);
            }
        }
    }

    private void saveTownNow(Town town) {
        if (ConfigManager.getInstance().isDebugLog()) {
            DebugLogger.saves++;
        }
        File townFolder = new File(Civs.dataLocation, "towns");
        if (!townFolder.exists()) {
            townFolder.mkdir();
        }
        File townFile = new File(townFolder, town.getName() + ".yml");
        try {
            if (!townFile.exists()) {
                townFile.createNewFile();
            }
            FileConfiguration config = new YamlConfiguration();
            config.load(townFile);


            config.set("name", town.getName());
            config.set("type", town.getType());
            config.set("location", Region.locationToString(town.getLocation()));
            config.set("people", null);
            config.set("devolved-today", town.isDevolvedToday());
            if (town.isGovTypeChangedToday()) {
                config.set("gov-type-changed-today", true);
            } else {
                config.set("gov-type-changed-today", null);
            }
            if (town.getLastActive() > -1) {
                config.set("last-active", town.getLastActive());
            } else {
                config.set("last-active", -1);
            }
            if (town.getRevolt().isEmpty()) {
                config.set("revolt", null);
            } else {
                saveRevolt(town, config);
            }
            for (UUID key : town.getRawPeople().keySet()) {
                if (town.getRawPeople().get(key).contains("ally")) {
                    continue;
                }
                config.set("people." + key, town.getPeople().get(key));
            }
            List<String> locationList = new ArrayList<>();
            for (Location lo : town.getChildLocations()) {
                locationList.add(Region.locationToString(lo));
            }
            config.set("child-locations", locationList);
            config.set("housing", town.getHousing());
            config.set("population", town.getPopulation());
            config.set("villagers", town.getVillagers());
            config.set("last-disable", town.getLastDisable());
            config.set("power", town.getPower());
            config.set("max-power", town.getMaxPower());
            config.set("gov-type", town.getGovernmentType());
            config.set("taxes", town.getTaxes());
            config.set("bank", town.getBankAccount());
            config.set("last-vote", town.getLastVote());
            config.set("votes", null);
            if (!town.getVotes().isEmpty()) {
                for (UUID uuid : town.getVotes().keySet()) {
                    for (UUID cUuid : town.getVotes().get(uuid).keySet()) {
                        config.set("votes." + uuid.toString() + "." + cUuid.toString(),
                                town.getVotes().get(uuid).get(cUuid));
                    }
                }
            }
            config.set("idiocracy-score", null);
            if (!town.getIdiocracyScore().isEmpty()) {
                for (UUID uuid : town.getIdiocracyScore().keySet()) {
                    config.set("idiocracy-score." + uuid.toString(),
                            town.getIdiocracyScore().get(uuid));
                }
            }

            config.set("bounties", null);
            if (town.getBounties() != null && !town.getBounties().isEmpty()) {
                for (int i = 0; i < town.getBounties().size(); i++) {
                    if (town.getBounties().get(i).getIssuer() != null) {
                        config.set("bounties." + i + ".issuer", town.getBounties().get(i).getIssuer().toString());
                    }
                    config.set("bounties." + i + ".amount", town.getBounties().get(i).getAmount());
                }
            }
            if (town.getColonialTown() == null) {
                config.set("colonial-town", null);
            } else {
                config.set("colonial-town", town.getColonialTown());
            }

            config.save(townFile);
        } catch (Exception e) {
            e.printStackTrace();
            Civs.logger.severe("Unable to save town " + town.getName() + ".yml");
        }
    }

    private void saveRevolt(Town town, FileConfiguration config) {
        ArrayList<String> uuidList = new ArrayList<>();
        for (UUID uuid : town.getRevolt()) {
            uuidList.add(uuid.toString());
        }
        config.set("revolt", uuidList);
    }

    public static TownManager getInstance() {
        if (townManager == null) {
            townManager = new TownManager();
            if (Civs.getInstance() != null) {
                townManager.loadAllTowns();
            }
        }
        return townManager;
    }

    public Set<Town> findCommonTowns(Civilian damagerCiv, Civilian dyingCiv) {
        HashSet<Town> commonTowns = new HashSet<>();
        for (Town town : sortedTowns) {
            if (town.getRawPeople().containsKey(damagerCiv.getUuid()) &&
                    town.getRawPeople().containsKey(dyingCiv.getUuid())) {
                commonTowns.add(town);
            }
        }
        return commonTowns;
    }

    public Town isOwnerOfATown(Civilian civilian) {
        for (Town town : sortedTowns) {
            if (!town.getRawPeople().containsKey(civilian.getUuid()) ||
                    !town.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER)) {
                continue;
            }
            return town;
        }
        return null;
    }

    public boolean townNameExists(String name) {
        for (String townName : towns.keySet()) {
            if (name.toLowerCase().equalsIgnoreCase(townName)) {
                return true;
            }
        }
        return false;
    }

    public void placeTown(Player player, String name, Town town) {
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        TownType townType = handleUsageErrors(player, name, town, civilian);
        if (townType == null) {
            return;
        }

        int modifier = ConfigManager.getInstance().getMinDistanceBetweenTowns();
        List<Town> intersectTowns = checkIntersect(player.getLocation(), townType, modifier);
        if (handleIntersectingTown(player, town, localeManager, townType, intersectTowns)) {
            return;
        }
        if (intersectTowns.isEmpty() && townType.getChild() != null) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "must-be-built-on-top").replace("$1", townType.getProcessedName())
                    .replace("$2", townType.getChild()));
            return;
        }


        String townTypeLocalName = townType.getDisplayName(player);
        if (handleTownRequirementRegions(player, townType, townTypeLocalName)) {
            return;
        }


        HashMap<UUID, String> people = new HashMap<>();
        people.put(player.getUniqueId(), Constants.OWNER);
        Location newTownLocation = player.getLocation();
        List<Location> childLocations = new ArrayList<>();
        TownType childTownType = null;
        String governmentType = null;
        int villagerCount = 0;
        double bank = 0;
        if (townType.getChild() != null) {
            Town intersectTown = intersectTowns.get(0);
            if (intersectTown.getPopulation() < townType.getChildPopulation()) {
                CivItem intersectTownItem = ItemManager.getInstance().getItemType(intersectTown.getType().toLowerCase());
                String localIntersectName = intersectTownItem.getDisplayName(player);
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player, "population-req")
                        .replace("$1", localIntersectName)
                        .replace("$2", "" + townType.getChildPopulation()));
                return;
            }

            int powerRequired = (int) Math.round((double) town.getMaxPower() * ConfigManager.getInstance().getPercentPowerForUpgrade());
            if (town.getPower() < powerRequired) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player, "not-enough-power")
                        .replace("$1", town.getName()).replace("$2", "" + powerRequired));
                return;
            }

            people = intersectTown.getPeople();
            newTownLocation = intersectTown.getLocation();
            childLocations.add(newTownLocation);
            name = intersectTown.getName();
            bank = intersectTown.getBankAccount();
            governmentType = intersectTown.getGovernmentType();
            childTownType = (TownType) ItemManager.getInstance().getItemType(intersectTown.getType());
            TownManager.getInstance().removeTown(intersectTown, false, false);
            villagerCount = intersectTown.getVillagers();
        }

        int housingCount = getHousingCount(newTownLocation, townType);
        Town newTown = new Town(name,
                townType.getProcessedName(),
                newTownLocation,
                people,
                townType.getPower(),
                townType.getMaxPower(), housingCount, villagerCount, -1);
        newTown.setEffects(new HashMap<>(townType.getEffects()));
        newTown.setChildLocations(childLocations);
        newTown.setBankAccount(bank);
        Government government = setGovTypeAndMaxPower(governmentType, newTown);

        if (childTownType != null) {
            evolveTown(player, civilian, townType, townTypeLocalName, childTownType, newTown, government);

        } else {
            if (!canJoinAnotherTown(player)) {
                player.sendMessage(Civs.getPrefix() +
                        localeManager.getTranslation(player, "residence-limit-reached").replace("$1", name));
                return;
            }
            TownCreatedEvent townCreatedEvent = new TownCreatedEvent(newTown, townType);
            newTown.setLastVote(System.currentTimeMillis());
            Bukkit.getPluginManager().callEvent(townCreatedEvent);
            if (townCreatedEvent.isCancelled()) {
                return;
            }
        }

        saveTown(newTown);
        addTown(newTown);
        player.getInventory().remove(player.getInventory().getItemInMainHand());

        player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                "town-created").replace("$1", newTown.getName()));
        if (ConfigManager.getInstance().getTownRings()) {
            newTown.createRing();
        }
        if (childTownType == null && GovernmentManager.getInstance().getGovermentTypes().size() > 1) {
            HashMap<String, String> params = new HashMap<>();
            params.put("town", newTown.getName());
            MenuManager.clearHistory(player.getUniqueId());
            MenuManager.getInstance().openMenu(player, "gov-list", params);
        }
    }

    @Nullable
    private TownType handleUsageErrors(Player player, String name, Town town, Civilian civilian) {
        LocaleManager localeManager = LocaleManager.getInstance();
        if (TownManager.getInstance().townNameExists(name) && (town == null ||
                !town.getName().equalsIgnoreCase(name) ||
                !town.getRawPeople().containsKey(civilian.getUuid()))) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "specify-town-name"));
            return null;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!CVItem.isCivsItem(itemStack)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "hold-town"));
            return null;
        }
        CivItem civItem = CivItem.getFromItemStack(itemStack);

        if (!(civItem instanceof TownType)) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "hold-town"));
            return null;
        }
        return (TownType) civItem;
    }

    private Government setGovTypeAndMaxPower(String governmentType, Town newTown) {
        if (governmentType != null) {
            newTown.setGovernmentType(governmentType);
        } else {
            newTown.setGovernmentType(ConfigManager.getInstance().getDefaultGovernmentType());
        }
        Government government = GovernmentManager.getInstance().getGovernment(newTown.getGovernmentType());
        if (government != null) {
            for (GovTypeBuff buff : government.getBuffs()) {
                if (buff.getBuffType() == GovTypeBuff.BuffType.MAX_POWER) {
                    newTown.setMaxPower((int) Math.round((double) newTown.getMaxPower() * (1 + (double) buff.getAmount() / 100)));
                    break;
                }
            }
        } else {
            government = GovernmentManager.getInstance().getGovernment(ConfigManager.getInstance().getDefaultGovernmentType());
        }
        return government;
    }

    @SuppressWarnings("unchecked")
    private boolean handleTownRequirementRegions(Player player, TownType townType, String townTypeLocalName) {
        LocaleManager localeManager = LocaleManager.getInstance();
        if (!townType.getReqs().isEmpty()) {
            HashMap<String, Integer> checkList = (HashMap<String, Integer>) townType.getReqs().clone();
            Set<Region> regions = RegionManager.getInstance().getRegionsXYZ(player.getLocation(), townType.getBuildRadius(),
                    townType.getBuildRadiusY(), townType.getBuildRadius(), false);
            for (Region region : regions) {
                RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
                if (!removeRegionFromCheckList(checkList, regionType)) {
                    cleanupCheckList(checkList, regionType);
                }
            }
            if (!checkList.isEmpty()) {
                player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                        "missing-region-requirements").replace("$1", townTypeLocalName));
                HashMap<String, String> params = new HashMap<>();
                StringBuilder regionString = new StringBuilder();
                for (Map.Entry<String, Integer> entry : checkList.entrySet()) {
                    regionString.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
                }
                regionString.substring(0, regionString.length() - 1);
                params.put("regionList", regionString.toString());
                MenuManager.clearHistory(player.getUniqueId());
                MenuManager.getInstance().openMenu(player, "region-type-list", params);
                return true;
            }
        }
        return false;
    }

    private boolean removeRegionFromCheckList(HashMap<String, Integer> checkList, RegionType regionType) {
        String regionTypeName = regionType.getProcessedName();
        if (checkList.containsKey(regionTypeName)) {
            if (checkList.get(regionTypeName) < 2) {
                checkList.remove(regionTypeName);
            } else {
                checkList.put(regionTypeName, checkList.get(regionTypeName) - 1);
            }
            return true;
        }
        return false;
    }

    private void cleanupCheckList(HashMap<String, Integer> checkList, RegionType regionType) {
        for (String groupType : regionType.getGroups()) {
            String groupName = groupType.toLowerCase();
            if (checkList.containsKey(groupName)) {
                if (checkList.get(groupName) < 2) {
                    checkList.remove(groupName);
                } else {
                    checkList.put(groupName, checkList.get(groupName) - 1);
                }
                return;
            }
        }
    }

    private boolean handleIntersectingTown(Player player, Town town, LocaleManager localeManager, TownType townType, List<Town> intersectTowns) {
        if (intersectTowns.size() > 1 ||
                    (!intersectTowns.isEmpty() &&
                    (townType.getChild() == null || !townType.getChild().equals(intersectTowns.get(0).getType())))) {
            TownType townType1 = (TownType) ItemManager.getInstance().getItemType(intersectTowns.get(0).getType());
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(player,
                    "too-close-town").replace("$1", townType.getDisplayName(player))
                    .replace("$2", townType1.getDisplayName(player)));
            UUID firstOwnerUuid = intersectTowns.get(0).getFirstOwner();
            String ownerName = "no one";
            if (firstOwnerUuid != null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(firstOwnerUuid);
                if (offlinePlayer.getName() != null) {
                    ownerName = offlinePlayer.getName();
                }
            }
            if (town != null) {
                Civs.logger.log(Level.INFO,"{0} failed to build a {1} at {2} because it would be too close to a {3} owned by {4}",
                        new Object[] { player.getName(), townType.getProcessedName(), Region.locationToString(town.getLocation()),
                                townType1.getProcessedName(), ownerName });
            } else {
                Civs.logger.log(Level.INFO,"{0} failed to build a {1} because it would be too close to a {2} owned by {3}",
                        new Object[] { player.getName(), townType.getProcessedName(), townType1.getProcessedName(), ownerName });
            }
            return true;
        }
        return false;
    }

    private void evolveTown(Player player, Civilian civilian, TownType townType, String townTypeLocalName, TownType childTownType, Town newTown, Government government) {
        TownEvolveEvent townEvolveEvent = new TownEvolveEvent(newTown, childTownType, townType);
        Bukkit.getPluginManager().callEvent(townEvolveEvent);

        if (government.getGovernmentType() == GovernmentType.COOPERATIVE && Civs.econ != null &&
                newTown.getBankAccount() > 0) {
            double price = newTown.getPrice();
            price = Math.min(price, newTown.getBankAccount());
            Civs.econ.depositPlayer(player, price);
            newTown.setBankAccount(newTown.getBankAccount() - price);
            String priceString = Util.getNumberFormat(price, civilian.getLocale());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "town-assist-price").replace("$1", priceString)
                    .replace("$2", townTypeLocalName));
        }
    }

    int getHousingCount(Location newTownLocation, TownType townType) {
        int housingCount = 0;
        for (Region region : getRegionsInTown(newTownLocation, townType.getBuildRadius(), townType.getBuildRadiusY())) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (regionType.getEffects().containsKey(HousingEffect.KEY)) {
                housingCount += Integer.parseInt(regionType.getEffects().get(HousingEffect.KEY));
            }
        }
        return housingCount;
    }

    public String getBiggestTown(Civilian civilian) {
        Town town = getInstance().isOwnerOfATown(civilian);
        if (town != null) {
            return town.getName();
        } else {
            int highestPopulation = 0;
            Town highestTown = null;
            for (Town to : getInstance().getTowns()) {
                if (!to.getPeople().containsKey(civilian.getUuid())) {
                    continue;
                }
                int pop = to.getPopulation();
                if (pop > highestPopulation) {
                    highestTown = to;
                    highestPopulation = pop;
                }
            }
            return highestTown == null ? "-" : highestTown.getName();
        }
    }

    public Set<Region> getRegionsInTown(Town town) {
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        return getRegionsInTown(town.getLocation(), townType.getBuildRadius(), townType.getBuildRadiusY());
    }

    private Set<Region> getRegionsInTown(Location location, int radius, int radiusY) {
        //TODO fix this to account for vertical radius being different
        return RegionManager.getInstance().getContainingRegions(location, radius);
    }

    public boolean canJoinAnotherTown(Player player) {
        ConfigManager cm = ConfigManager.getInstance();
        if (cm.getResidenciesCount() == -1) {
            return true;
        }

        Set<Town> townsForPlayer = getTownsForPlayer(player.getUniqueId());
        if (townsForPlayer.size() < cm.getResidenciesCount()) {
            return true;
        }

        Map<Integer, String> residenciesCountOverride = cm.getResidenciesCountOverride();
        for (Map.Entry<Integer, String> entry : residenciesCountOverride.entrySet()) {
            int key = entry.getKey();
            if (townsForPlayer.size() <= key) {
                if (Civs.perm.has(player, entry.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }
}

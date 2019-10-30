package org.redcastlemedia.multitallented.civs.regions.effects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.items.UnloadedInventoryHandler;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionUpkeep;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.DebugLogger;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.*;

public class WarehouseEffect implements Listener, RegionCreatedListener {
    public static final String KEY = "warehouse";
    public HashMap<Region, ArrayList<InventoryLocation>> invs = new HashMap<>();
    public HashMap<Region, HashMap<String, Inventory>> availableItems = new HashMap<>();
    private static WarehouseEffect instance = null;

    public static WarehouseEffect getInstance() {
        if (instance == null) {
            new WarehouseEffect();
        }
        return instance;
    }

    public WarehouseEffect() {
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
        instance = this;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.CHEST) {
            return;
        }

        Location l = Region.idToLocation(Region.blockLocationToString(event.getBlock().getLocation()));
        Region r = RegionManager.getInstance().getRegionAt(l);
        if (r == null) {
            return;
        }

        if (!r.getEffects().containsKey(KEY)) {
            return;
        }


        File dataFolder = new File(Civs.getInstance().getDataFolder(), "regions");
        if (!dataFolder.exists()) {
            return;
        }
        File dataFile = new File(dataFolder, r.getId() + ".yml");
        if (!dataFile.exists()) {
            return;
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(dataFile);
            List<String> locationList = config.getStringList("chests");
            locationList.add(Region.locationToString(l));
            config.set("chests", locationList);
            config.save(dataFile);
        } catch (Exception e) {
            Civs.logger.warning("Unable to save new chest for " + r.getId() + ".yml");
            return;
        }

        if (!invs.containsKey(r)) {
            invs.put(r, new ArrayList<>());
        }
        invs.get(r).add(new InventoryLocation(l, ((Chest) event.getBlock().getState()).getBlockInventory()));
    }


    @Override
    public void regionCreatedHandler(Region r) {
        if (!r.getEffects().containsKey(KEY)) {
            return;
        }

        ArrayList<InventoryLocation> chests = new ArrayList<>();
        chests.add(new InventoryLocation(r.getLocation(),
                ((Chest) r.getLocation().getBlock().getState()).getBlockInventory()));

        RegionType rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());
        double lx = Math.floor(r.getLocation().getX()) + 0.4;
        double ly = Math.floor(r.getLocation().getY()) + 0.4;
        double lz = Math.floor(r.getLocation().getZ()) + 0.4;
        double buildRadius = rt.getBuildRadius();

        int x = (int) Math.round(lx - buildRadius);
        int y = (int) Math.round(ly - buildRadius);
        y = y < 0 ? 0 : y;
        int z = (int) Math.round(lz - buildRadius);
        int xMax = (int) Math.round(lx + buildRadius);
        int yMax = (int) Math.round(ly + buildRadius);
        World world = r.getLocation().getWorld();
        if (world != null) {
            yMax = yMax > world.getMaxHeight() - 1 ? world.getMaxHeight() - 1 : yMax;
            int zMax = (int) Math.round(lz + buildRadius);

            for (int i = x; i < xMax; i++) {
                for (int j = y; j < yMax; j++) {
                    for (int k = z; k < zMax; k++) {
                        Block block = world.getBlockAt(i, j, k);

                        if (block.getType() == Material.CHEST) {
                            chests.add(new InventoryLocation(block.getLocation(),
                                    ((Chest) block.getState()).getBlockInventory()));
                        }
                    }
                }
            }
        }


        File dataFolder = new File(Civs.getInstance().getDataFolder(), "regions");
        if (!dataFolder.exists()) {
            return;
        }
        File dataFile = new File(dataFolder, r.getId() + ".yml");
        if (!dataFile.exists()) {
            System.out.println("warehouse region file does not exist");
            System.out.println(r.getId() + ".yml");
            return;
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(dataFile);
            ArrayList<String> locationList = new ArrayList<String>();
            for (InventoryLocation inventoryLocation : chests) {
                locationList.add(Region.blockLocationToString(inventoryLocation.getLocation()));
            }
            config.set("chests", locationList);
            config.save(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
            Civs.logger.warning("Unable to save new chest for " + r.getId() + ".yml");
            return;
        }
        checkExcessChests(r);
    }

    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        availableItems.remove(event.getRegion());
        invs.remove(event.getRegion());
    }

    public void refreshChest(Region region, Location location) {
        Block block = location.getBlock();
        if (block.getType() != Material.CHEST) {
            return;
        }
        Chest state = null;
        try {
            state = (Chest) block.getState();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        refreshChest(region, location, state);
    }

    public void refreshChest(Region region, Location location, Chest chest) {
        if (Civs.getInstance() == null) {
            return;
        }
        if (chest == null) {
            availableItems.get(region).remove(Region.locationToString(location));
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (!availableItems.containsKey(region)) {
                    return;
                }
                if (Util.isChestEmpty(chest.getBlockInventory())) {
                    availableItems.get(region).remove(Region.locationToString(location));
                } else {
                    if (!availableItems.containsKey(region)) {
                        availableItems.put(region, new HashMap<>());
                    }
                    availableItems.get(region).put(Region.locationToString(location), chest.getBlockInventory());
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onCustomEvent(RegionTickEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }
        //declarations
        Region r            = event.getRegion();
        Location l          = r.getLocation();

        checkExcessChests(r);

        HashSet<Region> deliverTo = new HashSet<>();
        //Check if any regions nearby need items
        Town town = TownManager.getInstance().getTownAt(r.getLocation());
        if (town == null) {
            return;
        }
        for (Region re : TownManager.getInstance().getContainingRegions(town.getName())) {
            if (re.getFailingUpkeeps().isEmpty()) {
                continue;
            }
            boolean hasMember = false;
            for (UUID uuid : r.getOwners()) {
                if (re.getOwners().isEmpty() || !re.getOwners().contains(uuid)) {
                    continue;
                }
                hasMember = true;
                break;
            }
            if (!hasMember) {
                for (UUID uuid : r.getRawPeople().keySet()) {
                    if (re.getOwners().isEmpty() || !re.getOwners().contains(uuid)) {
                        continue;
                    }
                    hasMember = true;
                    break;
                }
            }
            if (!hasMember) {
                continue;
            }
            deliverTo.add(re);
        }
        for (Region re : deliverTo) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(re.getType());
            if (regionType == null) {
                continue;
            }
            List<List<CVItem>> missingItems = getMissingItems(regionType, re.getFailingUpkeeps());

            if (missingItems.isEmpty()) {
                continue;
            }
            moveNeededItems(r, re, missingItems);
        }

        // To avoid cluttering the center chest, move items to available chests
        tidyCentralChest(r, UnloadedInventoryHandler.getInstance().getChestInventory(l), l);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Region r = RegionManager.getInstance().getRegionAt(event.getBlock().getLocation());
        if (r == null) {
            return;
        }
        boolean deletedSomething = false;

        if (invs.containsKey(r)) {
            deletedSomething = invs.get(r).remove(event.getBlock().getLocation());
        }
        if (availableItems.containsKey(r)) {
            deletedSomething = availableItems.get(r).remove(Region.locationToString(event.getBlock().getLocation())) != null ||
                    deletedSomething;
        }

        //Remove excess chests from the data file
        deletefromfile: if (deletedSomething) {
            File dataFolder = new File(Civs.getInstance().getDataFolder(), "regions");
            if (!dataFolder.exists()) {
                break deletefromfile;
            }
            File dataFile = new File(dataFolder, r.getId() + ".yml");
            if (!dataFile.exists()) {
                break deletefromfile;
            }
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(dataFile);
                ArrayList<String> locationList = new ArrayList<String>();
                for (InventoryLocation inventoryLocation : invs.get(r)) {
                    locationList.add(Region.blockLocationToString(inventoryLocation.getLocation()));
                }
                config.set("chests", locationList);
                config.save(dataFile);
            } catch (Exception e) {
                Civs.logger.warning("Unable to save new chest for " + r.getId() + ".yml");
                return;
            }
        }
    }

    private void checkExcessChests(Region r) {
        if ((!invs.containsKey(r) || invs.get(r).isEmpty()) && Civs.getInstance() != null) {
            // Since there isn't a cached list of chests for this warehouse, retrieve it from the data file
            File dataFolder = new File(Civs.getInstance().getDataFolder(), "regions");
            if (!dataFolder.exists()) {
                return;
            }
            File dataFile = new File(dataFolder, r.getId() + ".yml");
            if (!dataFile.exists()) {
                Civs.logger.severe("Data file not found " + r.getId() + ".yml");
                return;
            }
            FileConfiguration config = new YamlConfiguration();
            try {
                config.load(dataFile);
                ArrayList<InventoryLocation> tempLocations = processLocationList(config.getStringList("chests"));
                for (InventoryLocation inventoryLocation : tempLocations) {
                    if (!availableItems.containsKey(r)) {
                        availableItems.put(r, new HashMap<>());
                    }
                    availableItems.get(r).put(Region.locationToString(inventoryLocation.getLocation()),
                            inventoryLocation.getInventory());
                }
                invs.put(r, tempLocations);
            } catch (Exception e) {
                Civs.logger.warning("Unable to load chests from " + r.getId() + ".yml");
                e.printStackTrace();
                return;
            }
        }
    }

    private void tidyCentralChest(Region r, Inventory inventory, Location l) {
        int firstEmpty = inventory.firstEmpty();
        if (firstEmpty < 9) {
            return;
        }

        for (InventoryLocation inventoryLocation : invs.get(r)) {
            if (inventoryLocation.getLocation().equals(l)) {
                continue;
            }
            int currentChestFirstEmpty = inventoryLocation.getInventory().firstEmpty();
            int i = 0;
            while (currentChestFirstEmpty > -1 && i < 40) {
                //Are we done moving things out of the chest?
                ItemStack[] contents = inventory.getContents();
                ItemStack is = null;
                for (int k = contents.length; k > 0; k--) {
                    if (contents[k-1] != null && contents[k-1].getType() != Material.AIR) {
                        is = contents[k-1];
                        break;
                    }
                }
                if (is == null) {
                    return;
                }

                //Move the items
                CVItem item = CVItem.createFromItemStack(is);
                List<CVItem> tempList = new ArrayList<>();
                tempList.add(item);
                List<List<CVItem>> temptemp = new ArrayList<>();
                temptemp.add(tempList);
                Util.removeItems(temptemp, inventory);
                refreshChest(r, l);
                ArrayList<ItemStack> remainingItems = Util.addItems(temptemp, inventoryLocation.getInventory());
                refreshChest(r, inventoryLocation.getLocation());
                for (ItemStack iss : remainingItems) {
                    inventory.addItem(iss);
                }
                //currentChest.getBlockInventory().addItem(is);
                currentChestFirstEmpty = inventoryLocation.getInventory().firstEmpty();
                i++;
            }
        }
    }

    private ArrayList<InventoryLocation> processLocationList(List<String> input) {
        ArrayList<InventoryLocation> tempList = new ArrayList<>();
        for (String s : input) {
            Location location = Region.idToLocation(s);
            if (location != null && location.getBlock().getType() == Material.CHEST) {
                Inventory inventory = ((Chest) location.getBlock().getState()).getBlockInventory();
                tempList.add(new InventoryLocation(location, inventory));
            }
        }
        return tempList;
    }

    private HashSet<HashSet<CVItem>> convertToHashSet(List<List<CVItem>> input) {
        HashSet<HashSet<CVItem>> returnMe = new HashSet<>();
        for (List<CVItem> reqs : input) {
            HashSet<CVItem> tempSet = new HashSet<>();
            for (CVItem item : reqs) {
                tempSet.add(item.clone());
            }
            returnMe.add(tempSet);
        }
        return returnMe;
    }

    private void moveNeededItems(Region region, Region destination, List<List<CVItem>> neededItems) {
        if (!availableItems.containsKey(region)) {
            return;
        }
        Inventory destinationInventory = UnloadedInventoryHandler.getInstance().getChestInventory(destination.getLocation());

        if (destinationInventory.firstEmpty() < 0 || destinationInventory.firstEmpty() > destinationInventory.getSize() - 4) {
            return;
        }

        HashMap<InventoryLocation, HashMap<Integer, ItemStack>> itemsToMove = new HashMap<>();

        HashSet<String> removeTheseChests = new HashSet<>();
        HashSet<HashSet<CVItem>> req = convertToHashSet(neededItems);
        try {
            nextItem: for (String locationString : availableItems.get(region).keySet()) {
                Inventory inv = availableItems.get(region).get(locationString);
                InventoryLocation inventoryLocation = new InventoryLocation(Region.idToLocation(locationString), inv);
                if (Util.isChestEmpty(inv)) {
                    removeTheseChests.add(locationString);
                    continue;
                }
                int i = 0;
                for (ItemStack is : inv.getContents()) {
                    outer: for (HashSet<CVItem> orReqs : new HashSet<>(req)) {
                        for (CVItem orReq : new HashSet<>(orReqs)) {
                            if (is != null && is.getType() != Material.AIR &&
                                    orReq.equivalentItem(is, orReq.getDisplayName() != null)) {

                                if (!itemsToMove.containsKey(inventoryLocation)) {
                                    itemsToMove.put(inventoryLocation, new HashMap<>());
                                }

                                ItemStack nIS = CVItem.createFromItemStack(is).createItemStack();
                                if (orReq.getQty() > is.getAmount()) {
                                    orReq.setQty(orReq.getQty() - is.getAmount());
                                    itemsToMove.get(inventoryLocation).put(i, nIS);
                                } else {
                                    if (orReq.getQty() < is.getAmount()) {
                                        nIS.setAmount(is.getAmount() - orReq.getQty());
                                    }
                                    itemsToMove.get(inventoryLocation).put(i, nIS);

                                    orReqs.remove(orReq);
                                    if (orReqs.isEmpty()) {
                                        req.remove(orReqs);
                                    }
                                }
                                continue nextItem;
                            }
                            i++;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Civs.logger.warning("error moving items from warehouse");
        }
        for (String locationString : removeTheseChests) {
            availableItems.get(region).remove(locationString);
        }


        //move items from warehouse to needed region
        outerNew: for (InventoryLocation inventoryLocation : itemsToMove.keySet()) {
            for (Integer i : itemsToMove.get(inventoryLocation).keySet()) {
                ItemStack moveMe = itemsToMove.get(inventoryLocation).get(i);
                inventoryLocation.getInventory().removeItem(moveMe);
                refreshChest(region, inventoryLocation.getLocation());
                if (ConfigManager.getInstance().isDebugLog()) {
                    DebugLogger.inventoryModifications++;
                }
                destinationInventory.addItem(moveMe);
                RegionManager.getInstance().removeCheckedRegion(destination);
                for (Integer failingUpkeepIndex : new HashSet<>(destination.getFailingUpkeeps())) {
                    if (destination.hasUpkeepItems(failingUpkeepIndex, false)) {
                        destination.getFailingUpkeeps().remove(failingUpkeepIndex);
                    }
                }

                if (destinationInventory.firstEmpty() < 0) {
                    break outerNew;
                }
            }
        }
    }

    private List<List<CVItem>> cloneLists(List<List<CVItem>> input) {
        List<List<CVItem>> req = new ArrayList<>();
        for (List<CVItem> inputList : input) {
            List<CVItem> tempList = new ArrayList<>();
            for (CVItem cvItem : inputList) {
                tempList.add(cvItem.clone());
            }
            req.add(tempList);
        }
        return req;
    }

    private List<List<CVItem>> getMissingItems(RegionType rt, HashSet<Integer> failingUpkeeps) {
        List<List<CVItem>> req = new ArrayList<>();

        for (Integer index : failingUpkeeps) {
            RegionUpkeep regionUpkeep = rt.getUpkeeps().get(index);
            req.addAll(cloneLists(regionUpkeep.getInputs()));
        }
        return req;
    }

    protected void putInventoryLocation(Region region, Location location, Inventory inventory) {
        if (invs.get(region) != null) {
            invs.get(region).add(new InventoryLocation(location, inventory));
        } else {
            ArrayList<InventoryLocation> inventoryLocations = new ArrayList<>();
            inventoryLocations.add(new InventoryLocation(location, inventory));
            invs.put(region, inventoryLocations);
        }
    }

    @Getter
    @Setter
    private static class InventoryLocation {
        private Location location;
        private Inventory inventory;

        public InventoryLocation(Location location, Inventory inventory) {
            this.location = location;
            this.inventory = inventory;
        }
    }
}

package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.CVInventory;
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

@CivsSingleton
public class WarehouseEffect implements Listener, RegionCreatedListener {
    public static final String KEY = "warehouse";
    public Map<Region, List<CVInventory>> invs = new HashMap<>();
    public Map<Region, HashMap<String, CVInventory>> availableItems = new HashMap<>();
    private static WarehouseEffect instance = null;

    public static WarehouseEffect getInstance() {
        if (instance == null) {
            instance = new WarehouseEffect();
            Bukkit.getPluginManager().registerEvents(instance, Civs.getInstance());
        }
        return instance;
    }

    public WarehouseEffect() {
        RegionManager.getInstance().addRegionCreatedListener(KEY, this);
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


        r.getChests().add(Region.locationToString(l));
        RegionManager.getInstance().saveRegion(r);

        if (!invs.containsKey(r)) {
            invs.put(r, new ArrayList<>());
        }
        CVInventory cvInventory = UnloadedInventoryHandler.getInstance().getChestInventory(event.getBlockPlaced().getLocation());
        invs.get(r).add(cvInventory);
    }


    @Override
    public void regionCreatedHandler(Region r) {
        if (!r.getEffects().containsKey(KEY)) {
            return;
        }

        ArrayList<CVInventory> chests = new ArrayList<>();
        chests.add(UnloadedInventoryHandler.getInstance().getChestInventory(r.getLocation()));

        RegionType rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());
        double lx = Math.floor(r.getLocation().getX()) + 0.4;
        double ly = Math.floor(r.getLocation().getY()) + 0.4;
        double lz = Math.floor(r.getLocation().getZ()) + 0.4;
        double buildRadius = rt.getBuildRadius();

        int x = (int) Math.round(lx - buildRadius);
        int y = (int) Math.round(ly - buildRadius);
        y = Math.max(y, 0);
        int z = (int) Math.round(lz - buildRadius);
        int xMax = (int) Math.round(lx + buildRadius);
        int yMax = (int) Math.round(ly + buildRadius);
        World world = r.getLocation().getWorld();
        if (world != null) {
            yMax = Math.min(yMax, world.getMaxHeight() - 1);
            int zMax = (int) Math.round(lz + buildRadius);

            for (int i = x; i < xMax; i++) {
                for (int j = y; j < yMax; j++) {
                    for (int k = z; k < zMax; k++) {
                        Block block = world.getBlockAt(i, j, k);

                        if (block.getType() == Material.CHEST) {
                            chests.add(UnloadedInventoryHandler.getInstance().getChestInventory(block.getLocation()));
                        }
                    }
                }
            }
        }

        for (CVInventory cvInventory : chests) {
            r.getChests().add(Region.blockLocationToString(cvInventory.getLocation()));
        }
        RegionManager.getInstance().saveRegion(r);
        checkExcessChests(r);
    }

    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        availableItems.remove(event.getRegion());
        invs.remove(event.getRegion());
    }

    public void refreshChest(Region region, Location location) {
        refreshChest(region, location,
                UnloadedInventoryHandler.getInstance().getChestInventory(location));
    }

    public void refreshChest(Region region, Location location, CVInventory cvInventory) {
        if (Civs.getInstance() == null) {
            return;
        }
        if (cvInventory == null || !cvInventory.isValid()) {
            availableItems.get(region).remove(Region.locationToString(location));
            return;
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> {
            if (!availableItems.containsKey(region)) {
                return;
            }
            if (Util.isChestEmpty(cvInventory)) {
                availableItems.get(region).remove(Region.locationToString(location));
            } else {
                if (!availableItems.containsKey(region)) {
                    availableItems.put(region, new HashMap<>());
                }
                availableItems.get(region).put(Region.locationToString(location), cvInventory);
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
        populateRegionsNeedingDelivery(r, deliverTo, town);
        for (Region re : deliverTo) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(re.getType());
            if (regionType == null) {
                continue;
            }
            List<List<CVItem>> missingItems = getMissingItems(regionType, re.getFailingUpkeeps());

            if (!missingItems.isEmpty()) {
                moveNeededItems(r, re, missingItems);
            }
        }

        if (RegionManager.getInstance().hasRegionChestChanged(r)) {
            tidyCentralChest(r, UnloadedInventoryHandler.getInstance().getChestInventory(l), l);
        }
    }

    private void populateRegionsNeedingDelivery(Region r, HashSet<Region> deliverTo, Town town) {
        for (Region re : TownManager.getInstance().getContainingRegions(town.getName())) {
            RegionType rt = (RegionType) ItemManager.getInstance().getItemType(re.getType());
            if (re.getFailingUpkeeps().isEmpty() || rt.getUpkeeps().isEmpty() || !re.isWarehouseEnabled()) {
                continue;
            }
            if (!regionOwnerIsMemberOfWarehouse(r, re)) {
                continue;
            }
            deliverTo.add(re);
        }
    }

    private boolean regionOwnerIsMemberOfWarehouse(Region r, Region re) {
        for (UUID uuid : r.getOwners()) {
            if (!re.getOwners().isEmpty() && re.getOwners().contains(uuid)) {
                return true;
            }
        }
        for (UUID uuid : r.getRawPeople().keySet()) {
            if (!re.getOwners().isEmpty() && re.getOwners().contains(uuid)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Region r = RegionManager.getInstance().getRegionAt(event.getBlock().getLocation());
        if (r == null) {
            return;
        }
        boolean deletedSomething = false;

        if (invs.containsKey(r)) {
            CVInventory cvInventory = UnloadedInventoryHandler.getInstance().getChestInventory(event.getBlock().getLocation());
            deletedSomething = invs.get(r).remove(cvInventory);
        }
        if (availableItems.containsKey(r)) {
            deletedSomething = availableItems.get(r).remove(Region.locationToString(event.getBlock().getLocation())) != null ||
                    deletedSomething;
        }

        if (deletedSomething) {
            removeExcessChestFromFiles(r);
        }
    }

    private void removeExcessChestFromFiles(Region r) {
        r.getChests().clear();
        ArrayList<String> locationList = new ArrayList<>();
        for (CVInventory inventoryLocation : invs.get(r)) {
            locationList.add(Region.blockLocationToString(inventoryLocation.getLocation()));
        }
        r.getChests().addAll(locationList);
        RegionManager.getInstance().saveRegion(r);
    }

    private void checkExcessChests(Region r) {
        if ((!invs.containsKey(r) || invs.get(r).isEmpty()) && Civs.getInstance() != null) {
            List<CVInventory> tempLocations = processLocationList(r.getChests());
            for (CVInventory inventoryLocation : tempLocations) {
                if (!availableItems.containsKey(r)) {
                    availableItems.put(r, new HashMap<>());
                }
                availableItems.get(r).put(Region.locationToString(inventoryLocation.getLocation()),
                        inventoryLocation);
            }
            invs.put(r, tempLocations);
        }
    }

    private void tidyCentralChest(Region r, CVInventory inventory, Location l) {
        if (inventory == null) {
            RegionManager.getInstance().addCheckedRegion(r);
            return;
        }
        int firstEmpty = inventory.firstEmpty();
        if (firstEmpty < 9) {
            RegionManager.getInstance().addCheckedRegion(r);
            return;
        }

        for (CVInventory inventoryLocation : invs.get(r)) {
            if (inventoryLocation.getLocation().equals(l)) {
                continue;
            }
            int currentChestFirstEmpty = inventoryLocation.firstEmpty();
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
                ArrayList<ItemStack> remainingItems = Util.addItems(temptemp, inventoryLocation);
                refreshChest(r, inventoryLocation.getLocation());
                inventory.addItem(remainingItems.toArray(new ItemStack[0]));
                currentChestFirstEmpty = inventoryLocation.firstEmpty();
                i++;
            }
        }
    }

    private List<CVInventory> processLocationList(List<String> input) {
        ArrayList<CVInventory> tempList = new ArrayList<>();
        for (String s : input) {
            Location location = Region.idToLocation(s);
            CVInventory cvInventory = UnloadedInventoryHandler.getInstance().getChestInventory(location);
            tempList.add(cvInventory);
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
        CVInventory destinationInventory = UnloadedInventoryHandler.getInstance().getChestInventory(destination.getLocation());

        if (destinationInventory == null || destinationInventory.firstEmpty() < 0 ||
                destinationInventory.firstEmpty() > destinationInventory.getSize() - 4) {
            return;
        }

        HashMap<CVInventory, HashMap<Integer, ItemStack>> itemsToMove = new HashMap<>();

        HashSet<String> removeTheseChests = new HashSet<>();
        HashSet<HashSet<CVItem>> req = convertToHashSet(neededItems);
        try {
            nextItem: for (String locationString : availableItems.get(region).keySet()) {
                CVInventory inv = availableItems.get(region).get(locationString);
                if (Util.isChestEmpty(inv)) {
                    removeTheseChests.add(locationString);
                    continue;
                }
                int i = 0;
                for (ItemStack is : inv.getContents()) {
                    for (HashSet<CVItem> orReqs : new HashSet<>(req)) {
                        for (CVItem orReq : new HashSet<>(orReqs)) {
                            if (is != null && is.getType() != Material.AIR &&
                                    orReq.equivalentItem(is, orReq.getDisplayName() != null)) {

                                if (!itemsToMove.containsKey(inv)) {
                                    itemsToMove.put(inv, new HashMap<>());
                                }

                                ItemStack nIS = CVItem.createFromItemStack(is).createItemStack();
                                if (orReq.getQty() > is.getAmount()) {
                                    orReq.setQty(orReq.getQty() - is.getAmount());
                                    itemsToMove.get(inv).put(i, nIS);
                                } else {
                                    if (orReq.getQty() < is.getAmount()) {
                                        nIS.setAmount(is.getAmount() - orReq.getQty());
                                    }
                                    itemsToMove.get(inv).put(i, nIS);

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


        moveItemsFromWarehouseToRegion(region, destination, destinationInventory, itemsToMove);
    }

    private void moveItemsFromWarehouseToRegion(Region region, Region destination,
                                                CVInventory destinationInventory,
                                                HashMap<CVInventory, HashMap<Integer, ItemStack>> itemsToMove) {
        for (CVInventory inventoryLocation : itemsToMove.keySet()) {
            for (Integer i : itemsToMove.get(inventoryLocation).keySet()) {
                ItemStack moveMe = itemsToMove.get(inventoryLocation).get(i);
                inventoryLocation.removeItem(moveMe);
                refreshChest(region, inventoryLocation.getLocation());
                if (ConfigManager.getInstance().isDebugLog()) {
                    DebugLogger.inventoryModifications++;
                }
                destinationInventory.addItem(moveMe);
                RegionManager.getInstance().removeCheckedRegion(destination);
                for (Integer failingUpkeepIndex : new HashSet<>(destination.getFailingUpkeeps())) {
                    if (destination.hasUpkeepItems(failingUpkeepIndex, true)) {
                        destination.getFailingUpkeeps().remove(failingUpkeepIndex);
                        break;
                    }
                }

                if (destinationInventory.firstEmpty() < 0) {
                    return;
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

    protected void putInventoryLocation(Region region, CVInventory inventory) {
        if (invs.get(region) != null) {
            invs.get(region).add(inventory);
        } else {
            ArrayList<CVInventory> inventoryLocations = new ArrayList<>();
            inventoryLocations.add(inventory);
            invs.put(region, inventoryLocations);
        }
    }
}

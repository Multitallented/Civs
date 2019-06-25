package org.redcastlemedia.multitallented.civs.regions.effects;

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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionUpkeep;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.*;

public class WarehouseEffect implements Listener, RegionCreatedListener {
    public static final String KEY = "warehouse";
    public HashMap<Region, ArrayList<Location>> invs = new HashMap<>();
    public HashMap<Region, HashMap<Location, Chest>> availableItems = new HashMap<>();
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
            invs.put(r, new ArrayList<Location>());
        }
        invs.get(r).add(l);
    }


    @Override
    public void regionCreatedHandler(Region r) {
        if (!r.getEffects().containsKey(KEY)) {
            return;
        }

        ArrayList<Location> chests = new ArrayList<>();
        chests.add(r.getLocation());

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
                            chests.add(block.getLocation());
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
            return;
        }
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(dataFile);
            ArrayList<String> locationList = new ArrayList<String>();
            for (Location l : chests) {
                locationList.add(Region.blockLocationToString(l));
            }
            config.set("chests", locationList);
            config.save(dataFile);
        } catch (Exception e) {
            Civs.logger.warning("Unable to save new chest for " + r.getId() + ".yml");
            return;
        }
        invs.put(r, chests);
    }

    @EventHandler
    public void onRegionDestroyed(RegionDestroyedEvent event) {
        availableItems.remove(event.getRegion());
        invs.remove(event.getRegion());
    }

    @EventHandler
    public void onCustomEvent(RegionTickEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }
        //declarations
        Region r            = event.getRegion();
        Location l          = r.getLocation();
        RegionType rt       = (RegionType) ItemManager.getInstance().getItemType(r.getType());
        Chest rChest        = null;


        if (rt == null) {
            return;
        }

        // Is there a center chest?
        if (r.getLocation().getBlock().getType() != Material.CHEST) {
            return;
        } else {
            rChest = (Chest) r.getLocation().getBlock().getState();
        }

        // Check for excess chests
        if (!invs.containsKey(r) && Civs.getInstance() != null) {
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
                ArrayList<Location> tempLocations = processLocationList(config.getStringList("chests"));
                for (Location lo : tempLocations) {
                    Block block = lo.getBlock();
                    if (block.getType() != Material.CHEST) {
                        continue;
                    }
                    if (!availableItems.containsKey(r)) {
                        availableItems.put(r, new HashMap<>());
                    }
                    BlockState blockState;
                    try {
                        blockState = lo.getBlock().getState();
                        if (!(blockState instanceof Chest)) {
                            continue;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                    availableItems.get(r).put(lo, (Chest) blockState);
                }
                invs.put(r, tempLocations);
            } catch (Exception e) {
                Civs.logger.warning("Unable to load chests from " + r.getId() + ".yml");
                e.printStackTrace();
                return;
            }
        } else {
            // Find if any chests have been broken and remove them from the data file
            ArrayList<Location> removeMe = new ArrayList<>();
            for (Location lo : invs.get(r)) {
                if (lo.getBlock().getType() != Material.CHEST) {
                    removeMe.add(lo);
                    if (availableItems.containsKey(r)) {
                        availableItems.get(r).remove(lo);
                    }
                    continue;
                }
            }

            //Remove excess chests from the data file
            deletefromfile: if (!removeMe.isEmpty()) {
                for (Location lo : removeMe) {
                    invs.get(r).remove(lo);
                }
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
                    for (Location loc : invs.get(r)) {
                        locationList.add(loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ());
                    }
                    config.set("chests", locationList);
                    config.save(dataFile);
                } catch (Exception e) {
                    Civs.logger.warning("Unable to save new chest for " + r.getId() + ".yml");
                    break deletefromfile;
                }
            }
        }

        HashSet<Region> deliverTo = new HashSet<>();
        //Check if any regions nearby need items
        Town town = TownManager.getInstance().getTownAt(r.getLocation());
        if (town == null) {
            return;
        }
        for (Region re : TownManager.getInstance().getContainingRegions(town.getName())) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(re.getType());
            boolean hasMember = false;
            for (UUID uuid : r.getOwners()) {
                if (re.getOwners().isEmpty() || !re.getOwners().contains(uuid)) {
                    continue;
                }
                hasMember = true;
                break;
            }
            if (!hasMember) {
                for (UUID uuid : r.getPeople().keySet()) {
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
            for (int i=0; i<regionType.getUpkeeps().size(); i++) {
                if (!re.hasUpkeepItems(i, false)) {
                    deliverTo.add(re);
                    break;
                }
            }
        }
        for (Region re : deliverTo) {
            if (re.getLocation().getBlock().getType() != Material.CHEST) {
                continue;
            }
            Chest chest = (Chest) re.getLocation().getBlock().getState();
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(re.getType());
            if (regionType == null) {
                continue;
            }
            List<List<CVItem>> missingItems = getMissingItems(regionType, chest);

            if (missingItems.isEmpty()) {
                continue;
            }
            moveNeededItems(r, re, missingItems);
        }

        // To avoid cluttering the center chest, move items to available chests
        tidyCentralChest(r, rChest);
    }

    private void tidyCentralChest(Region r, Chest rChest) {
        int firstEmpty = rChest.getBlockInventory().firstEmpty();
        if (firstEmpty < 9) {
            return;
        }

        for (Chest currentChest : availableItems.get(r).values()) {
            if (currentChest.equals(rChest)) {
                continue;
            }
            int currentChestFirstEmpty = currentChest.getBlockInventory().firstEmpty();
            int i = 0;
            while (currentChestFirstEmpty > -1 && i < 40) {
                //Are we done moving things out of the chest?
                ItemStack[] contents = rChest.getBlockInventory().getContents();
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
                Util.removeItems(temptemp, rChest.getBlockInventory());
                ArrayList<ItemStack> remainingItems = Util.addItems(temptemp, currentChest.getBlockInventory());
                for (ItemStack iss : remainingItems) {
                    rChest.getBlockInventory().addItem(iss);
                }
                //currentChest.getBlockInventory().addItem(is);
                currentChestFirstEmpty = currentChest.getBlockInventory().firstEmpty();
                i++;
            }
        }
    }

    private ArrayList<Location> processLocationList(List<String> input) {
        ArrayList<Location> tempList = new ArrayList<Location>();
        for (String s : input) {
            Location location = Region.idToLocation(s);
            if (location != null) {
                tempList.add(location);
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
        Chest destinationChest = null;

        try {
            destinationChest = (Chest) destination.getLocation().getBlock().getState();
        } catch (Exception e) {
            return;
        }
        if (destinationChest.getBlockInventory().firstEmpty() < 0) {
            return;
        }

        HashMap<Chest, HashMap<Integer, ItemStack>> itemsToMove = new HashMap<>();


        HashSet<HashSet<CVItem>> req = convertToHashSet(neededItems);
        outer2: for (Iterator<HashSet<CVItem>> it = req.iterator(); it.hasNext();) {
            HashSet<CVItem> orReqs = it.next();
            outer1: for (Iterator<CVItem> its = orReqs.iterator(); its.hasNext();) {
                CVItem orReq = its.next();
                for (Chest chest : availableItems.get(region).values()) {
                    try {
                        Inventory inv = chest.getBlockInventory();

                        int i = 0;
                        for (ItemStack is : inv.getContents()) {
                            if (is != null && is.getType() != Material.AIR && orReq.equivalentItem(is, orReq.getDisplayName() != null)) {

                                if (!itemsToMove.containsKey(chest)) {
                                    itemsToMove.put(chest, new HashMap<Integer, ItemStack>());
                                }

                                ItemStack nIS = CVItem.createFromItemStack(is).createItemStack();
                                if (orReq.getQty() > is.getAmount()) {
                                    orReq.setQty(orReq.getQty() - is.getAmount());
                                    itemsToMove.get(chest).put(i, nIS);
                                } else {
                                    if (orReq.getQty() < is.getAmount()) {
                                        nIS.setAmount(is.getAmount() - orReq.getQty());
                                    }
                                    itemsToMove.get(chest).put(i, nIS);

                                    its.remove();
                                    if (orReqs.isEmpty()) {
                                        it.remove();
                                        continue outer2;
                                    }

                                    continue outer1;
                                }
                            }
                            i++;
                        }
                    } catch (Exception e) {
                        Civs.logger.warning("error moving items from warehouse");
                    }
                }
            }
        }

        //move items from warehouse to needed region
        outerNew: for (Chest chest : itemsToMove.keySet()) {
            for (Integer i : itemsToMove.get(chest).keySet()) {
                ItemStack moveMe = itemsToMove.get(chest).get(i);
                chest.getBlockInventory().removeItem(moveMe);
                destinationChest.getBlockInventory().addItem(moveMe);
                RegionManager.getInstance().removeCheckedRegion(destination);

                if (destinationChest.getBlockInventory().firstEmpty() < 0) {
                    break outerNew;
                }
            }
        }
    }

    private List<List<CVItem>> getMissingItems(RegionType rt, Chest chest) {
        List<List<CVItem>> req = new ArrayList<>();
        for (RegionUpkeep regionUpkeep : rt.getUpkeeps()) {
            for (List<CVItem> list : regionUpkeep.getInputs()) {
                ArrayList<CVItem> tempList = new ArrayList<>();
                for (CVItem item : list) {
                    tempList.add(item.clone());
                }
                req.add(tempList);
            }
            for (List<CVItem> list : regionUpkeep.getReagents()) {
                ArrayList<CVItem> tempList = new ArrayList<>();
                for (CVItem item : list) {
                    tempList.add(item.clone());
                }
                req.add(tempList);
            }
        }

        Inventory inv = chest.getBlockInventory();

        HashMap<Integer, List<CVItem>> removeMe = new HashMap<>();
        int k = 0;
        for (List<CVItem> orReqs : req) {

            for (CVItem orReq : orReqs) {

                for (ItemStack iss : inv.getContents()) {
                    if (iss == null) {
                        continue;
                    }

                    if (orReq.equivalentItem(iss)) {
                        if (orReq.getQty() - iss.getAmount() > 0) {
                            orReq.setQty(orReq.getQty() - iss.getAmount());
                        } else {

                            if (!removeMe.containsKey(k)) {
                                removeMe.put(k, new ArrayList<CVItem>());
                            }
                            removeMe.get(k).add(orReq);
                            break;
                        }
                    }
                }
            }
            k++;
        }
        List<List<CVItem>> removeLists = new ArrayList<>();
        for (Integer i : removeMe.keySet()) {
            for (CVItem item : removeMe.get(i)) {
                req.get(i).remove(item);
            }
            if (removeMe.get(i).isEmpty()) {
                removeLists.add(removeMe.get(i));
            }
        }

        for (List<CVItem> orReqs : removeLists) {
            req.remove(orReqs);
        }
        return req;
    }
}

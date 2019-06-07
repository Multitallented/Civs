package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class RaidPortEffect implements Listener, CreateRegionListener {
    public static String KEY = "raid_port";
    public static String CHARGING_KEY = "charging_raid_port";
    private HashMap<Region, Location> raidLocations = new HashMap<>();

    public RaidPortEffect() {
        RegionManager.getInstance().addCreateRegionListener(KEY, this);
        RegionManager.getInstance().addCreateRegionListener(CHARGING_KEY, this);
    }

    @Override
    public boolean createRegionHandler(Block block, Player player, RegionType rt) {
        Location l = Region.idToLocation(Region.blockLocationToString(block.getLocation()));

        if (!rt.getEffects().containsKey(KEY) && !rt.getEffects().containsKey(CHARGING_KEY)) {
            return true;
        }

        Town town = hasValidSign(l, rt, player.getUniqueId());

        if (town == null) {
            return false;
        }
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        Bukkit.broadcastMessage(Civs.getPrefix() + ChatColor.RED +
                LocaleManager.getInstance().getTranslation(civilian.getLocale(), "raid-porter-warning")
                .replace("$1", player.getDisplayName())
                .replace("$2", rt.getName())
                .replace("$3", town.getName()));
        CVItem raidRemote = CVItem.createCVItemFromString("STICK");
        raidRemote.setDisplayName("Controller " + rt.getName() + " " + Region.locationToString(l));

        l.getWorld().dropItemNaturally(l, raidRemote.createItemStack());
        player.sendMessage(Civs.getPrefix() + ChatColor.RED +
                LocaleManager.getInstance().getTranslation(civilian.getLocale(), "raid-remote")
                .replace("$1", rt.getName()));
        return true;
    }

    private Town hasValidSign(Location l, RegionType rt, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        Block block = l.getBlock().getRelative(BlockFace.UP);
        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return null;
        }

        String stringDistance = rt.getEffects().get(KEY);
        int distance = 200;
        if (stringDistance != null) {
            distance = Integer.parseInt(stringDistance);
        }

        Sign sign = (Sign) state;

        Town town;
        try {
            town = TownManager.getInstance().getTown(sign.getLine(0));
        } catch (Exception e) {
            block.breakNaturally();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "raid-target-lost").replace("$1", sign.getLine(0))
                    .replace("$2", distance + ""));
            return null;
        }
        if (town == null) {
            for (Town currentTown : TownManager.getInstance().getTowns()) {
                if (currentTown.getName().startsWith(sign.getLine(0))) {
                    town = currentTown;
                    break;
                }
            }
            if (town == null) {
                block.breakNaturally();
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "raid-target-lost").replace("$1", sign.getLine(0))
                        .replace("$2", distance + ""));
                return null;
            }
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

        if (townType.getBuildRadius() + distance < l.distance(town.getLocation())) {
            block.breakNaturally();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "raid-target-lost").replace("$1", town.getName())
                    .replace("$2", distance + ""));
            return null;
        }
        return town;
    }

    @EventHandler
    public void onPlayerInRegion(PlayerInRegionEvent event) {
        Region r = event.getRegion();
        Civilian civilian = CivilianManager.getInstance().getCivilian(event.getUuid());
        Player player = Bukkit.getPlayer(event.getUuid());
        if (!r.getEffects().containsKey(KEY)) {
            return;
        }
        if (!r.getLocation().getBlock().getRelative(BlockFace.UP).equals(player.getLocation().getBlock()) &&
                !r.getLocation().getBlock().equals(player.getLocation().getBlock())) {
            return;
        }
        Location l =        r.getLocation();
        RegionManager rm =  RegionManager.getInstance();
        RegionType rt =     (RegionType) ItemManager.getInstance().getItemType(r.getType());

        //Check to see if the Townships has enough reagents
        if (!r.hasUpkeepItems()) {
            return;
        }

        Town town = hasValidSign(l, rt, event.getUuid());
        if (town == null) {
            return;
        }

        Location targetLoc;
        if (!raidLocations.containsKey(r)) {
            targetLoc = findTargetLocation(town);

            if (targetLoc == null) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                        "searching-for-target"));
                return;
            }
        } else {
            targetLoc = raidLocations.get(r);
        }

        if (!isValidTeleportTarget(targetLoc, false)) {
            if (raidLocations.containsKey(r)) {
                raidLocations.remove(r);
            }
            l.getBlock().getRelative(BlockFace.UP).breakNaturally();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "raid-target-blocked"));
            return;
        }

        //Run upkeep but don't need to know if upkeep occured
        r.runUpkeep();
        player.teleport(targetLoc);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "teleported"));
    }

    private Location findTargetLocation(Town town) {
        Set<Region> potentialTargets = TownManager.getInstance().getContainingRegions(town.getName());
        int i = 0;
        for (Region currentRegion : potentialTargets) {
            double rand = Math.random();
            if (i+1 < potentialTargets.size() && rand < (1 / (double) potentialTargets.size())) {
                continue;
            }
            RegionType rt = (RegionType) ItemManager.getInstance().getItemType(currentRegion.getType());
            if (rt == null) {
                continue;
            }

            Location teleportTarget = findSafeTeleportTarget(currentRegion);
            if (teleportTarget != null) {
                return teleportTarget;
            }

            i++;
            if (i > 4) {
                break;
            }
        }
        return null;
    }

    private Location findSafeTeleportTarget(Region region) {
        Location location = region.getLocation();
        World currentWorld = location.getWorld();
        int xMax = (int) location.getX() + 1 + region.getRadiusXP();
        int xMin = (int) location.getX() - region.getRadiusXN();
        int yMax = (int) location.getY() + 1 + region.getRadiusYP();
        int yMin = (int) location.getY() - region.getRadiusYN();
        int zMax = (int) location.getZ() + 1 + region.getRadiusZP();
        int zMin = (int) location.getZ() - region.getRadiusZN();
        yMax = yMax > currentWorld.getMaxHeight() ? currentWorld.getMaxHeight() : yMax;
        yMin = yMin < 0 ? 0 : yMin;

        //Top
        top: {
            int y = yMax;
            for (int x = xMin; x < xMax; x++) {
                for (int z = zMin; z < zMax; z++) {
                    Location locationCheck = location.getWorld().getBlockAt(x, y, z).getLocation();
                    if (isValidTeleportTarget(locationCheck, true)) {
                        locationCheck.setX(Math.floor(locationCheck.getX()) + 0.5);
                        locationCheck.setZ(Math.floor(locationCheck.getZ()) + 0.5);
                        locationCheck.setY(Math.floor(locationCheck.getY()) + 1);
                        return locationCheck;
                    }
                }
            }
        }
        //Left
        left: {
            int x = xMin;
            for (int y = yMin; y < yMax; y++) {
                for (int z = zMin; z < zMax; z++) {
                    Location locationCheck = location.getWorld().getBlockAt(x, y, z).getLocation();
                    if (isValidTeleportTarget(locationCheck, true)) {
                        locationCheck.setX(Math.floor(locationCheck.getX()) + 0.5);
                        locationCheck.setZ(Math.floor(locationCheck.getZ()) + 0.5);
                        locationCheck.setY(Math.floor(locationCheck.getY()) + 1);
                        return locationCheck;
                    }
                }
            }
        }
        //Right
        right: {
            int x = xMax;
            for (int y = yMin; y < yMax; y++) {
                for (int z = zMin; z < zMax; z++) {
                    Location locationCheck = location.getWorld().getBlockAt(x, y, z).getLocation();
                    if (isValidTeleportTarget(locationCheck, true)) {
                        locationCheck.setX(Math.floor(locationCheck.getX()) + 0.5);
                        locationCheck.setZ(Math.floor(locationCheck.getZ()) + 0.5);
                        locationCheck.setY(Math.floor(locationCheck.getY()) + 1);
                        return locationCheck;
                    }
                }
            }
        }
        //Front
        front: {
            int z = zMax;
            for (int y = yMin; y < yMax; y++) {
                for (int x = xMin; x < xMax; x++) {
                    Location locationCheck = location.getWorld().getBlockAt(x, y, z).getLocation();
                    if (isValidTeleportTarget(locationCheck, true)) {
                        locationCheck.setX(Math.floor(locationCheck.getX()) + 0.5);
                        locationCheck.setZ(Math.floor(locationCheck.getZ()) + 0.5);
                        locationCheck.setY(Math.floor(locationCheck.getY()) + 1);
                        return locationCheck;
                    }
                }
            }
        }
        //Back
        back: {
            int z = zMin;
            for (int y = yMin; y < yMax; y++) {
                for (int x = xMin; x < xMax; x++) {
                    Location locationCheck = location.getWorld().getBlockAt(x, y, z).getLocation();
                    if (isValidTeleportTarget(locationCheck, true)) {
                        locationCheck.setX(Math.floor(locationCheck.getX()) + 0.5);
                        locationCheck.setZ(Math.floor(locationCheck.getZ()) + 0.5);
                        locationCheck.setY(Math.floor(locationCheck.getY()) + 1);
                        return locationCheck;
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidTeleportTarget(Location target, boolean preAdjusted) {
        if (!preAdjusted) {

            return !target.getBlock().getType().isSolid() &&
                    !target.getBlock().getRelative(BlockFace.UP).getType().isSolid() &&
                    target.getBlock().getRelative(BlockFace.DOWN).getType().isSolid();
        }
        return !target.getBlock().getRelative(BlockFace.UP).getType().isSolid() &&
                !target.getBlock().getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().isSolid() &&
                target.getBlock().getType().isSolid();
    }

    @EventHandler(ignoreCancelled = true)
    public void onRaidControllerUse(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getClickedBlock() == null) {
            return;
        }

        ItemStack itemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.STICK || !itemInHand.hasItemMeta()) {
            return;
        }
        String[] displayName = ChatColor.stripColor(itemInHand.getItemMeta().getDisplayName()).split(" ");
        if (!displayName[0].equals("Controller") || displayName.length < 3) {
            return;
        }

        Player player = event.getPlayer();
        Region r;
        RegionType rt;
        RegionManager rm = RegionManager.getInstance();
        try {
            r = rm.getRegionAt(Region.idToLocation(displayName[2]));
            if (r == null) {
                return;
            }
            rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());
            if (rt == null) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        if (!r.getEffects().containsKey(KEY)) {
            return;
        }

//        int distance = Integer.parseInt(r.getEffects().get(KEY));
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        Location targetLoc = event.getClickedBlock().getLocation();
        if (raidLocations.get(r) != null && raidLocations.get(r).equals(targetLoc)) {
            return;
        }
        if (!isValidTeleportTarget(targetLoc, true)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "port-not-found"));
            return;
        }

        Block block = r.getLocation().getBlock().getRelative(BlockFace.UP);
        if (block.getType() != Material.WALL_SIGN) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "raid-sign"));
            event.setCancelled(true);
            return;
        }
        Sign sign;
        try {
            sign = (Sign) block.getState();
        } catch (Exception e) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "raid-sign"));
            event.setCancelled(true);
            return;
        }
        Town town = TownManager.getInstance().getTownAt(targetLoc);
        if (town == null || !town.getName().equals(sign.getLine(0))) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "target-inside-town").replace("$1", sign.getLine(0)));
            event.setCancelled(true);
            return;
        }

        if (rm.getRegionAt(targetLoc) != null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "raid-target-inside-region"));
            event.setCancelled(true);
            return;
        }

        targetLoc.setX(Math.floor(targetLoc.getX()) + 0.5);
        targetLoc.setZ(Math.floor(targetLoc.getZ()) + 0.5);
        targetLoc.setY(Math.floor(targetLoc.getY()) + 1);

        raidLocations.put(r, targetLoc);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                "raid-target-set"));
        event.setCancelled(true);
    }

    @EventHandler
    public void onRename(RenameTownEvent event) {
        RegionManager rm = RegionManager.getInstance();
        for (Region r : rm.getAllRegions()) {
            if (!r.getEffects().containsKey("raid_port")) {
                continue;
            }

            Sign sign;
            Block b = r.getLocation().getBlock().getRelative(BlockFace.UP);
            try {
                if (!(b instanceof Sign)) {
                    continue;
                }
                sign = (Sign) b;
            } catch (Exception e) {
                continue;
            }
            String townName = sign.getLine(0);
            if (!townName.equalsIgnoreCase(event.getOldName())) {
                continue;
            }
            sign.setLine(0, event.getNewName());
        }
    }
}

package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.PlayerExitRegionEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerInRegionEvent;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.DiscordUtil;

@CivsSingleton
public class RaidPortEffect implements Listener, CreateRegionListener {
    public static String KEY = "raid_port";
    public static String CHARGING_KEY = "charging_raid_port";
    private HashMap<Region, Location> raidLocations = new HashMap<>();
    private HashMap<Town, Long> cooldowns = new HashMap<>();
    public static Set<Player> portedTo = new HashSet<>();

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new RaidPortEffect(), Civs.getInstance());
    }

    public RaidPortEffect() {
        RegionManager.getInstance().addCreateRegionListener(KEY, this);
        RegionManager.getInstance().addCreateRegionListener(CHARGING_KEY, this);
    }

    @EventHandler
    public void onPlayerExitRegion(PlayerExitRegionEvent event) {
        Player player = Bukkit.getPlayer(event.getUuid());
        if (player != null && event.getRegion().getEffects().containsKey(KEY)) {
            portedTo.remove(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        portedTo.remove(event.getPlayer());
    }

    @Override
    public boolean createRegionHandler(Block block, Player player, RegionType rt) {
        Location l = Region.idToLocation(Region.blockLocationToString(block.getLocation()));

        if (!rt.getEffects().containsKey(KEY) && !rt.getEffects().containsKey(CHARGING_KEY)) {
            return true;
        }

        Town town = hasValidSign(l, rt, player.getUniqueId());

        if (town == null) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "raid-sign"));
            return false;
        }

        if (!ConfigManager.getInstance().isAllowOfflineRaiding()) {
            boolean isOnline = false;
            for (UUID uuid : town.getRawPeople().keySet()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                if (offlinePlayer.isOnline()) {
                    isOnline = true;
                    break;
                }
            }
            if (!isOnline) {
                player.sendMessage(Civs.getPrefix() +
                        LocaleManager.getInstance().getTranslation(player, "raid-porter-offline")
                        .replace("$1", town.getName()));
                return false;
            }
        }

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        double hardshipBuffer;
        if (Civs.econ == null) {
            hardshipBuffer = 20000;
        } else {
            hardshipBuffer = Civs.econ.getBalance(player);
        }
        if (town.getHardship() > civilian.getHardship() + hardshipBuffer) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "hardship-too-high").replace("$1", town.getName()));
            return false;
        }

        for (Player player1 : Bukkit.getOnlinePlayers()) {
            String raidLocalName = LocaleManager.getInstance().getTranslation(player1,
                    rt.getProcessedName() + LocaleConstants.NAME_SUFFIX);
            player1.sendMessage(Civs.getPrefix() + ChatColor.RED +
                    LocaleManager.getInstance().getTranslation(player1, "raid-porter-warning")
                            .replace("$1", player.getDisplayName())
                            .replace("$2", raidLocalName)
                            .replace("$3", town.getName()));
        }
        if (Civs.discordSRV != null) {
            String raidLocalName = LocaleManager.getInstance().getTranslation(ConfigManager.getInstance().getDefaultLanguage(),
                    rt.getProcessedName() + LocaleConstants.NAME_SUFFIX);
            String defaultMessage = Civs.getPrefix() + ChatColor.RED +
                    LocaleManager.getInstance().getTranslation(ConfigManager.getInstance().getDefaultLanguage(), "raid-porter-warning")
                            .replace("$1", player.getDisplayName())
                            .replace("$2", raidLocalName)
                            .replace("$3", town.getName());
            defaultMessage += DiscordUtil.atAllTownOwners(town);
            DiscordUtil.sendMessageToMainChannel(defaultMessage);
        }
        CVItem raidRemote = CVItem.createCVItemFromString("STICK");
        raidRemote.setDisplayName(rt.getDisplayName(player));
        raidRemote.getLore().add(ChatColor.BLACK + Region.locationToString(l));

        l.getWorld().dropItemNaturally(l, raidRemote.createItemStack());
        player.sendMessage(Civs.getPrefix() + ChatColor.RED +
                LocaleManager.getInstance().getTranslation(player, "raid-remote")
                .replace("$1", rt.getDisplayName(player)));
        return true;
    }

    private Town hasValidSign(Location l, RegionType rt, UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        Block block = l.getBlock().getRelative(BlockFace.UP);
        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return null;
        }

        int distance = 200;
        if (rt.getEffects().get(KEY) != null) {
            String[] split = rt.getEffects().get(KEY).split("\\.");
            if (!split[0].isEmpty()) {
                distance = Integer.parseInt(split[0]);
            }
        }

        Sign sign = (Sign) state;

        Town town;
        try {
            town = TownManager.getInstance().getTown(sign.getLine(0));
        } catch (Exception e) {
            block.breakNaturally();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
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
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "raid-target-lost").replace("$1", sign.getLine(0))
                        .replace("$2", distance + ""));
                return null;
            }
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());

        if (townType.getBuildRadius() + distance < l.distance(town.getLocation())) {
            block.breakNaturally();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "raid-target-lost").replace("$1", town.getName())
                    .replace("$2", distance + ""));
            return null;
        }
        return town;
    }

    @EventHandler
    public void onPlayerInRegion(PlayerInRegionEvent event) {
        Region r = event.getRegion();
        Player player = Bukkit.getPlayer(event.getUuid());
        if (!r.getEffects().containsKey(KEY) || portedTo.contains(player)) {
            return;
        }
        if (!r.getLocation().getBlock().getRelative(BlockFace.UP).equals(player.getLocation().getBlock()) &&
                !r.getLocation().getBlock().equals(player.getLocation().getBlock())) {
            return;
        }
        Location l =        r.getLocation();
        RegionType rt =     (RegionType) ItemManager.getInstance().getItemType(r.getType());

        Town town = hasValidSign(l, rt, event.getUuid());
        if (town == null) {
            return;
        }

        long cooldown = 20;
        if (r.getEffects().get(KEY) != null) {
            String[] split = r.getEffects().get(KEY).split("\\.");
            if (split.length > 1) {
                cooldown = Long.parseLong(split[1]);
            }
        }

        if (cooldowns.containsKey(town) &&
                cooldowns.get(town) + cooldown * 1000 > System.currentTimeMillis()) {
            return;
        }

        if (!r.hasUpkeepItems()) {
            return;
        }

        Location targetLoc;
        if (!raidLocations.containsKey(r)) {
            targetLoc = findTargetLocation(town);

            if (targetLoc == null) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
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
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "raid-target-blocked"));
            return;
        }

        //Run upkeep but don't need to know if upkeep occured
        r.runUpkeep();
        player.teleport(targetLoc);
        cooldowns.put(town, System.currentTimeMillis());
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
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
        List<String> lore = itemInHand.getItemMeta().getLore();
        if (lore == null || lore.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();
        Region r;
        RegionType rt;
        RegionManager rm = RegionManager.getInstance();
        try {
            r = rm.getRegionAt(Region.idToLocation(ChatColor.stripColor(lore.get(0))));
            if (r == null) {
                return;
            }
            rt = (RegionType) ItemManager.getInstance().getItemType(r.getType());
            if (rt == null || !rt.getEffects().containsKey(KEY)) {
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
        if (!(block.getState() instanceof Sign)) {
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

    @EventHandler
    public void onTownDestroyed(TownDestroyedEvent event) {
        cooldowns.remove(event.getTown());
    }
}

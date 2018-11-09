package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import java.util.Date;
import java.util.HashMap;

public class SiegeEffect implements Listener, CreateRegionListener {
    public static String CHARGING_KEY = "charging_drain_power";
    public static String KEY = "drain_power";
    private HashMap<Location, Long> lastUpkeep = new HashMap<>();

    public SiegeEffect() {
        RegionManager.getInstance().addCreateRegionListener(KEY, this);
        RegionManager.getInstance().addCreateRegionListener(CHARGING_KEY, this);
    }

    @EventHandler
    public void onCustomEvent(RegionTickEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY)) {
            return;
        }
        Region region = event.getRegion();
        RegionType regionType = event.getRegionType();
        Location l = region.getLocation();

        //Check if the region has the shoot arrow effect and return arrow velocity
        String[] effectSplit = regionType.getEffects().get(KEY).split("\\.");
        long period = Long.parseLong(effectSplit[0]) * 1000;
        if (period < 1) {
            return;
        }
        int damage = 1;
        if (effectSplit.length > 1) {
            damage = Integer.parseInt(effectSplit[1]);
        }

        if (lastUpkeep.get(l) != null && period + lastUpkeep.get(l) > new Date().getTime()) {
            return;
        }

        //Check if valid siege machine position
        if (l.getBlock().getRelative(BlockFace.UP).getY() < l.getWorld().getHighestBlockAt(l).getY()) {
            return;
        }

        //Check to see if the Townships has enough reagents
        if (!region.hasUpkeepItems()) {
            return;
        }

        Block b = l.getBlock().getRelative(BlockFace.UP);
        if (!(b.getState() instanceof Sign)) {
            return;
        }

        //Find target Super-region
        Sign sign = (Sign) b.getState();
        String townName = sign.getLine(0);
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            for (Town currentTown : TownManager.getInstance().getTowns()) {
                if (currentTown.getName().startsWith(townName)) {
                    town = currentTown;
                    break;
                }
            }
            if (town == null) {
                sign.setLine(2, "invalid name");
                sign.update();
                return;
            }
        }

        //Check if too far away
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        double rawRadius = townType.getBuildRadius();
        try {
            if (town.getLocation().distance(l) - rawRadius >  150) {
                sign.setLine(2, "out of");
                sign.setLine(3, "range");
                sign.update();
                return;
            }
        } catch (IllegalArgumentException iae) {
            sign.setLine(2, "out of");
            sign.setLine(3, "range");
            sign.update();
            return;
        }

        if (town.getPower() < 1) {
            return;
        }

        //Run upkeep but don't need to know if upkeep occured
        //effect.forceUpkeep(l);
        region.runUpkeep(false);
        lastUpkeep.put(l, new Date().getTime());

        Location spawnLoc = l.getBlock().getRelative(BlockFace.UP, 3).getLocation();
        //Location srLoc = sr.getLocation();
        Location loc = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 15, spawnLoc.getZ());
        final Location loc1 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 20, spawnLoc.getZ());
        final Location loc2 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 25, spawnLoc.getZ());
        final Location loc3 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 30, spawnLoc.getZ());
        TNTPrimed tnt = l.getWorld().spawn(loc, TNTPrimed.class);
        tnt.setFuseTicks(1);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                TNTPrimed tnt = loc1.getWorld().spawn(loc1, TNTPrimed.class);
                tnt.setFuseTicks(1);
            }
        }, 5L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                TNTPrimed tnt = loc2.getWorld().spawn(loc2, TNTPrimed.class);
                tnt.setFuseTicks(1);
            }
        }, 10L);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), new Runnable() {
            @Override
            public void run() {
                TNTPrimed tnt = loc3.getWorld().spawn(loc3, TNTPrimed.class);
                tnt.setFuseTicks(1);
            }
        }, 15L);

        TownManager.getInstance().setTownPower(town, town.getPower() - damage);
    }

    @EventHandler
    public void onRename(RenameTownEvent event) {
        for (Region r : RegionManager.getInstance().getAllRegions()) {
            if (!r.getEffects().containsKey(KEY) &&
                    !r.getEffects().containsKey(CHARGING_KEY)) {
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
            String srName = sign.getLine(0);
            Town town = TownManager.getInstance().getTown(srName);
            if (town == null) {
                continue;
            }
            if (town.getName().equals(event.getOldName())) {
                sign.setLine(0, event.getNewName());
            }
        }
    }

    @Override
    public boolean createRegionHandler(Block block, Player player, RegionType regionType) {
        if (!regionType.getEffects().containsKey(KEY) &&
                !regionType.getEffects().containsKey(CHARGING_KEY)) {
            return true;
        }
        Location l = block.getLocation();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        Block b = l.getBlock().getRelative(BlockFace.UP);
        if (!(b.getState() instanceof Sign)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslation(civilian.getLocale(), "raid-sign"));
            return false;
        }

        if (l.getBlock().getRelative(BlockFace.UP).getY() < l.getWorld().getHighestBlockAt(l).getY()) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "no-blocks-above-chest").replace("$1", regionType.getName()));
            return false;
        }

        //Find target Super-region
        Sign sign = (Sign) b.getState();
        String townName = sign.getLine(0);
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            sign.setLine(0, "invalid target");
            sign.update();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslation(civilian.getLocale(), "raid-sign"));
            return false;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            Civilian civ = CivilianManager.getInstance().getCivilian(p.getUniqueId());
            p.sendMessage(Civs.getPrefix() + ChatColor.RED + LocaleManager.getInstance().getTranslation(
                    civ.getLocale(), "siege-built").replace("$1", p.getDisplayName())
                    .replace("$2", regionType.getName()).replace("$3", town.getName()));
        }
        return true;
    }
}

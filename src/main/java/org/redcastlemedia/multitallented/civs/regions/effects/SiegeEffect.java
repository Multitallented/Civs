package org.redcastlemedia.multitallented.civs.regions.effects;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.DiscordUtil;

@CivsSingleton
public class SiegeEffect implements Listener, CreateRegionListener {
    public static String CHARGING_KEY = "charging_drain_power";
    public static String KEY = "drain_power";

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new SiegeEffect(), Civs.getInstance());
    }

    public SiegeEffect() {
        RegionManager.getInstance().addCreateRegionListener(KEY, this);
        RegionManager.getInstance().addCreateRegionListener(CHARGING_KEY, this);
    }

    @EventHandler
    public void onCustomEvent(RegionTickEvent event) {
        if (!event.getRegion().getEffects().containsKey(KEY) || !event.isHasUpkeep()) {
            return;
        }
        Region region = event.getRegion();
        Location l = region.getLocation();

        String damageString = region.getEffects().get(KEY);
        int damage = 1;
        int distance = 150;
        if (damageString != null) {
            String[] damageStringSplit = damageString.split("\\.");
            damage = Integer.parseInt(damageStringSplit[0]);
            if (damageStringSplit.length > 1) {
                distance = Integer.parseInt(damageStringSplit[1]);
            }
        }

        //Check if valid siege machine position
        if (l.getBlock().getY() + 2 < l.getWorld().getHighestBlockAt(l).getY()) {
            return;
        }

        Block b = l.getBlock().getRelative(BlockFace.UP);
        BlockState state = b.getState();
        if (!(state instanceof Sign)) {
            return;
        }

        //Find target Super-region
        Sign sign = (Sign) state;
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
            if (town.getLocation().distance(l) - rawRadius > distance) {
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

        if (town.isDevolvedToday()) {
            sign.setLine(2, "max damage");
            sign.setLine(3, "reached for today");
            return;
        }

        double hardshipBuffer = 0;
        if (Civs.econ == null) {
            hardshipBuffer += 20000;
        }
        Set<UUID> siegeMachineOwners = region.getOwners();
        if (!siegeMachineOwners.isEmpty()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(siegeMachineOwners.iterator().next());
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            hardshipBuffer = civilian.getHardship();

            if (Civs.econ != null) {
                hardshipBuffer += Civs.econ.getBalance(player);
            }
        }
        if (town.getHardship() > hardshipBuffer) {
            sign.setLine(1,"hardship");
            sign.setLine(2,"limit");
            sign.setLine(3,"exceeded");
            sign.update();
            return;
        }

        Location spawnLoc = l.getBlock().getRelative(BlockFace.UP, 3).getLocation();
        Location loc = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 15, spawnLoc.getZ());
        final Location loc1 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 20, spawnLoc.getZ());
        final Location loc2 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 25, spawnLoc.getZ());
        final Location loc3 = new Location(spawnLoc.getWorld(), spawnLoc.getX(), spawnLoc.getY() + 30, spawnLoc.getZ());
        l.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc, 2);
        l.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> {
            loc1.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc1, 2);
            loc1.getWorld().playSound(loc1, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
        }, 5L);
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> {
            loc2.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc2, 2);
            loc2.getWorld().playSound(loc2, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
        }, 10L);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Civs.getInstance(), () -> {
            loc3.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, loc3, 2);
            loc3.getWorld().playSound(loc3, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
        }, 15L);

        reducePowerAndExchangeKarma(region, damage, town);
    }

    private void reducePowerAndExchangeKarma(Region region, int damage, Town town) {
        TownManager.getInstance().setTownPower(town, town.getPower() - damage);
        double karmaChange = (double) damage / (double) town.getMaxPower() * town.getPrice();
        if (!region.getOwners().isEmpty()) {
            CivilianManager.getInstance().exchangeHardship(town, region.getOwners().iterator().next(), karmaChange);
        } else {
            CivilianManager.getInstance().exchangeHardship(town, null, karmaChange);
        }
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
        Location l = Region.idToLocation(Region.blockLocationToString(block.getLocation()));
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        Block b = l.getBlock().getRelative(BlockFace.UP);
        BlockState state = b.getState();
        if (!(state instanceof Sign)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslation(player, "raid-sign"));
            return false;
        }

        if (l.getBlock().getY() + 2 < l.getWorld().getHighestBlockAt(l).getY()) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "no-blocks-above-chest").replace("$1", regionType.getDisplayName(player)));
            return false;
        }

        //Find target Super-region
        Sign sign = (Sign) state;
        String townName = sign.getLine(0);
        Town town = TownManager.getInstance().getTown(townName);
        if (town == null) {
            for (Town cTown : TownManager.getInstance().getTowns()) {
                if (cTown.getName().toLowerCase().startsWith(sign.getLine(0))) {
                    town = cTown;
                    break;
                }
            }
        }
        if (town == null) {
            sign.setLine(0, "invalid target");
            sign.update();
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                    .getTranslation(civilian.getLocale(), "raid-sign"));
            return false;
        } else {
            sign.setLine(0, town.getName());
            sign.update();
        }
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        double rawRadius = townType.getBuildRadius();
        if (town.getLocation().distance(l) - rawRadius >  150) {
            sign.setLine(2, "out of");
            sign.setLine(3, "range");
            sign.update();
            return false;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            Civilian civ = CivilianManager.getInstance().getCivilian(p.getUniqueId());
            String siegeMachineLocalName = regionType.getDisplayName(p);
            p.sendMessage(Civs.getPrefix() + ChatColor.RED + LocaleManager.getInstance().getTranslation(
                    civ.getLocale(), "siege-built").replace("$1", player.getDisplayName())
                    .replace("$2", siegeMachineLocalName).replace("$3", town.getName()));
        }
        if (Civs.discordSRV != null) {
            String siegeLocalName = regionType.getDisplayName();
            String defaultMessage = Civs.getPrefix() + ChatColor.RED + LocaleManager.getInstance().getTranslation(
                    ConfigManager.getInstance().getDefaultLanguage(), "siege-built").replace("$1", player.getDisplayName())
                    .replace("$2", siegeLocalName).replace("$3", town.getName());
            defaultMessage += DiscordUtil.atAllTownOwners(town);
            DiscordUtil.sendMessageToMainChannel(defaultMessage);
        }
        return true;
    }
}

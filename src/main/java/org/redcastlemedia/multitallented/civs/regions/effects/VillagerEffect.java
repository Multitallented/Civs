package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.scheduler.CommonScheduler;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@CivsSingleton
public class VillagerEffect implements CreateRegionListener, DestroyRegionListener, Listener, RegionCreatedListener {
    public static String KEY = "villager";
    protected static HashMap<String, Long> townCooldowns = new HashMap<>();

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new VillagerEffect(), Civs.getInstance());
    }

    public VillagerEffect() {
        RegionManager regionManager = RegionManager.getInstance();
        regionManager.addCreateRegionListener(KEY, this);
        regionManager.addRegionCreatedListener(KEY, this);
        regionManager.addDestroyRegionListener(KEY, this);
    }

    @EventHandler
    public void onRegionTickEvent(RegionTickEvent event) {
        Region region = event.getRegion();
        if (region.getEffects().containsKey(VillagerEffect.KEY)) {
            VillagerEffect.spawnVillager(region);
        }
    }

    @Override
    public void regionCreatedHandler(Region region) {
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        Block block = region.getLocation().getBlock();

        Town town = TownManager.getInstance().getTownAt(block.getLocation());
        String villagerCountString = region.getEffects().get(KEY);
        int villagerCount = 1;
        if (villagerCountString != null && !villagerCountString.isEmpty()) {
            try {
                villagerCount = Integer.parseInt(villagerCountString);
            } catch (Exception e) {

            }
        }
        if (town != null) {
            town.setVillagers(town.getVillagers() + villagerCount);
            TownManager.getInstance().saveTown(town);
        }
    }

    @Override
    public boolean createRegionHandler(Block block, Player player, RegionType regionType) {
        if (block.getRelative(BlockFace.UP, 1).getType() != Material.AIR ||
                block.getRelative(BlockFace.UP, 2).getType() != Material.AIR) {

            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(player, "building-requires-2space"));
            return false;
        }
        Town town = TownManager.getInstance().getTownAt(block.getLocation());
        if (town == null) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(player, "req-build-inside-town")
                    .replace("$1", regionType.getDisplayName(player)).replace("$2", "town"));
            return false;
        }
        return true;
    }

    @Override
    public void destroyRegionHandler(Region region) {
        if (!region.getEffects().containsKey(KEY)) {
            return;
        }
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town == null) {
            return;
        }
        String villagerCountString = region.getEffects().get(KEY);
        int villagerCount = 1;
        if (villagerCountString != null && !villagerCountString.isEmpty()) {
            try {
                villagerCount = Integer.parseInt(villagerCountString);
            } catch (Exception e) {

            }
        }
        town.setVillagers(Math.max(0, town.getVillagers() - villagerCount));
        TownManager.getInstance().saveTown(town);
    }

    static Villager spawnVillager(Region region) {
        if (!Util.isLocationWithinSightOfPlayer(region.getLocation())) {
            return null;
        }
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town == null) {
            return null;
        }
        // Don't spawn a villager if there aren't players in the town
        if (!CommonScheduler.getLastTown().values().contains(town)) {
            return null;
        }
        long cooldownTime = ConfigManager.getInstance().getVillagerCooldown() * 1000;
        if (townCooldowns.containsKey(town.getName()) &&
                townCooldowns.get(town.getName()) + cooldownTime > System.currentTimeMillis()) {
            return null;
        }

        int villagerCount = 0;
        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        int radius = townType.getBuildRadius();
        int radiusY = townType.getBuildRadiusY();
        if (!Util.isLocationWithinSightOfPlayer(town.getLocation())) {
            return null;
        }
        String villagerCountString = region.getEffects().get(KEY);
        int regionVillagerCount = 1;
        if (villagerCountString != null && !villagerCountString.isEmpty()) {
            try {
                regionVillagerCount = Integer.parseInt(villagerCountString);
            } catch (Exception e) {

            }
        }
        if (region.getLocation().getWorld().getNearbyEntities(region.getLocation(),
                Math.max(region.getRadiusXN(), region.getRadiusXP()),
                Math.max(region.getRadiusYN(), region.getRadiusYP()),
                Math.max(region.getRadiusZN(), region.getRadiusZP()),
                new Predicate<Entity>() {
                    @Override
                    public boolean test(Entity entity) {
                        return entity instanceof Villager;
                    }
                }).size() >= regionVillagerCount) {
            return null;
        }
        villagerCount = town.getLocation().getWorld().getNearbyEntities(town.getLocation(), radius, radiusY, radius,
                new Predicate<Entity>() {
                    @Override
                    public boolean test(Entity entity) {
                        return entity instanceof Villager;
                    }
                }).size();

        townCooldowns.put(town.getName(), System.currentTimeMillis());
        if (town.getVillagers() <= villagerCount) {
            return null;
        }

        return region.getLocation().getWorld().spawn(region.getLocation().add(0, 0.5, 0), Villager.class);
    }

    @EventHandler
    public void onVillagerDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Villager)) {
            return;
        }
        Location location = event.getEntity().getLocation();
        Town town = TownManager.getInstance().getTownAt(location);
        if (town == null) {
            return;
        }
        TownManager.getInstance().setTownPower(town,
                town.getPower() - ConfigManager.getInstance().getPowerPerNPCKill());
        double karmaChange = (double) ConfigManager.getInstance().getPowerPerNPCKill()
                / (double) town.getMaxPower() * town.getPrice();
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Entity entity = ((EntityDamageByEntityEvent) event.getEntity().getLastDamageCause()).getDamager();
            if (entity instanceof Player) {
                CivilianManager.getInstance().exchangeHardship(town, entity.getUniqueId(), karmaChange);
            } else if (entity instanceof Projectile) {
                Projectile projectile = (Projectile) entity;
                if (projectile.getShooter() instanceof Player) {
                    CivilianManager.getInstance().exchangeHardship(town, ((Player) projectile.getShooter()).getUniqueId(), karmaChange);
                }
            }
        }
    }

}

package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.RegionTickEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;

import java.lang.ref.PhantomReference;
import java.util.HashMap;

public class VillagerEffect implements CreateRegionListener, DestroyRegionListener, Listener {
    public static String KEY = "villager";
    protected static HashMap<String, Long> townCooldowns = new HashMap<>();
    protected static HashMap<String, Integer> townLimit = new HashMap<>();

    public VillagerEffect() {
        RegionManager regionManager = RegionManager.getInstance();
        regionManager.addCreateRegionListener(KEY, this);
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
    public boolean createRegionHandler(Block block, Player player, RegionType regionType) {
        if (block.getRelative(BlockFace.UP, 1).getType() != Material.AIR ||
                block.getRelative(BlockFace.UP, 2).getType() != Material.AIR) {

            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "building-requires-2space"));
            return false;
        }

        block.getWorld().spawn(block.getLocation(), Villager.class);

        Town town = TownManager.getInstance().getTownAt(block.getLocation());
        if (town != null) {
            town.setPopulation(town.getPopulation() + 1);
            TownManager.getInstance().saveTown(town);

            if (townLimit.containsKey(town.getName())) {
                townLimit.put(town.getName(), townLimit.get(town.getName()));
            } else {
                townLimit.put(town.getName(), 1);
            }
        }

        return true;
    }

    @Override
    public void destroyRegionHandler(Region region) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town == null) {
            return;
        }
        if (!townLimit.containsKey(town.getName())) {
            return;
        }
        if (townLimit.get(town.getName()) < 2) {
            townLimit.remove(town.getName());
        } else {
            townLimit.put(town.getName(), townLimit.get(town.getName()) - 1);
        }
    }

    public static Villager spawnVillager(Region region) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town == null) {
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
        for (Entity e : town.getLocation().getWorld().getNearbyEntities(town.getLocation(), radius, radiusY, radius)) {
            if (e instanceof Villager) {
                villagerCount++;
            }
        }

        if (townLimit.containsKey(town.getName()) && townLimit.get(town.getName()) <= villagerCount) {
            return null;
        }
        if (!region.getLocation().getChunk().isLoaded()) {
            region.getLocation().getChunk().load();
        }


        townCooldowns.put(town.getName(), System.currentTimeMillis());
        return region.getLocation().getWorld().spawn(region.getLocation(), Villager.class);
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
        int townPower = town.getPower();
        if (townPower > 0) {
            town.setPower(townPower - ConfigManager.getInstance().getPowerPerNPCKill());
            TownManager.getInstance().saveTown(town);
        }
    }
}

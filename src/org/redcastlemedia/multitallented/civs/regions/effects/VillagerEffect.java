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
import org.redcastlemedia.multitallented.civs.Civs;
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

public class VillagerEffect implements CreateRegionListener, DestroyRegionListener, Listener, RegionCreatedListener {
    public static String KEY = "villager";
    protected static HashMap<String, Long> townCooldowns = new HashMap<>();

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
        Block block = region.getLocation().getBlock();
        block.getWorld().spawn(block.getLocation(), Villager.class);

        Town town = TownManager.getInstance().getTownAt(block.getLocation());
        if (town != null) {
            town.setPopulation(town.getPopulation() + 1);
            town.setVillagers(town.getVillagers() + 1);
            TownManager.getInstance().saveTown(town);
        }
    }

    @Override
    public boolean createRegionHandler(Block block, Player player, RegionType regionType) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (block.getRelative(BlockFace.UP, 1).getType() != Material.AIR ||
                block.getRelative(BlockFace.UP, 2).getType() != Material.AIR) {

            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "building-requires-2space"));
            return false;
        }
        Town town = TownManager.getInstance().getTownAt(block.getLocation());
        if (town == null) {
            player.sendMessage(Civs.getPrefix() +
                    LocaleManager.getInstance().getTranslation(civilian.getLocale(), "req-build-inside-town")
                    .replace("$1", regionType.getName()).replace("$2", "town"));
            return false;
        }
        return true;
    }

    @Override
    public void destroyRegionHandler(Region region) {
        Town town = TownManager.getInstance().getTownAt(region.getLocation());
        if (town == null) {
            return;
        }
        town.setVillagers(Math.max(0, town.getVillagers() - 1));
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

        townCooldowns.put(town.getName(), System.currentTimeMillis());
        System.out.println(town.getVillagers() + ":" + villagerCount);
        if (town.getVillagers() <= villagerCount) {
            return null;
        }
        if (!region.getLocation().getChunk().isLoaded()) {
            region.getLocation().getChunk().load();
        }


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
        TownManager.getInstance().setTownPower(town,
                town.getPower() - ConfigManager.getInstance().getPowerPerNPCKill());
    }

}

package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class VillagerEffect implements CreateRegionListener, DestroyRegionListener, Listener {
    public static String KEY = "villager";

    public VillagerEffect() {
        RegionManager regionManager = RegionManager.getInstance();
        regionManager.addCreateRegionListener(KEY, this);
        regionManager.addDestroyRegionListener(KEY, this);
    }

    @Override
    public boolean createRegionHandler(Block block, Player player) {
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
        }

        //TODO track number of villagers per town
        return true;
    }

    @Override
    public void destroyRegionHandler(Region region) {
        //TODO decrement villagers per town
    }

    public static void spawnVillager(Region region) {
        //TODO spawn villager only if the town does not have enough
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

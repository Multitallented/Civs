package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.ArrowTurret;
import org.redcastlemedia.multitallented.civs.regions.effects.VillagerEffect;

public class RegionTickThread implements Runnable {

    @Override
    public void run() {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getAllRegions()) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            boolean hasUpkeep = !regionType.isDailyPeriod() && region.runUpkeep();

            if (hasUpkeep && region.getEffects().containsKey(ArrowTurret.KEY)) {
                shootArrow(region);
            }
            if (region.getEffects().containsKey(VillagerEffect.KEY)) {
                VillagerEffect.spawnVillager(region);
            }
        }
    }

    //Shoot arrows at mobs
    private void shootArrow(Region region) {
        //TODO shoot at players too
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        Location location = region.getLocation();
        for (Entity e : location.getChunk().getEntities()) { //TODO fix this so it gets all entities in range
            if (!(e instanceof Monster)) {
                continue;
            }
            Monster monster = (Monster) e;
            if (monster.getLocation().distance(location) > regionType.getEffectRadius()) {
                continue;
            }
            ArrowTurret.shootArrow(region, monster, region.getEffects().get(ArrowTurret.KEY), false);
            break;
        }
    }
}

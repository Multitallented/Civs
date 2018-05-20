package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.ArrowTurret;

public class RegionTickThread implements Runnable {

    @Override
    public void run() {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getAllRegions()) {
            boolean hasUpkeep = region.runUpkeep();

            if (hasUpkeep && region.getEffects().containsKey("arrow_turret")) {
                shootArrow(region);
            }
        }
    }

    //Shoot arrows at mobs
    private void shootArrow(Region region) {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        Location location = region.getLocation();
        for (Entity e : location.getChunk().getEntities()) {
            if (!(e instanceof Monster)) {
                continue;
            }
            Monster monster = (Monster) e;
            if (monster.getLocation().distance(location) > regionType.getEffectRadius()) {
                continue;
            }
            ArrowTurret.shootArrow(region, monster, region.getEffects().get("arrow_turret"), false);
            break;
        }
    }
}

package org.redcastlemedia.multitallented.civs.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.UUID;

public class RegionTickThread implements Runnable {

    @Override
    public void run() {
        RegionManager regionManager = RegionManager.getInstance();
        for (Region region : regionManager.getAllRegions()) {
            if (!region.shouldTick()) {
                continue;
            }

            Block block = region.getLocation().getBlock();
            ItemManager itemManager = ItemManager.getInstance();
            RegionType regionType = (RegionType) itemManager.getItemType(region.getType());
            Chest chest = null;
            if (block.getState() instanceof Chest) {
                chest = (Chest) block.getState();
            }
            boolean hasReagents = region.hasReagents();

            if (!hasReagents || (chest != null && chest.getInventory().firstEmpty() == -1)) {
                continue;
            }

            boolean hasMoney = false;
            if (Civs.econ != null) {
                double payout = regionType.getPayout();
                payout = payout / region.getOwners().size();
                for (UUID uuid : region.getOwners()) {
                    OfflinePlayer player = Bukkit.getPlayer(uuid);
                    if (player == null) {
                        continue;
                    }
                    if (payout > 0) {
                        Civs.econ.depositPlayer(player, payout);
                        hasMoney = true;
                    } else if (Civs.econ.has(player, payout)) {
                        Civs.econ.withdrawPlayer(player, Math.abs(payout));
                        hasMoney = true;
                    }
                }
            } else {
                hasMoney = true;
            }
            if (!hasMoney) {
                continue;
            }
            if (chest != null) {
                Util.removeItems(regionType.getInput(), chest.getInventory());
            }
            if (chest != null) {
                Util.addItems(regionType.getOutput(), chest.getInventory());
            }
            //TODO more effects

            region.tick();
        }
    }
}

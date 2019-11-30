package org.redcastlemedia.multitallented.civs.menus.common;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.HashMap;
import java.util.Map;

@CivsMenu(name = "community")
public class CommunityMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        return new HashMap<>();
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("your-towns")) {
            boolean isInATown = false;
            for (Town town : TownManager.getInstance().getTowns()) {
                if (town.getRawPeople().containsKey(civilian.getUuid())) {
                    isInATown = true;
                    break;
                }
            }
            if (!isInATown) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("alliances")) {
            if (AllianceManager.getInstance().getAllAlliances().isEmpty()) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("regions-for-sale")) {
            boolean hasRegionsForSale = false;
            for (Region r : RegionManager.getInstance().getAllRegions()) {
                if (r.getForSale() != -1 && (!r.getRawPeople().containsKey(civilian.getUuid()) ||
                        r.getRawPeople().get(civilian.getUuid()).contains("ally"))) {
                    hasRegionsForSale = true;
                    break;
                }
            }
            if (!hasRegionsForSale) {
                return new ItemStack(Material.AIR);
            }
        } else if (menuIcon.getKey().equals("ports")) {
            boolean hasPort = false;
            for (Region region : RegionManager.getInstance().getAllRegions()) {
                if (!region.getEffects().containsKey("port")) {
                    continue;
                }
                if (!region.getPeople().containsKey(civilian.getUuid())) {
                    continue;
                }
                //Don't show private ports
                if (region.getEffects().get("port") != null &&
                        !region.getPeople().get(civilian.getUuid()).contains("member") &&
                        !region.getPeople().get(civilian.getUuid()).contains("owner")) {
                    continue;
                }
                hasPort = true;
                break;
            }
            if (!hasPort) {
                return new ItemStack(Material.AIR);
            }
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}

package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.commands.PortCommand;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class PortMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<Region> regions = new ArrayList<>();
        Set<Region> regionSet = RegionManager.getInstance().getAllRegions();
        for (Region region : regionSet) {
            if (regions.contains(region)) {
                continue;
            }
            if (!region.getEffects().containsKey("port")) {
                continue;
            }
            //Don't show private ports
            if (!PortCommand.canPort(region, civilian.getUuid(), null)) {
                continue;
            }
            regions.add(region);
        }
        data.put("ports", regions);
        int maxPage = (int) Math.ceil((double) regions.size() / (double) itemsPerPage.get("ports"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        return data;
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("ports".equals(menuIcon.getKey())) {
            List<Region> regions = (List<Region>) MenuManager.getData(civilian.getUuid(), "ports");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            Region[] regionArray = new Region[regions.size()];
            regionArray = regions.toArray(regionArray);
            if (regionArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Region region = regionArray[startIndex + count];
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            CVItem cvItem = regionType.getShopIcon().clone();
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    region.getType() + "-name"));
            cvItem.getLore().clear();
            cvItem.getLore().add(ChatColor.BLACK + region.getId());
            cvItem.getLore().add(region.getLocation().getWorld().getName() + " " +
                    ((int) region.getLocation().getX()) + "x " +
                    ((int) region.getLocation().getY()) + "y " +
                    ((int) region.getLocation().getZ()) + "z ");
            Town town = TownManager.getInstance().getTownAt(region.getLocation());
            if (town != null) {
                cvItem.getLore().add(town.getName());
            }
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if (actionString.equals("teleport")) {
            String id = ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(0));
            Player player = Bukkit.getPlayer(civilian.getUuid());
            PlayerCommandPreprocessEvent commandPreprocessEvent = new PlayerCommandPreprocessEvent(player, "/cv port " + id);
            Bukkit.getPluginManager().callEvent(commandPreprocessEvent);
            if (!commandPreprocessEvent.isCancelled()) {
                player.performCommand("cv port " + id);
            }
        }
        return true;
    }

    @Override
    public String getFileName() {
        return "port";
    }
}

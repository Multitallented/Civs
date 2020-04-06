package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.commands.PortCommand;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.effects.TeleportEffect;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

@CivsMenu(name = "port")
public class PortMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        Region region = null;
        if (params.containsKey("region")) {
            region = RegionManager.getInstance().getRegionById(params.get("region"));
            data.put("region", region);
        }
        List<Region> regions = new ArrayList<>();
        Set<Region> regionSet = RegionManager.getInstance().getAllRegions();

        outer:
        for (Region currentRegion : regionSet) {
            if (regions.contains(currentRegion) || currentRegion.equals(region)) {
                continue;
            }
            if (!currentRegion.getEffects().containsKey("port")) {
                continue;
            }
            //Don't show private ports
            if (region == null) {
                if (!PortCommand.canPort(currentRegion, civilian.getUuid(), null)) {
                    continue;
                }
            } else {
                if (!TeleportEffect.isPotentialTeleportDestination(region, currentRegion)) {
                    continue;
                }
            }
            regions.add(currentRegion);
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
            CVItem cvItem = regionType.getShopIcon(civilian.getLocale());
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
            if (MenuManager.getData(civilian.getUuid(), "region") != null) {
                ArrayList<String> actionStrings = new ArrayList<>();
                actionStrings.add("set-teleport");
                putActionList(civilian, itemStack, actionStrings);
            } else {
                putActions(civilian, menuIcon, itemStack, count);
            }
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
            return true;
        } else if (actionString.equals("set-teleport")) {
            Region region = (Region) MenuManager.getData(civilian.getUuid(), "region");
            String id = ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(0));
            region.getEffects().put(TeleportEffect.KEY, id);
            RegionManager.getInstance().saveRegion(region);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}

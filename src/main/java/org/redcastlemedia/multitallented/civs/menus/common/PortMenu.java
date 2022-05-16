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
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
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
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsMenu(name = "port")
public class PortMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey(Constants.PAGE)) {
            data.put(Constants.PAGE, Integer.parseInt(params.get(Constants.PAGE)));
        } else {
            data.put(Constants.PAGE, 0);
        }
        Region region = null;
        if (params.containsKey(Constants.REGION)) {
            region = RegionManager.getInstance().getRegionById(params.get(Constants.REGION));
            data.put(Constants.REGION, region);
        }
        List<Region> regions = new ArrayList<>();
        Set<Region> regionSet = RegionManager.getInstance().getAllRegions();

        for (Region currentRegion : regionSet) {
            if (regions.contains(currentRegion) || currentRegion.equals(region)) {
                continue;
            }
            if ((region == null && !currentRegion.getEffects().containsKey("port")) ||
                    (region != null && !currentRegion.getEffects().containsKey(TeleportEffect.KEY))) {
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
        data.put("portMap", new HashMap<ItemStack, Region>());
        int maxPage = (int) Math.ceil((double) regions.size() / (double) itemsPerPage.get("ports"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        return data;
    }

    @Override @SuppressWarnings("unchecked")
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if ("ports".equals(menuIcon.getKey())) {
            List<Region> regions = (List<Region>) MenuManager.getData(civilian.getUuid(), "ports");
            int page = (int) MenuManager.getData(civilian.getUuid(), Constants.PAGE);
            int startIndex = page * menuIcon.getIndex().size();
            Region[] regionArray = new Region[regions.size()];
            regionArray = regions.toArray(regionArray);
            if (regionArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Region region = regionArray[startIndex + count];
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            CVItem cvItem = regionType.getShopIcon(player);
            cvItem.setDisplayName(regionType.getDisplayName(player));
            cvItem.getLore().clear();
            if (Civs.perm == null || !Civs.perm.playerHas(player, Constants.STREAM_PERMISSION)) {
                String coordString = LocaleManager.getInstance().getTranslation(player, "coords")
                        .replace("$1", region.getLocation().getWorld().getName())
                        .replace("$2", "" + ((int) region.getLocation().getX()))
                        .replace("$3", "" + ((int) region.getLocation().getY()))
                        .replace("$4", "" + ((int) region.getLocation().getZ()));
                cvItem.getLore().add(coordString);
            }
            Town town = TownManager.getInstance().getTownAt(region.getLocation());
            if (town != null) {
                cvItem.getLore().add(town.getName());
            }
            ItemStack itemStack = cvItem.createItemStack();
            ((Map<ItemStack, Region>) MenuManager.getData(civilian.getUuid(), "portMap")).put(itemStack, region);
            if (MenuManager.getData(civilian.getUuid(), Constants.REGION) != null) {
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

    @Override @SuppressWarnings("unchecked")
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if (actionString.equals("teleport")) {
            String id = ((Map<ItemStack, Region>) MenuManager.getData(civilian.getUuid(), "portMap")).get(clickedItem).getId();
            Player player = Bukkit.getPlayer(civilian.getUuid());
            PlayerCommandPreprocessEvent commandPreprocessEvent = new PlayerCommandPreprocessEvent(player, "/cv port " + id);
            Bukkit.getPluginManager().callEvent(commandPreprocessEvent);
            if (!commandPreprocessEvent.isCancelled()) {
                player.performCommand("cv port " + id);
            }
            return true;
        } else if (actionString.equals("set-teleport")) {
            Region region = (Region) MenuManager.getData(civilian.getUuid(), Constants.REGION);
            String id = ChatColor.stripColor(clickedItem.getItemMeta().getLore().get(0));
            region.getEffects().put(TeleportEffect.KEY, id);
            RegionManager.getInstance().saveRegion(region);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}

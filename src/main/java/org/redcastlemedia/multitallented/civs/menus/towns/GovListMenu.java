package org.redcastlemedia.multitallented.civs.menus.towns;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.Constants;

import java.util.*;

@CivsMenu(name = "gov-list") @SuppressWarnings("unused")
public class GovListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("town")) {
            data.put("town", TownManager.getInstance().getTown(params.get("town")));
        }
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<String> govList = new ArrayList<>(GovernmentManager.getInstance().getGovermentTypes());
        if (params.containsKey("leaderboard")) {
            HashMap<String, Integer> govPower = new HashMap<>();
            HashMap<String, Set<Town>> townsByGov = new HashMap<>();
            for (Town town : TownManager.getInstance().getTowns()) {
                String currentGovName = town.getGovernmentType().toLowerCase();
                if (!townsByGov.containsKey(currentGovName)) {
                    townsByGov.put(currentGovName, new HashSet<>());
                }
                townsByGov.get(currentGovName).add(town);
                if (govPower.containsKey(town.getGovernmentType().toLowerCase())) {
                    govPower.put(town.getGovernmentType().toLowerCase(), town.getPower() +
                            govPower.get(town.getGovernmentType().toLowerCase()));
                } else {
                    govPower.put(town.getGovernmentType().toLowerCase(), town.getPower());
                }
            }
            for (String currentGovName : GovernmentManager.getInstance().getGovermentTypes()) {
                if (!govPower.containsKey(currentGovName.toLowerCase())) {
                    govList.remove(currentGovName);
                }
            }
            govList = govList.stream().filter(govName -> {
                Government government = GovernmentManager.getInstance().getGovernment(govName);
                return government.isSelectable();
            }).toList();
            govList.sort((o1, o2) -> {
                int power1 = govPower.getOrDefault(o1.toLowerCase(), 0);
                int power2 = govPower.getOrDefault(o2.toLowerCase(), 0);
                return Integer.compare(power2, power1);
            });
            data.put("townsByGov", townsByGov);
            data.put("govPower", govPower);
        }
        data.put("govList", govList);
        data.put("govMap", new HashMap<ItemStack, String>());

        int maxPage = (int) Math.ceil((double) govList.size() / (double) itemsPerPage.get("governments"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if (menuIcon.getKey().equals("governments")) {
            HashMap<String, Integer> govPower = (HashMap<String, Integer>) MenuManager.getData(civilian.getUuid(), "govPower");
            boolean isLeaderboard = govPower != null;

            List<String> govList = (List<String>) MenuManager.getData(civilian.getUuid(), "govList");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (govList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            String govName = govList.get(startIndex + count);
            Government government = GovernmentManager.getInstance().getGovernment(govName);
            CVItem cvItem;
            if (isLeaderboard) {
                if (govPower.get(govName.toLowerCase()) == null) {
                    return new ItemStack(Material.AIR);
                }
                cvItem = government.getIcon(civilian, false);
                cvItem.getLore().add(LocaleManager.getInstance().getTranslation(player, "points")
                        .replace("$1", "" + govPower.get(govName.toLowerCase())));
            } else {
                cvItem = government.getIcon(civilian, true);
            }
            ItemStack itemStack = cvItem.createItemStack();
            if (isLeaderboard) {
                String townList = "";
                HashMap<String, Set<Town>> townsByGov = (HashMap<String, Set<Town>>) MenuManager.getData(civilian.getUuid(), "townsByGov");
                for (Town currentTown : townsByGov.get(govName.toLowerCase())) {
                    townList += currentTown.getName() + ",";
                }
                townList = townList.substring(0, townList.length() - 1);
                ArrayList<String> theseActions = new ArrayList<>();
                theseActions.add("menu:select-town?townList=" + townList);
                putActionList(civilian, itemStack, theseActions);
            } else {
                putActions(civilian, menuIcon, itemStack, count);
            }
            ((HashMap<ItemStack, String>) MenuManager.getData(civilian.getUuid(), "govMap")).put(itemStack, govName);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if ("select-gov".equals(actionString)) {
            Town town = (Town) MenuManager.getData(civilian.getUuid(), Constants.TOWN);
            if (town == null) {
                return true;
            }
            HashMap<ItemStack, String> govMap = (HashMap<ItemStack, String>) MenuManager.getData(civilian.getUuid(), "govMap");
            String govName = govMap.get(clickedItem);
            GovernmentManager.getInstance().transitionGovernment(town, govName, true);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}

package org.redcastlemedia.multitallented.civs.menus.towns;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.*;

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
                if (govPower.containsKey(town.getGovernmentType())) {
                    govPower.put(town.getGovernmentType().toLowerCase(), town.getPower() +
                            govPower.get(town.getGovernmentType().toLowerCase()));
                } else {
                    govPower.put(town.getGovernmentType().toLowerCase(), town.getPower());
                }
            }
            govList.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return govPower.getOrDefault(o2, 0).compareTo(govPower.getOrDefault(o1, 0));
                }
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

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("governments")) {
            List<String> govList = (List<String>) MenuManager.getData(civilian.getUuid(), "govList");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (govList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            String govName = govList.get(startIndex + count);
            Government government = GovernmentManager.getInstance().getGovernment(govName);
            CVItem cvItem;
            boolean isLeaderboard = false;
            HashMap<String, Integer> govPower = (HashMap<String, Integer>) MenuManager.getData(civilian.getUuid(), "govPower");
            if (govPower != null) {
                if (govPower.get(govName) == null) {
                    return new ItemStack(Material.AIR);
                }
                cvItem = government.getIcon(civilian.getLocale(), false);
                cvItem.getLore().add(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "points")
                        .replace("$1", "" + govPower.get(govName)));
                isLeaderboard = true;
            } else {
                cvItem = government.getIcon(civilian.getLocale(), true);
            }
            ItemStack itemStack = cvItem.createItemStack();
            if (isLeaderboard) {
                String townList = "";
                HashMap<String, Set<Town>> townsByGov = (HashMap<String, Set<Town>>) MenuManager.getData(civilian.getUuid(), "townsByGov");
                for (Town currentTown : townsByGov.get(govName)) {
                    townList += currentTown.getName() + ",";
                }
                townList = townList.substring(0, townList.length() - 1);
                ArrayList<String> theseActions = new ArrayList<>();
                theseActions.add("menu:select-town?townList=" + townList);
                actions.get(civilian.getUuid()).put(itemStack, theseActions);
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
            Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
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

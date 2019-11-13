package org.redcastlemedia.multitallented.civs.menus.people;

import java.util.*;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class PeopleMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<Civilian> civilians = new ArrayList<>();
        HashMap<UUID, String> ranks = null;
        boolean alreadyOnlineFiltered = false;
        boolean invite = !(!params.containsKey("invite") || "false".equals(params.get("invite")));
        data.put("invite", invite);
        if (params.containsKey("region")) {
            Region region = RegionManager.getInstance().getRegionById(params.get("region"));
            data.put("region", region);
            ranks = region.getRawPeople();
            if (invite) {
                addOnlinePlayers(civilians, region.getRawPeople().keySet());
                Town town = TownManager.getInstance().getTownAt(region.getLocation());
                if (town != null) {
                    for (UUID uuid : town.getRawPeople().keySet()) {
                        civilians.add(CivilianManager.getInstance().getCivilian(uuid));
                    }
                }
            } else {
                for (UUID uuid : region.getRawPeople().keySet()) {
                    civilians.add(CivilianManager.getInstance().getCivilian(uuid));
                }
            }
        } else if (params.containsKey("town")) {
            Town town = TownManager.getInstance().getTown(params.get("town"));
            data.put("town", town);
            ranks = town.getRawPeople();
            if (invite) {
                addOnlinePlayers(civilians, town.getRawPeople().keySet());
            } else {
                for (UUID uuid : town.getRawPeople().keySet()) {
                    civilians.add(CivilianManager.getInstance().getCivilian(uuid));
                }
            }
        } else if (params.containsKey("uuid")) {
            boolean filterOnline = params.containsKey("online");
            for (UUID uuid : civilian.getFriends()) {
                if (filterOnline && Bukkit.getPlayer(uuid) == null) {
                    continue;
                }
                civilians.add(CivilianManager.getInstance().getCivilian(uuid));
            }
        } else {
            alreadyOnlineFiltered = true;
            if (params.containsKey("online")) {
                addOnlinePlayers(civilians, new HashSet<>());
            } else {
                civilians.addAll(CivilianManager.getInstance().getCivilians());
            }
        }
        if (!alreadyOnlineFiltered && params.containsKey("online") && "true".equals(params.get("online"))) {
            civilians.removeIf(new Predicate<Civilian>() {
                @Override
                public boolean test(Civilian civilian) {
                    return Bukkit.getPlayer(civilian.getUuid()) != null;
                }
            });
        }
        if (params.containsKey("sort")) {
            data.put("sort", params.get("sort"));
            if ("points".equals(params.get("sort"))) {
                pointsSort(civilians);
            } else if ("rank".equals(params.get("sort"))) {
                rankSort(civilians, ranks);
            } else {
                alphabeticalSort(civilians);
            }
        } else {
            alphabeticalSort(civilians);
            data.put("sort", "alphabetical");
        }
        data.put("civilians", civilians);
        data.put("civMap", new HashMap<ItemStack, UUID>());
        int maxPage = (int) Math.ceil((double) civilians.size() / (double) itemsPerPage.get("people"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        return data;
    }

    private void rankSort(List<Civilian> civilians, HashMap<UUID, String> ranks) {
        civilians.sort(new Comparator<Civilian>() {
            @Override
            public int compare(Civilian o1, Civilian o2) {
                return Integer.compare(rankWeight(ranks.get(o1.getUuid())),
                        rankWeight(ranks.get(o2.getUuid())));
            }
        });
    }
    private int rankWeight(String rank) {
        if (rank.contains("owner")) {
            return 100;
        } else if (rank.contains("member")) {
            return 50;
        } else if (rank.contains("ally")) {
            return 25;
        } else {
            return 0;
        }
    }

    private void pointsSort(List<Civilian> civilians) {
        civilians.sort(new Comparator<Civilian>() {
            @Override
            public int compare(Civilian o1, Civilian o2) {
                return Double.compare(o1.getPoints(), o2.getPoints());
            }
        });
    }

    private void alphabeticalSort(List<Civilian> civilians) {
        civilians.sort(new Comparator<Civilian>() {
            @Override
            public int compare(Civilian civilian1, Civilian civilian2) {
                try {
                    OfflinePlayer offlinePlayer1 = Bukkit.getOfflinePlayer(civilian1.getUuid());
                    OfflinePlayer offlinePlayer2 = Bukkit.getOfflinePlayer(civilian2.getUuid());
                    return offlinePlayer1.getName().compareTo(offlinePlayer2.getName());
                } catch (NullPointerException npe) {
                    return 0;
                }
            }
        });
    }

    private void addOnlinePlayers(List<Civilian> civilianList, Set<UUID> blacklist) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (blacklist.contains(player.getUniqueId())) {
                continue;
            }
            civilianList.add(CivilianManager.getInstance().getCivilian(player.getUniqueId()));
        }
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("people".equals(menuIcon.getKey())) {
            List<Civilian> civilians = (List<Civilian>) MenuManager.getData(civilian.getUuid(), "civilians");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (civilians.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Civilian currentCivilian = civilians.get(startIndex + count);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(currentCivilian.getUuid());
            Player player = null;
            if (offlinePlayer.isOnline()) {
                player = (Player) offlinePlayer;
            }
            CVItem cvItem = new CVItem(Material.PLAYER_HEAD, 1);
            cvItem.setDisplayName(offlinePlayer.getName());
            ItemStack itemStack = cvItem.createItemStack();
            if (player != null) {
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                skullMeta.setOwningPlayer(player);
            }
            ((HashMap<ItemStack, UUID>) MenuManager.getData(civilian.getUuid(), "civMap")).put(itemStack, offlinePlayer.getUniqueId());
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if ("take-action".equals(actionString)) {
            UUID uuid = ((HashMap<ItemStack, UUID>) MenuManager.getData(civilian.getUuid(), "civMap")).get(clickedItem);
            Region region = (Region) MenuManager.getData(civilian.getUuid(), "region");
            Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
            Player player = Bukkit.getPlayer(civilian.getUuid());
            boolean invite = (Boolean) MenuManager.getData(civilian.getUuid(), "invite");
            if (region != null) {
                if (invite) {
                    player.performCommand("cv add " + clickedItem.getItemMeta().getDisplayName() + " " + region.getId());
                } else {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("region", region.getId());
                    params.put("uuid", uuid.toString());
                    MenuManager.getInstance().openMenu(player, "member-action", params);
                }
            } else if (town != null) {
                if (invite) {
                    player.performCommand("cv invite " + clickedItem.getItemMeta().getDisplayName() + " " + town.getName());
                } else {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("town", town.getName());
                    params.put("uuid", uuid.toString());
                    MenuManager.getInstance().openMenu(player, "member-action", params);
                }
            } else {
                HashMap<String, String> params = new HashMap<>();
                params.put("uuid", uuid.toString());
                MenuManager.getInstance().openMenu(player, "player", params);
            }
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }

    @Override
    public String getFileName() {
        return "people";
    }
}

package org.redcastlemedia.multitallented.civs.menus.people;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsMenu(name = "people") @SuppressWarnings("unused")
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
        Map<UUID, String> ranks = null;
        boolean alreadyOnlineFiltered = false;
        boolean invite = !(!params.containsKey("invite") || "false".equals(params.get("invite")));
        if (invite) {
            data.put("invite", true);
        } else {
            Boolean inviteData = (Boolean) MenuManager.getData(civilian.getUuid(), "invite");
            if (inviteData != null && inviteData) {
                invite = true;
                data.put("invite", true);
            }
        }
        Region region = (Region) MenuManager.getData(civilian.getUuid(), Constants.REGION);
        Town town = (Town) MenuManager.getData(civilian.getUuid(), Constants.TOWN);

        if (region != null || params.containsKey(Constants.REGION)) {
            if (region == null) {
                region = RegionManager.getInstance().getRegionById(params.get(Constants.REGION));
            }
            data.put(Constants.REGION, region);
            ranks = region.getRawPeople();
            if (invite) {
                addOnlinePlayers(civilians, region.getRawPeople().keySet());
                Town containingTown = TownManager.getInstance().getTownAt(region.getLocation());
                if (containingTown != null) {
                    for (UUID uuid : containingTown.getRawPeople().keySet()) {
                        if (region.getRawPeople().containsKey(uuid)) {
                            continue;
                        }
                        civilians.add(CivilianManager.getInstance().getCivilian(uuid));
                    }
                }
            } else {
                for (UUID uuid : region.getRawPeople().keySet()) {
                    civilians.add(CivilianManager.getInstance().getCivilian(uuid));
                }
            }
        } else if (town != null || params.containsKey(Constants.TOWN)) {
            if (town == null) {
                town = TownManager.getInstance().getTown(params.get(Constants.TOWN));
            }
            data.put(Constants.TOWN, town);
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
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_INVISIBLE)) {
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

    private void rankSort(List<Civilian> civilians, Map<UUID, String> ranks) {
        if (ranks == null || ranks.isEmpty()) {
            return;
        }
        civilians.sort(new Comparator<Civilian>() {
            @Override
            public int compare(Civilian o1, Civilian o2) {
                if (!ranks.containsKey(o1.getUuid()) ||
                        !ranks.containsKey(o2.getUuid())) {
                    return 0;
                }
                return Integer.compare(rankWeight(ranks.get(o1.getUuid())),
                        rankWeight(ranks.get(o2.getUuid())));
            }
        });
    }
    private int rankWeight(String rank) {
        if (rank.contains(Constants.OWNER)) {
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
                OfflinePlayer offlinePlayer1 = Bukkit.getOfflinePlayer(civilian1.getUuid());
                OfflinePlayer offlinePlayer2 = Bukkit.getOfflinePlayer(civilian2.getUuid());
                if (offlinePlayer1.getName() == null || offlinePlayer2.getName() == null) {
                    return 0;
                }
                try {
                    return offlinePlayer1.getName().compareTo(offlinePlayer2.getName());
                } catch (Exception e) {
                    Object[] args = { Civs.NAME, offlinePlayer1.getName(), offlinePlayer2.getName()};
                    Civs.logger.log(Level.WARNING, "{0} Failed to compare name {1} with {2}", args);
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
            if (Civs.perm != null && Civs.perm.has(player, Constants.ADMIN_INVISIBLE)) {
                continue;
            }
            civilianList.add(CivilianManager.getInstance().getCivilian(player.getUniqueId()));
        }
    }

    @Override @SuppressWarnings("unchecked")
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        String sort = (String) MenuManager.getData(civilian.getUuid(), "sort");
        if ("sort-rank".equals(menuIcon.getKey())) {
            if ("rank".equals(sort)) {
                return new ItemStack(Material.AIR);
            }
            Boolean invite = (Boolean) MenuManager.getData(civilian.getUuid(), "invite");
            if (invite != null && invite) {
                return new ItemStack(Material.AIR);
            }
            if (MenuManager.getData(civilian.getUuid(), Constants.REGION) == null &&
                    MenuManager.getData(civilian.getUuid(), Constants.TOWN) == null) {
                return new ItemStack(Material.AIR);
            }
        } else if ("sort-alphabetical".equals(menuIcon.getKey())) {
            if ("alphabetical".equals(sort)) {
                return new ItemStack(Material.AIR);
            }
        } else if ("sort-points".equals(menuIcon.getKey())) {
            if ("points".equals(sort)) {
                return new ItemStack(Material.AIR);
            }
        } else if ("people".equals(menuIcon.getKey())) {
            List<Civilian> civilians = (List<Civilian>) MenuManager.getData(civilian.getUuid(), "civilians");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (civilians.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Civilian currentCivilian = civilians.get(startIndex + count);
            Player currentPlayer = Bukkit.getPlayer(civilian.getUuid());
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(currentCivilian.getUuid());
            Player player = null;
            if (offlinePlayer.isOnline()) {
                player = (Player) offlinePlayer;
            }
            CVItem cvItem = new CVItem(Material.PLAYER_HEAD, 1);
            cvItem.setDisplayName(offlinePlayer.getName());
            if (MenuManager.getData(civilian.getUuid(), Constants.REGION) != null) {
                Region region = (Region) MenuManager.getData(civilian.getUuid(), Constants.REGION);
                addRank(civilian.getLocale(), cvItem, region.getRawPeople().get(offlinePlayer.getUniqueId()));
            } else if ((MenuManager.getData(civilian.getUuid(), Constants.TOWN) != null)) {
                Town town = (Town) MenuManager.getData(civilian.getUuid(), Constants.TOWN);
                addRank(civilian.getLocale(), cvItem, town.getRawPeople().get(offlinePlayer.getUniqueId()));
            }
            ItemStack itemStack = cvItem.createItemStack();
            if (player != null && Bukkit.getOnlineMode()) {
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                skullMeta.setOwningPlayer(player);
                itemStack.setItemMeta(skullMeta);
            }
            ((HashMap<ItemStack, UUID>) MenuManager.getData(civilian.getUuid(), "civMap")).put(itemStack, offlinePlayer.getUniqueId());
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    private void addRank(String locale, CVItem cvItem, String ranks) {
        if (ranks == null || ranks.isEmpty()) {
            return;
        }
        if (ranks.contains(Constants.OWNER)) {
            cvItem.getLore().add(LocaleManager.getInstance().getTranslation(locale, Constants.OWNER));
        }
        if (ranks.contains("member")) {
            cvItem.getLore().add(LocaleManager.getInstance().getTranslation(locale, "member"));
        }
        if (ranks.contains("recruiter")) {
            cvItem.getLore().add(LocaleManager.getInstance().getTranslation(locale, "recruiter"));
        }
        if (ranks.contains("ally")) {
            cvItem.getLore().add(LocaleManager.getInstance().getTranslation(locale, "guest"));
        }
    }

    @Override @SuppressWarnings("unchecked")
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        if ("take-action".equals(actionString)) {
            UUID uuid = ((HashMap<ItemStack, UUID>) MenuManager.getData(civilian.getUuid(), "civMap")).get(clickedItem);
            Region region = (Region) MenuManager.getData(civilian.getUuid(), Constants.REGION);
            Town town = (Town) MenuManager.getData(civilian.getUuid(), Constants.TOWN);
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (player == null) {
                return true;
            }
            Boolean invite = (Boolean) MenuManager.getData(civilian.getUuid(), "invite");
            if (region != null) {
                if (invite != null && invite && clickedItem.getItemMeta() != null) {
                    player.performCommand("cv add " + clickedItem.getItemMeta().getDisplayName() + " " + region.getId());
                } else {
                    HashMap<String, String> params = new HashMap<>();
                    params.put(Constants.REGION, region.getId());
                    params.put("uuid", uuid.toString());
                    MenuManager.getInstance().openMenu(player, "member-action", params);
                }
            } else if (town != null) {
                if (invite != null && invite && clickedItem.getItemMeta() != null) {
                    player.performCommand("cv invite " + clickedItem.getItemMeta().getDisplayName() + " " + town.getName());
                } else {
                    HashMap<String, String> params = new HashMap<>();
                    params.put(Constants.TOWN, town.getName());
                    params.put("uuid", uuid.toString());
                    MenuManager.getInstance().openMenu(player, "member-action", params);
                }
            } else {
                if (uuid == null) {
                    uuid = civilian.getUuid();
                }
                HashMap<String, String> params = new HashMap<>();
                params.put("uuid", uuid.toString());
                MenuManager.getInstance().openMenu(player, "player", params);
            }
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }
}

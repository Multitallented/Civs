package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.OwnershipUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

@CivsMenu(name = Constants.NATION) @SuppressWarnings("unused")
public class NationMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        if (!params.containsKey(Constants.NATION)) {
            return data;
        }
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        Nation nation = NationManager.getInstance().getNation(params.get(Constants.NATION));
        data.put(Constants.NATION, nation);
        if (nation.getLastRenamedBy() != null) {
            data.put("lastRenamed", nation.getLastRenamedBy().toString());
        }
        List<Town> townList = new ArrayList<>();
        for (String memberName : nation.getMembers()) {
            Town town = TownManager.getInstance().getTown(memberName);
            if (town == null) {
                continue;
            }
            townList.add(town);
        }
        data.put("townList", townList);
        data.put("power", nation.getPower());
        data.put("maxPower", nation.getMaxPower());
        data.put("claims", nation.getClaimCount());
        data.put("maxClaims", (int) Math.floor((double) nation.getPower() / ConfigManager.getInstance().getPowerPerNationClaim()));
        int maxPage = (int) Math.ceil((double) townList.size() / (double) itemsPerPage.get("members"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);

        return data;
    }


    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Nation nation = (Nation) MenuManager.getData(civilian.getUuid(), Constants.NATION);
        if (player == null || nation == null) {
            return new ItemStack(Material.AIR);
        }
        if (menuIcon.getActions().contains("set-capitol")) {
            if (setAirIfNoTownMember(civilian, nation)) {
                return new ItemStack(Material.AIR);
            }
        }
        if (menuIcon.getActions().contains("join-nation")) {
            Set<Town> ownedTowns = TownManager.getInstance().getOwnedTowns(civilian);
            if (ownedTowns.isEmpty()) {
                return new ItemStack(Material.AIR);
            }
            for (Town town : ownedTowns) {
                if (nation.getMembers().contains(town.getName())) {
                    return new ItemStack(Material.AIR);
                }
            }
        }
        if (menuIcon.getActions().contains("leave-nation")) {
            if (setAirIfNoTownMember(civilian, nation)) {
                return new ItemStack(Material.AIR);
            }
        }
        boolean isAuthorized = !OwnershipUtil.isNotAuthorized(player, nation);
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = nation.getIconAsCVItem();
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("constitution".equals(menuIcon.getKey())) {
            if (nation.getLore() == null) {
                return new ItemStack(Material.AIR);
            }
        } else if ("last-rename".equals(menuIcon.getKey())) {
            if (!isAuthorized || nation.getLastRenamedBy() == null) {
                return new ItemStack(Material.AIR);
            }
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nation.getLastRenamedBy());
            if (offlinePlayer.getName() != null) {
                CVItem lastRenameCVItem = menuIcon.createCVItem(player, count);
                lastRenameCVItem.setMat(Material.PLAYER_HEAD);
                ItemStack is = lastRenameCVItem.createItemStack();
                SkullMeta isMeta = (SkullMeta) is.getItemMeta();
                isMeta.setDisplayName(offlinePlayer.getName());
                isMeta.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                        "last-renamed-by").replace("$1", offlinePlayer.getName())));
                isMeta.setOwningPlayer(offlinePlayer);
                is.setItemMeta(isMeta);
                putActions(civilian, menuIcon, is, count);
                return is;
            } else {
                return new ItemStack(Material.AIR);
            }
        } else if ("rename".equals(menuIcon.getKey())) {
            if (!isAuthorized) {
                return new ItemStack(Material.AIR);
            }
        } else if ("capitol".equals(menuIcon.getKey())) {
            Town capitol = TownManager.getInstance().getTown(nation.getCapitol());
            if (capitol == null) {
                return new ItemStack(Material.AIR);
            }
            TownType townType = (TownType) ItemManager.getInstance().getItemType(capitol.getType());
            CVItem cvItem = townType.clone();
            cvItem.setDisplayName(capitol.getName());
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("members".equals(menuIcon.getKey())) {
            List<Town> townList = (List<Town>) MenuManager.getData(civilian.getUuid(), "townList");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (townList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Town town = townList.get(startIndex + count);
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.setLore(Util.textWrap(civilian, town.getSummary(player)));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    private boolean setAirIfNoTownMember(Civilian civilian, Nation nation) {
        Set<Town> ownedTowns = TownManager.getInstance().getOwnedTowns(civilian);
        if (ownedTowns.isEmpty()) {
            return true;
        }
        boolean noTownMember = true;
        for (Town town : ownedTowns) {
            if (nation.getMembers().contains(town.getName())) {
                noTownMember = false;
                break;
            }
        }
        if (noTownMember) {
            return true;
        }
        return false;
    }


    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack clickedItem) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        Nation nation = (Nation) MenuManager.getData(civilian.getUuid(), Constants.NATION);
        if (player == null || nation == null) {
            return true;
        }
        if ("get-lore".equals(actionString)) {
            ItemStack itemStack = nation.getLore();
            if (itemStack == null) {
                return true;
            }
            if (!player.getInventory().contains(itemStack)) {
                player.getInventory().addItem(itemStack);
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "item-received"));
            }
            return true;
        } else if ("set-capitol".equals(actionString)) {
            setCapitol(civilian, player, nation);
            return true;
        } else if ("leave-nation".equals(actionString)) {
            leaveNation(civilian, player, nation);
            return true;
        } else if ("join-nation".equals(actionString)) {
            joinNation(civilian, player, nation);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, clickedItem);
    }

    private static void leaveNation(Civilian civilian, Player player, Nation nation) {
        Set<Town> towns = TownManager.getInstance().getOwnedTowns(civilian);
        if (towns.size() == 1) {
            Town town = towns.iterator().next();
            leaveNation(player, nation, town);
        } else if (towns.size() > 1) {
            MenuManager.openMenuFromString(civilian, "select-town?nation=$nation$&leave=true");
        }
    }

    public static void leaveNation(Player player, Nation nation, Town town) {
        if (nation.getMembers().size() < 2) {
            NationManager.getInstance().removeNation(nation);
        } else {
            nation.getMembers().remove(town.getName());
            NationManager.getInstance().saveNation(nation);
        }
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "left-nation").replace("$1", town.getName())
                .replace("$2", nation.getName()));
        for (String townName : nation.getMembers()) {
            Town town1 = TownManager.getInstance().getTown(townName);
            for (UUID uuid : town1.getRawPeople().keySet()) {
                Player player1 = Bukkit.getPlayer(uuid);
                if (player1 != null) {
                    player1.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player1,
                            "left-nation").replace("$1", town.getName())
                            .replace("$2", nation.getName()));
                }
            }
        }
    }

    private void joinNation(Civilian civilian, Player player, Nation nation) {
        Set<Town> towns = TownManager.getInstance().getOwnedTowns(civilian);
        if (towns.size() == 1) {
            Town town = towns.iterator().next();
            joinNation(player, nation, town);
        } else if (towns.size() > 1) {
            MenuManager.openMenuFromString(civilian, "select-town?nation=$nation$&join=true");
        }
    }

    public static void joinNation(Player player, Nation nation, Town town) {
        nation.getNationApplications().add(town);
        String townName = nation.getCapitol();
        if (townName != null) {
            Town capitol = TownManager.getInstance().getTown(townName);
            for (Map.Entry<UUID, String> entry : capitol.getRawPeople().entrySet()) {
                if (entry.getValue().contains(Constants.OWNER)) {
                    Player player1 = Bukkit.getPlayer(entry.getKey());
                    if (player1 != null) {
                        sendInviteMessage(player, town, nation);
                    }
                }
            }
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                    "nation-app-sent").replace("$1", nation.getName()));
        }
    }

    private static void sendInviteMessage(Player player, Town town, Nation nation) {
        String inviteMessage = Civs.getRawPrefix() + LocaleManager.getInstance().getRawTranslation(player,
                "nation-invite").replace("$1", town.getName())
                .replace("$2", nation.getName());
        TextComponent component = Util.parseColorsComponent(inviteMessage);

        TextComponent acceptComponent = new TextComponent("[✔]");
        acceptComponent.setColor(ChatColor.GREEN);
        acceptComponent.setUnderlined(true);
        acceptComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cv acceptnation " + town.getName()));
        component.addExtra(acceptComponent);

        player.spigot().sendMessage(component);
    }

    private void setCapitol(Civilian civilian, Player player, Nation nation) {
        Set<Town> towns = TownManager.getInstance().getOwnedTowns(civilian);
        if (towns.size() == 1) {
            setCapitol(player, nation, towns.iterator().next());
        } else if (towns.size() > 1) {
            MenuManager.openMenuFromString(civilian, "select-town?nation=$nation$&capitol=true");
        }
    }

    public static void setCapitol(Player player, Nation nation, Town town) {
        if (nation.getCapitol() != null) {
            Town capitolTown = TownManager.getInstance().getTown(nation.getCapitol());
            if (town.getPopulation() < capitolTown.getPopulation()) {
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                        "capitol-must-exceed-pop").replace("$1", "" + capitolTown.getPopulation()));
                return;
            }
        }
        nation.setCapitol(town.getName());
        NationManager.getInstance().saveNation(nation);
        player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(player,
                "capitol-set").replace("$1", nation.getName())
                .replace("$2", town.getName()));
    }
}

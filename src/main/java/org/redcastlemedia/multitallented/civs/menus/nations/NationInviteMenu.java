package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
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
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "nation-invite") @SuppressWarnings("unused")
public class NationInviteMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        Nation nation = null;
        if (params.containsKey(Constants.NATION)) {
            nation = NationManager.getInstance().getNation(params.get(Constants.NATION));
            data.put(Constants.NATION, nation);
        } else {
            return new HashMap<>();
        }
        List<Town> apps = new ArrayList<>(nation.getNationApplications());
        data.put("apps", apps);
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        int maxPage = (int) Math.ceil((double) apps.size() / (double) itemsPerPage.get("apps"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        data.put("appMap", new HashMap<ItemStack, Town>());
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Nation nation = (Nation) MenuManager.getData(civilian.getUuid(), Constants.NATION);
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || nation == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = nation.getIconAsCVItem();
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("apps".equals(menuIcon.getKey())) {
            List<Town> townList = (List<Town>) MenuManager.getData(civilian.getUuid(), "apps");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (townList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Town town = townList.get(startIndex + count);
            Map<ItemStack, Town> townMap = (Map<ItemStack, Town>) MenuManager.getData(civilian.getUuid(), "appMap");
            CVItem cvItem = ItemManager.getInstance().getItemType(town.getType()).clone();
            cvItem.setDisplayName(town.getName());
            cvItem.setLore(Util.textWrap(civilian, town.getSummary(player)));
            ItemStack itemStack = cvItem.createItemStack();
            townMap.put(itemStack, town);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        Nation nation = (Nation) MenuManager.getData(civilian.getUuid(), Constants.NATION);
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || nation == null) {
            return true;
        }
        if ("decline-all-invites".equals(actionString)) {
            nation.getNationApplications().clear();
        } else if ("accept-application".equals(actionString)) {
            Map<ItemStack, Town> townMap = (Map<ItemStack, Town>) MenuManager.getData(civilian.getUuid(), "appMap");
            Town town = townMap.get(itemStack);
            NationManager.getInstance().addMemberToNation(nation, town);
            messageAllPlayersInNation(nation, town);
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }

    private void messageAllPlayersInNation(Nation nation, Town town) {
        for (String townName : nation.getMembers()) {
            Town cTown = TownManager.getInstance().getTown(townName);
            for (UUID uuid : cTown.getRawPeople().keySet()) {
                Player cPlayer = Bukkit.getPlayer(uuid);
                if (cPlayer != null) {
                    cPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(cPlayer,
                            "new-town-member").replace("$1", town.getName())
                            .replace("$2", nation.getName()));
                }
            }
        }
    }
}

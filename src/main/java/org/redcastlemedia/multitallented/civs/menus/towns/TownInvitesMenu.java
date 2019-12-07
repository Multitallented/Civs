package org.redcastlemedia.multitallented.civs.menus.towns;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.*;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CivsMenu(name = "town-invites") @SuppressWarnings("unused")
public class TownInvitesMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        Town town;
        if (params.containsKey("town")) {
            town = TownManager.getInstance().getTown(params.get("town"));
            data.put("town", town);
        } else {
            return new HashMap<>();
        }
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<String> inviteList = town.getAllyInvites();

        int maxPage = (int) Math.ceil((double) inviteList.size() / (double) itemsPerPage.get("invites"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("invites".equals(menuIcon.getKey())) {
            Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
            if (town == null) {
                return new ItemStack(Material.AIR);
            }
            List<String> inviteList = town.getAllyInvites();
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (inviteList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            String inviteName = inviteList.get(startIndex + count);
            Town inviteTown = TownManager.getInstance().getTown(inviteName);
            TownType inviteTownType = (TownType) ItemManager.getInstance().getItemType(inviteTown.getType());
            CVItem cvItem = inviteTownType.getShopIcon(civilian.getLocale()).clone();
            cvItem.setDisplayName(inviteTown.getName());
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    menuIcon.getDesc())));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        Town town = (Town) MenuManager.getData(civilian.getUuid(), "town");
        if (town == null) {
            return true;
        }
        if ("accept-invite".equals(actionString)) {
            String townName = itemStack.getItemMeta().getDisplayName();
            Town inviteTown = TownManager.getInstance().getTown(townName);
            town.getAllyInvites().remove(townName);
            AllianceManager.getInstance().allyTheseTowns(town, inviteTown);
            for (Player cPlayer : Bukkit.getOnlinePlayers()) {
                Civilian civilian1 = CivilianManager.getInstance().getCivilian(cPlayer.getUniqueId());
                cPlayer.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(civilian1.getLocale(),
                        "town-ally-request-accepted").replace("$1", town.getName())
                        .replace("$2", inviteTown.getName()));
            }
            TownManager.getInstance().saveTown(town);
            return true;
        } else if ("decline-all-invites".equals(actionString)) {
            town.getAllyInvites().clear();
            TownManager.getInstance().saveTown(town);
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}

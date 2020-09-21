package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.nations.Nation;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.util.Constants;

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
            // TODO
        } else if ("apps".equals(menuIcon.getKey())) {
            // TODO
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("decline-all-invites".equals(actionString)) {
            // TODO
        } else if ("accept-application".equals(actionString)) {
            // TODO
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}

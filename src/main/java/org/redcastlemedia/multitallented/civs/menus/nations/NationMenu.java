package org.redcastlemedia.multitallented.civs.menus.nations;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.nations.NationManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsMenu(name = Constants.NATION) @SuppressWarnings("unused")
public class NationMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        if (params.containsKey(Constants.NATION)) {
            data.put(Constants.NATION, NationManager.getInstance().getNation(params.get(Constants.NATION)));
        }
        return data;
    }


    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {

        return super.createItemStack(civilian, menuIcon, count);
    }

}

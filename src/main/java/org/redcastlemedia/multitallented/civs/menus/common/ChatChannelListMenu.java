package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;

@CivsMenu(name = "chat-channel-list") @SuppressWarnings("unused")
public class ChatChannelListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        return new HashMap<>();
    }

    @Override
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("icon".equals(menuIcon.getKey())) {
            // TODO finish this
        } else if ("chat-channels".equals(menuIcon.getKey())) {
            // TODO finish this
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("set-channel".equals(actionString)) {
            // TODO finish this
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}

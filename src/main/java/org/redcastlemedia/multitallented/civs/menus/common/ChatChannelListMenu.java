package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "chat-channel-list") @SuppressWarnings("unused")
public class ChatChannelListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        List<ChatChannel> channelList = new ArrayList<>();
        for (ChatChannel.ChatChannelType chatChannelType : ConfigManager.getInstance().getChatChannels().keySet()) {
            if (chatChannelType != ChatChannel.ChatChannelType.TOWN &&
                    chatChannelType != ChatChannel.ChatChannelType.NATION) {
                channelList.add(new ChatChannel(chatChannelType, null));
            }
        }
        if (ConfigManager.getInstance().getChatChannels().containsKey(ChatChannel.ChatChannelType.TOWN)) {
            for (Town town : TownManager.getInstance().getOwnedTowns(civilian)) {
                channelList.add(new ChatChannel(ChatChannel.ChatChannelType.TOWN, town));
            }
        }

        data.put("channelMap", new HashMap<ItemStack, ChatChannel>());
        data.put("channelList", channelList);

        int maxPage = (int) Math.ceil((double) channelList.size() / (double) itemsPerPage.get("chat-channels"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override @SuppressWarnings("unchecked")
    public ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if ("icon".equals(menuIcon.getKey())) {
            CVItem cvItem = menuIcon.createCVItem(player, count);
            cvItem.setDisplayName(civilian.getChatChannel().getName(player));
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    civilian.getChatChannel().getDesc(player))));
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("chat-channels".equals(menuIcon.getKey())) {
            List<ChatChannel> channelList =
                    (List<ChatChannel>) MenuManager.getData(civilian.getUuid(), "channelList");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (channelList.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            ChatChannel chatChannel = channelList.get(startIndex + count);
            CVItem cvItem;
            if (chatChannel.getChatChannelType() == ChatChannel.ChatChannelType.TOWN) {
                Town town = (Town) chatChannel.getTarget();
                TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
                cvItem = new CVItem(townType.getMat(), 1, 1, chatChannel.getName(player),
                        Util.textWrap(chatChannel.getDesc(player)));
            } else {
                cvItem = CVItem.createCVItemFromString(ConfigManager.getInstance().getChatChannels().get(chatChannel.getChatChannelType()));
                cvItem.setDisplayName(chatChannel.getName(player));
                cvItem.setLore(Util.textWrap(chatChannel.getDesc(player)));
            }
            ItemStack itemStack = cvItem.createItemStack();
            ((HashMap<ItemStack, ChatChannel>) MenuManager.getData(civilian.getUuid(), "channelMap"))
                    .put(itemStack, chatChannel);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override @SuppressWarnings("unchecked")
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("set-channel".equals(actionString)) {
            ChatChannel chatChannel = ((HashMap<ItemStack, ChatChannel>) MenuManager.getData(civilian.getUuid(), "channelMap"))
                    .get(itemStack);
            if (chatChannel != null) {
                civilian.setChatChannel(chatChannel);
            }
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}

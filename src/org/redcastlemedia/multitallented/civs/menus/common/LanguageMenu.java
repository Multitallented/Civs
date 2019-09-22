package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;

public class LanguageMenu extends CustomMenu {
    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if (menuIcon.getKey().equals("languages")) {
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            String[] languages = new String[LocaleManager.getInstance().getAllLanguages().size()];
            languages = LocaleManager.getInstance().getAllLanguages().toArray(languages);
            String language = languages[startIndex + count];
            CVItem cvItem = CVItem.createCVItemFromString(ChatColor.stripColor(LocaleManager.getInstance()
                    .getTranslation(language, "icon")));
            if (cvItem == null) {
                cvItem = new CVItem(Material.GRASS, count+1);
            }
            String name = LocaleManager.getInstance().getTranslation(language, "name");
            if (name == null) {
                name = "Error";
            }
            cvItem.setDisplayName(ChatColor.stripColor(name));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(language);
            cvItem.setLore(lore);
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, ItemStack cursorItem, ItemStack clickedItem) {
        if (!actions.containsKey(civilian.getUuid())) {
            return false;
        }
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return true;
        }
        List<String> actionStrings = actions.get(civilian.getUuid()).get(clickedItem);
        for (String actionString : actionStrings) {
            if (actionString.equals("select-lang")) {
                String itemName = clickedItem.getItemMeta().getDisplayName();
                String langKey = clickedItem.getItemMeta().getLore().get(0);
                civilian.setLocale(langKey);
                CivilianManager.getInstance().saveCivilian(civilian);
                Player player = Bukkit.getPlayer(civilian.getUuid());
                player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance()
                        .getTranslation(langKey, "language-set").replace("$1", itemName));
            }
        }
        return super.doActionAndCancel(civilian, cursorItem, clickedItem);
    }

        @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        int maxPage = (int) Math.ceil((double) LocaleManager.getInstance().getAllLanguages().size() /
                (double) itemsPerPage.get("members"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override
    public String getKey() {
        return "language";
    }

    @Override
    public String getFileName() {
        return "Language";
    }
}

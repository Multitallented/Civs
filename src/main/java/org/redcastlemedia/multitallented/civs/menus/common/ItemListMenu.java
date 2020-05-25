package org.redcastlemedia.multitallented.civs.menus.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.regions.effects.RepairEffect;

@CivsMenu(name = "item-list") @SuppressWarnings("unused")
public class ItemListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        List<CVItem> items = new ArrayList<>();
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }

        if (params.containsKey("items")) {
            String[] splitItems = params.get("items").split(",");
            for (String itemString : splitItems) {
                CVItem cvItem = convertItemStringToCVItem(itemString);
                if (cvItem != null) {
                    items.add(cvItem);
                }
            }
        }

        data.put("items", items);
        int maxPage = (int) Math.ceil((double) items.size() / (double) itemsPerPage.get("items"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("items".equals(menuIcon.getKey())) {
            List<CVItem> items = (List<CVItem>) MenuManager.getData(civilian.getUuid(), "items");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            CVItem[] itemArray = new CVItem[items.size()];
            itemArray = items.toArray(itemArray);
            if (itemArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            CVItem cvItem = itemArray[startIndex + count];
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    private CVItem convertItemStringToCVItem(String itemString) {
        String[] itemSplit = itemString.split("\\.");
        PotionEffectType potionEffectType = PotionEffectType.getByName(itemSplit[0]);
        if (potionEffectType != null) {
            return new CVItem(Material.POTION, 1, 100, itemString);
        }
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(itemSplit[0].toLowerCase()));
        if (enchantment != null) {
            return new CVItem(Material.ENCHANTED_BOOK, 1, 100, itemString);
        }
        try {
            Material material = Material.valueOf(itemSplit[0]);
            return new CVItem(material, 1, 100, itemString);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Do nothing
        }
        return null;
    }
}

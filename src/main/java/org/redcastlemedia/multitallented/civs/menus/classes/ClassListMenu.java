package org.redcastlemedia.multitallented.civs.menus.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsMenu(name = "class-list") @SuppressWarnings("unused")
public class ClassListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();

        List<CivClass> classes = new ArrayList<>(civilian.getCivClasses());
        classes.remove(civilian.getCurrentClass());
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        data.put("classes", classes);
        data.put("classMap", new HashMap<ItemStack, CivClass>());
        int maxPage = (int) Math.ceil((double) classes.size() / (double) itemsPerPage.get("classes"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("classes".equals(menuIcon.getKey())) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (player == null) {
                return new ItemStack(Material.AIR);
            }

            List<CivClass> items = (List<CivClass>) MenuManager.getData(civilian.getUuid(), "classes");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            CivClass[] itemArray = new CivClass[items.size()];
            itemArray = items.toArray(itemArray);
            if (itemArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            CivClass civClass = itemArray[startIndex + count];
            ClassType classType = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
            CVItem cvItem = classType.getShopIcon(player);
            cvItem.getLore().add(0, LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    "level").replace("$1", "" + civClass.getLevel()));
            ItemStack itemStack = cvItem.createItemStack();
            ((HashMap<ItemStack, CivClass>) MenuManager.getData(civilian.getUuid(), "classMap"))
                    .put(itemStack, civClass);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("switch-class".equals(actionString)) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (player == null) {
                return true;
            }

            CivClass civClass = ((HashMap<ItemStack, CivClass>) MenuManager.getData(civilian.getUuid(), "classMap"))
                    .get(itemStack);
            if (civClass != civilian.getCurrentClass()) {
                civilian.setCurrentClass(civClass);
                for (CivClass civClass1 : civilian.getCivClasses()) {
                    if (!civClass1.equals(civClass) && civClass1.isSelectedClass()) {
                        civClass1.setSelectedClass(false);
                        ClassManager.getInstance().saveClass(civClass1);
                    }
                }
                civClass.setSelectedClass(true);
                ClassManager.getInstance().saveClass(civClass);
                MenuManager.getAllData(civilian.getUuid()).put(Constants.CLASS, civClass);
                ClassManager.getInstance().loadPlayer(player, civilian);
                return true;
            }
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}

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
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "class-type-list") @SuppressWarnings("unused")
public class ClassTypeListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        List<ClassType> classes = new ArrayList<>();
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        if (params.containsKey("unlocked")) {
            data.put("unlocked", true);
            classes.addAll(ClassManager.getInstance().getUnlockedClasses(civilian));
        } else {
            for (CivItem civItem : ItemManager.getInstance().getAllItemTypes().values()) {
                if (civItem.getItemType() == CivItem.ItemType.CLASS) {
                    classes.add((ClassType) civItem);
                }
            }
        }
        data.put("classes", classes);
        data.put("classMap", new HashMap<ItemStack, ClassType>());

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

            List<ClassType> items = (List<ClassType>) MenuManager.getData(civilian.getUuid(), "classes");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            ClassType[] itemArray = new ClassType[items.size()];
            itemArray = items.toArray(itemArray);
            if (itemArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            ClassType classType = itemArray[startIndex + count];

            CVItem cvItem = classType.getShopIcon(civilian.getLocale());
            if (MenuManager.getAllData(civilian.getUuid()).containsKey("unlocked")) {
                List<String> unmetRequirements = ItemManager.getInstance().getAllUnmetRequirements(classType, civilian, false);
                if (!unmetRequirements.isEmpty()) {
                    cvItem.getLore().addAll(unmetRequirements);
                }
            }

            ItemStack itemStack = cvItem.createItemStack();
            ((HashMap<ItemStack, ClassType>) MenuManager.getData(civilian.getUuid(), "classMap"))
                    .put(itemStack, classType);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("switch-class".equals(actionString)) {
            ClassType classType = ((HashMap<ItemStack, ClassType>) MenuManager.getData(civilian.getUuid(), "classMap"))
                    .get(itemStack);
            if (!ItemManager.getInstance().hasItemUnlocked(civilian, classType)) {
                return true;
            }
            ClassManager.getInstance().createNewClass(civilian, classType);
            MenuManager.getAllData(civilian.getUuid()).put(Constants.CLASS, civilian.getCurrentClass());
            return true;
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}

package org.redcastlemedia.multitallented.civs.menus.spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.spells.SpellManager;
import org.redcastlemedia.multitallented.civs.spells.SpellType;
import org.redcastlemedia.multitallented.civs.util.Constants;

@CivsMenu(name = "spell-list") @SuppressWarnings("unused")
public class SpellListMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        List<SpellType> spellTypeList = new ArrayList<>();
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }

        Player player = Bukkit.getPlayer(civilian.getUuid());

        CivClass selectedClass = null;
        if (params.containsKey(Constants.CLASS)) {
            for (CivClass civClass : civilian.getCivClasses()) {
                if (civClass.getId() == Integer.parseInt(params.get(Constants.CLASS))) {
                    selectedClass = civClass;
                    String localClassName = LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                            selectedClass.getType() + LocaleConstants.NAME_SUFFIX);
                    data.put("classTypeName", localClassName);
                    data.put(Constants.CLASS, civClass);
                    break;
                }
            }
        }
        int selectedSlot = -1;
        if (params.containsKey("slot")) {
            selectedSlot = Integer.parseInt(params.get("slot"));
            data.put("slot", selectedSlot);
        }
        if (selectedClass != null && selectedSlot > -1) {
            spellTypeList.addAll(SpellManager.getInstance().getSpellsForSlot(selectedClass, selectedSlot));
        } else if (selectedClass != null) {
            spellTypeList.addAll(SpellManager.getInstance().getSpellsForClass(selectedClass));
        }
        data.put("spells", spellTypeList);
        data.put("spellMap", new HashMap<ItemStack, SpellType>());
        int maxPage = (int) Math.ceil((double) spellTypeList.size() / (double) itemsPerPage.get("spells"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("spells".equals(menuIcon.getKey())) {
            Player player = Bukkit.getPlayer(civilian.getUuid());
            if (player == null) {
                return new ItemStack(Material.AIR);
            }
            List<SpellType> items = (List<SpellType>) MenuManager.getData(civilian.getUuid(), "spells");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            SpellType[] itemArray = new SpellType[items.size()];
            itemArray = items.toArray(itemArray);
            if (itemArray.length <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            SpellType spellType = itemArray[startIndex + count];
            CVItem cvItem = spellType.getShopIcon(civilian.getLocale());
            List<String> unmetRequirements = ItemManager.getInstance().getAllUnmetRequirements(spellType, civilian, false);
            if (!unmetRequirements.isEmpty()) {
                cvItem.getLore().addAll(unmetRequirements);
            }
            ItemStack itemStack = cvItem.createItemStack();
            ((HashMap<ItemStack, SpellType>) MenuManager.getData(civilian.getUuid(), "spellMap"))
                    .put(itemStack, spellType);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }

    @Override @SuppressWarnings("unchecked")
    public boolean doActionAndCancel(Civilian civilian, String actionString, ItemStack itemStack) {
        if ("set-spell-slot".equals(actionString)) {
            CivClass civClass = (CivClass) MenuManager.getData(civilian.getUuid(), Constants.CLASS);
            if (civClass != null && MenuManager.getAllData(civilian.getUuid()).containsKey("slot")) {
                SpellType spellType = ((HashMap<ItemStack, SpellType>) MenuManager.getData(civilian.getUuid(), "spellMap"))
                        .get(itemStack);
                int slot = (int) MenuManager.getData(civilian.getUuid(), "slot");
                civClass.getSelectedSpells().put(civClass.getSpellSlotOrder().get(slot),
                        spellType.getProcessedName());
                return true;
            }
        }
        return super.doActionAndCancel(civilian, actionString, itemStack);
    }
}

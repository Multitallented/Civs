package org.redcastlemedia.multitallented.civs.menus;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;

public class ShopMenuTest extends TestUtil {

    @Before
    public void setup() {
        MenuManager.getInstance().clearOpenMenus();
    }

    @Test @SuppressWarnings("unchecked")
    public void shopMenuRootShouldNotDuplicateItems() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        Map<String, Object> data = MenuManager.menus.get("shop").createData(civilian, new HashMap<>());
        List<CivItem> shopItems = (List<CivItem>) data.get("shopItems");
        HashSet<CivItem> items = new HashSet<>();
        for (CivItem civItem : shopItems) {
            if (items.contains(civItem)) {
                fail("Duplicate item found " + civItem.getProcessedName());
            }
            items.add(civItem);
        }
    }

    @Test @SuppressWarnings("unchecked")
    public void shopMenuShopsShouldNotDuplicateItems() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        HashMap<String, String> params = new HashMap<>();
        params.put("parent", "shops");
        Map<String, Object> data = MenuManager.menus.get("shop").createData(civilian, params);
        List<CivItem> shopItems = (List<CivItem>) data.get("shopItems");
        HashSet<CivItem> items = new HashSet<>();
        for (CivItem civItem : shopItems) {
            if (items.contains(civItem)) {
                fail("Duplicate item found " + civItem.getProcessedName());
            }
            items.add(civItem);
        }
    }
}

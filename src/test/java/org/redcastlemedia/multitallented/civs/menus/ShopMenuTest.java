package org.redcastlemedia.multitallented.civs.menus;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.PlayerInventoryImpl;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

public class ShopMenuTest extends TestUtil {

    @Before
    public void setup() {
        MenuManager.getInstance().clearOpenMenus();
        RegionManager.getInstance().reload();
        TownManager.getInstance().reload();
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
        HashSet<String> items = new HashSet<>();
        for (CivItem civItem : shopItems) {
            if (items.contains(civItem.getProcessedName())) {
                fail("Duplicate item found " + civItem.getProcessedName());
            }
            items.add(civItem.getProcessedName());
        }
    }

    @Test @SuppressWarnings("unchecked")
    public void shopMenuShopsShouldNotDuplicateItemsForAdmins() {
        UUID uuid = new UUID(1, 9);
        Player player = mock(Player.class);
        when(player.isOp()).thenReturn(true);
        when(player.getInventory()).thenReturn(new PlayerInventoryImpl());
        when(Bukkit.getServer().getPlayer(uuid)).thenReturn(player);
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        HashMap<String, String> params = new HashMap<>();
        params.put("parent", "utilities");
        Map<String, Object> data = MenuManager.menus.get("shop").createData(civilian, params);
        List<CivItem> shopItems = (List<CivItem>) data.get("shopItems");
        HashSet<String> items = new HashSet<>();
        for (CivItem civItem : shopItems) {
            if (items.contains(civItem.getProcessedName())) {
                fail("Duplicate item found " + civItem.getProcessedName());
            }
            items.add(civItem.getProcessedName());
        }
    }

    @Test @SuppressWarnings("unchecked")
    public void shopMenuShouldNotContainEmptyFolders() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        Map<String, Object> data = MenuManager.menus.get("shop").createData(civilian, new HashMap<>());
        List<CivItem> shopItems = (List<CivItem>) data.get("shopItems");
        for (CivItem civItem : shopItems) {
            if ("defense".equals(civItem.getProcessedName())) {
                fail("Found empty defense folder in shop");
            }
        }
    }
}

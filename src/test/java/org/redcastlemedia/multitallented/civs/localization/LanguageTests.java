package org.redcastlemedia.multitallented.civs.localization;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

public class LanguageTests extends TestUtil {

    @Test
    public void localeTestShouldReturnProperLanguageString() {
        HashMap<String, String> mockLanguageMap = new HashMap<>();
        mockLanguageMap.put("no-region-type-found", "No se encontró ningún tipo de región");
        LocaleManager.getInstance().languageMap.put("es", mockLanguageMap);
        LocaleManager localeManager = LocaleManager.getInstance();
        Civilian civilian = new Civilian(TestUtil.player.getUniqueId(), "es", new HashMap<>(), null, new HashMap<>(),
                0, 0,0,0,0, 0, 0, false);

        assertEquals("No se encontró ningún tipo de región",
                localeManager.getTranslation(civilian.getLocale(), "no-region-type-found"));
    }

    @Test(expected = SuccessException.class)
    public void playerShouldNotBeAbleToDropItem() {
        RegionsTests.loadRegionTypeCobble();
        Item item = mock(Item.class);
        ItemStack itemStack = mock(ItemStack.class);
        ItemMeta itemMeta = mock(ItemMeta.class);
        when(itemMeta.getDisplayName()).thenReturn("Cobble");
        ArrayList<String> lore = new ArrayList<>();
        lore.add(TestUtil.player.getUniqueId().toString());
        lore.add("Civs Cobble");
        when(itemStack.hasItemMeta()).thenReturn(true);
        when(itemMeta.getLore()).thenReturn(lore);
        when(itemStack.getItemMeta()).thenReturn(itemMeta);
        when(item.getItemStack()).thenReturn(itemStack);
        doThrow(new SuccessException()).when(item).remove();
        PlayerDropItemEvent playerDropItemEvent = new PlayerDropItemEvent(TestUtil.player, item);
        CivilianListener civilianListener = new CivilianListener();
        civilianListener.onCivilianDropItem(playerDropItemEvent);
    }
}

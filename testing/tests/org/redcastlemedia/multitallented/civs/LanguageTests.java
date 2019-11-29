package org.redcastlemedia.multitallented.civs;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.bukkit.entity.Item;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.civilians.CivilianListener;
import org.redcastlemedia.multitallented.civs.regions.RegionsTests;

public class LanguageTests extends TestUtil {

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

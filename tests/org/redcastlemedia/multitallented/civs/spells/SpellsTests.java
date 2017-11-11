package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.items.ItemManager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpellsTests {
    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Test
    public void hungerSpellShouldReduceStamina() {
        Player player = mock(Player.class);
        when(player.getFoodLevel()).thenReturn(20);
        ArgumentCaptor<Integer> staminaCapture = ArgumentCaptor.forClass(Integer.class);
        loadSpellTypeHunger();
        Spell spell = new Spell("hunger", player, 1);
        spell.useAbility();
        verify(player).setFoodLevel(staminaCapture.capture());
        assertEquals(18, (int) staminaCapture.getValue());
    }

    public static void loadSpellTypeHunger() {
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "WOOL.14");
        config.set("type", "spell");
        config.set("name", "Hunger");
        ConfigurationSection components = new YamlConfiguration();
        ConfigurationSection component1 = new YamlConfiguration();
        ConfigurationSection yieldSection = new YamlConfiguration();
        yieldSection.set("stamina", -2);
        component1.set("yield", yieldSection);
        components.set("1", component1);
        config.set("components", components);
        ItemManager.getInstance().loadSpellType(config);
    }
}

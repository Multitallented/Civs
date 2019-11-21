package org.redcastlemedia.multitallented.civs.spells;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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

    @Test
    public void hungerSpellShouldBeOnCooldown() {
        Player player = mock(Player.class);
        UUID uuid = new UUID(1,8);
        when(player.getUniqueId()).thenReturn(uuid);
        CivilianManager.getInstance().createDefaultCivilian(player);
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        Spell spell2 = new Spell("hunger", player, 1);
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("cooldown", System.currentTimeMillis() + 40000);
        civilian.getStates().put("hunger.cooldown^1", new CivState(spell2, "cooldown^1", -1, -1, vars));
        when(player.getFoodLevel()).thenReturn(20);
        doThrow(new SuccessException()).when(player).setFoodLevel(Matchers.anyInt());
        loadSpellTypeHunger();
        Spell spell = new Spell("hunger", player, 1);
        spell.useAbility();
    }

    @Test
    public void hungerSpellShouldSetCooldown() {
        long currentTime = System.currentTimeMillis();
        Player player = mock(Player.class);
        UUID uuid = new UUID(1,6);
        when(player.getUniqueId()).thenReturn(uuid);
        CivilianManager.getInstance().createDefaultCivilian(player);
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        when(player.getFoodLevel()).thenReturn(20);
        loadSpellTypeHunger();
        Spell spell = new Spell("hunger", player, 1);
        spell.useAbility();
        assertTrue((long) civilian.getStates().get("hunger.cooldown^1").getVars().get("cooldown") >= currentTime + 10000);
    }

    public static void loadSpellTypeHunger() {
        FileConfiguration config = new YamlConfiguration();
        config.set("icon", "RED_WOOL");
        config.set("type", "spell");
        ConfigurationSection conditions = new YamlConfiguration();
        conditions.set("cooldown^1", 10000);
        config.set("conditions", conditions);
        ConfigurationSection components = new YamlConfiguration();
        ConfigurationSection component1 = new YamlConfiguration();
        ConfigurationSection yieldSection = new YamlConfiguration();
        yieldSection.set("stamina", -2);
        yieldSection.set("cooldown^1", 10000);
        component1.set("yield", yieldSection);
        components.set("1", component1);
        config.set("components", components);
        ItemManager.getInstance().loadSpellType(config, "Hunger");
    }
}

package org.redcastlemedia.multitallented.civs.spells;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.redcastlemedia.multitallented.civs.SuccessException;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;

public class SpellsTests extends TestUtil {

    private Civilian civilian;

    @Before
    public void setup() {
        if (ItemManager.getInstance().getItemType("hunger") == null) {
            loadSpellTypeHunger();
        }
        civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
    }

    @After
    public void teardown() {
        civilian.getStates().clear();
    }

    @Test
    public void defaultClassShouldHaveSpellsAvailableInSlot2() {
        CivClass civClass = new CivClass(new UUID(1, 5), TestUtil.player.getUniqueId(), "default");
        civClass.resetSpellSlotOrder();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType("default");
        assertFalse(classType.getSpellSlots().isEmpty());
        assertFalse(classType.getSpellSlots().get(2).isEmpty());
        assertFalse(SpellManager.getInstance().getSpellsForSlot(civClass, 2, false).isEmpty());
    }

    @Test
    public void varShouldBeCreatedFromConfig() {
        HashMap<String, Set<?>> mappedTargets = getBasicTargetMap();
        ConfigurationSection section = new MemoryConfiguration();
        section.set("variables.heal^1.target", "self");
        Spell spell = new Spell("empathy", TestUtil.player, 1);
        spell.createVariables(mappedTargets, section);
        assertNotNull(spell.getAbilityVariables().get("heal^1"));
    }

    @Test
    public void civStateShouldBeAddedForPassiveSpell() {
        SpellType spellType = (SpellType) ItemManager.getInstance().getItemType("chug");
        SpellManager.initPassiveSpell(civilian, spellType, TestUtil.player);
        boolean hasState = false;
        for (CivState state : civilian.getStates().values()) {
            if (state.getVars().containsKey("INSTANT_DRINK")) {
                hasState = true;
                break;
            }
        }
        assertTrue(hasState);
    }

    @NotNull
    private HashMap<String, Set<?>> getBasicTargetMap() {
        HashMap<String, Set<?>> mappedTargets = new HashMap<>();
        HashSet<Object> targets = new HashSet<>();
        targets.add(TestUtil.player);
        mappedTargets.put("self", targets);
        return mappedTargets;
    }

    @Test
    public void costsShouldBeMet() {
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.setMana(60);
        HashMap<String, Set<?>> mappedTargets = getBasicTargetMap();
        HashSet<String> fulfilledRequirements = new HashSet<>();
        String componentName = "1";
        ConfigurationSection config = new MemoryConfiguration();
        config.set("costs.mana", 35);
        Spell spell = new Spell("empathy", TestUtil.player, 1);
        boolean costsMet = spell.isCostsMet(mappedTargets, fulfilledRequirements, componentName, config);
        assertTrue(costsMet);
        assertEquals(60, civilian.getMana());
    }

    @Test
    public void alchemistClassShouldHaveSpellsAvailableInSlot2() {
        CivClass civClass = new CivClass(new UUID(1, 5), TestUtil.player.getUniqueId(), "alchemist");
        civClass.resetSpellSlotOrder();
        ClassType classType = (ClassType) ItemManager.getInstance().getItemType("alchemist");
        assertFalse(classType.getSpellSlots().isEmpty());
        assertFalse(classType.getSpellSlots().get(2).isEmpty());
        assertFalse(SpellManager.getInstance().getSpellsForSlot(civClass, 4, false).isEmpty());
    }

    @Test
    public void potionHealShouldNotBeUnlocked() {
        SpellType potionHeal = (SpellType) ItemManager.getInstance().getItemType("potion_heal");
        Civilian civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        assertFalse(ItemManager.getInstance().hasItemUnlocked(civilian, potionHeal));
    }

    @Test
    public void hungerSpellShouldReduceStamina() {
        Player player = mock(Player.class);
        when(player.getDisplayName()).thenReturn("Someone");
        when(player.getFoodLevel()).thenReturn(20);
        ArgumentCaptor<Integer> staminaCapture = ArgumentCaptor.forClass(Integer.class);
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
        Spell spell = new Spell("hunger", player, 1);
        spell.useAbility();
    }

    @Test
    public void hungerSpellShouldSetCooldown() {
        long currentTime = System.currentTimeMillis();
        Player player = mock(Player.class);
        when(player.getDisplayName()).thenReturn("Someone");
        UUID uuid = new UUID(1,6);
        when(player.getUniqueId()).thenReturn(uuid);
        CivilianManager.getInstance().createDefaultCivilian(player);
        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
        when(player.getFoodLevel()).thenReturn(20);
        Spell spell = new Spell("hunger", player, 1);
        spell.useAbility();
        assertTrue((long) civilian.getStates().get("hunger.cooldown^1").getVars().get("cooldown") >= currentTime + 10000);
    }

    private static void loadSpellTypeHunger() {
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
        ItemManager.getInstance().loadSpellType(config, "hunger");
    }
}

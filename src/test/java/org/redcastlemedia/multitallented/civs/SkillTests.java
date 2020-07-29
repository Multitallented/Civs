package org.redcastlemedia.multitallented.civs;

import static org.junit.Assert.assertEquals;

import org.bukkit.Material;
import org.junit.Before;
import org.junit.Test;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.skills.CivSkills;
import org.redcastlemedia.multitallented.civs.skills.Skill;
import org.redcastlemedia.multitallented.civs.skills.SkillManager;

public class SkillTests extends TestUtil {

    private Skill skill;
    private Civilian civilian;

    @Before
    public void setup() {
        civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        String skillName = CivSkills.CRAFTING.name().toLowerCase();
        skill = new Skill(skillName);
        civilian.getSkills().clear();
        civilian.getSkills().put(skillName, skill);
    }



    @Test
    public void expShouldCalcProperly() {
        skill.getAccomplishments().put(Material.OAK_PLANKS.name(), 9);
        skill.getAccomplishments().put(Material.CHEST.name(), 2);
        assertEquals(48, skill.getTotalExp(), 0.1);
    }

    @Test
    public void priceShouldGetDiscounted() {
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType("arrow_factory");
        skill.getAccomplishments().put(Material.OAK_PLANKS.name(), 9);
        skill.getAccomplishments().put(Material.CHEST.name(), 9);
        skill.getAccomplishments().put(Material.STICK.name(), 9);
        double expectedDiscount = (1.0 - (90.0 / 3000.0 * 0.3)) * regionType.getPrice();
        assertEquals(90.0, skill.getTotalExp(), 0.1);
        assertEquals(expectedDiscount, SkillManager.getInstance().getSkillDiscountedPrice(civilian, regionType), 0.001);
    }

    @Test
    public void skillBarShouldBeCorrectLength() {
        skill.getAccomplishments().put(Material.OAK_PLANKS.name(), 9);
        skill.getAccomplishments().put(Material.CHEST.name(), 9);
        skill.getAccomplishments().put(Material.BREAD.name(), 9);
        skill.getAccomplishments().put(Material.FURNACE.name(), 9);
        skill.getAccomplishments().put(Material.STONE_PICKAXE.name(), 9);
        skill.getAccomplishments().put(Material.OAK_PLANKS.name(), 9);
        skill.getAccomplishments().put(Material.STONE_AXE.name(), 9);
        skill.getAccomplishments().put(Material.OAK_BOAT.name(), 9);
        assertEquals(56, skill.getCurrentExpAsBar("en").length());
        assertEquals(24, skill.getExpToNextLevelAsBar("en").length());
    }

    @Test
    public void levelShouldCalcCorrectly() {
        skill.getAccomplishments().put(Material.OAK_PLANKS.name(), 9);
        skill.getAccomplishments().put(Material.CHEST.name(), 9);
        skill.getAccomplishments().put(Material.BREAD.name(), 9);
        skill.getAccomplishments().put(Material.FURNACE.name(), 9);
        skill.getAccomplishments().put(Material.STONE_PICKAXE.name(), 9);
        skill.getAccomplishments().put(Material.OAK_PLANKS.name(), 9);
        skill.getAccomplishments().put(Material.STONE_AXE.name(), 9);
        skill.getAccomplishments().put(Material.OAK_BOAT.name(), 9);
        skill.getAccomplishments().put(Material.CRAFTING_TABLE.name(), 9);
        skill.getAccomplishments().put(Material.RED_BED.name(), 9);
        skill.getAccomplishments().put(Material.STICK.name(), 9);
        skill.getAccomplishments().put(Material.OAK_STAIRS.name(), 9);
        skill.getAccomplishments().put(Material.OAK_TRAPDOOR.name(), 9);
        skill.getAccomplishments().put(Material.OAK_BUTTON.name(), 9);
        skill.getAccomplishments().put(Material.OAK_PRESSURE_PLATE.name(), 9);
        skill.getAccomplishments().put(Material.OAK_DOOR.name(), 9);
        skill.getAccomplishments().put(Material.OAK_FENCE.name(), 9);
        skill.getAccomplishments().put(Material.OAK_FENCE_GATE.name(), 9);
        skill.getAccomplishments().put(Material.OAK_SLAB.name(), 9);
        assertEquals(1, skill.getLevel());
    }

    @Test
    public void maxLevelSkillShouldDisplayCorrectly() {
        Skill skill = new Skill("sword");
        skill.getAccomplishments().put("COW", 10);
        skill.getAccomplishments().put("ZOMBIE", 10);
        skill.getAccomplishments().put("CHICKEN", 3);
        skill.getAccomplishments().put("PIG", 10);
        skill.getAccomplishments().put("SHEEP", 10);
        skill.getAccomplishments().put("SALMON", 5);
        skill.getAccomplishments().put("CREEPER", 10);
        skill.getAccomplishments().put("SKELETON", 10);
        skill.getAccomplishments().put("SPIDER", 9);
        skill.getAccomplishments().put("SQUID", 10);
        skill.getAccomplishments().put("ENDERMAN", 2);
        skill.getAccomplishments().put("PHANTOM", 10);
        skill.getAccomplishments().put("BEE", 10);
        civilian.getSkills().put("sword", skill);
        assertEquals(0, skill.getExpToNextLevel(), 0.01);
        assertEquals(300, skill.getCurrentLevelExp(), 0.01);
    }
}

package org.redcastlemedia.multitallented.civs.menus.people;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.skills.Skill;

@CivsMenu(name = "skills")
public class SkillsMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        UUID uuid = civilian.getUuid();
        if (params.containsKey("uuid")) {
            uuid =  UUID.fromString(params.get("uuid"));
            data.put("uuid", uuid);
        }
        List<Skill> skills = new ArrayList<>(CivilianManager.getInstance().getCivilian(uuid).getSkills().values());
        data.put("skills", skills);
        return data;
    }

    @Override
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        if ("skills".equals(menuIcon.getKey())) {
            List<Skill> skills = (List<Skill>) MenuManager.getData(civilian.getUuid(), "skills");

        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}

package org.redcastlemedia.multitallented.civs.menus.people;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.menus.MenuIcon;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.skills.Skill;
import org.redcastlemedia.multitallented.civs.skills.SkillManager;
import org.redcastlemedia.multitallented.civs.skills.SkillType;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsMenu(name = "skills") @SuppressWarnings("unused")
public class SkillsMenu extends CustomMenu {

    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        UUID uuid = civilian.getUuid();
        if (params.containsKey("uuid")) {
            uuid =  UUID.fromString(params.get("uuid"));
        }
        data.put("uuid", uuid);
        List<Skill> skills = new ArrayList<>(CivilianManager.getInstance().getCivilian(uuid).getSkills().values());
        data.put("skills", skills);
        int maxPage = (int) Math.ceil((double) skills.size() / (double) itemsPerPage.get("skills"));
        maxPage = maxPage > 0 ? maxPage - 1 : 0;
        data.put("maxPage", maxPage);
        if (params.containsKey("page")) {
            data.put("page", Integer.parseInt(params.get("page")));
        } else {
            data.put("page", 0);
        }
        return data;
    }

    @Override @SuppressWarnings("unchecked")
    protected ItemStack createItemStack(Civilian civilian, MenuIcon menuIcon, int count) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null) {
            return new ItemStack(Material.AIR);
        }
        if ("skills".equals(menuIcon.getKey())) {
            List<Skill> skills = (List<Skill>) MenuManager.getData(civilian.getUuid(), "skills");
            int page = (int) MenuManager.getData(civilian.getUuid(), "page");
            int startIndex = page * menuIcon.getIndex().size();
            if (skills.size() <= startIndex + count) {
                return new ItemStack(Material.AIR);
            }
            Skill skill = skills.get(startIndex + count);
            SkillType skillType = SkillManager.getInstance().getSkillType(skill.getType());
            CVItem cvItem = CVItem.createCVItemFromString(skillType.getIcon());
            String localSkillName = LocaleManager.getInstance().getRawTranslation(player,
                    skillType.getName() + LocaleConstants.SKILL_SUFFIX);
            cvItem.setQty(Math.max(1, skill.getLevel()));
            cvItem.setDisplayName(localSkillName);
            List<String> lore = new ArrayList<>();
            lore.add(LocaleManager.getInstance().getTranslation(player, "skill-bar")
                    .replace("$1", skill.getCurrentExpAsBar(civilian.getLocale()))
                    .replace("$2", skill.getExpToNextLevelAsBar(civilian.getLocale())));
            double currentLevelExp = skill.getCurrentLevelExp();
            lore.addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(player,
                    skillType.getName() + LocaleConstants.SKILL_DESC_SUFFIX)
                    .replace("$1", "" + skill.getLevel())
                    .replace("$2", "" + currentLevelExp)
                    .replace("$3", "" + (skill.getExpToNextLevel() + currentLevelExp))));
            cvItem.setLore(lore);
            ItemStack itemStack = cvItem.createItemStack();
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        } else if ("parent".equals(menuIcon.getKey())) {
            UUID uuid = (UUID) MenuManager.getData(civilian.getUuid(), "uuid");
            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            if (ConfigManager.getInstance().isSkinsInMenu()) {
                skullMeta.setOwningPlayer(offlinePlayer);
            }
            skullMeta.setDisplayName(offlinePlayer.getName());
            itemStack.setItemMeta(skullMeta);
            putActions(civilian, menuIcon, itemStack, count);
            return itemStack;
        }
        return super.createItemStack(civilian, menuIcon, count);
    }
}

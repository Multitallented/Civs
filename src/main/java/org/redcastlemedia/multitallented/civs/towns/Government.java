package org.redcastlemedia.multitallented.civs.towns;

import lombok.Getter;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Government {
    @Getter
    private final String name;
    @Getter
    private final GovernmentType governmentType;
    @Getter
    private final Set<GovTypeBuff> buffs;
    @Getter
    private final List<GovTransition> transitions;
    private final CVItem icon;

    public Government(String name, GovernmentType governmentType,
                      Set<GovTypeBuff> buffs, CVItem cvItem,
                      List<GovTransition> transitions) {
        this.name = name;
        this.governmentType = governmentType;
        this.buffs = buffs;
        this.icon = cvItem;
        this.transitions = transitions;
    }

    public CVItem getIcon(Civilian civilian) {
        return getIcon(civilian, true);
    }

    public CVItem getIcon(Civilian civilian, boolean isUseBuffs) {
        Player player = Bukkit.getPlayer(civilian.getUuid());
        CVItem cvItem = icon.clone();
        cvItem.setDisplayName(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                name.toLowerCase() + "-name"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Gov Type: " + name);
        lore.addAll(Util.textWrap(LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                name.toLowerCase() + "-desc")));
        if (isUseBuffs) {
            lore.addAll(getBuffDescriptions(civilian));
        }
        cvItem.setLore(lore);
        return cvItem;
    }

    public List<String> getBuffDescriptions(Civilian civilian) {
        ArrayList<String> lore = new ArrayList<>();
        Player player = Bukkit.getPlayer(civilian.getUuid());
        for (GovTypeBuff buff : buffs) {
            String applyString = getApplyString(buff);
            lore.addAll(Util.textWrap(civilian, LocaleManager.getInstance().getTranslationWithPlaceholders(
                    player, buff.getBuffType().name().toLowerCase() + "-buff-desc")
                    .replace("$1", buff.getAmount() + "")
                    .replace("$2", applyString)));
        }
        return lore;
    }

    String getApplyString(GovTypeBuff buff) {
        if (buff.getBuffType() == GovTypeBuff.BuffType.COST ||
                buff.getBuffType() == GovTypeBuff.BuffType.PAYOUT ||
                buff.getBuffType() == GovTypeBuff.BuffType.COOLDOWN) {
            StringBuilder stringBuilder = new StringBuilder();
            for (String groupName : buff.getGroups()) {
                stringBuilder.append(groupName);
                stringBuilder.append(", ");
            }
            for (String regionName : buff.getRegions()) {
                stringBuilder.append(regionName);
                stringBuilder.append(", ");
            }
            if (stringBuilder.length() < 1) {
                return "";
            }
            return stringBuilder.substring(0, stringBuilder.length() - 2);
        } else {
            return "";
        }
    }
}

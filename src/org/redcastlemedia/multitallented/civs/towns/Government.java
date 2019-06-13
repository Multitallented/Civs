package org.redcastlemedia.multitallented.civs.towns;

import lombok.Getter;

import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Government {
    @Getter
    private final GovernmentType governmentType;
    @Getter
    private final HashSet<GovTypeBuff> buffs;
    @Getter
    private final ArrayList<GovTransition> transitions;
    private final CVItem icon;

    public Government(GovernmentType governmentType,
                      HashSet<GovTypeBuff> buffs, CVItem cvItem,
                      ArrayList<GovTransition> transitions) {
        this.governmentType = governmentType;
        this.buffs = buffs;
        this.icon = cvItem;
        this.transitions = transitions;
    }

    public CVItem getIcon(String locale) {
        return getIcon(locale, true);
    }

    public CVItem getIcon(String locale, boolean isUseBuffs) {
        CVItem cvItem = icon.clone();
        cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(locale,
                governmentType.name().toLowerCase() + "-name"));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Gov Type: " + governmentType.name());
        lore.addAll(Util.textWrap("", LocaleManager.getInstance().getTranslation(locale,
                governmentType.name().toLowerCase() + "-desc")));
        if (isUseBuffs) {
            lore.addAll(getBuffDescriptions(locale));
        }
        cvItem.setLore(lore);
        return cvItem;
    }

    public ArrayList<String> getBuffDescriptions(String locale) {
        ArrayList<String> lore = new ArrayList<>();
        for (GovTypeBuff buff : buffs) {
            String applyString = getApplyString(buff);
            lore.addAll(Util.textWrap("", LocaleManager.getInstance().getTranslation(
                    locale, buff.getBuffType().name().toLowerCase() + "-buff-desc")
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

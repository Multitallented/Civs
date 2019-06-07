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
    private final HashMap<String, String> names;
    @Getter
    private final HashMap<String, String> descriptions;
    @Getter
    private final HashSet<GovTypeBuff> buffs;
    private final CVItem icon;

    public Government(GovernmentType governmentType,
                      HashMap<String, String> names,
                      HashMap<String, String> descriptions,
                      HashSet<GovTypeBuff> buffs, CVItem cvItem) {
        this.governmentType = governmentType;
        this.names = names;
        this.descriptions = descriptions;
        this.buffs = buffs;
        this.icon = cvItem;
    }

    public CVItem getIcon(String locale) {
        return getIcon(locale, true);
    }

    public CVItem getIcon(String locale, boolean isUseBuffs) {
        CVItem cvItem = icon.clone();
        cvItem.setDisplayName(names.get(locale));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Gov Type: " + governmentType.name());
        lore.addAll(Util.textWrap("", descriptions.get(locale)));
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

package org.redcastlemedia.multitallented.civs.towns;

import lombok.Getter;
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
        CVItem cvItem = icon.clone();
        cvItem.setDisplayName(names.get(locale));
        ArrayList<String> lore = new ArrayList<>();
        lore.add("Gov Type: " + governmentType.name());
        lore.addAll(Util.textWrap("", descriptions.get(locale)));
        cvItem.setLore(lore);
        return cvItem;
    }
}

package org.redcastlemedia.multitallented.civs.menus;

import org.bukkit.configuration.ConfigurationSection;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;
import lombok.Setter;

public class MenuIcon {
    @Getter @Setter
    private int index;
    @Getter @Setter
    private String icon;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String desc;

    public MenuIcon(ConfigurationSection section) {
        this.index = section.getInt("index", -1);
        this.icon = section.getString("icon", "STONE");
        this.name = section.getString("name", "items");
        this.desc = section.getString("desc", "items");
    }

    public MenuIcon(int index, String icon, String name, String desc) {
        this.index = index;
        this.name = name;
        this.icon = icon;
        this.desc = desc;
    }

    public CVItem createCVItem(String locale) {
        CVItem cvItem = CVItem.createCVItemFromString(icon);
        if (cvItem.getMmoItemType() == null) {
            cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(locale, name));
            cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(locale, desc)));
        }
        return cvItem;
    }
}

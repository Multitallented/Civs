package org.redcastlemedia.multitallented.civs.menus;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import lombok.Getter;
import lombok.Setter;

public class MenuIcon {
    @Getter
    private List<String> actions = new ArrayList<>();
    @Getter @Setter
    private ArrayList<Integer> index;
    @Getter @Setter
    private String icon;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private String desc;
    @Getter @Setter
    private String key;
    @Getter @Setter
    private String perm = "";

    public MenuIcon(String key, ConfigurationSection section) {
        this.key = key;
        if (section != null) {
            this.index = parseIndexArrayFromString(section.getString("index", "-1"));
            if (key.equals("back")) {
                MenuIcon backIcon = MenuManager.getInstance().getBackButton();
                this.icon = backIcon.getIcon();
                this.name = backIcon.getName();
                this.desc = backIcon.getDesc();
            } else if (key.equals("prev")) {
                MenuIcon prevButton = MenuManager.getInstance().getPrevButton();
                this.icon = prevButton.getIcon();
                this.name = prevButton.getName();
                this.desc = prevButton.getDesc();
            } else if (key.equals("next")) {
                MenuIcon nextButton = MenuManager.getInstance().getNextButton();
                this.icon = nextButton.getIcon();
                this.name = nextButton.getName();
                this.desc = nextButton.getDesc();
            } else {
                this.icon = section.getString("icon", "STONE");
                this.name = section.getString("name", "");
                this.desc = section.getString("desc", "");
                this.actions = section.getStringList("actions");
                this.perm = section.getString("permission", "");
            }
        }
    }
    public MenuIcon(String key, String icon, String name, String desc) {
        this.key = key;
        this.index = new ArrayList<>();
        this.name = name;
        this.icon = icon;
        this.desc = desc;
    }

    public MenuIcon(String key, int index, String icon, String name, String desc) {
        this.key = key;
        ArrayList<Integer> indexList = new ArrayList<>();
        indexList.add(index);
        this.index = indexList;
        this.name = name;
        this.icon = icon;
        this.desc = desc;
    }

    public CVItem createCVItem(String locale) {
        CVItem cvItem = CVItem.createCVItemFromString(icon);
        if (cvItem.getMmoItemType() == null) {
            if (!name.isEmpty()) {
                cvItem.setDisplayName(LocaleManager.getInstance().getTranslation(locale, name));
            }
            if (!desc.isEmpty()) {
                cvItem.setLore(Util.textWrap(LocaleManager.getInstance().getTranslation(locale, desc)));
            }
        }
        return cvItem;
    }

    public static ArrayList<Integer> parseIndexArrayFromString(String indexString) {
        ArrayList<Integer> indexList = new ArrayList<>();
        if (indexString.equals("-1")) {
            return indexList;
        }
        String[] splitList = indexString.split(",");
        for (String currentIndexString : splitList) {
            if (currentIndexString.contains("-")) {
                String[] currentSplit = currentIndexString.split("-");
                int startIndex = Integer.parseInt(currentSplit[0]);
                int endIndex = Integer.parseInt(currentSplit[1]);
                for (int i = startIndex; i <= endIndex; i++) {
                    indexList.add(i);
                }
            } else {
                indexList.add(Integer.parseInt(currentIndexString));
            }
        }
        return indexList;
    }
}

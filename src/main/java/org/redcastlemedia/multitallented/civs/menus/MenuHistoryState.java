package org.redcastlemedia.multitallented.civs.menus;

import lombok.Getter;

import java.util.Map;

@Getter
public class MenuHistoryState {
    private final String menuName;
    private final Map<String, Object> data;

    public MenuHistoryState(String menuName, Map<String, Object> data) {
        this.menuName = menuName;
        this.data = data;
    }
}

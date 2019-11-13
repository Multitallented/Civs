package org.redcastlemedia.multitallented.civs.menus.towns;

import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;

import java.util.HashMap;
import java.util.Map;

public class TownMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();

        return data;
    }

    @Override
    public String getFileName() {
        return "town";
    }
}

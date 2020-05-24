package org.redcastlemedia.multitallented.civs.menus.classes;

import java.util.HashMap;
import java.util.Map;

import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CivsMenu;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;

@CivsMenu(name = "class-type") @SuppressWarnings("unused")
public class ClassTypeMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        return data;
    }
}

package org.redcastlemedia.multitallented.civs.menus.people;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;

public class PlayerMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        HashMap<String, Object> data = new HashMap<>();
        if (params.containsKey("uuid")) {
            data.put("uuid", UUID.fromString(params.get("uuid")));
        }
        return data;
    }

    @Override
    public String getFileName() {
        return "player";
    }
}

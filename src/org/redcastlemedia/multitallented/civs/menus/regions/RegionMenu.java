package org.redcastlemedia.multitallented.civs.menus.regions;

import java.util.HashMap;
import java.util.Map;

import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.menus.CustomMenu;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

public class RegionMenu extends CustomMenu {
    @Override
    public Map<String, Object> createData(Civilian civilian, Map<String, String> params) {
        Map<String, Object> data = new HashMap<>();
        if (!params.containsKey("region")) {
            return data;
        }
        Region region = RegionManager.getInstance().getRegionById(params.get("region"));
        data.put("region", region);
        StringBuilder failingUpkeeps = new StringBuilder();
        for (Integer i : region.getFailingUpkeeps()) {
            failingUpkeeps.append(i).append(",");
        }
        failingUpkeeps.substring(0, failingUpkeeps.length() - 1);
        data.put("failingUpkeeps", failingUpkeeps.toString());
        data.put("regionType", region.getType());
        return data;
    }

    @Override
    public String getFileName() {
        return "region";
    }
}

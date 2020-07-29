package org.redcastlemedia.multitallented.civs.civilians.allowedactions;

import java.util.List;
import java.util.Map;

public final class AllowedActionsUtil {
    private AllowedActionsUtil() {

    }

    public static void loadAllowedActions(Map<String, Integer> allowedActions,
                                          List<String> configList) {
        for (String action : configList) {
            String[] actionSplit = action.split("\\.");
            int level = 1;
            if (actionSplit.length > 1) {
                level = Integer.parseInt(actionSplit[1]);
            } else {
                String[] matSplit = action.split("_");
                if (matSplit.length > 1) {
                    switch (matSplit[0]) {
                        case "NETHERITE":
                            allowedActions.put("NETHERITE_" + matSplit[1], 1);
                        case "DIAMOND":
                            allowedActions.put("DIAMOND_" + matSplit[1], 1);
                        case "IRON":
                            allowedActions.put("IRON_" + matSplit[1], 1);
                        case "GOLDEN":
                            allowedActions.put("GOLDEN_" + matSplit[1], 1);
                        case "LEATHER":
                            allowedActions.put("LEATHER_" + matSplit[1], 1);
                        case "STONE":
                            allowedActions.put("STONE_" + matSplit[1], 1);
                            break;
                        default:
                            break;
                    }
                }
            }
            allowedActions.put(actionSplit[0], level);
        }
    }
}

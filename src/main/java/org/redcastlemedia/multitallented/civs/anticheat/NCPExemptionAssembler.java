package org.redcastlemedia.multitallented.civs.anticheat;

import java.util.HashSet;
import java.util.Set;

import fr.neatmonster.nocheatplus.checks.CheckType;

public final class NCPExemptionAssembler {
    private NCPExemptionAssembler() {

    }

    public static Set<CheckType> mapExemptionTypeToCheatTypes(ExemptionType exemptionType) {
        Set<CheckType> checks = new HashSet<>();
        switch (exemptionType) {
            case FLY:
                checks.add(CheckType.MOVING_SURVIVALFLY);
                checks.add(CheckType.MOVING);
                break;
            case FALL:
                checks.add(CheckType.MOVING_NOFALL);
                break;
            case JESUS:
                checks.add(CheckType.MOVING_SURVIVALFLY);
                checks.add(CheckType.MOVING);
                break;
            case KILL_AURA:
                checks.add(CheckType.FIGHT);
                break;
            case FAST_BREAK:
                checks.add(CheckType.BLOCKBREAK);
                break;
            case FAST_PLACE:
                checks.add(CheckType.BLOCKPLACE);
                break;
            default:
                break;
        }
        return checks;
    }
}

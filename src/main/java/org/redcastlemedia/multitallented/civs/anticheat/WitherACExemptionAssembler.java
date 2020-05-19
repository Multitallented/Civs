package org.redcastlemedia.multitallented.civs.anticheat;

import java.util.HashSet;
import java.util.Set;

import com.gmail.olexorus.witherac.api.CheckType;

public final class WitherACExemptionAssembler {
    private WitherACExemptionAssembler() {

    }

    public static Set<CheckType> mapExemptionTypeToCheckType(ExemptionType exemptionType) {
        Set<CheckType> checkTypes = new HashSet<>();
        switch (exemptionType) {
            case FLY:
                checkTypes.add(CheckType.FLY);
                checkTypes.add(CheckType.SPEED);
                break;
            case FAST_PLACE:
            case FAST_BREAK:
                checkTypes.add(CheckType.BUILD_ANGLE);
                break;
            case KILL_AURA:
                checkTypes.add(CheckType.REACH);
                break;
            default:
                checkTypes.add(CheckType.SPEED);
                break;
        }
        return checkTypes;
    }
}

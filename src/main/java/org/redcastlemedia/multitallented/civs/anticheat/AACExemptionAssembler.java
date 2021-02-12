package org.redcastlemedia.multitallented.civs.anticheat;

import java.util.HashSet;
import java.util.Set;

import me.konsolas.aac.api.HackType;

public final class AACExemptionAssembler {
    private AACExemptionAssembler() {

    }
    public static Set<HackType> mapExemptionTypeToHackTypes(ExemptionType exemptionType) {
        Set<HackType> hackTypes = new HashSet<>();
        switch (exemptionType) {
            case FLY:
            case JESUS:
                hackTypes.add(HackType.MOVE);
                break;
            case FALL:
                hackTypes.add(HackType.NOFALL);
                break;
            case KILL_AURA:
                hackTypes.add(HackType.CRITICALS);
                hackTypes.add(HackType.FIGHTSPEED);
                hackTypes.add(HackType.HITBOX);
                hackTypes.add(HackType.KILLAURA);
                hackTypes.add(HackType.FASTBOW);
                hackTypes.add(HackType.INTERACT);
                break;
            case FAST_BREAK:
                hackTypes.add(HackType.FASTBREAK);
                hackTypes.add(HackType.INTERACT);
                hackTypes.add(HackType.FASTUSE);
                break;
            case FAST_PLACE:
                hackTypes.add(HackType.FASTPLACE);
                hackTypes.add(HackType.INTERACT);
                hackTypes.add(HackType.FASTUSE);
                break;
            default:
                break;
        }
        return hackTypes;
    }
}

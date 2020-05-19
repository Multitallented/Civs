package org.redcastlemedia.multitallented.civs.anticheat;

import java.util.HashSet;
import java.util.Set;

import me.vagdedes.spartan.system.Enums;

public final class SpartanExemptionAssembler {
    private SpartanExemptionAssembler() {

    }

    public static Set<Enums.HackType> mapExemptionTypeToHackType(ExemptionType exemptionType) {
        Set<Enums.HackType> hackTypes = new HashSet<>();
        switch (exemptionType) {
            case FLY:
                hackTypes.add(Enums.HackType.Fly);
                break;
            case JESUS:
                hackTypes.add(Enums.HackType.Jesus);
                break;
            case KILL_AURA:
                hackTypes.add(Enums.HackType.Criticals);
                hackTypes.add(Enums.HackType.HitReach);
                hackTypes.add(Enums.HackType.KillAura);
                hackTypes.add(Enums.HackType.NoSwing);
                hackTypes.add(Enums.HackType.CombatAnalysis);
                hackTypes.add(Enums.HackType.FastBow);
                hackTypes.add(Enums.HackType.Nuker);
                break;
            default:
                hackTypes.add(Enums.HackType.Jesus);
                break;
        }
        return hackTypes;
    }
}

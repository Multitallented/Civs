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
                hackTypes.add(Enums.HackType.IrregularMovements);
                hackTypes.add(Enums.HackType.Velocity);
                hackTypes.add(Enums.HackType.Sprint);
                break;
            case JESUS:
                hackTypes.add(Enums.HackType.Jesus);
                hackTypes.add(Enums.HackType.IrregularMovements);
                hackTypes.add(Enums.HackType.Velocity);
                hackTypes.add(Enums.HackType.Sprint);
                hackTypes.add(Enums.HackType.Liquids);
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
            case FAST_BREAK:
                hackTypes.add(Enums.HackType.BlockReach);
                hackTypes.add(Enums.HackType.FastBreak);
                hackTypes.add(Enums.HackType.GhostHand);
                break;
            case FAST_PLACE:
                hackTypes.add(Enums.HackType.BlockReach);
                hackTypes.add(Enums.HackType.FastPlace);
                hackTypes.add(Enums.HackType.GhostHand);
                break;
            default:
                hackTypes.add(Enums.HackType.IrregularMovements);
                hackTypes.add(Enums.HackType.ImpossibleActions);
                hackTypes.add(Enums.HackType.Exploits);
                break;
        }
        return hackTypes;
    }
}

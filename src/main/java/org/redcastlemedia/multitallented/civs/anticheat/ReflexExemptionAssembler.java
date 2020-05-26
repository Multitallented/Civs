package org.redcastlemedia.multitallented.civs.anticheat;

import java.util.HashSet;
import java.util.Set;

import rip.reflex.api.Cheat;

public final class ReflexExemptionAssembler {
    private ReflexExemptionAssembler() {

    }
    public static Set<Cheat> mapExemptionTypeToCheats(ExemptionType exemptionType) {
        Set<Cheat> cheats = new HashSet<>();
        switch(exemptionType) {
            case FLY:
                cheats.add(Cheat.Fly);
                break;
            case FALL:
                cheats.add(Cheat.Fall);
                break;
            case JESUS:
                cheats.add(Cheat.ElytraMove);
                cheats.add(Cheat.Speed);
                break;
            case KILL_AURA:
                cheats.add(Cheat.AntiKnockback);
                cheats.add(Cheat.Criticals);
                cheats.add(Cheat.KillAura);
                break;
            case FAST_BREAK:
            case FAST_PLACE:
                cheats.add(Cheat.BlockActions);
                break;
            default:
                break;
        }
        return cheats;
    }
}

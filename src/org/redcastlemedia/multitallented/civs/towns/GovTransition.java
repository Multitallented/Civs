package org.redcastlemedia.multitallented.civs.towns;

import lombok.Getter;

@Getter
public class GovTransition {
    private final int revolt;
    private final int moneyGap;
    private final int power;
    private final long inactive;
    private final GovernmentType transitionGovernmentType;


    public GovTransition(int revolt, int moneyGap, int power,
                         long inactive, GovernmentType transitionGovernmentType) {
        this.revolt = revolt;
        this.moneyGap = moneyGap;
        this.power = power;
        this.inactive = inactive;
        this.transitionGovernmentType = transitionGovernmentType;
    }
}

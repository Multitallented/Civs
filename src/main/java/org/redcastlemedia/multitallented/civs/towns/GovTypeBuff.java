package org.redcastlemedia.multitallented.civs.towns;

import lombok.Getter;

import java.util.HashSet;

@Getter
public class GovTypeBuff {
    private final BuffType buffType;
    private final int amount;
    private final HashSet<String> groups;
    private final HashSet<String> regions;

    public GovTypeBuff(BuffType buffType,
                       int amount,
                       HashSet<String> groups,
                       HashSet<String> regions) {
        this.buffType = buffType;
        this.amount = amount;
        this.groups = groups;
        this.regions = regions;
    }


    public enum BuffType {
        COOLDOWN,
        PAYOUT,
        POWER,
        MAX_POWER,
        COST
    }
}

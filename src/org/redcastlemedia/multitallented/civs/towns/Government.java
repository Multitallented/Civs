package org.redcastlemedia.multitallented.civs.towns;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;

@Getter
public class Government {
    private final GovernmentType governmentType;
    private final HashMap<String, String> names;
    private final HashMap<String, String> descriptions;
    private final HashSet<GovTypeBuff> buffs;

    public Government(GovernmentType governmentType,
                      HashMap<String, String> names,
                      HashMap<String, String> descriptions,
                      HashSet<GovTypeBuff> buffs) {
        this.governmentType = governmentType;
        this.names = names;
        this.descriptions = descriptions;
        this.buffs = buffs;
    }
}

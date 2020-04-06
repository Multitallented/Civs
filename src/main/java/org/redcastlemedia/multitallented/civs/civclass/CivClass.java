package org.redcastlemedia.multitallented.civs.civclass;


import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class CivClass {

    @Getter
    private final String type;
    @Getter
    private final UUID uuid;
    @Getter
    private final int id;
    @Getter @Setter
    private int manaPerSecond;
    @Getter @Setter
    private int maxMana;

    public CivClass(int id, UUID uuid, String type, int manaPerSecond, int maxMana) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.manaPerSecond = manaPerSecond;
        this.maxMana = maxMana;
    }
}

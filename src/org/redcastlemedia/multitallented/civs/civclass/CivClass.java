package org.redcastlemedia.multitallented.civs.civclass;


import java.util.UUID;

public class CivClass {

    private final String type;
    private final UUID uuid;
    private final int id;
    private final int manaPerSecond;
    private final int maxMana;

    public CivClass(int id, UUID uuid, String type, int manaPerSecond, int maxMana) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.manaPerSecond = manaPerSecond;
        this.maxMana = maxMana;
    }
    public String getType() {
        return type;
    }
    public UUID getUuid() {
        return uuid;
    }
    public int getId() {
        return id;
    }
    public int getManaPerSecond() {
        return manaPerSecond;
    }
    public int getMaxMana() {
        return maxMana;
    }
}

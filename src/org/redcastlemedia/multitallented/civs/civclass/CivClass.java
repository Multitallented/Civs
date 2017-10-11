package org.redcastlemedia.multitallented.civs.civclass;


import java.util.UUID;

public class CivClass {

    private final String type;
    private final UUID uuid;
    private final int id;

    public CivClass(int id, UUID uuid, String type) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
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
}

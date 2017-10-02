package org.redcastlemedia.multitallented.civs.civilians;

import java.util.UUID;

public class Civilian {

    private final UUID uuid;
    private String locale;

    public Civilian(UUID uuid, String locale) {
        this.uuid = uuid;
        this.locale = locale;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLocale() {
        return locale;
    }
    void setLocale(String locale) {
        this.locale = locale;
    }
}

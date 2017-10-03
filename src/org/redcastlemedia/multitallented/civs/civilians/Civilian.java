package org.redcastlemedia.multitallented.civs.civilians;

import org.redcastlemedia.multitallented.civs.util.CVItem;

import java.util.ArrayList;
import java.util.UUID;

public class Civilian {

    private final UUID uuid;
    private String locale;
    private final ArrayList<CVItem> items;

    public Civilian(UUID uuid, String locale, ArrayList<CVItem> items) {
        this.uuid = uuid;
        this.locale = locale;
        this.items = items;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLocale() {
        return locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    public ArrayList<CVItem> getItems() {
        return items;
    }
}

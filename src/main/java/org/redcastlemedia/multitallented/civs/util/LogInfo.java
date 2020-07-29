package org.redcastlemedia.multitallented.civs.util;

import org.redcastlemedia.multitallented.civs.Civs;

public final class LogInfo {
    private LogInfo() {

    }

    public static String NAME = Civs.getInstance().getDescription().getName();
    public static String DESC = "A powerful town plugin for RPG/RTS themed servers.\n| Allows you to build towns\n| " +
            "with defenses, farms mines and more.\n|\n| Formerly HeroStronghold / Townships.";
    public static String VERSION = Civs.getInstance().getDescription().getVersion();
    public static String AUTHOR = "Multitallented";
    public static String CONTRIBUTORS = "Clockworker";
    public static String ENABLED = NAME + " Version: " + VERSION + " is now enabled!";
    public static String DISABLED = NAME + " Version: " + VERSION + " is now disabled!";
    public static String INFO = "\n"
            + "----------------------------------------------------------------------\n| "
            + "Welcome to " + NAME + "!\n|\n| Plugin Version: " + VERSION + "\n| Author: "
            + AUTHOR + "\n| Contributors: " + CONTRIBUTORS + "\n| Description: " + DESC
            + "\n----------------------------------------------------------------------";
    public static String HOOKECON = "| Hooked into Economy plugin: ";
    public static String HOOKPERM = "| Hooked into Permission plugin: ";
    public static String HOOKCHAT = "| Hooked into Chat Plugin: ";
    public static String HOOKITEMS = "| Hooked into Item Manager Plugin: ";
    public static String PH_INFO = "----------------------------------------------";
    public static String PH_VOID = " ";

    public static String getPrefix() {
        return "[" + NAME + "] ";
    }
}

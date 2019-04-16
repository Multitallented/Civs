package org.redcastlemedia.multitallented.civs;

public class LogInfo
{
    public static String NAME = "Civs";
    public static String DESC = "A powerful Town plugin for RPG/RTS Themed servers.\n| Allows you to build towns\n| with defenses, farms mines and more.\n|\n| Former HeroStronghold / Townships.";
    public static String VERSION = "0.0.1";
    public static String AUTHOR = "Multitallented";
    public static String CONTRIBUTORS = "Clockworker";
    public static String ENABLED = NAME + " Version: " + VERSION + " is now enabled!";
    public static String DISABLED = NAME + " Version: " + VERSION + " is now disabled!";
    public static String INFO = "\n"
            + "----------------------------------------------------------------------\n| "
            + "Welcome to Civs!\n|\n| Plugin Name: " + NAME + "\n| Plugin Version: " + VERSION + "\n| Author: "
            + AUTHOR + "\n| Contributors: " + CONTRIBUTORS + "\n| Description: " + DESC
            + "\n----------------------------------------------------------------------";
    public static String HOOKECON = "| Hooked into Economy plugin: ";
    public static String HOOKPERM = "| Hooked into Permission plugin: ";
    public static String HOOKCHAT = "| Hooked into Chat Plugin: ";
    public static String PH_INFO = "----------------------------------------------";
    public static String PH_VOID = " ";

    public static String getPrefix() {
        return "[" + NAME + "] ";
    }
}

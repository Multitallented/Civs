package org.redcastlemedia.multitallented.civs.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;

public class Util {

    public static ArrayList<String> textWrap(String prefix, String input) {
        ArrayList<String> lore = new ArrayList<>();
        String sendMe = new String(input);
        String[] sends = sendMe.split(" ");
        String outString = "";
        for (String s : sends) {
            if (outString.length() > 40) {
                lore.add(outString);
                outString = "";
            }
            if (!outString.equals("")) {
                outString += prefix + " ";
            } else {
                outString += prefix;
            }
            outString += s;
        }
        lore.add(outString);
        return lore;
    }
    public static String parseColors(String input) {
        input = input.replaceAll("@\\{AQUA\\}", ChatColor.AQUA + "");
        input = input.replaceAll("@\\{BLACK\\}", ChatColor.BLACK + "");
        input = input.replaceAll("@\\{BLUE\\}", ChatColor.BLUE + "");
        input = input.replaceAll("@\\{BOLD\\}", ChatColor.BOLD + "");
        input = input.replaceAll("@\\{DARK_AQUA\\}", ChatColor.DARK_AQUA + "");
        input = input.replaceAll("@\\{DARK_BLUE\\}", ChatColor.DARK_BLUE + "");
        input = input.replaceAll("@\\{DARK_GRAY\\}", ChatColor.DARK_GRAY + "");
        input = input.replaceAll("@\\{DARK_GREEN\\}", ChatColor.DARK_GREEN + "");
        input = input.replaceAll("@\\{DARK_PURPLE\\}", ChatColor.DARK_PURPLE + "");
        input = input.replaceAll("@\\{DARK_RED\\}", ChatColor.DARK_RED + "");
        input = input.replaceAll("@\\{GOLD\\}", ChatColor.GOLD + "");
        input = input.replaceAll("@\\{GREEN\\}", ChatColor.GREEN + "");
        input = input.replaceAll("@\\{ITALIC\\}", ChatColor.ITALIC + "");
        input = input.replaceAll("@\\{LIGHT_PURPLE\\}", ChatColor.LIGHT_PURPLE + "");
        input = input.replaceAll("@\\{MAGIC\\}", ChatColor.MAGIC + "");
        input = input.replaceAll("@\\{RED\\}", ChatColor.RED + "");
        input = input.replaceAll("@\\{RESET\\}", ChatColor.RESET + "");
        input = input.replaceAll("@\\{STRIKETHROUGH\\}", ChatColor.STRIKETHROUGH + "");
        input = input.replaceAll("@\\{UNDERLINE\\}", ChatColor.UNDERLINE + "");
        input = input.replaceAll("@\\{WHITE\\}", ChatColor.WHITE + "");
        input = input.replaceAll("@\\{YELLOW\\}", ChatColor.YELLOW + "");
        return input;
    }
    public static boolean isSolidBlock(Material type) {
        return type != Material.AIR &&
                type != Material.LEVER &&
                type != Material.WALL_SIGN &&
                type != Material.TORCH &&
                type != Material.STONE_BUTTON &&
                type != Material.WOOD_BUTTON;
    }
    public static boolean validateFileName(String fileName) {
        return fileName.matches("^[^.\\\\/:*?\"<>|]?[^\\\\/:*?\"<>|]*")
                && getValidFileName(fileName).length()>0;
    }

    public static String getValidFileName(String fileName) throws IllegalStateException {
        String newFileName = fileName.replaceAll("^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*", "");
        if(newFileName.length()==0)
            throw new IllegalStateException(
                    "File Name " + fileName + " results in a empty fileName!");
        return newFileName;
    }
}

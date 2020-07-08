package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.util.Constants;

import java.util.HashMap;

@CivsCommand(keys = { "menu" })
public class MenuCommand extends CivCommand {

    public boolean runCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to open menu for non-players");
            return true;
        }
        Player player = (Player) commandSender;

        if (Civs.perm != null && Civs.perm.has(player, Constants.MENU_PERMISSION)) {
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslationWithPlaceholders(player,
                    LocaleConstants.PERMISSION_DENIED));
            return true;
        }

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        if (civilian.getTutorialIndex() == -1) {
            TutorialManager.getInstance().sendMessageForCurrentTutorialStep(civilian, true);
            civilian.setTutorialIndex(0);
            CivilianManager.getInstance().saveCivilian(civilian);
            return true;
        }
        String menuName;
        HashMap<String, String> params = new HashMap<>();
        if (strings.length < 2) {
            menuName = "main";
        } else {
            menuName = strings[1].split("\\?")[0];
            if (strings[1].contains("?")) {
                for (String param : strings[1].split("\\?")[1].split("&")) {
                    params.put(param.split("=")[0], param.split("=")[1]);
                }
            }
        }
        MenuManager.getInstance().openMenu(player, menuName, params);
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return commandSender instanceof Player;
    }
}

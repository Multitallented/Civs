package org.redcastlemedia.multitallented.civs.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialPath;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialStep;
import org.redcastlemedia.multitallented.civs.util.Constants;
import org.redcastlemedia.multitallented.civs.util.Util;

@CivsCommand(keys = { "advancetut" }) @SuppressWarnings("unused")
public class TutorialAdvanceCommand extends CivCommand {

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (Civs.perm == null || !Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION)) {
            Util.sendMessageToPlayerOrConsole(commandSender, "no-permission", "You don't have permission to use /cv advancetut PlayerName");
            return true;
        }
        if (args.length < 2) {
            Util.sendMessageToPlayerOrConsole(commandSender, "invalid-target", "Invalid command. Use /cv advancetut PlayerName");
            return true;
        }

        //0 advancetut
        //1 playerName
        String playerName = args[1];
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !player.isValid()) {
            Util.sendMessageToPlayerOrConsole(commandSender, "invalid-target", "Invalid target player. Did you spell the name right?");
            return true;
        }

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
        TutorialPath path = TutorialManager.getInstance().getPathByName(civilian.getTutorialPath());
        if (path == null) {
            Civs.logger.severe("Invalid path " + civilian.getTutorialPath() + " for " + player.getName() +
                    " in plugins/Civs/players/" + player.getUniqueId() + ".yml");
            return true;
        }
        int tutorialIndex = civilian.getTutorialIndex();
        if (path.getSteps().size() <= tutorialIndex) {
            Civs.logger.warning("Can't advance " + player.getName() + "'s tutorial progress past the end of current path");
            return true;
        }
        TutorialStep step = path.getSteps().get(tutorialIndex);

        String param = null;
        if (step.getType().equals("build") || step.getType().equals("upkeep") ||
                step.getType().equals("buy")) {
            param = step.getRegion();
        } else if (step.getType().equals("kill")) {
            param = step.getKillType();
        }

        if (param == null) {
            Civs.logger.warning("Unable to find tutorial type param. Make sure you are trying to advance a valid tutorial step (example: not choose a path)");
            return true;
        }
        TutorialManager.getInstance().completeStep(civilian,
                TutorialManager.TutorialType.valueOf(step.getType().toUpperCase()), param);
        return true;
    }

    @Override
    public boolean canUseCommand(CommandSender commandSender) {
        return Civs.perm != null && Civs.perm.has(commandSender, Constants.ADMIN_PERMISSION);
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            addAllOnlinePlayers(suggestions, args[1]);
            return suggestions;
        }
        return super.getWord(commandSender, args);
    }
}

package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialManager;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialPath;
import org.redcastlemedia.multitallented.civs.tutorials.TutorialStep;

@CivsCommand(keys = { "advancetut" })
public class TutorialAdvanceCommand implements CivCommand {

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (Civs.perm == null || !Civs.perm.has(commandSender, "civs.admin")) {
            sendMessage(commandSender, "no-permission", "You don't have permission to use /cv advancetut PlayerName");
            return true;
        }
        if (args.length < 2) {
            sendMessage(commandSender, "invalid-target", "Invalid command. Use /cv advancetut PlayerName");
            return true;
        }

        //0 advancetut
        //1 playerName
        String playerName = args[1];
        Player player = Bukkit.getPlayer(playerName);
        if (player == null || !player.isValid()) {
            sendMessage(commandSender, "invalid-target", "Invalid target player. Did you spell the name right?");
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

    private void sendMessage(CommandSender commandSender, String key, String message) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }
        if (player != null) {
            Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
                    civilian.getLocale(),
                    key
            ));
        } else {
            commandSender.sendMessage(message);
        }
    }
}

package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.alliances.Alliance;
import org.redcastlemedia.multitallented.civs.alliances.AllianceManager;
import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@CivsCommand(keys = { "ac" }) @SuppressWarnings("unused")
public class ToggleAllianceChatCommand extends ToggleChatChannelCommand<Alliance> {

    public ToggleAllianceChatCommand() {
        super(ChatChannel.ChatChannelType.ALLIANCE);
    }

    @Override
    public Alliance getRelevantTarget(Civilian c, String[] a) {
        if (a.length == 2) {
            return AllianceManager.getInstance().getAlliance(a[0]);
        }
        return null;
    }

    @Override
    public boolean isValid(Civilian c, Alliance alliance) {
        if (alliance == null) {
            return false;
        }

        HashSet<String> members = alliance.getMembers();
        TownManager manager = TownManager.getInstance();
        for (String member : members) {
            Town town = manager.getTown(member);
            if (town.getRawPeople().containsKey(c.getUuid())) {
                return true;
            }
        }

        return false;
    }


    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        Player player = (Player) commandSender;

        List<String> suggestions = new ArrayList<>();

        TownManager townManager = TownManager.getInstance();

        ArrayList<Alliance> allAlliances = AllianceManager.getInstance().getAllAlliances();
        for (Alliance ally : allAlliances) {
            for (String member : ally.getMembers()) {
                Town town = townManager.getTown(member);
                if (town.getRawPeople().containsKey(player.getUniqueId())) {
                    suggestions.add(ally.getName());
                }
            }
        }

        return suggestions;
    }
}

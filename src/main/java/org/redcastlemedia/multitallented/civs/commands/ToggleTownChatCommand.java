package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.civilians.ChatChannel;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@CivsCommand(keys = { "tc" }) @SuppressWarnings("unused")
public class ToggleTownChatCommand extends ToggleChatChannelCommand<Town> {

    public ToggleTownChatCommand() {
        super(ChatChannel.ChatChannelType.TOWN);
    }

    @Override
    public Town getRelevantTarget(Civilian c, String[] a) {
        if (a.length == 2) {
            return TownManager.getInstance().getTown(a[1]);
        } else {
            String biggestTown = TownManager.getInstance().getBiggestTown(c);
            if (biggestTown != null) {
                return TownManager.getInstance().getTown(biggestTown);
            }
        }
        return null;
    }


    @Override
    public boolean isValid(Civilian c, Town town) {
        return town != null && town.getRawPeople().containsKey(c.getUuid());
    }

    @Override
    public List<String> getWord(CommandSender commandSender, String[] args) {
        Player player = (Player) commandSender;
        TownManager instance = TownManager.getInstance();
        Set<Town> townsForPlayer = instance.getTownsForPlayer(player.getUniqueId());
        List<String> suggestions = new ArrayList<>();

        for (Town town : townsForPlayer) {
            suggestions.add(town.getName());
        }

        return suggestions;
    }
}

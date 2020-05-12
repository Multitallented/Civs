package org.redcastlemedia.multitallented.civs.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TabComplete implements TabCompleter {

    private final Map<String, CivCommand> commands;

    public TabComplete(Map<String, CivCommand> commands) {
        this.commands = commands;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> keys = new ArrayList<>();
        if (args.length < 1) {
            for (Map.Entry<String, CivCommand> commandEntry : commands.entrySet()) {
                if (commandEntry.getValue().canUseCommand(commandSender)) {
                    keys.add(commandEntry.getKey());
                }
            }
            return keys;
        }
//        if (commands.containsKey(args[0])) {
//            return commands.get(args[0]).getNextWordList(commandSender, args);
//        }
        return keys;
    }
}

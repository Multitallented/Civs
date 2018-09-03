package org.redcastlemedia.multitallented.civs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;

import java.util.HashMap;
import java.util.UUID;

public class PortCommand implements CivCommand {
    private HashMap<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public boolean runCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Civs.getPrefix() + "Unable to invite for non-players");
            return true;
        }
        Player player = (Player) commandSender;
        LocaleManager localeManager = LocaleManager.getInstance();
        ConfigManager configManager = ConfigManager.getInstance();

        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        if (!configManager.getPortDuringCombat() && civilian.isInCombat()) {
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "in-combat"));
            return true;
        }

        if (cooldowns.containsKey(player.getUniqueId())) {
            long cooldown = System.currentTimeMillis() - cooldowns.get(player.getUniqueId());
            if (cooldown < ConfigManager.getInstance().getPortCooldown() * 1000) {
                player.sendMessage(Civs.getPrefix() +
                        localeManager.getTranslation(civilian.getLocale(), "cooldown")
                                .replace("$1", ((int) cooldown / 1000) + ""));
                return true;
            }
        }

        if (player.getHealth() < ConfigManager.getInstance().getPortDamage()) {
            int healthNeeded = ConfigManager.getInstance().getPortDamage() + 1 - (int) player.getHealth();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "need-more-health").replace("$1", healthNeeded + ""));
            return true;
        }

        if (player.getFoodLevel() < ConfigManager.getInstance().getPortStamina()) {
            int foodNeeded = ConfigManager.getInstance().getPortStamina() + 1 - player.getFoodLevel();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "need-more-stamina").replace("$1", foodNeeded + ""));
            return true;
        }

        if (civilian.getMana() < ConfigManager.getInstance().getPortMana()) {
            int manaNeeded = ConfigManager.getInstance().getPortMana() + 1 - civilian.getMana();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "need-more-mana").replace("$1", manaNeeded + ""));
            return true;
        }

        if (Civs.econ != null &&
                !Civs.econ.has(player, ConfigManager.getInstance().getPortMoney())) {
            double manaNeeded = ConfigManager.getInstance().getPortMoney();
            player.sendMessage(Civs.getPrefix() + localeManager.getTranslation(civilian.getLocale(),
                    "not-enough-money").replace("$1", manaNeeded + ""));
            return true;
        }

        //TODO check reagents?

        //TODO begin port
        return true;
    }


}

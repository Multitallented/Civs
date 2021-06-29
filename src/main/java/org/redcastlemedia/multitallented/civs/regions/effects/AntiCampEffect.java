package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.TwoSecondEvent;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

@CivsSingleton
public class AntiCampEffect implements Listener {
    private static final HashMap<UUID, String> lastDeathTown = new HashMap<>();
    private static final HashMap<String, Long> lastPoison = new HashMap<>();
    private static final HashMap<UUID, ArrayList<Long>> lastDeath = new HashMap<>();
    public static final String KEY = "anticamp";

    public static void getInstance() {
        AntiCampEffect antiCampEffect = new AntiCampEffect();
        Bukkit.getPluginManager().registerEvents(antiCampEffect, Civs.getInstance());
    }

    public static boolean canActivateAntiCamp(UUID uuid, Town town) {
        if (!lastDeathTown.containsKey(uuid) || !lastDeathTown.get(uuid).equals(town.getName())) {
            return false;
        }
        return lastDeath.containsKey(uuid);
    }

    public static void activateAntiCamp(UUID uuid, Town town) {
        lastDeathTown.put(uuid, town.getName());
        ArrayList<Long> lastDeaths = new ArrayList<>();
        lastDeaths.add(System.currentTimeMillis() + getPeriod(town.getEffects().get(KEY)) * 1000);
        lastDeaths.add(System.currentTimeMillis() + getPeriod(town.getEffects().get(KEY)) * 1000);
        lastDeaths.add(System.currentTimeMillis() + getPeriod(town.getEffects().get(KEY)) * 1000);
        lastDeath.put(uuid, lastDeaths);
    }

    @EventHandler @SuppressWarnings("unused")
    public void onTownRename(RenameTownEvent event) {
        for (Map.Entry<UUID, String> entry : new HashMap<>(lastDeathTown).entrySet()) {
            if (event.getOldName().equals(entry.getValue())) {
                lastDeathTown.put(entry.getKey(), event.getNewName());
            }
        }
        for (Map.Entry<String, Long> entry : new HashMap<>(lastPoison).entrySet()) {
            if (event.getOldName().equals(entry.getKey())) {
                lastPoison.put(event.getNewName(), entry.getValue());
            }
        }
    }

    @EventHandler @SuppressWarnings("unused")
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastDeathTown.remove(event.getPlayer().getUniqueId());
        lastDeath.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW) @SuppressWarnings("unused")
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());


        if (civilian.getLastDamager() == null) {
            return;
        }

        //remove the killer from deathCounts
        lastDeath.remove(civilian.getLastDamager());

        //If they died outside of a town then I don't care
        Town town = TownManager.getInstance().getTownAt(player.getLocation());
        if (town == null) {
            return;
        }

        if (!town.getEffects().containsKey(KEY)) {
            return;
        }

        //If the person dying was a member, then increment their deathCount
        if (!town.getPeople().containsKey(player.getUniqueId())) {
            return;
        }

        sendReminderMessage(player, town);

        //Don't count deaths in a previous town
        if (lastDeathTown.containsKey(player.getUniqueId()) &&
                lastDeathTown.get(player.getUniqueId()) != null &&
                !lastDeathTown.get(player.getUniqueId()).equals(town.getName())) {
            lastDeath.remove(player.getUniqueId());
        }

        //if the person hasn't died yet then add to lastDeath
        if (!lastDeath.containsKey(player.getUniqueId())) {
            lastDeath.put(player.getUniqueId(), new ArrayList<>());
            lastDeath.get(player.getUniqueId()).add(System.currentTimeMillis());
        } else {
            lastDeath.get(player.getUniqueId()).add(System.currentTimeMillis());
        }
        if (lastDeath.get(player.getUniqueId()).size() > 3) {
            lastDeath.get(player.getUniqueId()).remove(0);
        }

        lastDeathTown.put(player.getUniqueId(), town.getName());
    }

    private void sendReminderMessage(Player player, Town town) {
        if (!town.getEffects().containsKey(KEY)) {
            return;
        }

        double antiCampCost = 0;
        if (town.getEffects().get(AntiCampEffect.KEY) != null) {
            String antiCampString = town.getEffects().get(AntiCampEffect.KEY);
            String[] splitString = antiCampString.split("\\.");
            if (splitString.length > 2) {
                antiCampCost = Double.parseDouble(splitString[2]);
            }
        }

        String activateMessage = Civs.getRawPrefix() + LocaleManager.getInstance().getRawTranslation(player,
                "activate-anticamp-question").replace("$1", player.getDisplayName())
                .replace("$2", town.getName())
                .replace("$3", "" + antiCampCost) + " ";
        TextComponent component = Util.parseColorsComponent(activateMessage);

        TextComponent acceptComponent = new TextComponent("[✓]");
        acceptComponent.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        acceptComponent.setUnderlined(true);
        acceptComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cv anticamp " + town.getName()));
        component.addExtra(acceptComponent);

        player.spigot().sendMessage(component);
    }

    @EventHandler
    public void onTwoSeconds(TwoSecondEvent event) {
        //Activate poison
        HashSet<UUID> removeMeDeathCounts = new HashSet<>();

        //Go through everyone who has died in their own town
        for (UUID name : lastDeath.keySet()) {

            //Cleanup
            {
                ArrayList<Integer> removeIndexes = new ArrayList<>();
                int i = 0;
                for (Long deathTime : lastDeath.get(name)) {
                    if (deathTime + 600000 < System.currentTimeMillis()) {
                        removeIndexes.add(i);
                    }
                    i++;
                }
                for (Integer j : removeIndexes) {
                    lastDeath.get(name).remove((int) j);
                }
            }

            //Skip people who haven't died enough
            if (lastDeath.get(name).size() < 3 || !lastDeathTown.containsKey(name)) {
                continue;
            }

            removeMeDeathCounts.add(name);
            Town town = TownManager.getInstance().getTown(lastDeathTown.get(name));
            lastDeathTown.remove(name);
            if (town == null) {
                continue;
            }
            if (!town.getEffects().containsKey(KEY)) {
                continue;
            }

            long period = getPeriod(town.getEffects().get(KEY));

            for (Player player : Bukkit.getOnlinePlayers()) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() + ChatColor.RED +
                        LocaleManager.getInstance().getTranslation(
                                civilian.getLocale(), "anti-camp-active"
                        ).replace("$1", town.getName()));
            }
            lastPoison.put(town.getName(), System.currentTimeMillis() + (period * 1000));
        }
        for (UUID uuid : removeMeDeathCounts) {
            lastDeath.remove(uuid);
        }

        //Deal Poison Damage
        ArrayList<String> removeMePoison = new ArrayList<>();
        for (String townName : lastPoison.keySet()) {

            Long lastPoisonTime = lastPoison.get(townName);

            if (lastPoisonTime == null || System.currentTimeMillis() > lastPoisonTime) {
                removeMePoison.add(townName);
                continue;
            }

            Town town = TownManager.getInstance().getTown(townName);
            if (town == null) {
                continue;
            }

            if (!town.getEffects().containsKey(KEY)) {
                continue;
            }
            int damage = 1;
            {
                String antiCampString = town.getEffects().get(KEY);
                if (antiCampString != null) {
                    String[] antiCampSplit = antiCampString.split("\\.");
                    damage = Integer.parseInt(antiCampSplit[0]);
                }
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (town.getPeople().containsKey(p.getUniqueId())) {
                    continue;
                }

                //TODO move this to player in town event?
                if (town != TownManager.getInstance().getTownAt(p.getLocation())) {
                    continue;
                }

                p.damage(damage);
                Civilian civilian = CivilianManager.getInstance().getCivilian(p.getUniqueId());
                if (ConfigManager.getInstance().isCombatTagEnabled() && !civilian.isInCombat()) {
                    long combatTagDuration = ConfigManager.getInstance().getCombatTagDuration();
                    p.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(p,
                            "combat-tagged").replace("$1", "" + combatTagDuration));
                }
                civilian.setLastDamage(System.currentTimeMillis());
            }
        }
        for (String s : removeMePoison) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
                player.sendMessage(Civs.getPrefix() + ChatColor.RED +
                        LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                                "anti-camp-expired").replace("$1", s));
            }
            lastPoison.remove(s);
        }
    }

    private static long getPeriod(String antiCampString) {
        long period = 2;
        if (antiCampString != null) {
            String[] antiCampSplit = antiCampString.split("\\.");
            if (antiCampSplit.length > 1) {
                period = Long.parseLong(antiCampSplit[1]);
            }
        }
        return period;
    }
}

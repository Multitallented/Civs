package org.redcastlemedia.multitallented.civs.regions.effects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.events.PlayerInTownEvent;
import org.redcastlemedia.multitallented.civs.events.TwoSecondEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.towns.TownType;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class AntiCampEffect implements Listener {
    private final HashMap<UUID, String> lastDeathTown = new HashMap<>();
    private final HashMap<String, Long> lastPoison = new HashMap<>();
    private final HashMap<UUID, ArrayList<Long>> lastDeath = new HashMap<>();
    private final String KEY = "anticamp";


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastDeathTown.remove(event.getPlayer().getUniqueId());
        lastDeath.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());


        if (civilian.getLastDamager() == null) {
            return;
        }

        //remove the killer from deathCounts
        lastDeath.remove(civilian.getLastDamager());

        //if the person who's dying has died more than twice, then I don't care
//            if (lastDeath.containsKey(player.getName()) && lastDeath.get(player.getName()).size() > 2) {
//                return;
//            }

        //If they died outside of a town then I don't care
        Town town = TownManager.getInstance().getTownAt(player.getLocation());
        if (town == null) {
            return;
        }

        TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
        if (townType == null) {
            return;
        }

        if (!townType.getEffects().containsKey(KEY)) {
            return;
        }

        //If the person dying was a member, then increment their deathCount
        if (town.getPeople().containsKey(player.getUniqueId())) {

            sendReminderMessage(player, town);

            //Don't count deaths in a previous town
            if (lastDeathTown.containsKey(player.getUniqueId()) &&
                    lastDeathTown.get(player.getUniqueId()) != null &&
                    !lastDeathTown.get(player.getUniqueId()).equals(town.getName())) {
                lastDeath.remove(player.getUniqueId());
            }

            //if the person hasn't died yet then add to lastDeath
            if (!lastDeath.containsKey(player.getUniqueId())) {
                lastDeath.put(player.getUniqueId(), new ArrayList<Long>());
                lastDeath.get(player.getUniqueId()).add(System.currentTimeMillis());
            } else {
                lastDeath.get(player.getUniqueId()).add(System.currentTimeMillis());
            }
            if (lastDeath.get(player.getUniqueId()).size() > 3) {
                lastDeath.get(player.getUniqueId()).remove(0);
            }

//                    if (deathCounts.containsKey(player.getName())) {
//                        deathCounts.put(player.getName(), deathCounts.get(player.getName()) + 1);
//                    } else {
//                        deathCounts.put(player.getName(), 1);
//                    }
            lastDeathTown.put(player.getUniqueId(), town.getName());
        }
    }

    private void sendReminderMessage(Player player, Town town) {
        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());

        double antiCampCost = 0;

        // TODO find the cost of the anti camp

        String activateMessage = Civs.getRawPrefix() + LocaleManager.getInstance().getRawTranslation(civilian.getLocale(),
                "activate-anticamp-question").replace("$1", player.getDisplayName())
                .replace("$2", town.getName())
                .replace("$3", "" + antiCampCost) + " ";
        TextComponent component = Util.parseColorsComponent(activateMessage);

        TextComponent acceptComponent = new TextComponent("[✓]");
        acceptComponent.setColor(net.md_5.bungee.api.ChatColor.GREEN);
        acceptComponent.setUnderlined(true);
        acceptComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cv anticamp"));
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
                ArrayList<Integer> removeIndexes = new ArrayList<Integer>();
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
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            if (townType == null) {
                continue;
            }
            if (!townType.getEffects().containsKey(KEY)) {
                continue;
            }

            long period = 2;
            {
                String antiCampString = townType.getEffects().get(KEY);
                if (antiCampString != null) {
                    String[] antiCampSplit = antiCampString.split("\\.");
                    if (antiCampSplit.length > 1) {
                        period = Long.parseLong(antiCampSplit[1]);
                    }
                }
            }

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
            TownType townType = (TownType) ItemManager.getInstance().getItemType(town.getType());
            if (townType == null) {
                continue;
            }

            if (!townType.getEffects().containsKey(KEY)) {
                continue;
            }
            int damage = 1;
            {
                String antiCampString = townType.getEffects().get(KEY);
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
}

package org.redcastlemedia.multitallented.civs.towns;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.events.PlayerEnterTownEvent;
import org.redcastlemedia.multitallented.civs.events.PlayerExitTownEvent;
import org.redcastlemedia.multitallented.civs.events.RegionCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.RegionDestroyedEvent;
import org.redcastlemedia.multitallented.civs.events.RegionUpkeepEvent;
import org.redcastlemedia.multitallented.civs.events.RenameTownEvent;
import org.redcastlemedia.multitallented.civs.events.TownCreatedEvent;
import org.redcastlemedia.multitallented.civs.events.TownDestroyedEvent;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.regions.RegionUpkeep;
import org.redcastlemedia.multitallented.civs.regions.effects.HousingEffect;

@CivsSingleton
public class TownBarManager implements Listener {
    private final HashMap<String, TownBar> townBars = new HashMap<>();

    public static void getInstance() {
        Bukkit.getPluginManager().registerEvents(new TownBarManager(), Civs.getInstance());
    }

    public TownBarManager()
    {
        for (Town town : TownManager.getInstance().getTowns())
        {
            createTownBar(town.getName());
        }
    }

    private void createTownBar(String townName)
    {
        if (hasTownBar(townName)) return;

        TownBar townBar = new TownBar(townName);
        townBars.put(townName, townBar);
    }

    private void removeTownBar(String townName)
    {
        if (!hasTownBar(townName)) return;

        townBars.get(townName).removeAllPlayers();
        townBars.remove(townName);
    }

    private boolean hasTownBar(String townName)
    {
        return townBars.containsKey(townName);
    }

    private TownBar getTownBar(String townName)
    {
        return townBars.get(townName);
    }

    @EventHandler
    public void onTownCreated(TownCreatedEvent event)
    {
        String townName = event.getTown().getName();

        createTownBar(townName);
    }

    @EventHandler
    public void onTownDestroyed(TownDestroyedEvent event)
    {
        String townName = event.getTown().getName();

        removeTownBar(townName);
    }

    @EventHandler
    public void onRenameTown(RenameTownEvent event)
    {
        String oldName = event.getOldName();
        String newName = event.getNewName();

        TownBar townBar = townBars.remove(oldName);
        townBars.put(newName, townBar);
        townBar.townName = newName;
    }

    @EventHandler
    public void onPlayerEnterTown(PlayerEnterTownEvent event)
    {
        UUID uuid = event.getUuid();
        String townName = event.getTown().getName();

        if (!hasTownBar(townName))
        {
            createTownBar(townName);
        }
        getTownBar(townName).addPlayer(uuid);
    }

    @EventHandler
    public void onPlayerExitTown(PlayerExitTownEvent event)
    {
        UUID uuid = event.getUuid();
        String townName = event.getTown().getName();

        if (hasTownBar(townName))
        {
            getTownBar(townName).removePlayer(uuid);
        }
    }

    @EventHandler
    public void onRegionUpkeep(RegionUpkeepEvent event) {
        Region region = event.getRegion();
        Town town = TownManager.getInstance().getTownAt(region.getLocation());

        if (town == null || !hasTownBar(town.getName())) {
            return;
        }

        TownBar townBar = getTownBar(town.getName());
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
        RegionUpkeep regionUpkeep = regionType.getUpkeeps().get(event.getUpkeepIndex());

        String title;
        BarColor barColor;

        String defaultLanguage = ConfigManager.getInstance().getDefaultLanguage();
        if (regionUpkeep.getPayout() > 0)
        {
            String payoutString = LocaleManager.getInstance().getTranslation(defaultLanguage, "money")
                    .replace("$1", "" + ChatColor.GREEN + regionUpkeep.getPayout());
            title = ChatColor.GREEN + payoutString + " " + regionType.getDisplayName();
            barColor = BarColor.GREEN;
        } else if (regionUpkeep.getPowerOutput() > 0) {
            String powerOutput = LocaleManager.getInstance().getTranslation(defaultLanguage, "town-power")
                    .replace("$1", "" + ChatColor.GREEN + regionUpkeep.getPowerOutput()).replace("/$2", "");
            title = ChatColor.GREEN + powerOutput + " " + regionType.getDisplayName();
            barColor = BarColor.GREEN;
        } else if (regionUpkeep.getPowerInput() > 0) {
            String powerInput = LocaleManager.getInstance().getTranslation(defaultLanguage, "town-power")
                    .replace("$1", "" + ChatColor.RED + regionUpkeep.getPowerInput()).replace("/$2", "");
            title = ChatColor.RED + powerInput + " " + regionType.getDisplayName();
            barColor = BarColor.RED;
        } else {
            title = ChatColor.AQUA + regionType.getDisplayName();
            barColor = BarColor.BLUE;
        }

        townBar.addNotification(title, barColor, 3);
        townBar.update();
    }

    @EventHandler
    public void onHousingAdded(RegionCreatedEvent event) {
        Region region = event.getRegion();
        Town town = TownManager.getInstance().getTownAt(region.getLocation());

        if (town == null || !hasTownBar(town.getName()) || !region.getEffects().containsKey(HousingEffect.KEY)) {
            return;
        }

        TownBar townBar = getTownBar(town.getName());
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());

        String defaultLanguage = ConfigManager.getInstance().getDefaultLanguage();
        String housingTitle = LocaleManager.getInstance().getTranslation(defaultLanguage, "housing-name");
        String title = ChatColor.GREEN + "+" + region.getEffects().get(HousingEffect.KEY) + " " + housingTitle +
                ChatColor.GREEN + " " + regionType.getDisplayName();
        townBar.addNotification(title, BarColor.GREEN, 5);
    }

    @EventHandler
    public void onHousingRemoved(RegionDestroyedEvent event) {
        Region region = event.getRegion();
        Town town = TownManager.getInstance().getTownAt(region.getLocation());

        if (town == null || !hasTownBar(town.getName()) || !region.getEffects().containsKey(HousingEffect.KEY)) {
            return;
        }

        TownBar townBar = getTownBar(town.getName());
        RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());

        String defaultLanguage = ConfigManager.getInstance().getDefaultLanguage();
        String housingTitle = LocaleManager.getInstance().getTranslation(defaultLanguage, "housing-name");
        String title = ChatColor.RED + "-" + region.getEffects().get(HousingEffect.KEY) + " " + housingTitle +
                ChatColor.RED + " " + regionType.getDisplayName();
        townBar.addNotification(title, BarColor.RED, 5);
    }
}

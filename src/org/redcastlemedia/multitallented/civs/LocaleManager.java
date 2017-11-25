package org.redcastlemedia.multitallented.civs;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LocaleManager {

    private static LocaleManager localeManager;
    HashMap<String, HashMap<String, String>> languageMap = new HashMap<>();

    public String getTranslation(String language, String key) {
        if (!languageMap.containsKey(language) ||
                !languageMap.get(language).containsKey(key)) {
            return languageMap.get(ConfigManager.getInstance().getDefaultLanguage()).get(key);
        }
        return languageMap.get(language).get(key);
    }
    public Set<String> getAllLanguages() {
        return languageMap.keySet();
    }

    public LocaleManager() {
        if (Civs.getInstance() == null) {
            loadConfig(new YamlConfiguration());
            localeManager = this;
        } else {
            localeManager = new LocaleManager(new File(Civs.getInstance().getDataFolder(), "locale.yml"));
        }
    }

    public LocaleManager(File localeFile) {
        localeManager = this;
        try {
            if (!localeFile.exists()) {
                Civs.logger.severe("No locale.yml found");
                return;
            }
            FileConfiguration localeConfig = new YamlConfiguration();
            localeConfig.load(localeFile);
            loadConfig(localeConfig);
        } catch (IOException | InvalidConfigurationException invalidConfigurationException) {
            invalidConfigurationException.printStackTrace();
        }
    }

    private void loadConfig(FileConfiguration localeConfig) {
        Set<String> configKeys = localeConfig.getKeys(false);
        if (!configKeys.contains("en")) {
            configKeys.add("en");
        }

        for (String langKey : configKeys) {
            HashMap<String, String> currentLanguage = new HashMap<>();
            currentLanguage.put("name",
                    localeConfig.getString(langKey + ".name", "Unnamed"));
            currentLanguage.put("icon",
                    localeConfig.getString(langKey + ".icon", "WOOL.14"));
            currentLanguage.put("back-button",
                    localeConfig.getString(langKey + ".back-button", "Back"));
            currentLanguage.put("language-menu",
                    localeConfig.getString(langKey + ".language-menu", "Select Language"));
            currentLanguage.put("language-set",
                    localeConfig.getString(langKey + ".language-set", "Your language has been set to $1"));
            currentLanguage.put("shop",
                    localeConfig.getString(langKey + ".shop", "Shop"));
            currentLanguage.put("price",
                    localeConfig.getString(langKey + ".price", "Price"));
            currentLanguage.put("buy-item",
                    localeConfig.getString(langKey + ".buy-item", "For Sale"));
            currentLanguage.put("buy",
                    localeConfig.getString(langKey + ".buy", "Buy $1"));
            currentLanguage.put("cancel",
                    localeConfig.getString(langKey + ".cancel", "Cancel"));
            currentLanguage.put("item-bought",
                    localeConfig.getString(langKey + ".item-bought", "Congrats! You just bought $1 for $2"));
            currentLanguage.put("items",
                    localeConfig.getString(langKey + ".items", "Items"));
            currentLanguage.put("community",
                    localeConfig.getString(langKey + ".community", "Community"));
            currentLanguage.put("size",
                    localeConfig.getString(langKey + ".size", "Size"));
            currentLanguage.put("range",
                    localeConfig.getString(langKey + ".range", "Range"));
            currentLanguage.put("build-reqs",
                    localeConfig.getString(langKey + ".build-reqs", "All the blocks you need to place to build a $1"));
            currentLanguage.put("too-close-region",
                    localeConfig.getString(langKey + ".too-close-region", "Your $1 would be too close to a $2"));
            currentLanguage.put("too-close-town",
                    localeConfig.getString(langKey + ".too-close-town", "Your $1 would be too close to another town"));
            currentLanguage.put("region-built",
                    localeConfig.getString(langKey + ".region-built", "You have successfully built a $1"));

            currentLanguage.put("no-region-type-found",
                    localeConfig.getString(langKey + ".no-region-type-found", "No region type found for $1"));
            currentLanguage.put("building-too-big",
                    localeConfig.getString(langKey + ".building-too-big", "You're building is too big to be a $1"));
            currentLanguage.put("no-required-blocks",
                    localeConfig.getString(langKey + ".no-required-blocks", "You haven't placed the required blocks to make a $1"));
            currentLanguage.put("cant-build-on-region",
                    localeConfig.getString(langKey + ".cant-build-on-region", "You can't build a $1 on top of a $2"));
            currentLanguage.put("rebuild-required",
                    localeConfig.getString(langKey + ".rebuild-required", "You need to build this $1 on top of a $2"));
            currentLanguage.put("prevent-civs-item-share",
                    localeConfig.getString(langKey + ".prevent-civs-item-share", "You are not allowed to share Civ items"));
            currentLanguage.put("region-protected",
                    localeConfig.getString(langKey + ".region-protected", "This region is protected"));
            currentLanguage.put("region-destroyed",
                    localeConfig.getString(langKey + ".region-destroyed", "Region $1 has been destroyed!"));
            currentLanguage.put("specify-player-town",
                    localeConfig.getString(langKey + ".specify-player-town", "Please specify a player and town"));
            currentLanguage.put("specify-player-region",
                    localeConfig.getString(langKey + ".specify-player-region", "Please specify a player and region"));
            currentLanguage.put("town-not-exist",
                    localeConfig.getString(langKey + ".town-not-exist", "$1 is not a town"));
            currentLanguage.put("no-permission-invite",
                    localeConfig.getString(langKey + ".no-permission-invite", "You don't have permission to invite people to $1"));
            currentLanguage.put("player-not-online",
                    localeConfig.getString(langKey + ".player-not-online", "$1 is not online"));
            currentLanguage.put("invite-player",
                    localeConfig.getString(langKey + ".invite-player", "$1 would like to invite you to join $2 $3"));
            currentLanguage.put("already-member",
                    localeConfig.getString(langKey + ".already-member", "$1 is already a member of $2"));
            currentLanguage.put("broke-own-region",
                    localeConfig.getString(langKey + ".broke-own-region", "Your $1 is missing blocks"));
            currentLanguage.put("no-permission",
                    localeConfig.getString(langKey + ".no-permission", "Permission denied"));
            currentLanguage.put("max-item",
                    localeConfig.getString(langKey + ".max-item", "You cant buy more than $2 $1"));
            currentLanguage.put("class-changed",
                    localeConfig.getString(langKey + ".class-changed", "Your class has changed to $1"));
            currentLanguage.put("classes",
                    localeConfig.getString(langKey + ".classes", "Classes"));
            currentLanguage.put("regions",
                    localeConfig.getString(langKey + ".regions", "Regions"));
            currentLanguage.put("blueprints",
                    localeConfig.getString(langKey + ".blueprints", "Blueprints"));
            currentLanguage.put("spells",
                    localeConfig.getString(langKey + ".spells", "Spells"));
            currentLanguage.put("not-enough-money",
                    localeConfig.getString(langKey + ".not-enough-money", "You dont have $$1"));
            currentLanguage.put("reagents",
                    localeConfig.getString(langKey + ".reagents", "All the items you need in the $1 chest"));
            currentLanguage.put("upkeep",
                    localeConfig.getString(langKey + ".upkeep", "All the items consumed by the $1"));
            currentLanguage.put("output",
                    localeConfig.getString(langKey + ".output", "All the items created by the $1"));
            currentLanguage.put("region-in-town",
                    localeConfig.getString(langKey + ".region-in-town", "Your region is in $1"));
            currentLanguage.put("view-members",
                    localeConfig.getString(langKey + ".view-members", "View members"));
            currentLanguage.put("add-member",
                    localeConfig.getString(langKey + ".add-member", "Add member"));
            currentLanguage.put("operation",
                    localeConfig.getString(langKey + ".operation", "Operation"));
            currentLanguage.put("region-working",
                    localeConfig.getString(langKey + ".region-working", "Your region is running smoothly"));
            currentLanguage.put("region-not-working",
                    localeConfig.getString(langKey + ".region-not-working", "Your region is missing something"));
            currentLanguage.put("owner",
                    localeConfig.getString(langKey + ".owner", "Owner"));
            currentLanguage.put("member",
                    localeConfig.getString(langKey + ".member", "Member"));
            currentLanguage.put("guest",
                    localeConfig.getString(langKey + ".guest", "Guest"));
            currentLanguage.put("recruiter",
                    localeConfig.getString(langKey + ".recruiter", "Recruiter"));
            currentLanguage.put("not-allowed-place",
                    localeConfig.getString(langKey + ".not-allowed-place", "You aren't allowed to place $1"));
            currentLanguage.put("starter-book",
                    localeConfig.getString(langKey + ".starter-book", "Menu for Civs"));
            currentLanguage.put("set-owner",
                    localeConfig.getString(langKey + ".set-owner", "Set Owner"));
            currentLanguage.put("set-member",
                    localeConfig.getString(langKey + ".set-member", "Set Member"));
            currentLanguage.put("set-guest",
                    localeConfig.getString(langKey + ".set-guest", "Set Guest"));
            currentLanguage.put("remove-member",
                    localeConfig.getString(langKey + ".remove-member", "Remove Member"));
            currentLanguage.put("member-added-region",
                    localeConfig.getString(langKey + ".member-added-region", "$1 is now a member of your $2"));
            currentLanguage.put("add-member-region",
                    localeConfig.getString(langKey + ".add-member-region", "You have been made a member of $1"));
            currentLanguage.put("member-description",
                    localeConfig.getString(langKey + ".member-description", "Members can build and use the region"));
            currentLanguage.put("guest-description",
                    localeConfig.getString(langKey + ".guest-description", "Guests can use doors and buttons in the region"));
            currentLanguage.put("owner-description",
                    localeConfig.getString(langKey + ".owner-description", "Owners can do anything in the region"));
            currentLanguage.put("remove-member-region",
                    localeConfig.getString(langKey + ".remove-member-region", "You are no longer a member of $1"));
            currentLanguage.put("member-removed-region",
                    localeConfig.getString(langKey + ".member-removed-region", "$1 is no longer a member of $2"));
            currentLanguage.put("add-owner-region",
                    localeConfig.getString(langKey + ".add-owner-region", "You have been made an owner of $1"));
            currentLanguage.put("owner-added-region",
                    localeConfig.getString(langKey + ".owner-added-region", "$1 is now an owner of your $2"));
            currentLanguage.put("add-guest-region",
                    localeConfig.getString(langKey + ".add-guest-region", "You have been made a guest of $1"));
            currentLanguage.put("guest-added-region",
                    localeConfig.getString(langKey + ".guest-added-region", "$1 is now a guest of your $2"));
            currentLanguage.put("invite-member-region",
                    localeConfig.getString(langKey + ".invite-member-region", "You have been become a member of $1"));
            currentLanguage.put("member-invited-region",
                    localeConfig.getString(langKey + ".member-invited-region", "$1 has been added to your $2"));
            currentLanguage.put("stand-in-region",
                    localeConfig.getString(langKey + ".stand-in-region", "Please have $1 stand in the region"));
            currentLanguage.put("prev-button",
                    localeConfig.getString(langKey + ".prev-button", "Previous"));
            currentLanguage.put("next-button",
                    localeConfig.getString(langKey + ".next-button", "Next"));
            currentLanguage.put("town-instructions",
                    localeConfig.getString(langKey + ".town-instructions", "To create a $1, use /cv town MyTownName"));
            currentLanguage.put("specify-town-name",
                    localeConfig.getString(langKey + ".specify-town-name", "Please specify a town name"));
            currentLanguage.put("hold-town",
                    localeConfig.getString(langKey + ".hold-town", "Please hold a town item"));
            currentLanguage.put("players",
                    localeConfig.getString(langKey + ".players", "Players"));
            currentLanguage.put("towns",
                    localeConfig.getString(langKey + ".towns", "Towns"));
            currentLanguage.put("your-towns",
                    localeConfig.getString(langKey + ".your-towns", "Your towns"));
            currentLanguage.put("wars",
                    localeConfig.getString(langKey + ".wars", "Wars"));
            currentLanguage.put("leaderboard",
                    localeConfig.getString(langKey + ".leaderboard", "Leaderboard"));
            currentLanguage.put("enter-town",
                    localeConfig.getString(langKey + ".enter-town", "You have entered $1"));
            currentLanguage.put("exit-town",
                    localeConfig.getString(langKey + ".exit-town", "You have exited $1"));
            currentLanguage.put("town-created",
                    localeConfig.getString(langKey + ".town-created", "$1 has been created!"));
            currentLanguage.put("town-destroyed",
                    localeConfig.getString(langKey + ".town-destroyed", "$1 has been destroyed!"));
            currentLanguage.put("new-town-member",
                    localeConfig.getString(langKey + ".new-town-member", "$1 has joined $2"));
            currentLanguage.put("no-commands-in-jail",
                    localeConfig.getString(langKey + ".no-commands-in-jail", "You cant use commands in jail for another $1"));
            currentLanguage.put("repeat-kill",
                    localeConfig.getString(langKey + ".repeat-kill", "$1 was killed too recently. No points awarded."));
            currentLanguage.put("kill-streak",
                    localeConfig.getString(langKey + ".kill-streak", "$1 is on a killstreak of $2"));
            currentLanguage.put("kill-joy",
                    localeConfig.getString(langKey + ".kill-joy", "$1 ended $2's killstreak of $3"));
            currentLanguage.put("death",
                    localeConfig.getString(langKey + ".death", "You lost $1 points for dying"));
            currentLanguage.put("kill",
                    localeConfig.getString(langKey + ".kill", "Kill: $1"));
            currentLanguage.put("low-health",
                    localeConfig.getString(langKey + ".low-health", "Low health bonus: $1"));
            currentLanguage.put("killstreak-points",
                    localeConfig.getString(langKey + ".killstreak-points", "Killstreak bonus: $1"));
            currentLanguage.put("killjoy-points",
                    localeConfig.getString(langKey + ".killjoy-points", "Killjoy bonus: $1"));
            currentLanguage.put("total-points",
                    localeConfig.getString(langKey + ".total-points", "Total points: $1"));
            currentLanguage.put("karma",
                    localeConfig.getString(langKey + ".karma", "Karma: $1"));
            currentLanguage.put("karma-gained",
                    localeConfig.getString(langKey + ".karma-gained", "Karma: +$1, $2 money gained"));
            currentLanguage.put("karma-lost",
                    localeConfig.getString(langKey + ".karma-lost", "Karma: -$1, $2 money lost"));
            currentLanguage.put("must-be-built-on-top",
                    localeConfig.getString(langKey + ".must-be-built-on-top", "A $1 must be built on top of a $2"));
            currentLanguage.put("mana-use-exp",
                    localeConfig.getString(langKey + ".mana-use-exp", "You cant use this unless you have full mana"));
            currentLanguage.put("region-type",
                    localeConfig.getString(langKey + ".region-type", "Region Type"));
            currentLanguage.put("destroy",
                    localeConfig.getString(langKey + ".destroy", "Destroy"));
            languageMap.put(langKey, currentLanguage);
        }
    }

    public static LocaleManager getInstance() {
        if (localeManager == null) {
            localeManager = new LocaleManager();
        }
        return localeManager;
    }
}

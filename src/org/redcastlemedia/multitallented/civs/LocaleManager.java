package org.redcastlemedia.multitallented.civs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
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
            loadDefaults();
            localeManager = this;
        } else {
            localeManager = new LocaleManager(new File(Civs.getInstance().getDataFolder(), "locale.yml"));
        }
    }

    public LocaleManager(File localeFile) {
        loadFile(localeFile);
        localeManager = this;
    }

    private void loadFile(File localeFile) {
        FileConfiguration localeConfig = new YamlConfiguration();
        loadDefaults();
        try {
            if (!localeFile.exists()) {
                Civs.logger.severe("No locale.yml found");
                return;
            }
            localeConfig.load(localeFile);

            for (String langKey : localeConfig.getKeys(false)) {
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
                languageMap.put(langKey, currentLanguage);
            }


        } catch (Exception e) {
            Civs.logger.severe("Unable to read from locale.yml");
        }
    }
    private void loadDefaults() {
        HashMap<String, String> englishMap = new HashMap<>();
        englishMap.put("name", "English");
        englishMap.put("icon", "WOOL.14");
        englishMap.put("back-button", "Back");
        englishMap.put("language-menu", "Select Language");
        englishMap.put("language-set", "Your language has been set to $1");
        englishMap.put("shop", "Shop");
        englishMap.put("price", "Price");
        englishMap.put("buy-item", "For Sale");
        englishMap.put("buy", "Buy $1");
        englishMap.put("cancel", "Cancel");
        englishMap.put("item-bought", "Congrats! You just bought $1 for $2");
        englishMap.put("items", "Items");
        englishMap.put("community", "Community");
        englishMap.put("size", "Size");
        englishMap.put("range", "Range");
        englishMap.put("build-reqs", "All the blocks you need to place to build a $1");
        englishMap.put("too-close-region", "Your $1 would be too close to a $2");
        englishMap.put("too-close-town", "Your $1 would be too close to another town");
        englishMap.put("town-created", "$1 has been created!");
        englishMap.put("town-destroyed", "$1 has been destroyed!");
        englishMap.put("region-built", "You have successfully built a $1");
        englishMap.put("no-region-type-found", "No region type found for $1");
        englishMap.put("building-too-big", "You're building is too big to be a $1");
        englishMap.put("no-required-blocks", "You haven't placed the required blocks to make a $1");
        englishMap.put("cant-build-on-region", "You can't build a $1 on top of a $2");
        englishMap.put("rebuild-required", "You need to build this $1 on top of a $2");
        englishMap.put("prevent-civs-item-share", "You are not allowed to share Civ items");
        englishMap.put("region-protected", "This region is protected");
        englishMap.put("region-destroyed", "Region $1 has been destroyed!");
        englishMap.put("specify-player-town", "Please specify a player and town");
        englishMap.put("specify-player-region", "Please specify a player and region");
        englishMap.put("town-not-exist", "$1 is not a town");
        englishMap.put("no-permission-invite", "You don't have permission to invite people to $1");
        englishMap.put("player-not-online", "$1 is not online");
        englishMap.put("invite-player", "$1 would like to invite you to join $2 $3");
        englishMap.put("already-member", "$1 is already a member of $2");
        englishMap.put("broke-own-region", "Your $1 is missing blocks");
        englishMap.put("no-permission", "Permission denied");
        englishMap.put("max-item", "You cant buy more than $2 $1");
        englishMap.put("class-changed", "Your class has changed to $1");
        englishMap.put("classes", "Classes");
        englishMap.put("blueprints", "Blueprints");
        englishMap.put("regions", "Regions");
        englishMap.put("spells", "Spells");
        englishMap.put("not-enough-money", "You dont have $$1");
        englishMap.put("reagents", "All the items you need in the $1 chest");
        englishMap.put("upkeep", "All the items consumed by the $1");
        englishMap.put("output", "All the items created by the $1");
        englishMap.put("region-in-town", "Your region is in $1");
        englishMap.put("view-members", "Your region is in $1");
        englishMap.put("add-member", "Add member");
        englishMap.put("operation", "Operation");
        englishMap.put("region-working", "Your region is running smoothly");
        englishMap.put("region-not-working", "Your region is missing something");
        englishMap.put("owner", "Owner");
        englishMap.put("member", "Member");
        englishMap.put("recruiter", "Recruiter");
        englishMap.put("guest", "Guest");
        englishMap.put("not-allowed-place", "You aren't allowed to place $1");
        englishMap.put("starter-book", "Menu for Civs");
        englishMap.put("set-member", "Set Member");
        englishMap.put("set-owner", "Set Owner");
        englishMap.put("set-guest", "Set Guest");
        englishMap.put("remove-member", "Remove Member");
        englishMap.put("member-added-region", "$1 is now a member of your $2");
        englishMap.put("add-member-region", "You have been made a member of $1");
        englishMap.put("member-description", "Members can build and use the region");
        englishMap.put("guest-description", "Guests can use doors and buttons in the region");
        englishMap.put("owner-description", "Owners can do anything in the region");
        englishMap.put("remove-member-region", "You are no longer a member of $1");
        englishMap.put("member-removed-region", "$1 is no longer a member of $2");
        englishMap.put("add-owner-region", "You have been made an owner of $1");
        englishMap.put("owner-added-region", "$1 is now an owner of your $2");
        englishMap.put("add-guest-region", "You have been made a guest of $1");
        englishMap.put("guest-added-region", "$1 is now a guest of your $2");
        englishMap.put("invite-member-region", "You have been become a member of $1");
        englishMap.put("member-invited-region", "$1 has been added to your $2");
        englishMap.put("stand-in-region", "Please have $1 stand in the region");
        englishMap.put("prev-button", "Previous");
        englishMap.put("next-button", "Next");
        englishMap.put("town-instructions", "To create a $1, use /cv town MyTownName");
        englishMap.put("specify-town-name", "Please specify a town name");
        englishMap.put("hold-town", "Please hold a town item");
        englishMap.put("players", "Players");
        englishMap.put("towns", "Towns");
        englishMap.put("your-towns", "Your towns");
        englishMap.put("wars", "Wars");
        englishMap.put("leaderboard", "Leaderboard");
        englishMap.put("enter-town", "You have entered $1");
        englishMap.put("exit-town", "You have exited $1");
        englishMap.put("new-town-member", "$1 has joined $2");

        languageMap.put("en", englishMap);
    }

    public static LocaleManager getInstance() {
        if (localeManager == null) {
            localeManager = new LocaleManager();
        }
        return localeManager;
    }
}

package org.redcastlemedia.multitallented.civs.civilians;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.anticheat.ExemptionType;
import org.redcastlemedia.multitallented.civs.civclass.CivClass;
import org.redcastlemedia.multitallented.civs.civclass.ClassManager;
import org.redcastlemedia.multitallented.civs.civclass.ClassType;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.items.CivItem;
import org.redcastlemedia.multitallented.civs.items.ItemManager;
import org.redcastlemedia.multitallented.civs.regions.Region;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;
import org.redcastlemedia.multitallented.civs.regions.RegionType;
import org.redcastlemedia.multitallented.civs.skills.Skill;
import org.redcastlemedia.multitallented.civs.spells.civstate.BuiltInCivState;
import org.redcastlemedia.multitallented.civs.spells.civstate.CivState;
import org.redcastlemedia.multitallented.civs.spells.effects.ManaEffect;
import org.redcastlemedia.multitallented.civs.towns.Town;
import org.redcastlemedia.multitallented.civs.towns.TownManager;
import org.redcastlemedia.multitallented.civs.util.ActionBarUtil;
import org.redcastlemedia.multitallented.civs.util.MessageUtil;
import org.redcastlemedia.multitallented.civs.util.PermissionUtil;

import lombok.Getter;
import lombok.Setter;

public class Civilian {

    @Getter
    private final UUID uuid;
    @Getter
    private final Map<CivItem, Integer> exp;
    @Getter
    private final Set<CivClass> civClasses = new HashSet<>();
    private String locale;
    @Getter @Setter
    private Map<String, Integer> stashItems;
    @Getter
    private final Map<String, CivState> states;
    @Getter @Setter
    private Location respawnPoint = null;
    private long lastJail = 0;
    private int kills;
    private int killStreak;
    private int deaths;
    private long lastDeath = 0;
    private int highestKillStreak;
    private double points;
    private int karma;
    private int mana;
    @Getter @Setter
    private long lastDamage = -1;
    @Getter @Setter
    private UUID lastDamager;
    @Getter @Setter
    private Set<UUID> friends = new HashSet<>();
    @Getter @Setter
    private List<Bounty> bounties = new ArrayList<>();
    @Getter @Setter
    private long lastKarmaDepreciation;
    @Getter @Setter
    private double hardship;
    @Getter @Setter
    private int daysSinceLastHardshipDepreciation;
    @Getter @Setter
    private int tutorialIndex;
    @Getter @Setter
    private Set<String> completedTutorialSteps = new HashSet<>();
    @Getter @Setter
    private String tutorialPath;
    @Getter @Setter
    private int tutorialProgress;
    @Getter @Setter
    private boolean useAnnouncements;
    @Getter @Setter
    private ChatChannel chatChannel;

    private CivClass currentClass;
    @Getter
    private final Set<ExemptionType> exemptions = new HashSet<>();
    @Getter @Setter
    private HashMap<String, Skill> skills = new HashMap<>();
    @Getter
    private Map<Integer, ItemStack> combatBar = new HashMap<>();

    public Civilian(UUID uuid, String locale, Map<String, Integer> stashItems,
                    Map<CivItem, Integer> exp, int kills, int killStreak, int deaths, int highestKillStreak,
                    double points, int karma) {
        this.uuid = uuid;
        this.locale = locale;
        this.stashItems = stashItems;
        this.exp = exp;
        this.states = new HashMap<>();
        this.kills = kills;
        this.killStreak = killStreak;
        this.deaths = deaths;
        this.highestKillStreak = highestKillStreak;
        this.points = points;
        this.karma = karma;
        this.mana = 0;
        this.chatChannel = new ChatChannel(ChatChannel.ChatChannelType.GLOBAL, null);
    }

    public String getLocale() {
        if (locale == null) {
            locale = ConfigManager.getInstance().getDefaultLanguage();
        }
        return locale;
    }
    public CivClass getRawCurrentClass() {
        return currentClass;
    }
    public CivClass getCurrentClass() {
        if (currentClass == null) {
            Civs.logger.log(Level.WARNING, "Null class detected for player {}", uuid);
            if (!civClasses.isEmpty()) {
                ClassManager.getInstance().switchClass(this, this.civClasses.iterator().next());
            } else {
                currentClass = ClassManager.getInstance().createDefaultClass(uuid);
            }
            if (currentClass == null) {
                Civs.logger.log(Level.SEVERE, "Unable to recover default class for player {}", uuid);
            }
        }
        return currentClass;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    public long getLastJail() { return lastJail; }
    public void refreshJail() { lastJail = System.currentTimeMillis(); }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public int getKillStreak() { return killStreak; }
    public void setKillStreak(int killStreak) { this.killStreak = killStreak; }
    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void refreshDeath() { this.lastDeath = System.currentTimeMillis(); }
    public long getLastDeath() { return lastDeath; }
    public int getHighestKillStreak() { return highestKillStreak; }
    public void setHighestKillStreak(int highestKillStreak) { this.highestKillStreak = highestKillStreak; }
    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
    public int getKarma() { return karma; }
    public void setKarma(int karma) { this.karma = karma; }
    public int getMana() { return mana; }
    public void setMana(int mana) {
        setMana(mana, true);
    }
    public void setMana(int mana, boolean setManaBar) {
        this.mana = Math.max(mana, 0);
        Player player = Bukkit.getPlayer(uuid);
        if (setManaBar && player != null && player.isOnline()) {
            String message = ManaEffect.getManaBar(this);
            ActionBarUtil.sendActionBar(player, message);
        }
    }

    public void setCurrentClass(CivClass civClass) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        Player player = offlinePlayer.getPlayer();
        if (currentClass != null) {
            CivItem civItem = ItemManager.getInstance().getItemType(currentClass.getType());
            if (civItem != null && Civs.perm != null && player != null) {
                ClassType classTypeOld = (ClassType) civItem;
                for (Map.Entry<String, Integer> entry : classTypeOld.getClassPermissions().entrySet()) {
                    if (civClass.getLevel() >= entry.getValue()) {
                        Civs.perm.playerRemove(player, entry.getKey());
                    }
                }
            }
        }
        currentClass = civClass;
        ClassType classTypeNew = (ClassType) ItemManager.getInstance().getItemType(civClass.getType());
        if (Civs.perm != null && player != null) {
            for (Map.Entry<String, Integer> entry : classTypeNew.getClassPermissions().entrySet()) {
                if (currentClass.getLevel() >= entry.getValue()) {
                    Civs.perm.playerAdd(player, entry.getKey());
                }
            }
        }
    }

    public boolean isInCombat() {
        if (lastDamage < 0) {
            return false;
        }
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || player.isDead()) {
            lastDamager = null;
            lastDamage = -1;
            return false;
        }
        if (lastDamager != null && (Bukkit.getPlayer(lastDamager) == null ||
                Bukkit.getPlayer(lastDamager).isDead())) {
            lastDamager = null;
            lastDamage = -1;
            return false;
        }
        int combatTagDuration = ConfigManager.getInstance().getCombatTagDuration();
        combatTagDuration *= 1000;
        if (lastDamage + combatTagDuration < System.currentTimeMillis()) {
            lastDamager = null;
            lastDamage = -1;
            return false;
        }
        return true;
    }

    public boolean hasBuiltInState(BuiltInCivState builtInCivState) {
        for (CivState state : states.values()) {
            if (state.getVars().containsKey(builtInCivState.name())) {
                return true;
            }
        }
        return false;
    }

    public void addExp(CivItem civItem, int amount) {
        if (civItem == null || exp == null) {
            return;
        }
        exp.merge(civItem, amount, Integer::sum);
    }

    public int getLevel(CivItem civItem) {
        if (civItem != null && civItem.getItemType() == CivItem.ItemType.CLASS) {
            int level = 0;
            for (CivClass civClass : civClasses) {
                if (civClass.getType().equalsIgnoreCase(civItem.getProcessedName())) {
                    level = Math.max(level, civClass.getLevel());
                }
            }
            return level;
        }

        if (civItem == null || exp == null || exp.get(civItem) == null) {
            return 1;
        }
        double experience = exp.get(civItem);
        if (experience == 0) {
            return 1;
        }
        ConfigManager configManager = ConfigManager.getInstance();
        double modifier = configManager.getExpModifier();
        double base = configManager.getExpBase();
        int level = 1;
        for (;;) {
            experience -= base + (level - 1) * modifier * base;
            if (experience < 0) {
                break;
            }
            level++;
        }
        return level;
    }

    public boolean isAtGroupMax(String group) {
        if (ConfigManager.getInstance().getGroups().get(group) != null &&
                ConfigManager.getInstance().getGroups().get(group) <= getCountGroup(group)) {
            return true;
        }
        return false;
    }

    public String isAtMax(CivItem civItem) {
        return isAtMax(civItem, false);
    }

    public String isAtMax(CivItem civItem, boolean isCountRebuild) {
        String processedName = civItem.getProcessedName();
        int rebuildBonus = 0;
        if (isCountRebuild && CivItem.ItemType.REGION == civItem.getItemType() &&
                null != ((RegionType) civItem).getRebuild() && !((RegionType) civItem).getRebuild().isEmpty()) {
            rebuildBonus = 1;
        }
        boolean atMax = civItem.getCivMax() != -1 &&
                civItem.getCivMax() <= getCountStashItems(processedName) + getCountNonStashItems(processedName);
        if (atMax) {
            return civItem.getProcessedName();
        }
        ConfigManager configManager = ConfigManager.getInstance();
        if (civItem.getGroups() == null ||
                civItem.getGroups().isEmpty()) {
            return null;
        }
        for (String group : civItem.getGroups()) {
            if (configManager.getGroups().get(group) != null &&
                    configManager.getGroups().get(group) + rebuildBonus <= getCountGroup(group)) {
                return group;
            }
        }
        return null;
    }

    public int getCountStashItems(String name) {
        int count = 0;
        for (String currentName : stashItems.keySet()) {
            if (currentName.equalsIgnoreCase(name)) {
                count += stashItems.get(currentName);
            }
        }
        return count;
    }

    public int getCountGroup(String group) {
        int count = 0;
        HashSet<String> removeThese = new HashSet<>();
        ItemManager itemManager = ItemManager.getInstance();
        for (String currentName : stashItems.keySet()) {
            CivItem item = ItemManager.getInstance().getItemType(currentName);
            if (item == null) {
                removeThese.add(currentName);
                continue;
            }
            if (item.getGroups().contains(group)) {
                count += stashItems.get(currentName);
            }
        }
        for (String removeThis : removeThese) {
            stashItems.remove(removeThis);
        }
        for (ItemStack is : Bukkit.getPlayer(uuid).getInventory()) {
            if (is == null || !is.hasItemMeta()) {
                continue;
            }
            String displayName = is.getItemMeta().getDisplayName();
            if (displayName == null) {
                continue;
            }
            displayName = ChatColor.stripColor(displayName).replace(
                    ChatColor.stripColor(ConfigManager.getInstance().getCivsItemPrefix()), "").toLowerCase();
            CivItem item = itemManager.getItemType(displayName);
            if (item == null) {
                continue;
            }
            if (!item.getGroups().contains(group)) {
                continue;
            }
            count += is.getAmount();
        }

        for (Region region : RegionManager.getInstance().getAllRegions()) {
            CivItem item = itemManager.getItemType(region.getType());
            if (!item.getGroups().contains(group)) {
                continue;
            }
            if (!region.getOwners().contains(uuid)) {
                continue;
            }
            count++;
        }
        return count;
    }

    public int getCountRegions(String name) {
        int count = 0;
        for (Region region : RegionManager.getInstance().getAllRegions()) {
            RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
            if (region.getOwners().contains(uuid) && (name == null ||
                    region.getType().equalsIgnoreCase(name) ||
                    regionType.getGroups().contains(name))) {
                count++;
            }
        }
        return count;
    }

    public int getCountNonStashItems(String name) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack is : player.getInventory()) {
            if (!CVItem.isCivsItem(is)) {
                continue;
            }
            CivItem civItem = CivItem.getFromItemStack(is);
            if (civItem == null) {
                continue;
            }
            if (!civItem.getProcessedName().equalsIgnoreCase(name) &&
                    !civItem.getGroups().contains(name)) {
                continue;
            }
            count += is.getAmount();
        }

        count += getCountRegions(name);
        return count;
    }

    public void sortBounties() {
        if (bounties.size() < 2) {
            return;
        }
        Collections.sort(bounties, new Comparator<Bounty>() {
            @Override
            public int compare(Bounty o1, Bounty o2) {
                if (o1.getAmount() == o2.getAmount()) {
                    return 0;
                }
                return o1.getAmount() > o2.getAmount() ? 1 : -1;
            }
        });
    }

    public Bounty getHighestBounty() {
        Bounty highestBounty = null;
        for (Bounty bounty : this.bounties) {
            if (highestBounty == null || highestBounty.getAmount() < bounty.getAmount()) {
                highestBounty = bounty;
            }
        }
        for (Town town : TownManager.getInstance().getTowns()) {
            if (!town.getPeople().containsKey(uuid)) {
                continue;
            }
            for (Bounty bounty : town.getBounties()) {
                if (highestBounty == null || highestBounty.getAmount() < bounty.getAmount()) {
                    highestBounty = bounty;
                }
            }
        }
        return highestBounty;
    }

    public boolean isFriend(Civilian friend) {
        return friends.contains(friend.getUuid());
    }

    public void awardSkill(Player player, Collection<String> skillTypes, String skillName) {
        if (!ConfigManager.getInstance().isUseSkills()) {
            return;
        }
        for (Skill skill : skills.values()) {
            if (skill.getType().equalsIgnoreCase(skillName)) {
                double currentExp = 0;
                for (String potionName : skillTypes) {
                    currentExp += skill.addAccomplishment(potionName);
                }
                MessageUtil.saveCivilianAndSendExpNotification(player, this, skill, currentExp);
            }
        }
    }

    public void awardSkill(Player player, String skillType, String skillName) {
        if (!ConfigManager.getInstance().isUseSkills()) {
            return;
        }
        for (Skill skill : skills.values()) {
            if (skill.getType().equalsIgnoreCase(skillName)) {
                double currentExp = 0;
                currentExp += skill.addAccomplishment(skillType);
                MessageUtil.saveCivilianAndSendExpNotification(player, this, skill, currentExp);
            }
        }
    }
}

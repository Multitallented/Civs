package org.redcastlemedia.multitallented.civs.tutorials;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.CivsSingleton;
import org.redcastlemedia.multitallented.civs.ConfigManager;
import org.redcastlemedia.multitallented.civs.localization.LocaleConstants;
import org.redcastlemedia.multitallented.civs.localization.LocaleManager;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.menus.MenuManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.util.CommandUtil;
import org.redcastlemedia.multitallented.civs.util.FallbackConfigUtil;
import org.redcastlemedia.multitallented.civs.util.PermissionUtil;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@CivsSingleton(priority = CivsSingleton.SingletonLoadPriority.HIGH)
public class TutorialManager {
    private static TutorialManager tutorialManager = null;

    HashMap<String, TutorialPath> tutorials = new HashMap<>();

    public static TutorialManager getInstance() {
        if (tutorialManager == null) {
            tutorialManager = new TutorialManager();
            tutorialManager.loadTutorialFile();
        }
        return tutorialManager;
    }

    public void reload() {
        tutorials.clear();
        loadTutorialFile();
    }

    private void loadTutorialFile() {
        File dataFolder = Civs.dataLocation;
        File tutorialFile = new File(dataFolder, "Civs/tutorial.yml");
        FileConfiguration tutorialConfig = FallbackConfigUtil.getConfig(tutorialFile, "tutorial.yml");

        try {
            for (String key : tutorialConfig.getKeys(false)) {
                TutorialPath path = new TutorialPath();
                String iconString = tutorialConfig.getString(key + ".icon", "CHEST");
                if (iconString == null) {
                    iconString = "CHEST";
                }
                path.setIcon(CVItem.createCVItemFromString(iconString));

                for (Map<?,?> map : tutorialConfig.getMapList(key + ".steps")) {
                    TutorialStep tutorialStep = new TutorialStep();

                    tutorialStep.setType((String) map.get("type"));
                    tutorialStep.setRegion((String) map.get("region"));
                    tutorialStep.setKillType((String) map.get("kill-type"));
                    Integer times = (Integer) map.get("times");
                    tutorialStep.setTimes(times == null ? 1 : times);
                    LinkedHashMap<?,?> rewards = (LinkedHashMap<?,?>) map.get("rewards");
                    if (rewards != null) {
                        if (rewards.get("money") != null) {
                            if (rewards.get("money") instanceof Double) {
                                Double money = (Double) rewards.get("money");
                                tutorialStep.setRewardMoney(money);
                            } else if (rewards.get("money") instanceof Integer) {
                                Integer money = (Integer) rewards.get("money");
                                tutorialStep.setRewardMoney(money);
                            }
                        }

                        List<String> commands = (List<String>) rewards.get("commands");
                        if (commands != null) {
                            tutorialStep.setCommands(commands);
                        }
                        List<String> permissions = (List<String>) rewards.get("permissions");
                        if (permissions != null) {
                            tutorialStep.setCommands(permissions);
                        }

                        List<String> itemList = (List<String>) rewards.get("items");
                        if (itemList != null) {
                            for (String itemString : itemList) {
                                tutorialStep.getRewardItems().add(CVItem.createCVItemFromString(itemString));
                            }
                        }
                    }
                    ArrayList<String> pathsList = (ArrayList<String>) map.get("paths");
                    if (pathsList != null) {
                        tutorialStep.setPaths(pathsList);
                    }

                    path.getSteps().add(tutorialStep);
                }
                tutorials.put(key, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Civs.logger.severe("Unable to load tutorial.yml");
        }
    }

    public void completeStep(Civilian civilian, TutorialType type, String param) {
        if (!ConfigManager.getInstance().isUseTutorial()) {
            return;
        }

        if (civilian.getTutorialIndex() == -1) {
            return;
        }

        TutorialPath path = tutorials.get(civilian.getTutorialPath());
        if (path == null) {
            return;
        }
        if (path.getSteps().size() <= civilian.getTutorialIndex()) {
            return;
        }
        TutorialStep step = path.getSteps().get(civilian.getTutorialIndex());
        if (step == null) {
            return;
        }

        if (!step.getType().equalsIgnoreCase(type.toString())) {
            return;
        }

        if ((type.equals(TutorialType.BUILD) || type.equals(TutorialType.UPKEEP) ||
                type.equals(TutorialType.BUY)) &&
                !param.equalsIgnoreCase(step.getRegion())) {
            return;
        }
        if (type.equals(TutorialType.KILL) && !param.equals(step.getKillType())) {
            return;
        }

        int progress = civilian.getTutorialProgress();
        int maxProgress = step.getTimes();
        if (progress + 1 < maxProgress) {
            civilian.setTutorialProgress(progress + 1);
            CivilianManager.getInstance().saveCivilian(civilian);
            // TODO send message of progress made?
            return;
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(civilian.getUuid());
        Player player = null;
        if (offlinePlayer.isOnline()) {
            player = offlinePlayer.getPlayer();
        }

        ArrayList<CVItem> itemList = step.getRewardItems();
        if (itemList != null && !itemList.isEmpty() && player != null && player.isOnline()) {
            giveItemsToPlayer(player, itemList);
        }

        double money = step.getRewardMoney();
        if (money > 0 && Civs.econ != null) {
            Civs.econ.depositPlayer(offlinePlayer, money);
        }
        List<String> permissions = step.getPermissions();
        if (Civs.perm != null && !permissions.isEmpty()) {
            for (String permission : permissions) {
                PermissionUtil.applyPermission(offlinePlayer, permission);
            }
        }
        List<String> commands = step.getCommands();
        if (!commands.isEmpty()) {
            for (String command : commands) {
                CommandUtil.performCommand(offlinePlayer, command);
            }
        }

        civilian.setTutorialProgress(0);
        civilian.setTutorialIndex(civilian.getTutorialIndex() + 1);
        CivilianManager.getInstance().saveCivilian(civilian);

        Util.spawnRandomFirework(player);

        sendMessageForCurrentTutorialStep(civilian, true);
    }

    public void sendMessageForCurrentTutorialStep(Civilian civilian, boolean useHr) {
        if (civilian.getTutorialIndex() == -1) {
            return;
        }
        TutorialPath path = tutorials.get(civilian.getTutorialPath());
        if (path == null) {
            return;
        }
        if (path.getSteps().size() <= civilian.getTutorialIndex()) {
            return;
        }
        TutorialStep step = path.getSteps().get(civilian.getTutorialIndex());
        if (step == null) {
            return;
        }


        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || !player.isOnline()) {
            return;
        }
        for (String message : getNextTutorialStepMessage(civilian, useHr)) {
            player.sendMessage(Civs.getPrefix() + message);
        }

        String type = step.getType();
        if ("choose".equals(type)) {
            player.closeInventory();
            MenuManager.getInstance().openMenu(player, "tutorial-choose-path", new HashMap<>());
        }
    }

    public List<String> getNextTutorialStepMessage(Civilian civilian, boolean useHr) {
        List<String> messages = new ArrayList<>();
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (player == null || !player.isOnline()) {
            return messages;
        }
        String rawMessage = LocaleManager.getInstance().getTranslation(player,
                "tut-" + civilian.getTutorialPath() + "-" + civilian.getTutorialIndex());
        if (rawMessage == null || rawMessage.isEmpty()) {
            return messages;
        }
        if (useHr) {
            messages.add("-----------------" + Civs.NAME + "-----------------");
        }
        messages.addAll(Util.parseColors(Util.textWrap(civilian, rawMessage)));
        if (useHr) {
            messages.add("--------------------------------------");
        }
        return messages;
    }


    private void giveItemsToPlayer(Player player, List<CVItem> itemList) {
        for (CVItem cvItem : itemList) {
            int firstEmpty = player.getInventory().firstEmpty();
            if (firstEmpty > -1) {
                player.getInventory().addItem(cvItem.createItemStack());
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), cvItem.createItemStack());
            }
        }
    }

    public TutorialPath getPathByName(String pathName) {
        return tutorials.get(pathName);
    }

    public List<CVItem> getPathIcons(Civilian civilian) {
        ArrayList<CVItem> returnList = new ArrayList<>();
        if (civilian.getTutorialIndex() == -1) {
            return returnList;
        }
        TutorialPath path = tutorials.get(civilian.getTutorialPath());
        if (path == null) {
            return returnList;
        }
        if (civilian.getTutorialIndex() >= path.getSteps().size()) {
            civilian.setTutorialIndex(path.getSteps().size() - 1);
        }
        if (civilian.getTutorialIndex() < 0) {
            civilian.setTutorialIndex(0);
        }
        TutorialStep step = path.getSteps().get(civilian.getTutorialIndex());
        if (step == null) {
            return returnList;
        }
        if (!"choose".equals(step.getType())) {
            return returnList;
        }
        ArrayList<String> pathsList = step.getPaths();
        if (pathsList == null) {
            return returnList;
        }
        for (String pathKey : pathsList) {
            TutorialPath newPath = tutorials.get(pathKey);
            CVItem cvItem = newPath.getIcon();
            String name = LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "tut-" + pathKey + LocaleConstants.NAME_SUFFIX);
            cvItem.setDisplayName(name);
            cvItem.setLore(Util.textWrap(civilian, LocaleManager.getInstance().getTranslation(civilian.getLocale(),
                    "tut-" + pathKey + LocaleConstants.DESC_SUFFIX)));
            returnList.add(cvItem);
        }

        return returnList;
    }

    public List<String> getPaths(Civilian civilian) {
        ArrayList<String> returnList = new ArrayList<>();
        if (civilian.getTutorialIndex() == -1) {
            return returnList;
        }
        TutorialPath path = tutorials.get(civilian.getTutorialPath());
        if (path == null) {
            return returnList;
        }
        if (civilian.getTutorialIndex() >= path.getSteps().size()) {
            civilian.setTutorialIndex(path.getSteps().size() - 1);
        }
        if (civilian.getTutorialIndex() < 0) {
            civilian.setTutorialIndex(0);
        }
        TutorialStep step = path.getSteps().get(civilian.getTutorialIndex());
        if (step == null) {
            return returnList;
        }
        if (!"choose".equals(step.getType())) {
            return returnList;
        }
        ArrayList<String> pathsList = step.getPaths();
        if (pathsList == null) {
            return returnList;
        }
        return pathsList;
    }

    public void printTutorial(HumanEntity player, Civilian civilian) {
        String tutorialUrl = ConfigManager.getInstance().getTutorialUrl();
        player.sendMessage(Util.parseColors(ConfigManager.getInstance().getTopGuideSpacer()));
        TutorialManager.getInstance().sendMessageForCurrentTutorialStep(civilian, false);
        player.sendMessage(LocaleManager.getInstance().getTranslation(civilian.getLocale(), "tutorial-click"));
        player.sendMessage(tutorialUrl);
        player.sendMessage(Util.parseColors(ConfigManager.getInstance().getBottomGuideSpacer()));
    }


    public enum TutorialType {
        BUILD,
        UPKEEP,
        KILL,
        BUY
    }
}

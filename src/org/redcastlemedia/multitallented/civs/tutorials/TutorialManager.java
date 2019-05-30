package org.redcastlemedia.multitallented.civs.tutorials;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.menus.TutorialChoosePathMenu;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TutorialManager {
    private static TutorialManager tutorialManager = null;

    HashMap<String, TutorialPath> tutorials = new HashMap<>();

    public TutorialManager() {
        tutorialManager = this;

        loadTutorialFile();
    }

    public static TutorialManager getInstance() {
        if (tutorialManager == null) {
            new TutorialManager();
        }
        return tutorialManager;
    }

    public void reload() {
        tutorials.clear();
        loadTutorialFile();
    }

    private void loadTutorialFile() {
        if (Civs.getInstance() == null) {
            return;
        }

        File dataFolder = Civs.getInstance().getDataFolder();
        File tutorialFile = new File(dataFolder, "tutorial.yml");
        FileConfiguration tutorialConfig = new YamlConfiguration();

        try {
            tutorialConfig.load(tutorialFile);
            for (String key : tutorialConfig.getKeys(false)) {
                TutorialPath path = new TutorialPath();
                String iconString = tutorialConfig.getString(key + ".icon", "CHEST");
                if (iconString == null) {
                    iconString = "CHEST";
                }
                path.setIcon(CVItem.createCVItemFromString(iconString));
                ConfigurationSection section = tutorialConfig.getConfigurationSection(key + ".names");
                if (section != null) {
                    for (String locale : section.getKeys(false)) {
                        path.getNames().put(locale, section.getString(locale));
                    }
                }

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

                        List<String> itemList = (List<String>) rewards.get("items");
                        if (itemList != null) {
                            for (String itemString : itemList) {
                                tutorialStep.getRewardItems().add(CVItem.createCVItemFromString(itemString));
                            }
                        }
                    }
                    LinkedHashMap<?,?> messages = (LinkedHashMap<?,?>) map.get("messages");
                    if (messages != null) {
                        for (Object locale : messages.keySet()) {
                            tutorialStep.getMessages().put((String) locale, (String) messages.get(locale));
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
        if (Civs.getInstance() == null) {
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
        Player player = Bukkit.getPlayer(civilian.getUuid());

        ArrayList<CVItem> itemList = step.getRewardItems();
        if (itemList != null && !itemList.isEmpty() && player.isOnline()) {
            giveItemsToPlayer(player, itemList);
        }

        double money = step.getRewardMoney();
        if (money > 0 && Civs.econ != null) {
            Civs.econ.depositPlayer(player, money);
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
        HashMap<String, String> messageMap = step.getMessages();
        if (!messageMap.isEmpty()) {
            String rawMessage = messageMap.get(civilian.getLocale());
            if (rawMessage == null) {
                rawMessage = messageMap.get("en");
            }
            if (rawMessage != null && player.isOnline()) {
                if (useHr) {
                    player.sendMessage("-----------------" + Civs.NAME + "-----------------");
                }
                List<String> messages = Util.parseColors(Util.textWrap("", rawMessage));
                for (String message : messages) {
                    player.sendMessage(Civs.getPrefix() + message);
                }
                if (useHr) {
                    player.sendMessage("--------------------------------------");
                }
            }
        }

        String type = step.getType();
        if ("choose".equals(type)) {
            player.closeInventory();
            player.openInventory(TutorialChoosePathMenu.createMenu(civilian));
        }
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

    public List<CVItem> getPaths(Civilian civilian) {
        ArrayList<CVItem> returnList = new ArrayList<>();
        if (civilian.getTutorialIndex() == -1) {
            return returnList;
        }
        TutorialPath path = tutorials.get(civilian.getTutorialPath());
        if (path == null) {
            return returnList;
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
            String name = newPath.getNames().get(civilian.getLocale());
            if ("".equals(name)) {
                name = path.getNames().get("en");
            }
            if (!"".equals(name)) {
                cvItem.setDisplayName(name);
                ArrayList<String> lore = new ArrayList<>();
                lore.add(pathKey);
                cvItem.setLore(lore);
                returnList.add(cvItem);
            }
        }

        return returnList;
    }


    public enum TutorialType {
        BUILD,
        UPKEEP,
        KILL,
        BUY
    }
}

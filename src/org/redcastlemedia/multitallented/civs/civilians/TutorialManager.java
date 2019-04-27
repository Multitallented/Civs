package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.menus.TutorialChoosePathMenu;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TutorialManager {
    private static TutorialManager tutorialManager = null;

    private FileConfiguration tutorialConfig;

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

    private void loadTutorialFile() {
        if (Civs.getInstance() == null) {
            return;
        }

        File dataFolder = Civs.getInstance().getDataFolder();
        File tutorialFile = new File(dataFolder, "tutorial.yml");
        tutorialConfig = new YamlConfiguration();

        try {
            tutorialConfig.load(tutorialFile);
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

        ConfigurationSection path = tutorialConfig.getConfigurationSection(civilian.getTutorialPath());
        if (path == null) {
            return;
        }
        List<Map<?,?>> steps = path.getMapList("steps");
        if (steps.size() <= civilian.getTutorialIndex()) {
            return;
        }
        Map<?,?> step = steps.get(civilian.getTutorialIndex());
        if (step == null) {
            return;
        }

        if (!((String) step.get("type")).equalsIgnoreCase(type.toString())) {
            return;
        }

        if ((type.equals(TutorialType.BUILD) || type.equals(TutorialType.UPKEEP) ||
                type.equals(TutorialType.BUY)) &&
                !param.equalsIgnoreCase(((String) step.get("region")))) {
            return;
        }
        if (type.equals(TutorialType.KILL) && !param.equals(step.get("kill-type"))) {
            return;
        }

        int progress = civilian.getTutorialProgress();
        Integer times = (Integer) step.get("times");
        int maxProgress = times == null ? 0 : times;
        if (progress < maxProgress) {
            civilian.setTutorialProgress(progress + 1);
            CivilianManager.getInstance().saveCivilian(civilian);
            // TODO send message of progress made?
            return;
        }
        Player player = Bukkit.getPlayer(civilian.getUuid());

        LinkedHashMap<?,?> rewards = (LinkedHashMap<?,?>) step.get("rewards");
        if (rewards != null) {
            List<String> itemList = (List<String>) rewards.get("items");
            if (itemList != null && !itemList.isEmpty() && player.isOnline()) {
                giveItemsToPlayer(player, itemList);
            }

            Object moneyDouble = rewards.get("money");
            double money = 0;
            if (moneyDouble != null) {
                if (moneyDouble instanceof Double) {
                    money = (Double) moneyDouble;
                } else if (moneyDouble instanceof Integer) {
                    money = (Integer) moneyDouble;
                }

                if (money > 0 && Civs.econ != null) {
                    Civs.econ.depositPlayer(player, money);
                }
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
        ConfigurationSection path = tutorialConfig.getConfigurationSection(civilian.getTutorialPath());
        if (path == null) {
            return;
        }
        List<Map<?,?>> stepList = path.getMapList("steps");
        if (stepList.size() <= civilian.getTutorialIndex()) {
            return;
        }
        Map<?,?> step = stepList.get(civilian.getTutorialIndex());
        if (step == null) {
            return;
        }


        Player player = Bukkit.getPlayer(civilian.getUuid());
        Object messageObj = step.get("messages");
        if (messageObj != null) {
            String rawMessage = (String) ((LinkedHashMap<?,?>) step.get("messages")).get(civilian.getLocale());
            if (rawMessage == null) {
                rawMessage = (String) ((LinkedHashMap<?,?>) step.get("messages")).get("en");
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

        Object type = step.get("type");
        if ("choose".equals(type)) {
            player.closeInventory();
            player.openInventory(TutorialChoosePathMenu.createMenu(civilian));
        }
    }


    private void giveItemsToPlayer(Player player, List<String> itemList) {
        for (String item : itemList) {
            CVItem cvItem = CVItem.createCVItemFromString(item);
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
        ConfigurationSection path = tutorialConfig.getConfigurationSection(civilian.getTutorialPath());
        if (path == null) {
            return returnList;
        }
        Map<?,?> step = path.getMapList("steps").get(civilian.getTutorialIndex());
        if (step == null) {
            return returnList;
        }
        if (!"choose".equals(step.get("type"))) {
            return returnList;
        }
        Object pathsObj = step.get("paths");
        if (pathsObj == null) {
            return returnList;
        }
        for (String pathKey : (List<String>) pathsObj) {
            String matString = tutorialConfig.getString(pathKey + ".icon", "CHEST");
            String name = tutorialConfig.getString(pathKey + ".names." + civilian.getLocale(), "");
            if ("".equals(name)) {
                name = tutorialConfig.getString(pathKey + ".names.en", "");
            }
            if (!"".equals(name)) {
                name = name.replaceAll("\\.", "").replaceAll("\\*", "");
                CVItem cvItem = CVItem.createCVItemFromString(matString);
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

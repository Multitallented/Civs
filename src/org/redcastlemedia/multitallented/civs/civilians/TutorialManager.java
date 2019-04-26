package org.redcastlemedia.multitallented.civs.civilians;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.redcastlemedia.multitallented.civs.Civs;
import org.redcastlemedia.multitallented.civs.util.CVItem;
import org.redcastlemedia.multitallented.civs.util.Util;

import java.io.File;
import java.util.List;

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
        File dataFolder = Civs.getInstance().getDataFolder();
        File tutorialFile = new File(dataFolder, "tutorial.yml");
        tutorialConfig = new YamlConfiguration();

        try {
            tutorialConfig.load(tutorialFile);

        } catch (Exception e) {
            Civs.logger.severe("Unable to load tutorial.yml");
        }
    }

    public void completeStep(Civilian civilian, TutorialType type, String param) {
        if (civilian.getTutorialIndex() == -1) {
            return;
        }

        ConfigurationSection path = tutorialConfig.getConfigurationSection(civilian.getTutorialPath());
        if (path == null) {
            return;
        }
        ConfigurationSection step = path.getConfigurationSection("steps[" + civilian.getTutorialIndex() + "]");
        if (step == null) {
            return;
        }

        if (!step.getString("type").equalsIgnoreCase(type.toString())) {
            return;
        }

        if ((type.equals(TutorialType.BUILD) || type.equals(TutorialType.UPKEEP)) &&
                !param.equalsIgnoreCase(step.getString("region"))) {
            return;
        }
        if (type.equals(TutorialType.KILL) && !param.equals(step.getString("kill-type"))) {
            return;
        }

        int progress = civilian.getTutorialProgress();
        int maxProgress = step.getInt("times", 0);
        if (progress < maxProgress) {
            civilian.setTutorialProgress(progress + 1);
            CivilianManager.getInstance().saveCivilian(civilian);
            // TODO send message of progress made?
            return;
        }

        Player player = Bukkit.getPlayer(civilian.getUuid());
        List<String> itemList = step.getStringList("rewards.items");
        if (itemList != null && !itemList.isEmpty() && player.isOnline()) {
            giveItemsToPlayer(player, itemList);
        }

        double money = step.getDouble("rewards.money", 0);
        if (money > 0 && Civs.econ != null) {
            Civs.econ.depositPlayer(player, money);
        }

        civilian.setTutorialProgress(0);
        civilian.setTutorialIndex(civilian.getTutorialIndex() + 1);
        CivilianManager.getInstance().saveCivilian(civilian);

        Util.spawnRandomFirework(player);

        sendMessageForCurrentTutorialStep(civilian);
    }

    public void sendMessageForCurrentTutorialStep(Civilian civilian) {
        if (civilian.getTutorialIndex() == -1) {
            return;
        }
        ConfigurationSection path = tutorialConfig.getConfigurationSection(civilian.getTutorialPath());
        if (path == null) {
            return;
        }
        ConfigurationSection step = path.getConfigurationSection("steps[" + civilian.getTutorialIndex() + "]");
        if (step == null) {
            return;
        }


        String rawMessage = step.getString("messages." + civilian.getLocale(), "");
        if ("".equals(rawMessage)) {
            rawMessage = step.getString("messages.en", "");
        }
        Player player = Bukkit.getPlayer(civilian.getUuid());
        if (!"".equals(rawMessage) && player.isOnline()) {
            List<String> messages = Util.parseColors(Util.textWrap("", rawMessage));
            for (String message : messages) {
                player.sendMessage(Civs.getPrefix() + message);
            }
        }

        // TODO open menu if type == CHOOSE
    }


    private void giveItemsToPlayer(Player player, List<String> itemList) {
        for (String item : itemList) {
            CVItem cvItem = CVItem.createCVItemFromString(item);
            if (player.getInventory().firstEmpty() > -1) {
                player.getInventory().addItem(cvItem.createItemStack());
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), cvItem.createItemStack());
            }
        }
    }


    public enum TutorialType {
        BUILD,
        UPKEEP,
        CHOOSE,
        KILL
    }
}

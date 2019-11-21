package org.redcastlemedia.multitallented.civs.tutorials;

import org.bukkit.Bukkit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.redcastlemedia.multitallented.civs.TestUtil;
import org.redcastlemedia.multitallented.civs.civilians.Civilian;
import org.redcastlemedia.multitallented.civs.civilians.CivilianManager;
import org.redcastlemedia.multitallented.civs.items.CVItem;
import org.redcastlemedia.multitallented.civs.regions.RegionManager;

import java.util.ArrayList;
import java.util.HashMap;

public class TutorialTests {
    private Civilian civilian;
    private HashMap<String, TutorialPath> tutorials;

    @BeforeClass
    public static void onBeforeEverything() {
        if (Bukkit.getServer() == null) {
            TestUtil.serverSetup();
        }
    }

    @Before
    public void onBefore() {
        RegionManager.getInstance().reload();
        TutorialManager.getInstance().reload();
        civilian = CivilianManager.getInstance().getCivilian(TestUtil.player.getUniqueId());
        civilian.setTutorialPath("default");
        tutorials = new HashMap<>();

        TutorialPath defaultPath = new TutorialPath();
        defaultPath.setIcon(CVItem.createCVItemFromString("DIRT"));
        TutorialStep tutorialStep = new TutorialStep();
        tutorialStep.setType("choose");
        ArrayList<String> paths = new ArrayList<>();
        paths.add("merchant");
        tutorialStep.setPaths(paths);
        defaultPath.getSteps().add(tutorialStep);
        tutorials.put("default", defaultPath);

        TutorialPath merchantPath = new TutorialPath();
        merchantPath.setIcon(CVItem.createCVItemFromString("DIAMOND"));
        tutorials.put("merchant", merchantPath);

        TutorialManager.getInstance().tutorials = tutorials;
    }
}

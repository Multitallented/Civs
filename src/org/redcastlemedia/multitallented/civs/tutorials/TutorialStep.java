package org.redcastlemedia.multitallented.civs.tutorials;

import java.util.ArrayList;
import java.util.HashMap;

import org.redcastlemedia.multitallented.civs.util.CVItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorialStep {
    private String type;
    private String region;
    private int times;
    private String killType;
    private ArrayList<CVItem> rewardItems;
    private double rewardMoney;
    private HashMap<String, String> messages;
    private ArrayList<String> paths;

    public TutorialStep() {
        messages = new HashMap<>();
        rewardItems = new ArrayList<>();
        paths = new ArrayList<>();
    }
}

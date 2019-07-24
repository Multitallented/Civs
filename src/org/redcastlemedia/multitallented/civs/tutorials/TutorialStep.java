package org.redcastlemedia.multitallented.civs.tutorials;

import java.util.ArrayList;

import org.redcastlemedia.multitallented.civs.items.CVItem;

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
    private ArrayList<String> paths;

    public TutorialStep() {
        rewardItems = new ArrayList<>();
        paths = new ArrayList<>();
    }
}

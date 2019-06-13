package org.redcastlemedia.multitallented.civs.tutorials;

import java.util.ArrayList;
import java.util.HashMap;

import org.redcastlemedia.multitallented.civs.util.CVItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TutorialPath {
    private CVItem icon;
    private ArrayList<TutorialStep> steps;

    public TutorialPath() {
        steps = new ArrayList<>();
    }
}

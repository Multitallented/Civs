package org.redcastlemedia.multitallented.civs.tutorials;

import java.util.ArrayList;

import org.redcastlemedia.multitallented.civs.items.CVItem;

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

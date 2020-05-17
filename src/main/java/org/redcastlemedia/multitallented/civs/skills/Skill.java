package org.redcastlemedia.multitallented.civs.skills;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Skill {
    private Map<String, Integer> accomplishments = new HashMap<>();
    private final String type;

    public Skill(String type) {
        this.type = type;
    }
}

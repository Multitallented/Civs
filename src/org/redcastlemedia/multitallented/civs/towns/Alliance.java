package org.redcastlemedia.multitallented.civs.towns;

import java.util.HashSet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alliance {
    private String name;
    private HashSet<String> members;
}

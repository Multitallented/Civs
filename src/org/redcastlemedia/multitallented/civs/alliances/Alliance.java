package org.redcastlemedia.multitallented.civs.alliances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alliance {
    private String name;
    private HashSet<String> members;
    private UUID lastRenamedBy;
    private HashMap<UUID, HashSet<ChunkClaim>> nationClaims = new HashMap<>();

    public Alliance() {
        members = new HashSet<>();
    }
}

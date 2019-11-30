package org.redcastlemedia.multitallented.civs.civilians;

import java.util.UUID;

public class Bounty {
    private final UUID issuer;
    private final double amount;

    public Bounty(UUID issuer, double amount) {
        this.issuer = issuer;
        this.amount = amount;
    }

    public UUID getIssuer() {
        return issuer;
    }

    public double getAmount() {
        return amount;
    }
}

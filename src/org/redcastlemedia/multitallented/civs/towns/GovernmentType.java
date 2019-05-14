package org.redcastlemedia.multitallented.civs.towns;

public enum GovernmentType {
    OLIGARCHY, // ownership is for sale
    DEMOCRACY, // periodic voting on ownership, alliances, taxes?
    DICTATORSHIP,
    COMMUNISM, // all payouts distributed among town, member overrides
    COOPERATIVE, // all payouts are distributed
    ANARCHY, // everyone is an owner
    LIBERTARIAN, // no overrides, no owners, indestructible, anyone can join
    SOCIALISM, // Farms, factories, mines, quarries are public
    DEMOCRATIC_SOCIALISM, // socialism + democracy
    KRATEROCRACY, // Rule by the strong
    MERITOCRACY, // Ownership given to person who contributes the most power to the town
    CYBERSYNACY, // Rule by AI
    TRIBALISM, // unable to join other towns
    FEUDALISM, // only owner can build regions
    COLONIALISM, // ownership override by linked town
    CAPITALISM // pay to get more votes
}

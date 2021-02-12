package org.redcastlemedia.multitallented.civs.regions;

import lombok.Getter;
import lombok.Setter;
import org.redcastlemedia.multitallented.civs.items.CVItem;

import java.util.List;

public class RegionUpkeep {
    private final List<List<CVItem>> reagents;
    private final List<List<CVItem>> inputs;
    private final List<List<CVItem>> outputs;
    private final double payout;
    private final double exp;
    private int powerReagent;
    private int powerInput;
    private int powerOutput;
    @Getter
    private final String perm;
    @Getter @Setter
    private String command;

    public int getPowerReagent() {
        return powerReagent;
    }

    public void setPowerReagent(int powerReagent) {
        this.powerReagent = powerReagent;
    }

    public int getPowerInput() {
        return powerInput;
    }

    public void setPowerInput(int powerInput) {
        this.powerInput = powerInput;
    }

    public int getPowerOutput() {
        return powerOutput;
    }

    public void setPowerOutput(int powerOutput) {
        this.powerOutput = powerOutput;
    }

    public RegionUpkeep(List<List<CVItem>> reagents,
                        List<List<CVItem>> inputs,
                        List<List<CVItem>> outputs,
                        double payout,
                        double exp,
                        String perm) {
        this.reagents = reagents;
        this.inputs = inputs;
        this.outputs = outputs;
        this.payout = payout;
        this.exp = exp;
        this.perm = perm;
    }

    public List<List<CVItem>> getReagents() {
        return reagents;
    }

    public List<List<CVItem>> getInputs() {
        return inputs;
    }

    public List<List<CVItem>> getOutputs() {
        return outputs;
    }

    public double getPayout() {
        return payout;
    }

    public double getExp() {
        return exp;
    }
}

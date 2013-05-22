package org.rlcommunity.environments.octopus.protocol;

public class State {
    
    private double[] data;
    private boolean terminal;
    
    public State(double[] data, boolean terminal) {
        this.data = data;
        this.terminal = terminal;
    }
    
    public double[] getData() { return data; }
    
    public boolean isTerminal() { return terminal; }
}
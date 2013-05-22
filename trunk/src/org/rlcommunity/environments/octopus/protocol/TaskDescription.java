package org.rlcommunity.environments.octopus.protocol;

public class TaskDescription {
    
    private int numStateVariables, numActionVariables;
    
    public TaskDescription(int numStateVariables, int numActionVariables) {
        this.numStateVariables = numStateVariables;
        this.numActionVariables = numActionVariables;
    }
    
    public int getNumStateVariables() { return numStateVariables; }
    
    public int getNumActionVariables() { return numActionVariables; }    
}

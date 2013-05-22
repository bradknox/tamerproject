package org.rlcommunity.environments.octopus.protocol;

public class Observable {
    
    private State state;
    private double reward;
    
    public Observable(State state, double reward) {
        this.state = state;
        this.reward = reward;
    }
    
    public State getState() { return state; }
    
    public double getReward() { return reward; }
}

package org.rlcommunity.environments.octopus;
public interface EnvironmentObserver {
    
    public void episodeStarted();
    
    public void stateChanged(double reward);
    
    public void episodeFinished();
}

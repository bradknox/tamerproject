package org.rlcommunity.environments.octopus;

import java.io.*;
import java.util.*;

public class RewardLogger implements EnvironmentObserver {

    private PrintWriter out;
    
    public RewardLogger(String agentName) {
        try {
            File logFile = new File(String.format(
                    "%2$s-%1$tY-%1$tm-%1$td-%1$tH-%1$tM-%1$tS.log",
                    new Date(), agentName));
            out = new PrintWriter(logFile);
            
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    finish();
                }
            } );
        } catch (FileNotFoundException ex) { }
    }
    
    public void episodeStarted() { }
    
    public void stateChanged(double reward) {
        out.print(reward);
        out.print(' ');
    }
    
    public void episodeFinished() {
        out.println();
        out.flush();
    }
    
    public void finish() {
        out.close();
    }
}

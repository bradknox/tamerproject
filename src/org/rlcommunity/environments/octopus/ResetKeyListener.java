package org.rlcommunity.environments.octopus;

import java.awt.event.*;

public class ResetKeyListener implements KeyListener {
    
    private volatile boolean resetRequested;
    
    public ResetKeyListener() {
        resetRequested = false;
    }
    
    public void keyPressed(KeyEvent ke) { }
    
    public void keyReleased(KeyEvent ke) { }
        
    public void keyTyped(KeyEvent ke) {
        if (ke.getKeyChar() == ' ') {
            resetRequested = true;
        }
    }
    
    public boolean resetRequested() {
        boolean requested = resetRequested;
        resetRequested = false;
        return requested;
    }
}
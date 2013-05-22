/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rlcommunity.rlviz.app.frames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import java.lang.reflect.Constructor;

/**
 *
 * @author btanner
 */
public class GenericVizFrame extends JFrame {
static {
        if (System.getProperty("mrj.version") != null) {
            // the Mac specific code will go here
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
    }
    static int nextId = 0;
    VizMenus theMenus = null;
    VisualizerVizFrame envVizFrame = null;
    VisualizerVizFrame agentVizFrame = null;
    GenericVizFrame controlVizFrame = null;
    Vector<GenericVizFrame> otherFrames = null;
    private int thisId = 0;

    public GenericVizFrame(String windowName) {
        super(windowName);
        thisId = nextId;
        nextId++;
        otherFrames = new Vector<GenericVizFrame>();
    }

    GenericVizFrame() {
        this("No Name Given");
    }

    public void setFrames(GenericVizFrame controlVizFrame, VisualizerVizFrame envVizFrame, VisualizerVizFrame agentVizFrame) {
        this.controlVizFrame = controlVizFrame;
        this.envVizFrame = envVizFrame;
        this.agentVizFrame = agentVizFrame;

        if (controlVizFrame != null && controlVizFrame.getId() != getId()) {
            otherFrames.add(controlVizFrame);
        }
        if (envVizFrame != null && envVizFrame.getId() != getId()) {
            otherFrames.add(envVizFrame);
        }
        if (agentVizFrame != null && agentVizFrame.getId() != getId()) {
            otherFrames.add(agentVizFrame);
        }
    }

    public int getId() {
        return thisId;
    }

    public void makeMenus() {
        theMenus = new VizMenus(this);
    }

    public static void showAboutBox() {
        //default title and icon
        String theMessage = RLVizFrame.programName + " was created by Brian Tanner and the RLAI group at the University of Alberta.";
        theMessage += "\nYou're probably using as part of the RL Competition.  Good luck!  http://rl-competition.org";
        theMessage += "\nCopyright 2007.";
        theMessage += "\nhttp://research.tannerpages.com  email: brian@tannerpages.com";
        JOptionPane.showMessageDialog(null, theMessage, "About " + RLVizFrame.programName, JOptionPane.INFORMATION_MESSAGE);
    }
    
  
}

class VizMenus implements ActionListener {

    
    GenericVizFrame theVizFrame = null;

    VizMenus(GenericVizFrame theVizFrame) {
        this.theVizFrame = theVizFrame;
        //Where the GUI is created:
        JMenuBar menuBar;
        JMenu windowMenu;
        //Create the menu bar.
        menuBar = new JMenuBar();

        //Check if we're on a mac
        if (System.getProperty("mrj.version") == null) {
            //Linux
            makeLinuxProgramMenu(menuBar);
        } else {
//          	System.loadLibrary("btViz.MacOSAboutHandler")
			try {
    			ClassLoader theClassLoader = ClassLoader.getSystemClassLoader();
    			Class<?> testObjectClass=theClassLoader.loadClass("org.rlcommunity.rlviz.app.frames.MacOSAboutHandler");    
    			Constructor<?> emptyConstructor = testObjectClass.getConstructor();
    			Object classInstance =  (Object)emptyConstructor.newInstance();
			} catch (Exception ex) {
    			System.err.println("Problem loading MacOSAboutHandler using classloader... blowing up now!");
				System.exit(1);
			}
		}

        windowMenu = new JMenu("Window");
        menuBar.add(windowMenu);

        mainVizWindowButton = new JMenuItem("Experiment Controls");
        windowMenu.add(mainVizWindowButton);
        mainVizWindowButton.addActionListener(this);
        if (theVizFrame.envVizFrame != null) {
            envVizWindowButton = new JMenuItem("Environment Visualizer");
            windowMenu.add(envVizWindowButton);
            envVizWindowButton.addActionListener(this);
        }
        if (theVizFrame.agentVizFrame != null) {
            agentVizWindowButton = new JMenuItem("Agent Visualizer");
            windowMenu.add(agentVizWindowButton);
            agentVizWindowButton.addActionListener(this);
        }

        theVizFrame.setJMenuBar(menuBar);
    }
    JMenuItem aboutButton = null;
    JMenuItem quitButton = null;
    JMenuItem mainVizWindowButton = null;
    JMenuItem envVizWindowButton = null;
    JMenuItem agentVizWindowButton = null;

    private void makeLinuxProgramMenu(JMenuBar menuBar) {
        JMenu programMenu;
        programMenu = new JMenu(RLVizFrame.programName);
        menuBar.add(programMenu);

        aboutButton = new JMenuItem("About " + RLVizFrame.programName);
        programMenu.add(aboutButton);

        aboutButton.addActionListener(this);
        quitButton = new JMenuItem("Quit " + RLVizFrame.programName);
        programMenu.add(quitButton);
        quitButton.addActionListener(this);
    }
    
    private void open(GenericVizFrame frameToOpen){
        if(frameToOpen!=null){
            if(!frameToOpen.isVisible())frameToOpen.setVisible(true);
            frameToOpen.toFront();
        }
    }

    public void actionPerformed(ActionEvent theEvent) {
        if (theEvent.getSource() == aboutButton) {
            GenericVizFrame.showAboutBox();
        }
        if (theEvent.getSource() == mainVizWindowButton) {
            open(theVizFrame.controlVizFrame);
        }
        if (theEvent.getSource() == envVizWindowButton) {
            open(theVizFrame.envVizFrame);
        }
        if (theEvent.getSource() == agentVizWindowButton) {
            open(theVizFrame.agentVizFrame);
        }
        if (theEvent.getSource() == quitButton) {
            System.exit(0);
        }
    }
}
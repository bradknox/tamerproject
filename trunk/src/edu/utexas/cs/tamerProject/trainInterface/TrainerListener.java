package edu.utexas.cs.tamerProject.trainInterface;


/*
 * TrainerListener
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;


/**
 * TrainerListener creates a window that receives keyboard input from the
 * human trainer. It must be in focus to receive input. TrainerListener is
 * not used when the agent, environment, and experiment are run within an
 * applet (via the rl-applet codebase). 
 * 
 * @author bradknox
 *
 */
public class TrainerListener extends JFrame
	implements KeyListener,
        ActionListener
{
	static final long serialVersionUID = 0;

    JTextArea displayArea;
    JTextField typingArea;
	JButton toggleButton;
	JButton posButton;
	JButton negButton;
//	JButton actButton;
    static final String newline = System.getProperty("line.separator");

	GeneralAgent agent = null;
//	boolean control = false;
	boolean reinf = false;

    public TrainerListener(String name) {
        super(name);
    }
	
	public TrainerListener(String name, GeneralAgent agent) throws java.awt.HeadlessException{
		super(name);
		this.agent = agent;
	}
    
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public static TrainerListener createAndShowGUI(GeneralAgent agent) throws java.awt.HeadlessException{
        //Create and set up the window.
        TrainerListener frame = new TrainerListener("TrainerListener", agent); // calls parent JFrame's constructor
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/**GroupLayout layout = new GroupLayout(frame);
		   frame.getContentPane().setLayout(layout);**/
        
        //Set up the content pane.
        frame.addComponentsToPane(agent.getInTrainSess());
        
        
        //Display the window.
		frame.setPreferredSize(new Dimension(150, 120));
        frame.pack();
        frame.setVisible(true);
        	
//        System.out.println("Trainer interface window created.");
        return frame;
    }
    
    private void addComponentsToPane(boolean inTrainSess) {
        //Uncomment this if you wish to turn off focus
        //traversal.  The focus subsystem consumes
        //focus traversal keys, such as Tab and Shift Tab.
        //If you uncomment the following line of code, this
        //disables focus traversal and the Tab events will
        //become available to the key event listener.
        //typingArea.setFocusTraversalKeysEnabled(false);
        
        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setPreferredSize(new Dimension(375, 125));
        
        
        toggleButton = new JButton("Begin training");
        toggleButton.addActionListener(this);
		toggleButton.addKeyListener(this);
		if (inTrainSess)
			this.toggleButton.setText("Stop training");
		getContentPane().add(toggleButton, BorderLayout.PAGE_END);
		
		
		if (agent.getClass().getName().contains("TamerAgent") ||
				agent.getClass().getName().contains("TamerRLAgent") ||
				agent.getClass().getName().contains("AsynchDPAgent") ||
				agent.getClass().getName().contains("DPAgent")) {
			reinf = true;
			posButton = new JButton("+");
	        posButton.addActionListener(this);
			posButton.addKeyListener(this);
			if (inTrainSess)  	
				posButton.setEnabled(true);
			else
				posButton.setEnabled(false);  
			
			negButton = new JButton("-");
	        negButton.addActionListener(this);
			negButton.addKeyListener(this);
			if (inTrainSess)  	
				negButton.setEnabled(true);
			else
				negButton.setEnabled(false); 
			getContentPane().add(posButton, BorderLayout.EAST);
	        getContentPane().add(negButton, BorderLayout.WEST);
		}
       
		toggleButton.requestFocusInWindow();
    }
    

    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
    	//System.out.println("typed: " + e.getKeyCode());
    }
    
    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
    	//System.out.println("pressed: " + e.getKeyCode());
    	processKeyPress(e.getKeyCode(), e.getKeyChar());
    }
    
    public void processKeyPress(int keyCode, char keyChar){
    	//System.out.println("processKeyPress(" + keyCode +", " + keyChar + ")");
		if (keyChar == '/' ||
    			keyCode == KeyEvent.VK_PAGE_DOWN){
			if (reinf) posButton.doClick();
		}
		else if (keyChar == 'z' ||
    			keyCode == KeyEvent.VK_PAGE_UP){
			if (reinf) negButton.doClick();
		}
		else if (keyCode == KeyEvent.VK_ESCAPE ||
				keyCode == 't'){
//			boolean inTrainSessBefore = agent.inTrainSess;
			toggleButton.doClick();
//			while (inTrainSessBefore == agent.inTrainSess) //// doClick() spawns own thread?, so wait til finished
//				GeneralAgent.sleep(10);
			if (agent.params.safeAction != null)
				agent.safeActionOnly = !agent.getInTrainSess();
			else
				System.out.println("No safe action set: agent.params.safeAction == null");
			if (agent.safeActionOnly)
				System.out.println("Only taking safe action for this non-training period.");
		}
		else if (keyCode == '.'){ //// having this button near '/' could cause problems in experiments
//			boolean inTrainSessBefore = agent.inTrainSess;
			toggleButton.doClick();
//			while (inTrainSessBefore == agent.inTrainSess) //// doClick() spawns own thread?, so wait til finished
//				GeneralAgent.sleep(10);
			agent.safeActionOnly = false;
		}
		else if (keyChar == 'T'){
			if (agent.getClass().getName().equals("edu.utexas.cs.tamerProject.agents.combo.TamerRLAgent"))
				((TamerRLAgent)agent).toggleTamerControl(); 
		}
		else if (keyChar == 'p'){
			agent.togglePause(); 
		}
		else if (keyChar != ' '){
			agent.receiveKeyInput(keyChar);
		}
    }
    
    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {
    	processKeyReleased(e.getKeyCode(), e.getKeyChar());
    }

    public void processKeyReleased(int keyCode, char keyChar){
		if (keyChar == ' '){
			toggleButton.doClick();
			if (agent.getInTrainSess())
				agent.safeActionOnly = false;
		}
    }
    
    /** Handle the button click. */
    public void actionPerformed(ActionEvent e) {
		String actCmd = e.getActionCommand();
		if (actCmd.equals("+")) {
			agent.receiveKeyInput('/');
		}
		else if (actCmd.equals("-")) {
			agent.receiveKeyInput('z');
		}
		else if (actCmd.equals("Stop training")) {
			if (reinf) {
				posButton.setEnabled(false);
				negButton.setEnabled(false);
			}
			toggleButton.setText("Begin training");
			agent.receiveKeyInput(' ');
		}
		else if (actCmd.equals("Begin training")) {
			if (reinf) {
				posButton.setEnabled(true);
				negButton.setEnabled(true);
			}
			toggleButton.setText("Stop training");
			System.out.println("enabled");
			agent.receiveKeyInput(' ');
		}
				
		//System.out.println("e.getActionCommand(): " + e.getActionCommand());
    }
    
}
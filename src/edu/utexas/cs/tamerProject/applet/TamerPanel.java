package edu.utexas.cs.tamerProject.applet;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.imitation.ImitationAgent;

public class TamerPanel extends RLPanel {
	private static final long serialVersionUID = 4279715485022371641L;

	public static boolean printInstructionsOnGUI = false;

	final double FEEDBACK_FLASH_TIME = 0.10;  // in seconds
	public static boolean DISPLAY_TRAINING = true;
	private double timeOfLastFeedback = Double.MIN_VALUE;
	private double lastFeedback = 0;
	boolean demoActionTaken = false;
	boolean shouldFlash = false;
	boolean demoMode = false;
	//	boolean startPressed = false;


	public void init(AgentInterface agent, EnvironmentInterface env) {
		super.init(agent, env);
	}

	public void paintComponent( Graphics g ) {
		//System.out.println("RLPanel object in TamerPanel.paint(): " + System.identityHashCode(this));
		if (RLApplet.DEBUG_TIME) {
			System.out.println("Time of TamerPanel paint: " + ((System.currentTimeMillis() / 1000.0) % 1000));
			System.out.flush();
		}
		if (!runLocal.expInitialized)
			return;
		double currTime = getCurrentTimeInSecs();
		BufferedImage unmolestedImage = null;
		if (RLApplet.DEBUG_TIME) {
			System.out.println("Time before envImageLock: " + ((System.currentTimeMillis() / 1000.0) % 1000));
			System.out.flush();
		}
		if (this.bufferedEnvImage != null) {
			synchronized(envImageLock) {
				if (RLApplet.DEBUG_TIME) {
					System.out.println("Time right after envImageLock: " + ((System.currentTimeMillis() / 1000.0) % 1000));
					System.out.flush();
				}
				unmolestedImage = this.bufferedEnvImage;
				this.bufferedEnvImage = getImageCopy(unmolestedImage);
				drawTamerMeta(bufferedEnvImage.createGraphics(), getCurrentTimeInSecs());
			}
		}
		super.paintComponent(g, currTime);

		synchronized(envImageLock) {
			this.bufferedEnvImage = unmolestedImage;
		}
		//    	g.drawOval(10, 10, 190, 100);
	}


	protected void drawTamerMeta(Graphics2D g, double currTime) {
		Color startColor = g.getColor();
		g.setColor(Color.black);

		if (this.agent instanceof GeneralAgent && ((GeneralAgent)this.agent).takesHRew) { 
			// agent must receive reward; this test would be better if it'd just look at one shared GeneralAgent variable instead of hard-coding classes
			//System.out.println("agent class: " + this.agent.getClass().getName());
			if (RLApplet.DEBUG_TIME) {
				System.out.println("flash time diff: " + (currTime - this.timeOfLastFeedback));
				System.out.flush();
			}
		}

		if (!(this.agent instanceof ImitationAgent)) {    	
			if(((GeneralAgent)this.agent).getInTrainSess()){
				//System.out.println("Feedback flash");
				shouldFlash = true;
				demoMode = false;
				if (TamerPanel.printInstructionsOnGUI) {
					g.drawString("Training by reward feedback. Reward with the up arrow and punish with the down arrow", 4*g.getFontMetrics().charWidth(' '), 
							(int)(0.10 * this.getHeight()) + (int)(0.2 * g.getFontMetrics().getHeight()));
				}
				if (currTime - this.timeOfLastFeedback < FEEDBACK_FLASH_TIME ) {
					//System.out.println("demoMode? " + demoMode+"; demoActionTaken? "+demoActionTaken);
					if(shouldFlash){
						//System.out.println("flash");
						//System.out.flush();
						drawFeedbackFlash((Graphics2D)g, 0.7f, this.lastFeedback > 0);
					} 
				}
			}
		}
		if(!((GeneralAgent)this.agent).getInTrainSess() && TamerPanel.DISPLAY_TRAINING) {
			g.drawString("Not training", 4*g.getFontMetrics().charWidth(' '), 
							this.getHeight() - (int)(0.2 * g.getFontMetrics().getHeight()));
		}


		g.setColor(startColor);
	}

	public static AlphaComposite makeComposite(float alpha) {
		int type = AlphaComposite.SRC_OVER;
		return(AlphaComposite.getInstance(type, alpha));
	}		

	private void drawFeedbackFlash(Graphics2D g2d, float alpha, boolean positiveFeedback) {
		Composite originalComposite = g2d.getComposite();
		g2d.setComposite(makeComposite(alpha));
		Color flashColor = positiveFeedback ? new Color(60,135,174) : new Color(250, 70, 70);
		g2d.setPaint(flashColor);
		g2d.fill(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
		g2d.setComposite(originalComposite);
	}

	private void drawFeedbackFlashDemo(Graphics2D g2d, float alpha) {
		Composite originalComposite = g2d.getComposite();
		g2d.setComposite(makeComposite(alpha));
		Color flashColor = new Color(0,100,0);
		g2d.setPaint(flashColor);
		g2d.fill(new Rectangle(10, 10, 20, 20));
		g2d.setComposite(originalComposite);
	}

	public void keyPressed( KeyEvent e ) {
		//		if (!runLocal.expInitialized) { // when the experiment has not been initialized, the other key responses could cause problems
		//			if (e.getKeyChar() == '2'){
		//				startPressed = true;
		//			}
		//			return;
		//		}
		if (e.getKeyCode() == KeyEvent.VK_PAGE_UP)
			e.setKeyChar('z');
		if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
			e.setKeyChar('/');
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			e.setKeyChar('k');
		if (e.getKeyCode() == KeyEvent.VK_UP)
			e.setKeyChar('i');
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			e.setKeyChar('l');
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			e.setKeyChar('j');

		super.keyPressed(e);
		sendKeyToAgent(e.getKeyChar());

		//System.out.println("key pressed in TamerPanel: " + e.getKeyChar());
		if (e.getKeyChar() == 'z' || e.getKeyChar() == 'k') {
			this.showNegRew();
		}
		else if (e.getKeyChar() == '/' || e.getKeyChar() == 'i') {
			this.showPosRew();
		} else if (e.getKeyChar() == 'l' || e.getKeyChar() == 'j'){
			if(demoMode){
				timeOfLastFeedback = getCurrentTimeInSecs();
				demoActionTaken = true;
			}
		}
		e.consume();
	}


	public void sendKeyToAgent(char c){
		if (runLocal.expInitialized)
			((GeneralAgent)this.agent).receiveKeyInput(c);
	}

	public void showNegRew(){
		if(shouldFlash){
			lastFeedback = -1;
			timeOfLastFeedback = getCurrentTimeInSecs();
		}
	}

	public void showPosRew(){
		if(shouldFlash){
			lastFeedback = 1;
			timeOfLastFeedback = getCurrentTimeInSecs();
		}
	}
}

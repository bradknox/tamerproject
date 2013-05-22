package edu.utexas.cs.tamerProject.applet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultFocusManager;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlviz.app.VisualizerFactory;
import org.rlcommunity.rlviz.app.VisualizerPanel;
import org.rlcommunity.rlviz.app.frames.VisualizerVizFrame;

import rlVizLib.messaging.environment.EnvVisualizerNameRequest;
import rlVizLib.messaging.environment.EnvVisualizerNameResponse;
import rlVizLib.visualization.AbstractVisualizer;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;


public class PreExpPanel extends JPanel 
					implements KeyListener, MouseListener {
	private static final long serialVersionUID = 111112222333445L;
	
	boolean startPressed = false;
	
	public boolean HITStateChecked = false;
	boolean prevHITFinished = false;
	private boolean hitIDGiven = false;
	private String hitID = "";
	
	private char lastPressed = ' ';
	
	private String topPrompt = "Carefully type your HIT ID";
	private String bottomPrompt = "and press enter: ";
	private String subPrompt = "(Copy-and-pasting will not work.)";
	private int promptSize = 20;
	int cnt = 0;
	
	public boolean isHITReadyForStart() {
		return (this.prevHITFinished && this.hitIDGiven);
	}
	public boolean isHITIDGiven() {
		return this.hitIDGiven;
	}
	public String getHITID() {
		return this.hitID;
	}
	
	public void init() {	
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setRequestFocusEnabled(true);
		addKeyListener( this );
		addMouseListener( this );
		//setDoubleBuffered(true);
		setBackground(Color.white);	
		
		
//		KeyEventDispatcher myKeyEventDispatcher = new DefaultFocusManager();
//		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(myKeyEventDispatcher);
		
		//System.out.println("SwingUtilities.isEventDispatchThread(): " + SwingUtilities.isEventDispatchThread() );
		
		requestFocus();
	}
	
	

	public void paint (Graphics g) {
//		System.out.println("PreExpPanel paint(). isFocusOwner: " + isFocusOwner());
		requestFocusInWindow();
		drawWaitScreen((Graphics2D)g);
	}

	private void drawWaitScreen(Graphics2D g2d) {
		cnt++;
		Color bgColor = Color.WHITE;
		if (isFocusOwner())
			bgColor = Color.LIGHT_GRAY;
		g2d.setPaint(bgColor);
		g2d.fill(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
		
		g2d.setColor(Color.BLACK);
		Font origFont = g2d.getFont();
		String topStr, midStr;
		String lowerStr = "";;
		if (isFocusOwner()) {
			if (isHITReadyForStart()) {
				g2d.setFont(new Font(origFont.getName(), origFont.getStyle(), 20));
				topStr = "";
				midStr = "Press the spacebar to begin. ";
			}
			else {
				
				if (this.hitIDGiven && this.HITStateChecked) {
					this.topPrompt = "Wrong HIT ID or previous task was not";
					this.bottomPrompt = "finished. Retry ID: "; // TODO log this
					this.promptSize = 14;
					this.hitID = "";
					this.hitIDGiven = false;
					this.HITStateChecked = false;
				}
				g2d.setFont(new Font(origFont.getName(), origFont.getStyle(), promptSize));
				topStr = topPrompt;
				midStr = bottomPrompt + this.hitID;
				lowerStr = subPrompt;
			}
		}
		else {
			g2d.setFont(new Font(origFont.getName(), origFont.getStyle(), 20));
			topStr = "";
			midStr = "Double-click here after finishing the video.";// + cnt + " " + isFocusOwner() + " " + isRequestFocusEnabled() +
					//" " + isFocusable(); //
		}

		g2d.drawString(topStr, 
				(this.getWidth() - g2d.getFontMetrics().stringWidth(topStr)) / 2, 
				this.getHeight() / 2 - (2 * g2d.getFont().getSize()));
		g2d.drawString(midStr, 
						(this.getWidth() - g2d.getFontMetrics().stringWidth(midStr)) / 2, 
						this.getHeight() / 2);
		g2d.drawString(lowerStr, 
				(this.getWidth() - g2d.getFontMetrics().stringWidth(lowerStr)) / 2, 
				this.getHeight() / 2 + (2 * g2d.getFont().getSize()));
	}
	
	
	
	
	public void keyPressed( KeyEvent e ) {
		requestFocus();
		System.out.println("pressed in preexppanel: " + e.getKeyChar());
		lastPressed = e.getKeyChar();

		
		if (!this.isHITReadyForStart() && !hitIDGiven) {
			if (e.getKeyCode() == e.VK_ENTER) {
				if (this.hitID.replaceAll("[^0-9]", "").length() > 0)
					this.hitIDGiven = true;
				else {
					this.hitID = "";
					topPrompt = "HIT ID must contain a number.";
					bottomPrompt = "Try again: ";
				}
			}
			else if (e.getKeyCode() == e.VK_BACK_SPACE) {
				this.hitID = this.hitID.substring(0, this.hitID.length() - 1);
			}
			else if(e.getKeyChar() != e.CHAR_UNDEFINED) {
				this.hitID += e.getKeyChar();
				this.hitID = this.hitID.replace("?", "").replaceAll("\\s","");
			}
		}
		
		if (this.isHITReadyForStart() && e.getKeyCode() == e.VK_SPACE){
			startPressed = true;
		}
		
		e.consume();
	}

	public void keyReleased( KeyEvent e ) {}
	public void keyTyped( KeyEvent e ) {}

	
	public void mouseEntered( MouseEvent e ) { }
	public void mouseExited( MouseEvent e ) { }
	public void mousePressed( MouseEvent e ) { }
	public void mouseReleased( MouseEvent e ) { }
	public void mouseClicked( MouseEvent e ) {
		requestFocus();
		System.out.println("Click. In focus: " + isFocusOwner());		
	}
	
					
}



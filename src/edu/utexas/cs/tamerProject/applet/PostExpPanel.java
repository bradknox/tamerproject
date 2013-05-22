package edu.utexas.cs.tamerProject.applet;

import java.awt.Color;
import java.awt.Composite;
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


public class PostExpPanel extends JPanel 
					implements KeyListener, MouseListener {
	private static final long serialVersionUID = 111112222333445L;
		
	boolean resetPressed = false;
	boolean resettable = false;
	Timer repaintTimer;
	boolean lastTask = false;
	public int hitIDNum = Integer.MIN_VALUE;
	
	public void setIsLastTask(boolean isLastTask) {
		this.lastTask = isLastTask;
	}
	
	public void init() {	
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		setRequestFocusEnabled(true);
		addKeyListener( this );
		addMouseListener( this );
		//setDoubleBuffered(true);
		setBackground(Color.white);	
		
		System.out.println("PostExpPanel init()");
		
		//System.out.println("SwingUtilities.isEventDispatchThread(): " + SwingUtilities.isEventDispatchThread() );
	    
		ActionListener actionListener = new ActionListener() {
	    	public void actionPerformed(ActionEvent actionEvent) {
	    		PostExpPanel.this.repaint();
//	    		System.out.println("PostExpPanel timer action");
	        }

	    };
	    repaintTimer = new Timer(500, actionListener);
	    repaintTimer.start();
		
	    //requestFocus();
	}
	
	

	public void paint (Graphics g) {
//		System.out.println("PostExpPanel paint(). isFocusOwner: " + isFocusOwner());
		//requestFocusInWindow();
		drawEndScreen((Graphics2D)g);
		//System.out.println("PostExpPanel paint()");
	}
	
	@Override
	public void update(Graphics g){
		System.out.println("PostExpPanel update()");
	}

	private void drawEndScreen(Graphics2D g2d) {
//		float alpha = 0.9f;
//		Composite originalComposite = g2d.getComposite();
//		g2d.setComposite(TamerPanel.makeComposite(alpha));
		Color bgColor = Color.WHITE;
		g2d.setPaint(bgColor);
		g2d.fill(new Rectangle(0, 0, this.getWidth(), this.getHeight()));
//		g2d.setComposite(originalComposite);
		
		g2d.setColor(Color.BLACK);
		Font origFont = g2d.getFont();
		String endString = "Thanks!";
		g2d.setFont(new Font(origFont.getName(), origFont.getStyle(), 24));
		g2d.drawString(endString, 
						(this.getWidth() - g2d.getFontMetrics().stringWidth(endString)) / 2, 
						this.getHeight() / 2 - g2d.getFont().getSize());
		if (TamerApplet.isHIT) {
			if (this.lastTask) {
				int finishCode = convertIDToFinishCode(this.hitIDNum);
				endString = "You're almost done.";
				g2d.setFont(new Font(origFont.getName(), origFont.getStyle(), 16));
				g2d.drawString(endString, 
						(this.getWidth() - g2d.getFontMetrics().stringWidth(endString)) / 2, 
						this.getHeight() / 2 + g2d.getFont().getSize());
				endString = "\nGo back to the HIT, answer the questions,";
				g2d.drawString(endString, 
						(this.getWidth() - g2d.getFontMetrics().stringWidth(endString)) / 2, 
						this.getHeight() / 2 + (5 * g2d.getFont().getSize()));
				endString = "and enter finish code " + finishCode + ".";
				g2d.drawString(endString, 
						(this.getWidth() - g2d.getFontMetrics().stringWidth(endString)) / 2, 
						this.getHeight() / 2 + (7 * g2d.getFont().getSize()));
				
			}
			else {
				endString = "Click \"Next\" below and move to the next part of the HIT.";
				g2d.setFont(new Font(origFont.getName(), origFont.getStyle(), 16));
				g2d.drawString(endString, 
						(this.getWidth() - g2d.getFontMetrics().stringWidth(endString)) / 2, 
						this.getHeight() / 2 + g2d.getFont().getSize());
			}
		}
		g2d.setFont(origFont);
	}
	
	
	private static int convertIDToFinishCode(int id) {
		int code = id;
		code += 13;
		code *= 7;
		return code;
	}
	

	private static int convertFinishCodeToID(int code) {
		int id = code;
		id /= 7;
		id -= 13;
		return id;
	}
	
	public void keyPressed( KeyEvent e ) {
		System.out.println("pressed in postexppanel: " + e.getKeyChar());
		if (e.getKeyChar() == 'r' && this.resettable){
			resetPressed = true;
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
	}
	
	public static void main(String[] args) {
		int id = 000114;
		System.out.println("id: " + id);
		System.out.println("finish code: " + convertIDToFinishCode(id));
		System.out.println("converted back: " + convertFinishCodeToID(convertIDToFinishCode(id)));
		
		int code = 1029;
		System.out.println("code: " + code);
		System.out.println("converted back: " + convertFinishCodeToID(code));
		
		for (int i = 0; i < 1000; i++) {
			if (convertFinishCodeToID(convertIDToFinishCode(i)) != i)
				System.out.println("ID and code conversion failed.");
		}
		System.out.println("ID and code conversion test finished.");
	}
	
					
}



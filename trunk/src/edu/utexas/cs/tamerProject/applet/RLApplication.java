package edu.utexas.cs.tamerProject.applet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;

import javax.swing.JFrame;

public class RLApplication extends JFrame{
	private static final long serialVersionUID = 1182135608739287112L;

	private RLPanel rlPanel;
	
	int WIDTH = 400;
	int HEIGHT = 400;
	
	public RLApplication() {
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setResizable(true);
		this.setBackground(Color.white);
		rlPanel = new RLPanel();
		rlPanel.setSize(WIDTH, HEIGHT - 10);
		rlPanel.init(null, null);
		

		add(rlPanel);
		rlPanel.runLocal.initExp();
		rlPanel.runLocal.startExp();
	}
	
	// Only called if running as an application
	public static void main(String[] args) {
		RLApplication rlApplication = new RLApplication();
		rlApplication.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		rlApplication.pack();
		rlApplication.setVisible(true);
	}	
}

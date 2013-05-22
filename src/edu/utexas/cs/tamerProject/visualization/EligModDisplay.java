package edu.utexas.cs.tamerProject.visualization;

import javax.swing.JFrame;
import java.lang.Thread;
import java.util.Arrays;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.TexturePaint;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.rlcommunity.rlglue.codec.types.Action;

import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;


public class EligModDisplay extends JPanel implements ActionListener{

	static final long serialVersionUID = 0;
	final static int REFRESH_INTERVAL = 1000; // in ms
	private TamerRLAgent agent;
	Timer timer;
	
	int movingX = 0;
	
	

	public EligModDisplay(TamerRLAgent agent){
		super();
		this.agent = agent;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	

	// draw shapes with Java 2D API
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g); // call superclass's paintComponent
		
		Graphics2D g2d = (Graphics2D) g; // cast g to Graphics2D
		setBackground(Color.BLACK);
		
		
		int ht = this.getHeight();
		int width = this.getWidth();
		
		/*
		 * Draw feature array as a 2D grid, using the action to determine color
		 */
		double[] featColorIndicator = {0.0, 0.0, 0.0};
		if (this.agent.lastObsAndAct.getObs() != null) {
			featColorIndicator[this.agent.lastObsAndAct.getAct().intArray[0]] = 1.0;
			double[] feats = this.agent.hInf.featGen.getSFeats(this.agent.lastObsAndAct.getObs());
//			for (int i = 0; i < feats.length; i++)
//				feats[i] *= 2;
			VizUtils.drawArray(0, (int)(ht / 4.0), width / 4.0, 3 * (ht / 4.0), VizUtils.convertTo2D(feats), g2d, featColorIndicator, true);
		}
		
		/*
		 * Draw H-hat influence and eligibility traces for each action
		 */
		int numActions = this.agent.featGen.possStaticActions.size();
		Action a = new Action();
		a.intArray = new int[1];
		for (int actNum = 0; actNum < numActions; actNum++) {
			double[] actColorIndicator = {0,0,0};
			actColorIndicator[actNum] = 1.0;
			a.intArray[0] = actNum;
			/*
			 * Draw traces
			 */
			double[] traces = this.agent.hInf.lastStepTraces;
			int tracesPerAct = traces.length / numActions;
			double[] thisActTraces = Arrays.copyOfRange(traces, tracesPerAct * actNum, (tracesPerAct * (actNum + 1)) - 1);
			VizUtils.drawArray((int)((1 + actNum) * (width / 4.0)), (int)(ht / 4.0), width / 4.0, 3 * (ht / 4.0), 
											VizUtils.convertTo2D(thisActTraces), g2d, actColorIndicator, true);
			/*
			 * Draw influence
			 */
			if (this.agent.lastObsAndAct.getObs() != null) {
				double hInfWt = this.agent.hInf.getHInfluence(this.agent.lastObsAndAct.getObs(), a, true) / this.agent.hInf.COMB_PARAM;
				//System.out.println("hInf for act " + actNum + ": " + hInfWt);
				g2d.setPaint(new Color((float)(actColorIndicator[0]),
						   				(float)(actColorIndicator[1]),
						   				(float)(actColorIndicator[2])));
				g2d.fill(new Rectangle2D.Double((int)((width / 4.0) * (actNum + 1)), 0, 
						(int)(hInfWt * (width / 4.0)), // * (actNum + 2)) - (int)((width / 4.0) * (actNum + 1)),
						(int)(ht / 4.0) - 1));
			}
		}	
	}

	
	
	
	
	
	
	public static void createAndShowEligModDisplay(TamerRLAgent agent) {
		// create frame for EligModDisplay
		JFrame frame = VizUtils.createGenericDisplay(960, 320);		
		
		// create ShapesJPanel
		EligModDisplay display = new EligModDisplay(agent);
		frame.add(display); // add shapesJPanel to frame

		
		frame.setVisible(true); // display frame
		display.timer = VizUtils.setRepaintTimer(display, 100, 3000);
	}


	
	public static void main(String[] args) {
		EligModDisplay.createAndShowEligModDisplay(new TamerRLAgent());
	}
}
/*
 *  Copyright 2009 Brian Tanner.
 *
 *  brian@tannerpages.com
 *  http://research.tannerpages.com
 *
 *  This source file is from one of:
 *  {rl-coda,rl-glue,rl-library,bt-agentlib,rl-viz}.googlecode.com
 *  Check out http://rl-community.org for more information!
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */


package edu.utexas.cs.tamerProject.environments.robotarm.visualizer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.Arrays;
import java.lang.Thread;

import edu.utexas.cs.tamerProject.environments.robotarm.RobotArmState;

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

public class RobotArmComponent implements SelfUpdatingVizComponent, Observer {

    private final RobotArmVisualizer theVisualizer;
    private VizComponentChangeListener theChangeListener;

	private static final double zoneRectWidth = 0.01d; //0.0025d
	private static final double agentWidth = 0.5; //1.0;// 0.035;

	private int[] agentPos = null;
	private int[] agentPrevPos = null;
	private int lastAct = Integer.MIN_VALUE;
	private int totalSteps = -1;

	private Color[] armColors;
	
    public RobotArmComponent(RobotArmVisualizer theVisualizer) {
        this.theVisualizer = theVisualizer;
        armColors = new Color[7];
        armColors[0] = new Color(255, 0, 0);	
        armColors[1] = new Color(0, 0, 255);	
        armColors[2] = new Color(255, 165, 0);	
        armColors[3] = new Color(238, 130, 238);	
        armColors[4] = new Color(0, 128, 0);
        armColors[5] = new Color(75, 0, 130);	
        armColors[6] = new Color(255, 255, 0);
        theVisualizer.getTheGlueState().addObserver(this);
    }

    public void render(Graphics2D g) {
    	//System.err.println("rendering thread: " + Thread.currentThread().getName() + 
    	//		", " + ((System.currentTimeMillis() / 1000.0) % 1000));
    	//System.err.flush();
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		        RenderingHints.VALUE_ANTIALIAS_ON);


    	g.setColor(Color.GRAY);
		Stroke stroke = new BasicStroke(0.001f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g.setStroke(stroke);
		
		
    	/**
    	 * RELEVENT NEW VISUALIZATION CODE
    	 */
		
		/*
		 * Update agent position and action
		 */
		int currentTotalSteps = theVisualizer.getTotalSteps();
		if (this.totalSteps != currentTotalSteps) {
			this.totalSteps = currentTotalSteps; 
			if (agentPos != null)
				agentPrevPos = agentPos.clone();
			agentPos = theVisualizer.getLastAgentCoord();
			lastAct = theVisualizer.getLastAgentAct();
		}
//		// check for reaching goal
//		if (RLGlue.isCurrentEpisodeOver()) {
//			agentPos = RobotArmState.goalLoc.clone(); // show agent in goal
//		}
		if (theVisualizer.getTimeStep() == 1) {
			agentPrevPos = null;
			lastAct = Integer.MIN_VALUE;
		}
		//System.out.println("time step: " + theVisualizer.getTimeStep());
//		System.out.println("Agent state in render(): " + Arrays.toString(agentPos));
		
		g.setColor(Color.BLACK);
		stroke = new BasicStroke(0.005f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		g.setStroke(stroke);


		/*
		 * Draw segments
		 */ 
		Point2D lastJointLoc = RobotArmState.origin;
		double lastJointAng = 0;
//		System.out.println("agentPos: " + Arrays.toString(agentPos));
//		System.out.println("RobotArmState.worldDims: " + Arrays.toString(RobotArmState.worldDims));
		for (int segI = 0; segI < RobotArmState.worldDims.length; segI++) {
			double jointAng;
			//System.out.println("segI: " + segI);
			if (segI == 0)
				jointAng = (((0.5 + agentPos[segI]) / (RobotArmState.worldDims[segI])) * Math.PI) + Math.PI;
			else
				jointAng = lastJointAng + ((agentPos[segI] / (RobotArmState.worldDims[segI] - 1.0)) * 2 * Math.PI);
			Point2D newJointLoc = new Point2D.Double(lastJointLoc.getX() + (Math.cos(jointAng) * RobotArmState.SEG_LENS[segI]), 
													lastJointLoc.getY() + (Math.sin(jointAng) * RobotArmState.SEG_LENS[segI]));
			
			g.setColor(this.armColors[segI]);
			g.draw(new Line2D.Double(lastJointLoc, newJointLoc));
			lastJointLoc = newJointLoc;
			lastJointAng = jointAng;
			
		}
		
		/*
		 * Draw grasp radius
		 */
		Composite originalComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g.setPaint(new Color(100, 150, 255));
			
		Ellipse2D graspCircle = new Ellipse2D.Double(lastJointLoc.getX() - RobotArmState.graspRadius,
													lastJointLoc.getY() - RobotArmState.graspRadius,
													RobotArmState.graspRadius * 2, RobotArmState.graspRadius * 2);
		g.fill(graspCircle);
		g.setComposite(originalComposite);
		
		
		
		/*
		 * Draw target object
		 */
		double targetDotRadius = 0.01;
		double targetCircleRadius = 0.03;
		originalComposite = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
		g.setPaint(new Color(20, 57, 74));
			
		Ellipse2D targetDot = new Ellipse2D.Double(RobotArmState.targetLoc.getX() - targetDotRadius,
												RobotArmState.targetLoc.getY() - targetDotRadius,
												targetDotRadius * 2, targetDotRadius * 2);
		g.fill(targetDot);
		
		Ellipse2D targetCircle = new Ellipse2D.Double(RobotArmState.targetLoc.getX() - targetCircleRadius,
													RobotArmState.targetLoc.getY() - targetCircleRadius,
													targetCircleRadius * 2, targetCircleRadius * 2);
		g.draw(targetCircle);
		
		g.setComposite(originalComposite);
		
		
		
		
		
		

	
	
	
	
		
		
		
				


		
		
		

		


		    	
    	
	}
    
    

	private double normX(double num) {
		return (num / RobotArmState.worldDims[0]);
	}
	private double normY(double num) {
		return (num / RobotArmState.worldDims[1]);
	}
	private double normZ(double num) {
		return (num / RobotArmState.worldDims[2]);
	}
	
	public static Shape resizeRect(final RectangularShape rectangularShape, final double xScale,
													final double yScale) {
		final RectangularShape retval = (RectangularShape) rectangularShape.clone();
		
		double oldWidth = retval.getWidth();
		double oldHeight = retval.getHeight();
		retval.setFrame(retval.getX() + (((1 - xScale) * oldWidth)/2.0), 
						retval.getY() + (((1 - yScale) * oldHeight)/2.0), 
						xScale * oldWidth, yScale * oldHeight);
		return retval;
	}

	
	
    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }

    //boolean everDrawn=false;
    public void update(Observable o, Object arg) {
    	if (theChangeListener != null) {
    		theChangeListener.vizComponentChanged(this);
    	}
        /** if(!everDrawn){
             theChangeListener.vizComponentChanged(this);
             everDrawn=true;
			 }**/
    }
    
    

    
    
}

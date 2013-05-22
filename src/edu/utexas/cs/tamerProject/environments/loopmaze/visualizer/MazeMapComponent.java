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


package edu.utexas.cs.tamerProject.environments.loopmaze.visualizer;

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

import edu.utexas.cs.tamerProject.environments.loopmaze.Zone;
import edu.utexas.cs.tamerProject.environments.loopmaze.ZoneGen;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMazeState;

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

public class MazeMapComponent implements SelfUpdatingVizComponent, Observer {

    private final LoopMazeVisualizer theVisualizer;
    private VizComponentChangeListener theChangeListener;
    private final Vector<Zone> theZones = ZoneGen.makeZones();

	private static final double zoneRectWidth = 0.01d; //0.0025d
	private static final double agentWidth = 0.5; //1.0;// 0.035;

	private int[] agentPos = null;
	private int[] agentPrevPos = null;
	private int lastAct = Integer.MIN_VALUE;
	private int totalSteps = -1;

    public MazeMapComponent(LoopMazeVisualizer theVisualizer) {
        this.theVisualizer = theVisualizer;
        theVisualizer.getTheGlueState().addObserver(this);
    }

    public void render(Graphics2D g) {
    	//System.err.println("rendering thread: " + Thread.currentThread().getName() + 
    	//		", " + ((System.currentTimeMillis() / 1000.0) % 1000));
    	//System.err.flush();
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		        RenderingHints.VALUE_ANTIALIAS_ON);


		//if (LoopMazeState.worldDims != null) {
    	
		if (true) {
			double worldWidth = LoopMazeState.worldDims[0];
			double worldHeight = LoopMazeState.worldDims[1];
			
			for (double x = 0.0d; x <= worldWidth; x += zoneRectWidth) {
				for (double y = 0.0d; y <= worldHeight; y += zoneRectWidth) {
					int[] thisPoint = {(int)(x+zoneRectWidth/2.0d), (int)(y+zoneRectWidth/2.0d)};
					float thisPenalty=0.0f;
					for (Zone zone : theZones) {
						thisPenalty+=zone.getReward(thisPoint);
					}
					//If we are in penalty region, draw the zone
					if(thisPenalty<0.0d){
						//empirically have determined maxpenalty = -80
						float scaledPenalty=thisPenalty / (-80.0f);
						//Going to sqrt the penalty to bias it towards 1
						scaledPenalty=(float) Math.sqrt(scaledPenalty);
						//Now we have a number in 0/1
						Color scaledColor=new Color(scaledPenalty,1.0f,1.0f,.75f);
						g.setColor(scaledColor);
						Rectangle2D thisRect=new Rectangle2D.Double(normX(x),normY(y),normX(zoneRectWidth),normY(zoneRectWidth));
						g.fill(thisRect);
					}
					
				}
			}
			
			/*
			 * Draw grid 
			 */
			g.setColor(Color.GRAY);
			Stroke stroke = new BasicStroke(0.001f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			g.setStroke(stroke);
			for (int x = 0; x <= worldWidth; x += 1) {
				Line2D line = new Line2D.Double(normX(x), 0.0, normX(x), 1.0);
				g.draw(line);
			}
			for (int y = 0; y <= worldHeight; y += 1) {
				Line2D line = new Line2D.Double(0.0, normY(y), 1.0, normY(y));
				g.draw(line);
			}
			
			/*
			 * Gray out illegal states
			 */
			g.setColor(Color.LIGHT_GRAY);
			for (int x = 0; x < worldWidth; x += 1) {
				for (int y = 0; y < worldHeight; y += 1) {
					if (LoopMazeState.stateMap[y][x] != 1) { 
					
					//(!LoopMazeState.isStateLegal(x,y)) {
						Rectangle2D deadRect = new Rectangle2D.Double(normX(x), normY(y),
																	  normX(1), normY(1));
						g.fill(deadRect);
					}
				}
			} 
			
			

			
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
			// check for reaching absorbing state
//			if (RLGlue.isCurrentEpisodeOver()) {
//				agentPos = LoopMazeState.goalLoc.clone(); // show agent in goal TODO this will now be incorrect
//			}
			if (theVisualizer.getTimeStep() == 1) {
				agentPrevPos = null;
				lastAct = Integer.MIN_VALUE;
			}
			//System.out.println("time step: " + theVisualizer.getTimeStep());
			
			
			
			
			
					
			/*
			 * Determine whether last action moved agent horizontally or vertically.
			 */
			boolean horizontalAct = true;	
			if (lastAct != Integer.MIN_VALUE && (lastAct == 2 || lastAct == 3))  // down or up
        		horizontalAct = false;
			boolean hasMoved = false;
			
			
			/*
			 * Draw travel line from previous position
			 */
			if (agentPos != null && agentPrevPos != null) {
				//g.setColor(Color.GRAY);
				stroke = new BasicStroke(0.015f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
				g.setStroke(stroke);
				Point2D lastPoint = new Point2D.Double(normX(agentPrevPos[0] + 0.5), normY(agentPrevPos[1] + 0.5));
				Point2D currPoint = new Point2D.Double(normX(agentPos[0] + 0.5), normY(agentPos[1] + 0.5));
				if (!lastPoint.equals(currPoint))
					hasMoved = true;
				
				GradientPaint travelPaint = new GradientPaint(lastPoint, Color.WHITE, 
						currPoint, Color.LIGHT_GRAY, false);
				g.setPaint(travelPaint);
				
//				g.draw(new Line2D.Double(lastPoint, currPoint));
				
				if (lastAct != Integer.MIN_VALUE){
					double wheelDisplacement = 0.15;
					Point2D lastFirstWheel;
					Point2D lastSecondWheel;
					Point2D currFirstWheel;
					Point2D currSecondWheel;
					if (horizontalAct) {
						lastFirstWheel = new Point2D.Double(lastPoint.getX(), lastPoint.getY() + normY(wheelDisplacement));
						lastSecondWheel = new Point2D.Double(lastPoint.getX(), lastPoint.getY() - normY(wheelDisplacement));
						currFirstWheel = new Point2D.Double(currPoint.getX(), currPoint.getY() + normY(wheelDisplacement));
						currSecondWheel = new Point2D.Double(currPoint.getX(), currPoint.getY() - normY(wheelDisplacement));
						 
						g.draw(new Line2D.Double(lastFirstWheel, currFirstWheel));
						g.draw(new Line2D.Double(lastSecondWheel, currSecondWheel));
					}
					else {
						lastFirstWheel = new Point2D.Double(lastPoint.getX() + normX(wheelDisplacement), lastPoint.getY());
						lastSecondWheel = new Point2D.Double(lastPoint.getX() - normX(wheelDisplacement), lastPoint.getY());
						currFirstWheel = new Point2D.Double(currPoint.getX() + normX(wheelDisplacement), currPoint.getY());
						currSecondWheel = new Point2D.Double(currPoint.getX() - normX(wheelDisplacement), currPoint.getY());
						 
						g.draw(new Line2D.Double(lastFirstWheel, currFirstWheel));
						g.draw(new Line2D.Double(lastSecondWheel, currSecondWheel));
					}
				}				
				//System.out.println("lastPoint: " + lastPoint);
				//System.out.println("currPoint: " + currPoint);
				//System.out.println("agentAct in viz: " + lastAct);
			}
			String agentPrevPosStr = agentPrevPos == null ? "null" : Arrays.toString(agentPrevPos);
			String agentPosStr = agentPos == null ? "null" : Arrays.toString(agentPos);
			//System.out.println("\nagentPrevPos in viz: " + agentPrevPosStr);
			//System.out.println("agentPos in viz: " + agentPosStr);
				
			
			/*
			 * Draw fail state below agent
			 */				
			if (LoopMazeState.failLoc != null) {
				g.setPaint(new Color(0,0,0));//161, 20, 3));
				Rectangle2D failRect = new Rectangle2D.Double(normX(LoopMazeState.failLoc[0]),
															  normY(LoopMazeState.failLoc[1]),
															  normX(1),normY(1));
				g.fill(failRect);
			}
			
			
			/*
			 * Draw agent
			 */
			Rectangle2D agentRect = new Rectangle2D.Double(normX(agentPos[0] + 0.25), normY(agentPos[1] + 0.25), 
					   normX(agentWidth), normY(agentWidth));
			g.setColor(Color.BLACK);
			//g.fill(resizeRect(agentRect,1.1, 1.1));
			stroke = new BasicStroke(0.005f);//, BasicStroke..CAP_ROUND, BasicStroke.JOIN_ROUND);
			g.setStroke(stroke);
			g.draw(agentRect);
			g.setColor(new Color(70, 100, 20));
			g.fill(agentRect);
			
			
			

			
			/*
			 * Draw action eyes. All of the parameters at top are in units of a cell width or height.
			 */
			double eyeShiftVal = 0.22; 
			double eyeHt = 0.12;
			double eyeWidth = 0.12;
			double eyeDistFromCenter = 0.1;
			double bumpDistFromCenter = 0.12;
			if (lastAct != Integer.MIN_VALUE){
//				double eyeShiftX = 0;
//				double eyeShiftY = 0;
				double ellipseHt = 0;
				double ellipseWidth = 0;
				int xDelta = 0;
				int yDelta = 0;
				// Figure out dimensions and placement of eyes
		        if (horizontalAct) { // right or left
		        	xDelta = 1;
		            ellipseWidth = normX(eyeHt);
		            ellipseHt = normY(eyeWidth);
		            if (lastAct == 1)  // left
		            	xDelta = -1;
		        }       
		        else { // down or up
		        	yDelta = 1;
		            ellipseWidth = normX(eyeWidth);
		            ellipseHt = normY(eyeHt);
		        	if (lastAct == 3)  // up
		        		yDelta = -1;
		        }
		        double ellipseTopLeftX = normX(agentPos[0] + 0.5) + normX(xDelta * eyeShiftVal) - (0.5 * ellipseWidth);
		        double ellipseTopLeftY = normY(agentPos[1] + 0.5) + normY(yDelta * eyeShiftVal) - (0.5 * ellipseHt);

		        
				Ellipse2D agentFirstEyeEllipse;
				Ellipse2D agentSecondEyeEllipse;				

				
				if (horizontalAct) {
					agentFirstEyeEllipse = new Ellipse2D.Double(ellipseTopLeftX, ellipseTopLeftY + normY(eyeDistFromCenter), 
																ellipseWidth, ellipseHt);
					agentSecondEyeEllipse = new Ellipse2D.Double(ellipseTopLeftX, ellipseTopLeftY - normY(eyeDistFromCenter), 
																ellipseWidth, ellipseHt);
				}
				else {
					agentFirstEyeEllipse = new Ellipse2D.Double(ellipseTopLeftX + normX(eyeDistFromCenter), ellipseTopLeftY, 
							ellipseWidth, ellipseHt);
					agentSecondEyeEllipse = new Ellipse2D.Double(ellipseTopLeftX - normX(eyeDistFromCenter), ellipseTopLeftY, 
							ellipseWidth, ellipseHt);
				}
				
				
				/*
				 * Draw bump before eyes (X mark for when agent hits a wall)
				 */
				if (!hasMoved) {
					g.setColor(Color.RED);			        
					Point2D bumpLineLeftStart = null;
					Point2D bumpLineLeftEnd = null;
					Point2D bumpLineRightStart = null;
					Point2D bumpLineRightEnd = null;
					double bumpStartDispl = eyeShiftVal + eyeHt;
					
					if (horizontalAct){
						bumpLineLeftStart = new Point2D.Double(normX(agentPos[0] + 0.5 + ((bumpStartDispl - 0.02) * xDelta)), 
								agentFirstEyeEllipse.getCenterY());
						bumpLineRightStart = new Point2D.Double(normX(agentPos[0] + 0.5 + ((bumpStartDispl - 0.02) * xDelta)),
								agentSecondEyeEllipse.getCenterY());
						bumpLineLeftEnd = new Point2D.Double(normX(agentPos[0] + 0.5 + (0.47 * xDelta)), 
							agentFirstEyeEllipse.getCenterY());
						bumpLineRightEnd = new Point2D.Double(normX(agentPos[0] + 0.5 + (0.47 * xDelta)),
								agentSecondEyeEllipse.getCenterY());
					}
					else {
						bumpLineLeftStart = new Point2D.Double(agentFirstEyeEllipse.getCenterX(), 
								normY(agentPos[1] + 0.5 + ((bumpStartDispl - 0.02) * yDelta)));
						bumpLineRightStart = new Point2D.Double(agentSecondEyeEllipse.getCenterX(),
								normY(agentPos[1] + 0.5 + ((bumpStartDispl - 0.02) * yDelta)));
						bumpLineLeftEnd = new Point2D.Double(agentFirstEyeEllipse.getCenterX(), 
								normY(agentPos[1] + 0.5 + (0.48 * yDelta)));
						bumpLineRightEnd = new Point2D.Double(agentSecondEyeEllipse.getCenterX(),
								normY(agentPos[1] + 0.5 + (0.48 * yDelta)));
					}
					g.draw(new Line2D.Double(bumpLineLeftStart, bumpLineRightEnd));
					g.draw(new Line2D.Double(bumpLineRightStart, bumpLineLeftEnd));
				}
				
				
				
				g.setColor(Color.BLACK);
				double eyeLineScale = 1.3;
				stroke = new BasicStroke(0.005f);//, BasicStroke..CAP_ROUND, BasicStroke.JOIN_ROUND);
				g.setStroke(stroke);
				g.fill(resizeRect(agentFirstEyeEllipse, eyeLineScale, eyeLineScale));
				g.fill(resizeRect(agentSecondEyeEllipse, eyeLineScale, eyeLineScale));
				
				g.setColor(Color.WHITE);
				g.fill(agentFirstEyeEllipse);
				g.fill(agentSecondEyeEllipse);
//				g.transform(AffineTransform.getScaleInstance(2, 2));
				
			}
			
			
			Composite originalComposite = g.getComposite();
			
			/*
			 * Draw goal
			 */
			if (LoopMazeState.goalLoc != null) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
				g.setPaint(new Color(107, 157, 174));
				Rectangle2D goalRect = new Rectangle2D.Double(normX(LoopMazeState.goalLoc[0]),
															  normY(LoopMazeState.goalLoc[1]),
															  normX(1),normY(1));
				g.fill(goalRect);
			}
						
			/*
			 * Draw fail state on top of agent
			 */	
			
			if (LoopMazeState.failLoc != null) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
				g.setPaint(new Color(0,0,0));//161, 20, 3));
				Rectangle2D failRect = new Rectangle2D.Double(normX(LoopMazeState.failLoc[0]),
															  normY(LoopMazeState.failLoc[1]),
															  normX(1),normY(1));
				g.fill(failRect);
			}
			
			
			g.setComposite(originalComposite);
			
			
			
			/*
			 * Draw vertical walls
			 */
			stroke = new BasicStroke(0.008f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			g.setStroke(stroke);
			Line2D wallLine;
			g.setColor(Color.BLACK);
			for (int x = 0; x < LoopMazeState.vertWalls[0].length; x += 1) {
				for (int y = 0; y < LoopMazeState.vertWalls.length; y += 1) {
					if (LoopMazeState.vertWalls[y][x] != 0) {
						wallLine = new Line2D.Double(normX(x+1), normY(y),
														  normX(x+1), normY(y+1));
						g.draw(wallLine);
					}
				}
			}
			
			/*
			 * Draw horizontal walls
			 */
			for (int x = 0; x < LoopMazeState.horWalls[0].length; x += 1) {
				for (int y = 0; y < LoopMazeState.horWalls.length; y += 1) {
					if (LoopMazeState.horWalls[y][x] != 0) {
						wallLine = new Line2D.Double(normX(x), normY(y+1),
														  normX(x+1), normY(y+1));
						g.draw(wallLine);
					}
				}
			}
			//Rectangle2D outerWalls = new Rectangle2D.Double(0.0, 0.0, 1.0, 1.0);
			//g.draw(outerWalls);

			if (LoopMazeState.stateMap[agentPos[1]][agentPos[0]] == 2 || (agentPrevPos != null &&
					LoopMazeState.stateMap[agentPrevPos[1]][agentPrevPos[0]] == 2)) {
		        Font f = new Font("Verdana", 0, 12);
		        g.setFont(f);
		        g.scale(.005, .005);
				String secretStr = "(Secret passage... Shhhh...)";
				// Measure the font and the message
				FontRenderContext frc = g.getFontRenderContext();
				Rectangle2D bounds = g.getFont().getStringBounds(secretStr, frc);
				LineMetrics metrics = g.getFont().getLineMetrics(secretStr, frc);
				float width = (float) bounds.getWidth();     // The width of our text
				float lineheight = metrics.getHeight();      // Total line height
				float ascent = metrics.getAscent();          // Top of text to baseline

				float x = (float) (200f - width)/2;
				float y = (float) ((200f - lineheight)/2 + ascent);
				g.setColor(Color.RED);
				g.drawString(secretStr, x, y); 
			}
			
		}
		else{
			System.out.println("LoopMazeState.worldDims: " + LoopMazeState.worldDims);
			System.out.println("LoopMazeState.goalLoc: " + LoopMazeState.goalLoc);
			if (LoopMazeState.stateMap != null) {
				System.out.println("stateMap in GridMapComponent: ");
				for (int i = 0; i < LoopMazeState.stateMap.length; i++) {
					System.out.println(Arrays.toString(LoopMazeState.stateMap[i]));
				}
			}
		}
    	
    	
    	
    	
	}

	private double normX(double num) {
		return (num / LoopMazeState.worldDims[0]);
	}
	private double normY(double num) {
		return (num / LoopMazeState.worldDims[1]);
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

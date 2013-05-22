/*
Copyright 2007 Brian Tanner
http://rl-library.googlecode.com/
brian@tannerpages.com
http://brian.tannerpages.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

package org.rlcommunity.environments.tetris.visualizer;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import java.util.Observable;
import java.util.Observer;

import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

import rlVizLib.visualization.SelfUpdatingVizComponent;
import rlVizLib.visualization.VizComponentChangeListener;

public class TetrisBlocksComponent implements SelfUpdatingVizComponent, Observer {
	private TetrisVisualizer tetVis = null;
	private int lastUpdateTimeStep=-1;

	private boolean bgFlash = true;
	private static final int feedbackLimit = 4;
        String CURR_REINF_FILENAME = null;
	private int currFeedback = 0;
	private int prevBlockFeedback = 0;
	private int[] prevBlockRecord = null;
	private int secLastBlockFeedback = 0;
	private int[] secLastBlockRecord = null;

	public TetrisBlocksComponent(TetrisVisualizer ev){
		// TODO Write Constructor
		this.tetVis = ev;
                ev.getGlueState().addObserver(this);
		
		//String rlLibraryPath = System.getenv("RLLIBRARY");

//		System.out.println("Tetris environment's read of RL-Library path: " + rlLibraryPath);
//		if (rlLibraryPath != null){
//			CURR_REINF_FILENAME = rlLibraryPath + "/currReinf.log";
//		}
//		else {
//			System.err.println("RLLIBRARY environment variable must be set. Terminating Java environment.");
//			System.exit(0);
//		}

	}

	public void render(Graphics2D g) {

		Rectangle2D agentRect;
		int numCols = tetVis.getWidth();
		int numRows = tetVis.getHeight();
		int[] tempWorld = tetVis.getWorld();
		int[] previousBlock = tetVis.getPreviousBlock();
		//int[] secToLastBlock = tetVis.getSecToLastBlock();
	
		boolean prevBlkSame = true;
		if (previousBlock != null) {
			if (prevBlockRecord == null){
				prevBlkSame = false;
			}
			else {
				for (int i = 0; i < previousBlock.length; i++) {
					if (previousBlock[i] != prevBlockRecord[i]){
						prevBlkSame = false;
					}
				}
			}
		}

		if (!prevBlkSame){
			secLastBlockFeedback = prevBlockFeedback;
			prevBlockFeedback = 0;
			secLastBlockRecord = prevBlockRecord;
			prevBlockRecord = previousBlock;			
		}
		
		boolean flash = false;
		if (currFeedback != 0
				&& (Math.abs(prevBlockFeedback) < feedbackLimit
				|| Math.abs(prevBlockFeedback + currFeedback) < feedbackLimit)) {
			flash = true;
		}
		prevBlockFeedback += currFeedback;
		
		
		//Desired abstract block size
		int DABS=10;
		int scaleFactorX=numCols*DABS;
		int scaleFactorY=numRows*DABS;
		
		int w = DABS;
		int h = DABS;
		int x=0;
		int y = 0;
		AffineTransform saveAT = g.getTransform();
		g.setColor(Color.GRAY);
		g.scale(1.0d/(double)scaleFactorX,1.0d/(double)scaleFactorY);		
		
		for(int i= 0; i<numRows; i++){
		for(int j=0; j<numCols; j++){
			x = j*DABS;
			y = i*DABS;
			int tetIndex = i*numCols+j;
                        int thisBlockColor=tempWorld[tetIndex];
			Color color = null;
			double noCol = 0.5;
			if(thisBlockColor!=0){
                                switch(thisBlockColor){
                          case 1: // line
                              color = makeColor(noCol, noCol, noCol); //
                              break;
                          case 2: // square
                              color = makeColor(noCol, noCol, noCol); //
                              break;
                          case 3: // triton
                              color = makeColor(noCol, noCol, noCol); //
                              break;
                          case 4: // z
                              color = makeColor(0.3, 0.05, 0.05); //
                              break;
                          case 5: // s
                              color =  makeColor(0.0, 0.3, 0.3); //
                              break;
                          case 6: // mirrored L
                              color = makeColor(0.0, 0.3, 0.3); //
                              break;
                          case 7: // L
                              color = makeColor(0.3, 0.05, 0.05); //
                              break;
                              
//                          case 1: // line
//                              color = makeColor(0.5, 0.3, 0.1); //
//                              break;
//                          case 2: // square
//                              color = makeColor(0.4, 0.2, 0.2); //
//                              break;
//                          case 3: // triton
//                              color = makeColor(0.5, 0.4, 0.1); //
//                              break;
//                          case 4: // z
//                              color = makeColor(0.2, 0.3, 0.1);
//                              break;
//                          case 5: // s
//                              color =  makeColor(0.2, 0.4, 0.4); //
//                              break;
//                          case 6: // mirrored L
//                              color = makeColor(0.5, 0.1, 0.4); //
//                              break;
//                          case 7: // L
//                              color = makeColor(0.2, 0.1, 0.4); //
//                              break;
//                                case 1: // line
//                                    color = makeColor(0.4, 0.6, 0.2); //
//                                    break;
//                                case 2: // square
//                                    color = makeColor(0.4, 0.2, 0.2); //
//                                    break;
//                                case 3: // triton
//                                    color = makeColor(0.4, 0.6, 0.2); //
//                                    break;
//                                case 4: // z
//                                    color = makeColor(0.0, 0.6, 0.2);
//                                    break;
//                                case 5: // s
//                                    color =  makeColor(0.0, 0.6, 0.6); //
//                                    break;
//                                case 6: // mirrored L
//                                    color = makeColor(0.4, 0.2, 0.6); //
//                                    break;
//                                case 7: // L
//                                    color = makeColor(0.0, 0.2, 0.6); //
//                                    break;
								                
//                                case 1: // line
//                                    color = makeColor(0.6, 0.4, 0.0); //
//                                    break;
//                                case 2: // square
//                                    color = makeColor(0.6, 0.0, 0.0); //
//                                    break;
//                                case 3: // triton
//                                    color = makeColor(0.6, 0.6, 0.0); //
//                                    break;
//                                case 4: // z
//                                    color = makeColor(0.0, 0.4, 0.0);
//                                    break;
//                                case 5: // s
//                                    color =  makeColor(0.0, 0.6, 0.6); //
//                                    break;
//                                case 6: // mirrored L
//                                    color = makeColor(0.6, 0.0, 0.6); //
//                                    break;
//                                case 7: // L
//                                    color = makeColor(0.0, 0.0, 0.6); //
//                                    break;
                                }
				if (color != null) {
					g.setColor(color);
				}
				

				boolean specialBlock = false;
				if (previousBlock != null &&
				    checkForAndHandleSpecialBlock(previousBlock, currFeedback, flash, tetIndex, g, x, y, w, h)) {
					specialBlock = true;
				}

				/*if (secToLastBlock != null &&
				    checkForAndHandleSpecialBlock(secToLastBlock, secLastBlockFeedback, flash, tetIndex, g, x, y, w, h)) {
					specialBlock = true;	
				} */
				if (!specialBlock) {
					//System.out.println("Filling rect for normal block.");
					g.fill3DRect(x, y, w, h, true);
				}
										
			}
			else {
				g.setColor(Color.WHITE); //bg color
				if (bgFlash && flash){
					if (currFeedback > 0) { // && l < feedback) { // selects as many indices as there have been reinforcements
						g.setColor(makeColor(0.5, 1.0, 0.5));
					}
					else if (currFeedback < 0) { // && (-1* l) > feedback) {
						g.setColor(makeColor(1.0, 0.5, 0.5));
					}
				}
				agentRect = new Rectangle2D.Double(x, y, w, h); //bdg, took this out	
                                if(tetVis.printGrid())
                                    g.fill3DRect(x,y,w,h,true);
                                else
                                    g.fill(agentRect);
			}
		}
	}
        g.setColor(Color.GRAY);
   	    g.drawRect(0,0,DABS*numCols,DABS*numRows);
	    g.setTransform(saveAT);
	    currFeedback = 0;
	}

	private boolean checkForAndHandleSpecialBlock(int[] block, int feedback, boolean flash, int tetIndex, Graphics2D g,
						      int x, int y, int w, int h) {
		//System.out.println("x");
		boolean specialBlock = false;
		Color color = null;
		for (int l = 0; l < block.length; l++) {		
			if (block[l] == tetIndex) {
				specialBlock = true;
				if (feedback > 0 && flash) { // && l < feedback) { // selects as many indices as there have been reinforcements
					color = Color.GREEN;
				}
				else if (feedback < 0 && flash) { // && (-1* l) > feedback) {
					color = Color.RED;
				}
				else {
					color = Color.BLACK;
				}
				g.setColor(color);
				g.fill3DRect(x, y, w, h, true);
			}
		}
		return specialBlock;
	}
	
	
	public void setCurrFeedback(int feedback){
		currFeedback = feedback;		
	}

	private Color makeColor(double r, double g, double b){
		int iR = (int)(r * 255);
		int iG = (int)(g * 255);
		int iB = (int)(b * 255);
		return new Color(iR, iG, iB);
	}

	/* private int checkForFeedback(){
		int feedback = 0;
		// Stream to read file
		FileInputStream fIn;		

		
		try
		{

		    // Open an input stream
		    fIn = new FileInputStream (this.CURR_REINF_FILENAME);

		    DataInputStream dIn = new DataInputStream(fIn);

			String line = null;
			while((line = dIn.readLine()) != null){
				try{
					feedback += Integer.parseInt(line);
				}
				catch (NumberFormatException e) {
					System.err.println("Invalid entry in current reinforcement file: " + e);
				}
			}
			
		    fIn.close();		
			
			(new File(this.CURR_REINF_FILENAME)).delete();
			
		}
		// Catches any error conditions
		catch (IOException e)
		{
		    ; //System.out.println("Couldn't find it: " + e);
		}
		return feedback;
	
	}*/
	
	
   /**
     * This is the object (a renderObject) that should be told when this component needs to be drawn again.
     */
    private VizComponentChangeListener theChangeListener;

    public void setVizComponentChangeListener(VizComponentChangeListener theChangeListener) {
        this.theChangeListener = theChangeListener;
    }

    /**
     * This will be called when TinyGlue steps.
     * @param o
     * @param arg
     */
    public void update(Observable o, Object arg) {
        if (theChangeListener != null) {
            if(arg instanceof Observation){
                tetVis.updateAgentState(false);
                theChangeListener.vizComponentChanged(this);
            }
            if(arg instanceof Reward_observation_terminal){
                tetVis.updateAgentState(false);
                theChangeListener.vizComponentChanged(this);
            }
        }
    }


}

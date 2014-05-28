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
package edu.utexas.cs.tamerProject.environments.cartarm;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.*;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;



/**
 * This class manages all of the problem parameters, current state variables, 
 * and state transition and reward dynamics.
 *
 * @author Brad Knox
 */
public class CartArmState {

	public static final int[] worldIntDims = {20, 20};//{5, 8, 5, 6, 4, 5}; // discretizations of each joint's angle
	public static final int[] worldDoubleDims = {10, 10};
	
	public static final Point2D origin = new Point2D.Double(0.5, 1.0);
	public static final double[] SEG_LENS = {0.05, 0.1, 0.2, 0.1, 0.2, 0.05, 0.05}; // segment lengths of arm (those after index (worldIntDims.length - 1) are unused)
	
	public static final double graspRadius = 0.05;
	public static final Point2D targetLoc = new Point2D.Double(0.8, 0.6); 
	
    private int[] relativeJointAngs;
    private int[] prevRelativeJointAngs;

    private double[] agentLoc; // x, y, and orient
    private double[] agentPrevLoc;

    
    private static int[] defaultInitPosition = null;
    private static int[] overridingDefInitPosition = null; // If used, this value will be hidden from the agent using an env model, which reflect defaultInitPosition. 

    private static Random randomGenerator;
    //These are configurable
    private static boolean randomStarts = false;
    private static double transitionNoise = 0.0d;

	// These should be configurable
    final private static int agentSpeed = 1;
    final public static double rewardPerStep = -1.0;
    final public static double rewardAtGoal = 0.0;


	private static final int NUM_SEGMENTS = worldDims.length - 2;
    
    private int lastAction = 0;
    
    public static void setDefaultInitPos(int[] startState) { CartArmState.defaultInitPosition = startState; }
    public static int[] getDefaultInitPosition() { return CartArmState.defaultInitPosition; }
    public static void setOverridingDefInitPos(int[] startState) { CartArmState.overridingDefInitPosition = startState; }
    public static int[] getOverridingDefInitPosition() { return CartArmState.overridingDefInitPosition; }
    
    public CartArmState(boolean randomStartStates, double transitionNoise, long randomSeed, boolean allowSecretPaths) {
		System.out.println("static worldDims: " + Arrays.toString(CartArmState.worldDims));
    	

		System.out.println("Random starts? " + CartArmState.randomStarts);
		CartArmState.randomStarts = randomStartStates;
		CartArmState.transitionNoise = transitionNoise;

        if (randomSeed == 0) {
        	CartArmState.randomGenerator = new Random();
        } else {
        	CartArmState.randomGenerator = new Random(randomSeed);
        }

        //Throw away the first few because the first bits are not that random.
        randomGenerator.nextDouble();
        randomGenerator.nextDouble();
        reset();
    }


    /**
     * Returns an unmodifiable list of the zones.
     * @return
     */
//    public List<Zone> getZones() {
//        return Collections.unmodifiableList(theZones);
//    }

    public int[] getPosition() {
        return relativeJointAngs.clone();
    }
    
    public int[] getPrevPosition() {
        return prevRelativeJointAngs.clone();
    }
    
    public static List<Point2D> getJointLocs(int[] relativeJointAngs){
    	ArrayList<Point2D> jointLocs = new ArrayList<Point2D>();
    	jointLocs.add(CartArmState.origin);
    	
//    	int[] agentPos = getPosition();
    	
		Point2D lastJointLoc = CartArmState.origin;
		
		double lastJointAng = 0;
		
		for (int segI = 0; segI < CartArmState.worldDims.length; segI++) {
			double jointAng;
			if (segI == 0)
				jointAng = (((0.5 + relativeJointAngs[segI]) / (CartArmState.worldDims[segI])) * Math.PI) + Math.PI;
			else
				jointAng = lastJointAng + ((relativeJointAngs[segI] / (CartArmState.worldDims[segI] - 1.0)) * 2 * Math.PI);
			Point2D newJointLoc = new Point2D.Double(lastJointLoc.getX() + (Math.cos(jointAng) * SEG_LENS[segI]), 
													lastJointLoc.getY() + (Math.sin(jointAng) * SEG_LENS[segI]));

			jointLocs.add(newJointLoc);
			
			lastJointLoc = newJointLoc;
			lastJointAng = jointAng;
		}
		return jointLocs;
    }
    

    /**
     * Calculate the reward 
     * @return
     */
    public double getReward() {
        return CartArmState.getReward(this.agentLoc, this.relativeJointAngs);
    }

    public static double getReward(double[] agentLoc, int[] relativeJointAngs){
        if (CartArmState.inGoalRegion(relativeJointAngs)) {
            return rewardAtGoal;
        } else {
            return rewardPerStep;
        }
    }
    
    
    /**
     * IS the agent past the goal marker? TODO finish
     * @return
     */
    public boolean inGoalRegion() {
		return CartArmState.inGoalRegion(relativeJointAngs);
    }
    /**
     * IS the agent past the goal marker?
     * @return
     */
    public static boolean inGoalRegion(int[] relativeJointAngs) {
    	// get last joint loc
		List<Point2D> jointLocs = CartArmState.getJointLocs(relativeJointAngs);
		Point2D endEffectorLoc = jointLocs.get(jointLocs.size() - 1);
    	
    	// find dist to goal
    	double distSqrdToGoal = Math.pow(Math.abs(endEffectorLoc.getX() - targetLoc.getX()), 2) +
    							Math.pow(Math.abs(endEffectorLoc.getY() - targetLoc.getY()), 2);
    	double distToTarget = Math.sqrt(distSqrdToGoal);
    	
    	// return whether close enough to pick up
    	
		return distToTarget < CartArmState.graspRadius;
    }

    protected void reset() {
    	Observation startObs = CartArmState.sampleEnvStart(true); 
        this.relativeJointAngs = startObs.intArray;
        this.agentLoc = startObs.doubleArray;
        if (prevRelativeJointAngs != null)
        	prevRelativeJointAngs = prevRelativeJointAngs.clone();
        else
        	prevRelativeJointAngs = relativeJointAngs.clone();
    }

    public static Observation sampleEnvStart(boolean envAsking) {
    	int[] agentLoc = new int[CartArmState.worldDims.length];
    	if ((CartArmState.overridingDefInitPosition == null || !envAsking)
    			 && defaultInitPosition != null){
			agentLoc = Arrays.copyOf(defaultInitPosition, defaultInitPosition.length);
    	}
    	else if (CartArmState.overridingDefInitPosition != null) {
    		agentLoc = Arrays.copyOf(overridingDefInitPosition, overridingDefInitPosition.length);
    	}
    	else {
    		agentLoc = CartArmState.getRandomStartState();
    	}
		if (CartArmState.randomStarts) {
			agentLoc = CartArmState.getRandomStartState();
		}
		Observation obs = new Observation();
		obs.intArray = agentLoc;
		return obs;
    }
    
    private static int[] getRandomStartState() {
    	int[] agentLoc = new int[CartArmState.worldDims.length];
		do {
			for (int i = 0; i < agentLoc.length; i++){
				agentLoc[i] = CartArmState.randomGenerator.nextInt(worldDims[i]); 
			}
		} while (inGoalRegion(agentLoc) || !CartArmState.isStateLegal(agentLoc));
		return agentLoc;
    }
    
    void update(int a) {
		//System.out.println("CartArmState.worldDims: " + CartArmState.worldDims);
		//System.out.println("CartArmState.goalLoc: " + CartArmState.goalLoc);
		//if (CartArmState.stateMap != null) {
		//	System.out.println("stateMap in CartArmState: ");
		//	for (int i = 0; i < CartArmState.stateMap.length; i++) {
		//		System.out.println(Arrays.toString(CartArmState.stateMap[i]));
		//	}
		//}

        lastAction = a;
        Action act = new Action();
        act.intArray = new int[1];
        act.intArray[0] = a;
        
        prevRelativeJointAngs = relativeJointAngs.clone();
        
        Observation obs = new Observation();
        obs.intArray = relativeJointAngs;
        
        relativeJointAngs = CartArmState.sampleNextObs(obs, act).intArray;
    }
    
    public static Observation sampleNextObs(Observation obs, Action act) {
		int[] agentLoc = new int[CartArmState.worldDims.length];
		System.out.println("prev obs: " + obs);
		agentLoc = obs.intArray.clone();
		int[] nextAgentLoc = agentLoc.clone();
		
		int locChange = (act.intArray[0] % 2 == 0) ? agentSpeed : (-1 * agentSpeed);
			
		nextAgentLoc[act.intArray[0] / 2] += locChange; 
		
		//double XNoise = randomGenerator.nextGaussian() * transitionNoise * agentSpeed;
		//double YNoise = randomGenerator.nextGaussian() * transitionNoise * agentSpeed;

		//nextX += XNoise;
		//nextY += YNoise;

		
		if (CartArmState.isMoveLegal(agentLoc, nextAgentLoc)) {
			agentLoc = nextAgentLoc;
		}
		else { // no change
			agentLoc = agentLoc.clone();
		}

		Observation newObs = new Observation();
		newObs.intArray = agentLoc;
		
		return newObs;	
    }

	public static boolean isStateLegal(int[] jointAngs){
		/*
		 * Are joint angles in specified range?
		 */
		for (int i = 0; i < jointAngs.length; i++) {
			if (jointAngs[i] < 0 || jointAngs[i] >= CartArmState.worldDims[i])
				return false;
		}	
		
		/*
		 * Are all 2D joint locations in bounds?
		 */
		List<Point2D> jointLocs = CartArmState.getJointLocs(jointAngs);
		for (Point2D jointLoc : jointLocs) {
			if (jointLoc.getX() > 1 || jointLoc.getX() < 0 ||
					jointLoc.getY() > 1 || jointLoc.getY() < 0)
				return false;
		}
		
		return true;	
	}
	
	public static boolean isMoveLegal(int[] currJointAngs, int[] nextJointAngs) {
		if (!CartArmState.isStateLegal(nextJointAngs))
				return false;

		return true;
	}

	
	
    public int getLastAction() {
        return lastAction;
    }

    Observation makeObservation() {
        Observation currentObs = new Observation(CartArmState.worldDims.length, 0);
        int[] position = getPosition();
        currentObs.intArray = Arrays.copyOf(position, position.length);
        return currentObs;
    }

//
//	public void loadMapFromFile(String filePath) {
//		ArrayList<int[]> stateList = new ArrayList<int[]>();
//		try{
//			FileReader fr = new FileReader(filePath); 
//			BufferedReader br = new BufferedReader(fr);	
//			String line;
//			while((line = br.readLine()) != null) { 
//				char[] rowChars = line.toCharArray();
//				int[] stateRow = new int[rowChars.length];
//				for (int i = 0; i < rowChars.length; i++) {
//					stateRow[i] = Integer.valueOf("" + rowChars[i]).intValue();
//				}
//				stateList.add(stateRow);
//			} 
//		}
//		catch (Exception e){
//			System.err.println("Error in loadMapFromFile(): " + e.getMessage() + "\nExiting.");
//			System.exit(0);
//		}
//		
//		this.reset();
//	}
}

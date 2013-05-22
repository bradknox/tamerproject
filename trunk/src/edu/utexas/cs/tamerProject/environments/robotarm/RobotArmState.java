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
package edu.utexas.cs.tamerProject.environments.robotarm;

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
public class RobotArmState {

	public static final int[] worldDims = {20, 20, 20};//{5, 8, 5, 6, 4, 5};
	
	public static final Point2D origin = new Point2D.Double(0.5, 1.0);
	public static final double[] SEG_LENS = {0.3, 0.1, 0.2, 0.1, 0.2, 0.05, 0.05};
	
	public static final double graspRadius = 0.05;
	public static final Point2D targetLoc = new Point2D.Double(0.8, 0.6); 
	
    private int[] agentPosition;
    private int[] agentPrevPosition;

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


	private static final int NUM_SEGMENTS = 3;
    
    private int lastAction = 0;
    
    public static void setDefaultInitPos(int[] startState) { RobotArmState.defaultInitPosition = startState; }
    public static int[] getDefaultInitPosition() { return RobotArmState.defaultInitPosition; }
    public static void setOverridingDefInitPos(int[] startState) { RobotArmState.overridingDefInitPosition = startState; }
    public static int[] getOverridingDefInitPosition() { return RobotArmState.overridingDefInitPosition; }
    
    public RobotArmState(boolean randomStartStates, double transitionNoise, long randomSeed, boolean allowSecretPaths) {
		System.out.println("static worldDims: " + Arrays.toString(RobotArmState.worldDims));
    	

		System.out.println("Random starts? " + RobotArmState.randomStarts);
		RobotArmState.randomStarts = randomStartStates;
		RobotArmState.transitionNoise = transitionNoise;

        if (randomSeed == 0) {
        	RobotArmState.randomGenerator = new Random();
        } else {
        	RobotArmState.randomGenerator = new Random(randomSeed);
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
        return agentPosition.clone();
    }
    
    public int[] getPrevPosition() {
        return agentPrevPosition.clone();
    }
    
    public static List<Point2D> getJointLocs(int[] relativeJointAngs){
    	ArrayList<Point2D> jointLocs = new ArrayList<Point2D>();
    	jointLocs.add(RobotArmState.origin);
    	
//    	int[] agentPos = getPosition();
    	
		Point2D lastJointLoc = RobotArmState.origin;
		
		double lastJointAng = 0;
		
		for (int segI = 0; segI < RobotArmState.worldDims.length; segI++) {
			double jointAng;
			if (segI == 0)
				jointAng = (((0.5 + relativeJointAngs[segI]) / (RobotArmState.worldDims[segI])) * Math.PI) + Math.PI;
			else
				jointAng = lastJointAng + ((relativeJointAngs[segI] / (RobotArmState.worldDims[segI] - 1.0)) * 2 * Math.PI);
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
        return RobotArmState.getReward(this.agentPosition);
    }

    public static double getReward(int[] relativeJointAngs){
        if (RobotArmState.inGoalRegion(relativeJointAngs)) {
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
		return RobotArmState.inGoalRegion(agentPosition);
    }
    /**
     * IS the agent past the goal marker?
     * @return
     */
    public static boolean inGoalRegion(int[] relativeJointAngs) {
    	// get last joint loc
		List<Point2D> jointLocs = RobotArmState.getJointLocs(relativeJointAngs);
		Point2D endEffectorLoc = jointLocs.get(jointLocs.size() - 1);
    	
    	// find dist to goal
    	double distSqrdToGoal = Math.pow(Math.abs(endEffectorLoc.getX() - targetLoc.getX()), 2) +
    							Math.pow(Math.abs(endEffectorLoc.getY() - targetLoc.getY()), 2);
    	double distToTarget = Math.sqrt(distSqrdToGoal);
    	
    	// return whether close enough to pick up
    	
		return distToTarget < RobotArmState.graspRadius;
    }

    protected void reset() {
        agentPosition = RobotArmState.sampleEnvStart(true).intArray;   
        if (agentPrevPosition != null)
        	agentPrevPosition = agentPrevPosition.clone();
        else
        	agentPrevPosition = agentPosition.clone();
    }

    public static Observation sampleEnvStart(boolean envAsking) {
    	int[] agentLoc = new int[RobotArmState.worldDims.length];
    	if ((RobotArmState.overridingDefInitPosition == null || !envAsking)
    			 && defaultInitPosition != null){
			agentLoc = Arrays.copyOf(defaultInitPosition, defaultInitPosition.length);
    	}
    	else if (RobotArmState.overridingDefInitPosition != null) {
    		agentLoc = Arrays.copyOf(overridingDefInitPosition, overridingDefInitPosition.length);
    	}
    	else {
    		agentLoc = RobotArmState.getRandomStartState();
    	}
		if (RobotArmState.randomStarts) {
			agentLoc = RobotArmState.getRandomStartState();
		}
		Observation obs = new Observation();
		obs.intArray = agentLoc;
		return obs;
    }
    
    private static int[] getRandomStartState() {
    	int[] agentLoc = new int[RobotArmState.worldDims.length];
		do {
			for (int i = 0; i < agentLoc.length; i++){
				agentLoc[i] = RobotArmState.randomGenerator.nextInt(worldDims[i]); 
			}
		} while (inGoalRegion(agentLoc) || !RobotArmState.isStateLegal(agentLoc));
		return agentLoc;
    }
    
    void update(int a) {
		//System.out.println("RobotArmState.worldDims: " + RobotArmState.worldDims);
		//System.out.println("RobotArmState.goalLoc: " + RobotArmState.goalLoc);
		//if (RobotArmState.stateMap != null) {
		//	System.out.println("stateMap in RobotArmState: ");
		//	for (int i = 0; i < RobotArmState.stateMap.length; i++) {
		//		System.out.println(Arrays.toString(RobotArmState.stateMap[i]));
		//	}
		//}

        lastAction = a;
        Action act = new Action();
        act.intArray = new int[1];
        act.intArray[0] = a;
        
        agentPrevPosition = agentPosition.clone();
        
        Observation obs = new Observation();
        obs.intArray = agentPosition;
        
        agentPosition = RobotArmState.sampleNextObs(obs, act).intArray;
    }
    
    public static Observation sampleNextObs(Observation obs, Action act) {
		int[] agentLoc = new int[RobotArmState.worldDims.length];
		agentLoc = obs.intArray.clone();
		int[] nextAgentLoc = agentLoc.clone();
		
		int locChange = (act.intArray[0] % 2 == 0) ? agentSpeed : (-1 * agentSpeed);
			
		nextAgentLoc[act.intArray[0] / 2] += locChange; 
		
		//double XNoise = randomGenerator.nextGaussian() * transitionNoise * agentSpeed;
		//double YNoise = randomGenerator.nextGaussian() * transitionNoise * agentSpeed;

		//nextX += XNoise;
		//nextY += YNoise;

		
		if (RobotArmState.isMoveLegal(agentLoc, nextAgentLoc)) {
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
			if (jointAngs[i] < 0 || jointAngs[i] >= RobotArmState.worldDims[i])
				return false;
		}	
		
		/*
		 * Are all 2D joint locations in bounds?
		 */
		List<Point2D> jointLocs = RobotArmState.getJointLocs(jointAngs);
		for (Point2D jointLoc : jointLocs) {
			if (jointLoc.getX() > 1 || jointLoc.getX() < 0 ||
					jointLoc.getY() > 1 || jointLoc.getY() < 0)
				return false;
		}
		
		return true;	
	}
	
	public static boolean isMoveLegal(int[] currJointAngs, int[] nextJointAngs) {
		if (!RobotArmState.isStateLegal(nextJointAngs))
				return false;

		return true;
	}

	
	
    public int getLastAction() {
        return lastAction;
    }

    Observation makeObservation() {
        Observation currentObs = new Observation(RobotArmState.worldDims.length, 0);
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

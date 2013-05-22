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
package edu.utexas.cs.tamerProject.environments.loopmaze;

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
public class LoopMazeState {
	public static int[][] stateMap = {	{1,1,1,1,1,1},
										{1,0,1,1,1,1},
										{1,0,1,0,0,1},
										{1,2,1,1,1,1},
										{1,0,1,1,1,1},
										{1,1,1,1,1,1}	}; // 2 means the state is secret (not shown in visualizer but can be traversed)
	


	
	public static int[][] vertWalls = {	{0,0,0,0,1},
										{1,1,0,0,0},
										{1,1,1,0,1},
										{2,2,0,0,0},
										{1,1,0,0,0},
										{0,0,0,0,0}	}; // 2 means the walls are secretly traversable if secret paths are enabled.
	public static int[][] horWalls = {	{0,1,1,1,1,0},
										{0,0,0,1,1,0},
										{0,0,0,1,1,0},
										{0,0,0,0,0,0},
										{0,1,1,1,1,0}	};

    private int[] agentPosition = {1,1};
    private int[] agentPrevPosition = {1,1};
    private static Vector<Zone> theZones = ZoneGen.makeZones();
	public static int[] worldDims;
	public static int[] goalLoc = null;
	public static int[] failLoc = null;
    private static int[] defaultInitPosition = {4, 0};
    private static int[] overridingDefInitPosition = null; // If used, this value will be hidden from the agent using an env model, which reflect defaultInitPosition. 

    private static Random randomGenerator;
    //These are configurable
    private static boolean randomStarts = false;
    private static double transitionNoise = 0.0d;
    private static boolean allowSecretPaths;





	// These should be configurable
    final private static double agentSpeed = 1.0;
    final public static double rewardPerStep = -1.0;
    final public static double rewardAtGoal = 0.0;
	final public static double rewardAtFail = -20.0;
    
    private int lastAction = 0;
    
    public static void setDefaultInitPos(int[] startState) { LoopMazeState.defaultInitPosition = startState; }
    public static int[] getDefaultInitPosition() { return LoopMazeState.defaultInitPosition; }
    public static void setOverridingDefInitPos(int[] startState) { LoopMazeState.overridingDefInitPosition = startState; }
    public static int[] getOverridingDefInitPosition() { return LoopMazeState.overridingDefInitPosition; }
    
    public LoopMazeState(boolean randomStartStates, double transitionNoise, long randomSeed, boolean allowSecretPaths) {
		if (LoopMazeState.stateMap == null) {
			LoopMazeState.stateMap = new int[5][5];
			for (int y = 0; y < LoopMazeState.stateMap.length; y++) {
				for (int x = 0; x < LoopMazeState.stateMap[0].length; x++) {
					LoopMazeState.stateMap[x][y] = 1;
				}
			}
		}

		this.setWorldVars();
		System.out.println("Random starts? " + LoopMazeState.randomStarts);
		LoopMazeState.randomStarts = randomStartStates;
		LoopMazeState.transitionNoise = transitionNoise;
        LoopMazeState.allowSecretPaths = allowSecretPaths;

        if (randomSeed == 0) {
        	LoopMazeState.randomGenerator = new Random();
        } else {
        	LoopMazeState.randomGenerator = new Random(randomSeed);
        }

        //Throw away the first few because the first bits are not that random.
        randomGenerator.nextDouble();
        randomGenerator.nextDouble();
        reset();
    }

	public void setWorldVars() {
		LoopMazeState.worldDims = new int[2];
		LoopMazeState.worldDims[0] = stateMap[0].length;
		LoopMazeState.worldDims[1] = stateMap.length;
		LoopMazeState.goalLoc = new int[2];
		LoopMazeState.goalLoc[0] = worldDims[0] - 1;
		LoopMazeState.goalLoc[1] = 0;
//		LoopMazeState.failLoc = new int[2];
//		LoopMazeState.failLoc[0] = worldDims[0] - 2;
//		LoopMazeState.failLoc[1] = 3;
		System.out.println("worldDims: " + Arrays.toString(worldDims));
		System.out.println("static worldDims: " + Arrays.toString(LoopMazeState.worldDims));
	}

    public void addZone(Zone newZone) {
        theZones.add(newZone);
    }

    public void clearZones() {
        theZones.clear();
    }

    /**
     * Returns an unmodifiable list of the zones.
     * @return
     */
    public List<Zone> getZones() {
        return Collections.unmodifiableList(theZones);
    }

    public int[] getPosition() {
        return agentPosition.clone();
    }
    
    public int[] getPrevPosition() {
        return agentPrevPosition.clone();
    }

    /**
     * Calculate the reward 
     * @return
     */
    public double getReward() {
        return LoopMazeState.getReward(this.agentPosition);
    }

    public static double getReward(int[] agentLoc){
    	double zoneReward = getZoneReward(agentLoc);
        if (LoopMazeState.inGoalRegion(agentLoc)) {
            return zoneReward + rewardAtGoal;
        } 
        else if(LoopMazeState.inFailRegion(agentLoc)) {
        	return zoneReward + rewardAtFail;
        }
        else {
            return zoneReward + rewardPerStep;
        }
    }
    
    
    private static double getZoneReward(int[] agentLoc) {
        double totalZoneReward = 0;
        for (Zone zone : theZones) {
            totalZoneReward += zone.getReward(agentLoc);
        }
        return totalZoneReward;
    }

    /**
     * IS the agent past the goal marker?
     * @return
     */
    public boolean inGoalRegion() {
		return LoopMazeState.inGoalRegion(agentPosition);
    }
    /**
     * IS the agent past the goal marker?
     * @return
     */
    public static boolean inGoalRegion(int[] agentLoc) {
		//System.out.print("pos: " + agentLoc[0] + ", " +  agentLoc[1]);
		//System.out.println("; inGoalRegion: " + Arrays.equals(goalLoc, agentLoc));
		return Arrays.equals(goalLoc, agentLoc);
    }

    public boolean inFailRegion() {
		return LoopMazeState.inFailRegion(agentPosition);
    }
    public static boolean inFailRegion(int[] agentLoc) {
		//System.out.print("pos: " + agentLoc[0] + ", " +  agentLoc[1]);
		//System.out.println("; inGoalRegion: " + Arrays.equals(goalLoc, agentLoc));
		return Arrays.equals(failLoc, agentLoc);
    }
    
    protected void reset() {
        agentPosition = LoopMazeState.sampleEnvStart(true).intArray;        
		agentPrevPosition = agentPrevPosition.clone();
    }

    public static Observation sampleEnvStart(boolean envAsking) {
    	int[] agentLoc = new int[2];
    	if (LoopMazeState.overridingDefInitPosition == null || !envAsking) {
			agentLoc[0] = defaultInitPosition[0];
			agentLoc[1] = defaultInitPosition[1];
    	}
    	else {
    		agentLoc[0] = overridingDefInitPosition[0];
    		agentLoc[1] = overridingDefInitPosition[1];
    	}
		if (LoopMazeState.randomStarts) {
			do {
				int randStartX = LoopMazeState.randomGenerator.nextInt(worldDims[0]);
				int randStartY = LoopMazeState.randomGenerator.nextInt(worldDims[1]);
				agentLoc[0] = randStartX;
				agentLoc[1] = randStartY;
			} while (inGoalRegion(agentLoc) || 
					inFailRegion(agentLoc) ||
					!LoopMazeState.isStateLegal(agentLoc[0],agentLoc[1]));
		}
		Observation obs = new Observation();
		obs.intArray = agentLoc;
		return obs;
    }
    
    void update(int a) {
		//System.out.println("LoopMazeState.worldDims: " + LoopMazeState.worldDims);
		//System.out.println("LoopMazeState.goalLoc: " + LoopMazeState.goalLoc);
		//if (LoopMazeState.stateMap != null) {
		//	System.out.println("stateMap in LoopMazeState: ");
		//	for (int i = 0; i < LoopMazeState.stateMap.length; i++) {
		//		System.out.println(Arrays.toString(LoopMazeState.stateMap[i]));
		//	}
		//}

        lastAction = a;
        Action act = new Action();
        act.intArray = new int[1];
        act.intArray[0] = a;
        
        agentPrevPosition[0] = agentPosition[0];
        agentPrevPosition[1] = agentPosition[1];
        
        Observation obs = new Observation();
        obs.intArray = agentPosition;
        
        agentPosition = LoopMazeState.sampleNextObs(obs, act).intArray;
    }
    
    public static Observation sampleNextObs(Observation obs, Action act) {
		int[] agentLoc = new int[2];
		agentLoc[0] = obs.intArray[0];
		agentLoc[1] = obs.intArray[1];

		int nextX = agentLoc[0];
		int nextY = agentLoc[1];
		if (act.intArray[0] == 0) { // right
			nextX += agentSpeed;
		}
		if (act.intArray[0] == 1) { // left
			nextX -= agentSpeed;
		}
		if (act.intArray[0] == 2) { // down
			nextY += agentSpeed;
		}
		if (act.intArray[0] == 3) { // up
			nextY -= agentSpeed;
		}
		//double XNoise = randomGenerator.nextGaussian() * transitionNoise * agentSpeed;
		//double YNoise = randomGenerator.nextGaussian() * transitionNoise * agentSpeed;

		//nextX += XNoise;
		//nextY += YNoise;

		nextX = Math.min(nextX, worldDims[0] - 1);
		nextX = Math.max(nextX, 0);
		nextY = Math.min(nextY, worldDims[1] - 1);
		nextY = Math.max(nextY, 0);
		if (!LoopMazeState.isMoveLegal(agentLoc[0], agentLoc[1], nextX, nextY)) {
			nextX = agentLoc[0];
			nextY = agentLoc[1];
		}

		agentLoc[0] = nextX;
		agentLoc[1] = nextY;

		Observation newObs = new Observation();
		newObs.intArray = agentLoc;
		
		return newObs;	
    }

	public static boolean isStateLegal(int x, int y){
		int row = y;
		int col = x;
		
		/** System.out.println("stateMap: ");
		for (int i = 0; i < stateMap.length; i++) {
			System.out.println(Arrays.toString(stateMap[i]));
		}
		System.out.println("isStateLegal(" + x + ", " + y + "): " + (LoopMazeState.stateMap[row][col] == 1)); **/
		if (LoopMazeState.allowSecretPaths)
			return (LoopMazeState.stateMap[row][col] != 0);
		else
			return (LoopMazeState.stateMap[row][col] == 1);
	}
	
	public static boolean isMoveLegal(int currX, int currY, int nextX, int nextY) {
		if (!LoopMazeState.isStateLegal(nextX,nextY))
				return false;
		if (currX != nextX){
			//System.out.println("checking for vert wall at indices: " + currY + ", " + Math.min(nextX, currX));
			if (LoopMazeState.allowSecretPaths)
				return (LoopMazeState.vertWalls[currY][Math.min(nextX, currX)] != 1);
			else
				return (LoopMazeState.vertWalls[currY][Math.min(nextX, currX)] == 0);
		}
		else if (currY != nextY){
			//System.out.println("checking for hor wall at indices: " + Math.min(nextY, currY) + ", " + currX);
			if (LoopMazeState.allowSecretPaths)
				return (LoopMazeState.horWalls[Math.min(nextY, currY)][currX] != 1);
			else
				return (LoopMazeState.horWalls[Math.min(nextY, currY)][currX] == 0);
		}
		return true; // not moving
	}

    public int getLastAction() {
        return lastAction;
    }

    Observation makeObservation() {
        Observation currentObs = new Observation(2, 0);
        currentObs.intArray[0] = getPosition()[0];
        currentObs.intArray[1] = getPosition()[1];

        return currentObs;

    }


	public void loadMapFromFile(String filePath) {
		ArrayList<int[]> stateList = new ArrayList<int[]>();
		try{
			FileReader fr = new FileReader(filePath); 
			BufferedReader br = new BufferedReader(fr);	
			String line;
			while((line = br.readLine()) != null) { 
				char[] rowChars = line.toCharArray();
				int[] stateRow = new int[rowChars.length];
				for (int i = 0; i < rowChars.length; i++) {
					stateRow[i] = Integer.valueOf("" + rowChars[i]).intValue();
				}
				stateList.add(stateRow);
			} 
		}
		catch (Exception e){
			System.err.println("Error in loadMapFromFile(): " + e.getMessage() + "\nExiting.");
			System.exit(0);
		}
		stateMap = stateList.toArray(new int[0][0]);
		setWorldVars();
		this.reset();
	}
}

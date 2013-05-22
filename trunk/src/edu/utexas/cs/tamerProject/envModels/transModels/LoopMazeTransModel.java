package edu.utexas.cs.tamerProject.envModels.transModels;

import java.util.Arrays;
import java.util.Random;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;

/**
 * This class should not be used by new code. Instead, use EnvWrapper with
 * LoopMaze passed to the constructor. 
 * 
 * @author bradknox
 *
 */
public class LoopMazeTransModel extends EnvTransModel {
	
	
	/*
	 * The parameters below MUST match those in the environmental code. 
	 */	
	public static int[][] stateMap = {	{1,1,1,1,1,1},
		{1,0,1,1,1,1},
		{1,0,1,0,0,1},
		{1,0,1,1,1,1},
		{1,0,1,1,1,1},
		{1,1,1,1,1,1}	};

	public static int[][] vertWalls = {	{0,0,0,0,1},
		{1,1,0,0,0},
		{1,1,1,0,1},
		{1,1,0,0,0},
		{1,1,0,0,0},
		{0,0,0,0,0}	};
	public static int[][] horWalls = {	{0,1,1,1,1,0},
		{0,0,0,1,1,0},
		{0,0,0,1,1,0},
		{0,0,0,0,0,0},
		{0,1,1,1,1,0}	};

	
	public static int[] worldDims;
	public static int[] goalLoc;
	private static final int[] defaultInitPosition = {4, 0};

	final private Random randomGenerator;
	long randomSeed = 0;
	
	//These are configurable
	private boolean randomStarts = false;

	// These should be configurable
	final private double agentSpeed = 1.0;
	final public double rewardPerStep = -1.0;
	final public double rewardAtGoal = 0.0;
	


	public LoopMazeTransModel() {
		System.err.println("This class should not be used by new code. " +
				"Instead, use EnvWrapper with LoopMaze passed to the constructor.");
		if (stateMap == null) {
			stateMap = new int[5][5];
			for (int y = 0; y < stateMap.length; y++) {
				for (int x = 0; x < stateMap[0].length; x++) {
					stateMap[x][y] = 1;
				}
			}
		}
		this.setWorldVars();
		//System.out.println("Random starts? " + this.randomStarts);
				

		if (randomSeed == 0) {
			this.randomGenerator = new Random();
		} else {
			this.randomGenerator = new Random(randomSeed);
		}
		//Throw away the first few because the first bits are not that random.
		randomGenerator.nextDouble();
		randomGenerator.nextDouble();
	}
	public void setWorldVars() {
		LoopMazeTransModel.worldDims = new int[2];
		LoopMazeTransModel.worldDims[0] = stateMap[0].length;
		LoopMazeTransModel.worldDims[1] = stateMap.length;
		LoopMazeTransModel.goalLoc = new int[2];
		LoopMazeTransModel.goalLoc[0] = worldDims[0] - 1;
		LoopMazeTransModel.goalLoc[1] = 0;
	}


	
	
	
	
	
	

  
    
    
    public ObsAndTerm getStartObs(){
    	int[] agentPosition = new int[2];
		agentPosition[0] = defaultInitPosition[0];
		agentPosition[1] = defaultInitPosition[1];
		if (randomStarts) {
			do {
				int randStartX = randomGenerator.nextInt(worldDims[0]);
				int randStartY = randomGenerator.nextInt(worldDims[1]);
				agentPosition[0] = randStartX;
				agentPosition[1] = randStartY;
			} while (inGoalRegion(agentPosition) || !LoopMazeTransModel.isStateLegal(agentPosition[0],agentPosition[1]));
		}
		Observation obs = new Observation();
		obs.intArray = agentPosition;
		return new ObsAndTerm(obs, false);
    }

    
    
    /**
     * Mountain Car is deterministic in the version used by this code, so sampling simply 
     *  returns the deterministically caused next observation.
     */
	public ObsAndTerm sampleNextObsNoForceCont(Observation obs, Action act) {
		int[] agentPosition = new int[2];
		agentPosition[0] = obs.intArray[0];
		agentPosition[1] = obs.intArray[1];

		int nextX = agentPosition[0];
		int nextY = agentPosition[1];
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
		if (!LoopMazeTransModel.isMoveLegal(agentPosition[0], agentPosition[1], nextX, nextY)) {
			nextX = agentPosition[0];
			nextY = agentPosition[1];
		}

		agentPosition[0] = nextX;
		agentPosition[1] = nextY;

		Observation newObs = new Observation();
		newObs.intArray = agentPosition;
		
		boolean terminal = false;
		
		/*
         * Check for terminal state
         */
        if (inGoalRegion(agentPosition))
        	terminal = true;
        
		return new ObsAndTerm(newObs, terminal);
	}
	


	public boolean isObsLegal(Observation obs){
		return LoopMazeTransModel.isStateLegal(obs.intArray[0], obs.intArray[1]);
	}
	public boolean isObsTerminal(Observation obs){
		return LoopMazeTransModel.inGoalRegion(obs.intArray);
	}

	public static boolean isStateLegal(int x, int y){
		int row = y;
		int col = x;
		return (LoopMazeTransModel.stateMap[row][col] == 1);
	}

	public static boolean isMoveLegal(int currX, int currY, int nextX, int nextY) {
		if (!LoopMazeTransModel.isStateLegal(nextX,nextY))
			return false;
		if (currX != nextX){
			//System.out.println("checking for vert wall at indices: " + currY + ", " + Math.min(nextX, currX));
			return (LoopMazeTransModel.vertWalls[currY][Math.min(nextX, currX)] == 0);
		}
		else if (currY != nextY){
			//System.out.println("checking for hor wall at indices: " + Math.min(nextY, currY) + ", " + currX);
			return (LoopMazeTransModel.horWalls[Math.min(nextY, currY)][currX] == 0);
		}
		return true; // not moving
	}

	/**
	 * Is the agent in the goal region?
	 * @return
	 */
	public static boolean inGoalRegion(int[] agentPosition) {
		return Arrays.equals(goalLoc, agentPosition);
	}




}

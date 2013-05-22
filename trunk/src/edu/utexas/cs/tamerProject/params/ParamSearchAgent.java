package edu.utexas.cs.tamerProject.params;

import java.util.Arrays;
import java.util.Random;
import java.lang.Double;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;

/**
 * Task specific variables: PARAM_RANGES, PARAM_TEST_LEVELS, rewFloor
 * 
 * @author bradknox
 *
 */

public class ParamSearchAgent implements AgentInterface{
	
	
	private final double[][] PARAM_RANGES_MC = {{0.001, 0.2},{0.5,1.0}, {0.0, 0.6}, {15, 41}, {0.01, 1.0}}; // Mountain car
	private final double[][] PARAM_RANGES_CARTPOLE = {{0.001, 0.2},{0.5,1.0}, {0.0, 0.6}, {5, 12}, {0.01, 0.25}}; // Cartpole
	private final double[][] PARAM_RANGES_ACROBOT = {{0.001, 0.2},{0.5,1.0}, {0.0, 0.6}, {5, 12}, {0.01, 0.25}}; // Acrobot
	private double[][] PARAM_RANGES;
	private int[] PARAM_TEST_LEVELS = {2,2,2,1,2};
	private int NUM_TEST_VALS;
	private double[] bestObservedParams;
	private double bestRewSum = -100000000000.0;
	private double[] currParams;
	
	private int EPS_PER_RUN;
	private double rewFloor = (-130 * this.EPS_PER_RUN) - 4000;
	private int RUNS_PER_PARAM_SET;
	private int numEpsFinshedForRun = 0;
	private int numRunsFinishedForParamSet = 0;
	private int currLevel = 0;
	protected SarsaLambdaAgent rlAgent;
	protected SarsaLambdaAgent rescueRLAgent;
	private Params agentParams;
	private String taskSpec;
	
	//// hard-code the set of parameters to be tested, their bounds, and the level to search to
	private int lastParamI = -1;
	private int currParamI = -1;
	private int numLevelsFinished = 0;
	private double[] paramValsToTest;
	private int currParamTestValI = 0;
	
	private double[] rewSums;

	private Random randGen = new Random();

	private void changeParamVals() {
		// copy from best observed params
		this.currParams = new double[this.bestObservedParams.length];
		for (int i = 0; i < this.bestObservedParams.length; i++) {
			this.currParams[i] = this.bestObservedParams[i];
		}
		// add new param
		this.currParams[this.currParamI] = this.paramValsToTest[this.currParamTestValI];
		System.out.println("Changed param " + this.currParamI + " to " + this.currParams[this.currParamI]);
		
		// the internal discount factor, 
		this.agentParams.stepSize = this.currParams[0]; // the step size for updates, 
		this.agentParams.traceDecayFactor = this.currParams[1]; // lambda for eligibility traces, 
		this.agentParams.selectionParams.put("epsilon", "" + this.currParams[2]); // e for e-greedy action selection, 
		this.agentParams.featGenParams.put("basisFcnsPerDim", "" + (int)this.currParams[3]); // the number of RBF features.
		this.agentParams.featGenParams.put("relWidth", "" + this.currParams[4]); // the variance of the Gaussian RBFs, 
	}
	
	private double[] getCurrParamVec() {
		double[] paramVec = new double[this.PARAM_RANGES.length];
		paramVec[0] = this.agentParams.stepSize; // the step size for updates, 
		paramVec[1] = this.agentParams.traceDecayFactor; // lambda for eligibility traces, 
		paramVec[2] = Double.valueOf(this.agentParams.selectionParams.get("epsilon")).doubleValue(); // e for e-greedy action selection, 
		paramVec[3] = Double.valueOf(this.agentParams.featGenParams.get("basisFcnsPerDim")).doubleValue(); // the number of RBF features.
		paramVec[4] = Double.valueOf(this.agentParams.featGenParams.get("relWidth")).doubleValue(); // the variance of the Gaussian RBFs,
		return paramVec;
	}
	
	public void agent_init(String taskSpec) {
		this.taskSpec = taskSpec;
		String envName = GeneralAgent.getEnvName((new TaskSpec(taskSpec)).getExtraString());
		if (envName.equals("Mountain-Car")){
			PARAM_RANGES = PARAM_RANGES_MC;
			RUNS_PER_PARAM_SET = 5;
			EPS_PER_RUN = 500;
		}
		else if (envName.equals("CartPole")){
			PARAM_RANGES = PARAM_RANGES_CARTPOLE;
			RUNS_PER_PARAM_SET = 2;
			EPS_PER_RUN = 150;
		}
		else if (envName.equals("Acrobot")) {
			PARAM_RANGES = PARAM_RANGES_ACROBOT;
			RUNS_PER_PARAM_SET = 2;
			EPS_PER_RUN = 500;
		}
		else {
			PARAM_RANGES = PARAM_RANGES_CARTPOLE; // this can be the default for now
			RUNS_PER_PARAM_SET = 2;
			EPS_PER_RUN = 100;
		}
		this.rewSums = new double[PARAM_RANGES.length];
		this.NUM_TEST_VALS = PARAM_RANGES.length;
		
		this.rescueRLAgent = new SarsaLambdaAgent();
		this.rescueRLAgent.agent_init(taskSpec);
		this.rlAgent = new SarsaLambdaAgent();
    	this.rlAgent.agent_init(taskSpec);
    	this.agentParams = this.rlAgent.params;
    	
    	//// set parameters in rlAgent
    	this.currParamI = 4;
//    	this.currParamI = this.randGen.nextInt(this.NUM_TEST_VALS);
    	System.out.println("\n\nthis.currParamI: " + this.currParamI);
    	this.setNewTestVals(this.PARAM_RANGES[this.currParamI]);
    	System.out.println("this.PARAM_RANGES[this.currParamI]: " + Arrays.toString(this.PARAM_RANGES[this.currParamI]));
    	this.bestObservedParams = this.getCurrParamVec();
    	System.out.println("Best observed params: " + Arrays.toString(this.bestObservedParams));
    	this.changeParamVals();
    	System.out.println("Current params: " + Arrays.toString(this.currParams));
    	this.rlAgent = new SarsaLambdaAgent();
    	this.rlAgent.params = this.agentParams;
    	this.rlAgent.agent_init(taskSpec);
    }
    
    public Action agent_start(Observation o) {
    	this.rescueRLAgent.agent_start(o);
    	return rlAgent.agent_start(o);
    }

    public Action agent_step(double r, Observation o) {
    	if (this.rlAgent.rewThisEp < -5000) {
    		System.out.print("rescue" + this.rlAgent.stepsThisEp);
    		return this.rescueRLAgent.agent_step(r, o);
    	}
    	else
    		return this.rlAgent.agent_step(r,o);
    }
    
    public void agent_end(double r) {
    	if (this.rlAgent.rewThisEp < -5000){
    		this.rescueRLAgent.agent_end(r);
    		this.rlAgent.totalRew += this.rewFloor;
    	}
    	else
    		this.rlAgent.agent_end(r);
    	this.numEpsFinshedForRun++;
    	if (this.rlAgent.totalRew < this.rewFloor) {
    		System.out.println("REW_FLOOR: " + this.rewFloor);
    		System.out.println("Total reward: " + this.rlAgent.totalRew);
    	}
    	if (this.numEpsFinshedForRun % this.EPS_PER_RUN == 0 ||
    			this.rlAgent.totalRew < this.rewFloor) {
    		this.numRunsFinishedForParamSet++;
    		this.runEnd();
    		this.numEpsFinshedForRun = 0;
    	}
    }
	
    private void runEnd() {
    	//// add reward to array that keeps tally
    	this.rewSums[currParamTestValI] += this.rlAgent.totalRew;
    	System.out.println("rewSums: " + Arrays.toString(this.rewSums));
    	
    	if ((this.numRunsFinishedForParamSet % this.RUNS_PER_PARAM_SET) == 0){
    		this.numRunsFinishedForParamSet = 0;
    		//// move to next param val; if that would go outofbounds, move to 0 and check for a new level
    		this.currParamTestValI++;
    		if (currParamTestValI >= this.paramValsToTest.length){
    			this.currParamTestValI = 0;
    			int maxValI = this.getIndexOfMaxVal(this.rewSums);
    			double rewSum = this.rewSums[maxValI];
    			rewSums = new double[this.PARAM_RANGES.length];
    			this.numLevelsFinished++;
    			double[] inclusiveParamBounds = new double[2];
    			
    			if (this.numLevelsFinished < this.PARAM_TEST_LEVELS[this.currParamI]){
    					
    				//// new param bounds from best param val
    				if (maxValI == 0) {
    					inclusiveParamBounds[0] = this.paramValsToTest[maxValI];
    					inclusiveParamBounds[1] = this.paramValsToTest[maxValI] + 
    							((this.paramValsToTest[maxValI + 1] -  this.paramValsToTest[maxValI]) * 
    							 (this.NUM_TEST_VALS / (this.NUM_TEST_VALS + 1.0)));
    				}
    				else if (maxValI == this.NUM_TEST_VALS - 1) {
    					inclusiveParamBounds[0] =  this.paramValsToTest[maxValI - 1] + 
    							((this.paramValsToTest[maxValI] -  this.paramValsToTest[maxValI - 1]) * 
    							 (1.0 / (this.NUM_TEST_VALS + 1.0)));
    					inclusiveParamBounds[1] = this.paramValsToTest[this.NUM_TEST_VALS - 1];
    				}
    				else { 
    					inclusiveParamBounds[0] =  this.paramValsToTest[maxValI - 1] + 
								((this.paramValsToTest[maxValI] -  this.paramValsToTest[maxValI - 1]) * 
								 (1.0 / (this.NUM_TEST_VALS + 2.0)));
    					inclusiveParamBounds[1] =  this.paramValsToTest[maxValI] + 
    							((this.paramValsToTest[maxValI + 1] -  this.paramValsToTest[maxValI]) * 
    							 ((this.NUM_TEST_VALS + 1.0) / (this.NUM_TEST_VALS + 2.0)));
    				}
    			}
    			else { // if levels are finished, 
    				//// save best observed param (MAYBE ONLY IF IT BREAKS PREVIOUS BEST SCORE?)
    				System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    				if (rewSum > this.bestRewSum)	{
    					this.bestObservedParams[this.currParamI] = this.paramValsToTest[maxValI];
    					this.bestRewSum = rewSum;
    					System.out.println("\n\n\n\n\nNew best reward sum: " + rewSum + "\n\n\n");
    					if (this.bestRewSum < 0)
    						this.rewFloor = (this.bestRewSum / this.RUNS_PER_PARAM_SET) * 1.3;
    				}
    					
    				// pick new param to test
    				int newParamI = this.randGen.nextInt(this.NUM_TEST_VALS);
    				while ((newParamI == this.currParamI) || (newParamI == this.lastParamI))
    					newParamI = this.randGen.nextInt(this.NUM_TEST_VALS);
    				
    				this.lastParamI = this.currParamI;
    				this.currParamI = newParamI;
    				
					inclusiveParamBounds[0] = this.PARAM_RANGES[this.currParamI][0];
					inclusiveParamBounds[1] = this.PARAM_RANGES[this.currParamI][1];
    				
    				this.numLevelsFinished = 0;
    			}
    		
    			this.setNewTestVals(inclusiveParamBounds);
    			
    		}
    	}
    	
    	//// reset agent, possibly with new params
    	System.out.println("\n\nBest observed params: " + Arrays.toString(this.bestObservedParams));
    	this.changeParamVals();
    	System.out.println("this.getCurrParamVec(): " + Arrays.toString(this.getCurrParamVec()));
    	System.out.println("Current params: " + Arrays.toString(this.currParams));
    	this.rlAgent = new SarsaLambdaAgent();
    	this.rlAgent.params = this.agentParams;
    	this.rlAgent.agent_init(taskSpec);
    	
    }
    
    private static int getIndexOfMaxVal(double[] vals) {
    	int maxI = 0;
    	System.out.println("starting max at 0: " + vals[maxI]);
    	for (int i = 1; i < vals.length; i++) {
    		if (vals[i] > vals[maxI]) {
    			maxI = i;
    			System.out.println("new max at " + i + ": " + vals[maxI]);
    		}
    		else{
    			System.out.println("val at " + i + " not a new max: " + vals[i]);
    		}
    	}
    	return maxI;
    }
    

    
    private void setNewTestVals(double[] inclusiveParamBounds){
		//// with new param bounds, create an array of params to test
    	this.paramValsToTest = new double[this.NUM_TEST_VALS];
		for (int i = 0; i < this.NUM_TEST_VALS; i++){
			this.paramValsToTest[i] = inclusiveParamBounds[0] + ((inclusiveParamBounds[1] - inclusiveParamBounds[0]) 
							* ((double)i / (this.NUM_TEST_VALS - 1)));
		}
		System.out.println("this.paramValsToTest: " + Arrays.toString(this.paramValsToTest));
	}

    
    public String agent_message(String theMessage) {
    	return null
    	;
    }
    
    public void agent_cleanup() {
    	this.rlAgent.agent_cleanup();
    }
    
    public static void main(String[] args) {
        AgentLoader L=new AgentLoader(new ParamSearchAgent());
        L.run();
    }
	
    

		
	
}
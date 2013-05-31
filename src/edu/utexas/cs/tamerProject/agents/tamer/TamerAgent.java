/*
Adapted by Brad Knox from RandomAgent.java by Brian Tanner

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
package edu.utexas.cs.tamerProject.agents.tamer;

import java.util.Arrays;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;

import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.agents.CreditAssignParamVec;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.HLearner;
import edu.utexas.cs.tamerProject.trainInterface.TrainerListener;
import edu.utexas.cs.tamerProject.utils.Stopwatch;


/**
 * This class flexibly implements a TAMER agent, which learns from human 
 * reward, input by keyboard.  
 * 
 * @author bradknox
 *
 */
public class TamerAgent extends GeneralAgent implements AgentInterface {

	public HLearner hLearner;
	protected double lastStepStartTime;

	public TrainerListener trainerListener;

	/*
	 * Time of agent pause at end of episode in milliseconds, where
	 * agent simply waits to finish agent_end(), which TinyGlueExtended
	 * will wait for. This pause can be used to allow the trainer to 
	 * add reward or punishment at the ends of episodes. 
	 * 
	 *  *** When possible, create the pause in RunLocalExperiment
	 *  instead through its static variable PAUSE_DUR_AFTER_EP. ****
	 */
	public int EP_END_PAUSE = 0; //2000; /
	public static boolean verifyObsFitsEnvDesc = true;
	
	
    public static void main(String[] args){
    	TamerAgent agent = new TamerAgent();
    	agent.processPreInitArgs(args);
    	if (agent.glue) {
        	AgentLoader L=new AgentLoader(agent);
        	L.run();
    	}
    	else {
    		agent.runSelf();
    	}
    }  
    
	public void processPreInitArgs(String[] args) {
		System.out.println("\n[------Tamer process pre-init args------] " + Arrays.toString(args));
		super.processPreInitArgs(args);
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];
    		if (argType.equals("-tamerModel") && (i+1) < args.length){
    			if (args[i+1].equals("linear")) {
    				System.out.println("Setting model to linear model");
    				this.params.featClass = "FeatGen_RBFs";
    				this.params.modelClass = "IncGDLinearModel";
    				
    				// These fit the RBF class that was tested to give identical output with that of the python code
    				this.params.featGenParams.put("basisFcnsPerDim", "40");
    				this.params.featGenParams.put("relWidth", "0.08");
    				this.params.featGenParams.put("biasFeatVal", "0.1");
    				this.params.featGenParams.put("normMin", "-1");
    				this.params.featGenParams.put("normMax", "1");
   					
   					// Learning params
    				this.params.initModelWSamples = false;
    				this.params.initWtsValue = 0.0;
    				this.params.stepSize = 0.001; // matches python code
    			}
    			else if (args[i+1].equals("kNN")) {
    				this.params.modelClass = "WekaModelPerActionModel";
    				this.params.featClass = "FeatGen_NoChange";
    				this.params.initModelWSamples = false; //// no biasing in MC for ALIHT paper and ICML workshop paper
    				this.params.numBiasingSamples = 100;
    				this.params.biasSampleWt = 0.1;
    				this.params.wekaModelName = "IBk"; //// IBk for ALIHT paper and ICML workshop paper
    			}
    			else {
    				System.out.println("\nIllegal TamerAgent model type. Exiting.\n\n");
    				System.exit(1);
    			}
    			
    			System.out.println("agent model set to: " + args[i+1]);
    		}
    		else if (argType.equals("-credType") && (i+1) < args.length){
    			if (args[i+1].equals("aggregate")) {
    				this.params.delayWtedIndivRew = false;
    				this.params.noUpdateWhenNoRew = false;
    			}
    			else if (args[i+1].equals("aggregRewOnly")) {
    				this.params.delayWtedIndivRew = false;
    				this.params.noUpdateWhenNoRew = true;    				
    			}
    			else if (args[i+1].equals("indivAlways")) {
    				this.params.delayWtedIndivRew = true;
    				this.params.noUpdateWhenNoRew = false;
    			}
    			else if (args[i+1].equals("indivRewOnly")) {
    				this.params.delayWtedIndivRew = true;
    				this.params.noUpdateWhenNoRew = true;    				
    			}
    			else{
    				System.out.println("\nIllegal TamerAgent credit assignment type. Exiting.\n\n");
    				System.exit(1);
    			}
    			System.out.println("agent.credType set to: " + args[i+1]);
    		}
		}
	}

	public void receiveKeyInput(char c){
		super.receiveKeyInput(c);
		System.out.println("TamerAgent receives key: " + c);
		if (c == '/') {
			this.addHRew(1.0);
		}
		else if (c == 'z') {
			this.addHRew(-1.0);
		}
		else if (c == '?') {
			this.addHRew(10.0);
		}
		else if (c == 'Z') {
			this.addHRew(-10.0);
		}
		else if (c == ' ' && this.allowUserToggledTraining) {
			this.toggleInTrainSess();
			this.hLearner.credA.setInTrainSess(Stopwatch.getComparableTimeInSec(), this.inTrainSess);
		}
		else if (c == 'S') {
			this.model.saveDataAsArff(this.envName, (int)Stopwatch.getWallTimeInSec(), "");
		}
		
//		System.out.println("hRewList after key input: " + this.hRewList.toString());
	}
    
	public void initRecords() {
		super.initRecords();
		if (this.hLearner != null)
			this.hLearner.clearHistory();
		this.lastStepStartTime = -10000000;
	}
  

    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());
        return p;
    }
    
    
    
    
	// Called when the environment is loaded (when "Load Experiment" is clicked in RLViz)
    public void agent_init(String taskSpec) {
    	GeneralAgent.agent_init(taskSpec, this);
		
		//// CREATE CreditAssignParamVec
		CreditAssignParamVec credAssignParams = new CreditAssignParamVec(this.params.distClass, 
														this.params.creditDelay, 
														this.params.windowSize,
														this.params.extrapolateFutureRew,
														this.params.delayWtedIndivRew,
														this.params.noUpdateWhenNoRew);
		
		//// INITIALIZE TAMER
		this.hLearner = new HLearner(this.model, credAssignParams);
		
		this.actSelector = new ActionSelect(this.model, this.params.selectionMethod, 
											this.params.selectionParams, this.currObsAndAct.getAct().duplicate());


		
		//LogTrainer.trainOnLog("/Users/bradknox/rl-library/data/cartpole_tamer/recTraj-wbknox-tamerOnly-1295030420.488000.log", this);
		if (!GeneralAgent.isApplet && enableGUI) {
			//Schedule a job for event dispatch thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					trainerListener = TrainerListener.createAndShowGUI(TamerAgent.this);
				}
			});
		}
		if (this.actSelector.getRewModel() == null)
			this.actSelector.setRewModel(this.model);
		this.endInitHelper();
    }
    



    


	// Called at the beginning of each episode (in RLViz, it's first called when "Start" is first clicked)
    public Action agent_start(Observation o, double time, Action predeterminedAct) {
    	//System.out.println("---------------------------start TAMER ep " + this.currEpNum);
    	// System.out.println("\n\n------------new episode-----------");
		this.startHelper();
		
		//this.hLearner.newEpisode();	 //// CLEAR HISTORY and do any other set up
		this.lastStepStartTime = -10000000; // should cause a big problem if it's used during the first time step (which shouldn't happen)
		
        return agent_step(0.0, o, time, predeterminedAct);
    }
    
    
    

    public Action agent_step(double r, Observation o, double startTime, Action predeterminedAct) {
    	return agent_step(r, o, startTime, predeterminedAct, this.lastObsAndAct.getAct());
    }
    
    public Action agent_step(double r, Observation o, double startTime, Action predeterminedAct, Action tieBreakAction) {
    	//System.out.println("\n-----------------Tamer step---------------\n");
    	//System.out.println("Training? " + this.inTrainSess);
    	//System.out.println("Tamer obs: " + Arrays.toString(o.intArray));
    	if (verifyObsFitsEnvDesc)
    		this.checkObs(o);
    	//System.out.println("rew list in TAMER: " + this.hRewList.toString());
    	this.stepStartTime = startTime;
		this.stepStartHelper(r, o); // this.stepStartTime (set in stepStartHelper()) ends last step and starts new step
		//System.out.println("TAMER this.stepStartTime: " + String.format("%f", this.stepStartTime));
    	this.hLearner.recordTimeStepEnd(startTime);
//    	if (this.stepsThisEp > 1)
//    		System.out.println("Tamer feats for last obs-act: " + Arrays.toString(this.featGen.getFeats(o, this.lastObsAndAct.getAct())));
    	
    	/*
    	 * PROCESS PREVIOUS TIME STEP
    	 */
//		if (this.stepsThisEp == 2)
			//System.out.println("Predicted human reward for last step in TAMER: " + this.getVal(this.lastObsAndAct));
		processPrevTimeStep(this.stepStartTime);
		this.hLearner.processSamples(startTime, inTrainSess);
		
		/*
		 *  GET ACTION
		 */
		this.currObsAndAct.setAct(predeterminedAct);
		//System.out.print("tamerAgent ");
		if (this.currObsAndAct.actIsNull()) {
			this.currObsAndAct.setAct(this.actSelector.selectAction(o, tieBreakAction));
		}
//		else { // TODO this is for debugging only. it should be removed.
//			this.actSelector.selectAction(o, tieBreakAction);
//		}
    	
//		if (this.stepsThisEp == 399)
//			System.out.println("TAMER act vals: " + Arrays.toString(this.model.getStateActOutputs(o, this.model.getPossActions(o))));
		
		this.lastStepStartTime = this.stepStartTime;
		//if (this.currObsAndAct.getAct().intArray.length > 0)
		//	System.out.println("TAMER action: " + this.currObsAndAct.getAct().intArray[0]);
//		if (this.lastAct != null)
//			System.out.println("TAMER last action: " + this.lastAct.intArray[0]);
		this.stepEndHelper(r, o);
		if (this.isTopLevelAgent) // If not top level, TamerAgent's chosen action might not be the actual action. This must be called by the primary class.
			this.hLearner.recordTimeStepStart(this.featGen.getFeats(o, this.currObsAndAct.getAct()), startTime);
		
		return this.currObsAndAct.getAct();
    }

    
    
    
    public void agent_end(double r, double time) {
    	this.stepStartTime = time;
    	this.endHelper(r);
		//// PROCESS PREVIOUS TIME STEP
		processPrevTimeStep(this.stepStartTime);
    	this.actSelector.anneal();
    	GeneralAgent.sleep(EP_END_PAUSE);
    }

    
    
    
	protected void processPrevTimeStep(double borderTime){ // if this does RL, it will need more: the observation and last reward
		//// GET FEATURES OF PREVIOUS TIME STEP 
//		if (this.stepsThisEp > 1) {
//			double[] feats = this.featGen.getFeats(this.lastObs, this.lastAct);
			//		System.out.println("state-action feats from last step: " + Arrays.toString(feats));
			//		System.out.println("inTrainSess: " + inTrainSess);

			//// RECORD LAST TIME STEP (must be recorded after action chosen to have state-action features)
//			this.hLearner.recordTimeStep(feats, this.lastStepStartTime);
//		}

		
		if (inTrainSess) //// UPDATE
			this.hLearner.processHRew(this.hRewThisStep);

		
		if (verbose)
			System.out.println("hRewThisStep: " + this.hRewThisStep.toString());
	}
    

    public void agent_cleanup() {
        
    }




	
	private void getHandCodedHRew(){
		if ((this.lastObsAndAct.getObs().doubleArray[1] > 0 && this.lastObsAndAct.getAct().intArray[0] == 2) ||
			(this.lastObsAndAct.getObs().doubleArray[1] <= 0 && this.lastObsAndAct.getAct().intArray[0] == 0))
			this.addHRew(1.0);
//			this.hRewThisStep = 1.0;
		else
			this.addHRew(-1.0);
//			this.hRewThisStep = -1.0;
		System.out.println("\thRewThisStep: " + hRewThisStep.toString());
	}

}



/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 */
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "General TAMER Agent";
    }

    public String getShortName() {
        return "Tamer Agent";
    }

    public String getAuthors() {
        return "Brad Knox";
    }

    public String getInfoUrl() {
        return "http://www.cs.utexas.edu/~bradknox";
    }

    public String getDescription() {
        return "RL-Library Java Version of a general Tamer agent.";
	}
}


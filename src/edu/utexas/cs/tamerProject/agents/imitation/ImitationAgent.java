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
package edu.utexas.cs.tamerProject.agents.imitation;

import java.util.Arrays;
import java.util.Date;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import rlVizLib.general.hasVersionDetails;

import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.HLearner;
import edu.utexas.cs.tamerProject.agents.CreditAssignParamVec;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.trainInterface.TrainerListener;
import edu.utexas.cs.tamerProject.utils.Stopwatch;

// TODO Separate ImitationAgent from TamerAgent, maybe moving common code to a TeachableAgent abstract parent class


/**
 * ImitationAgent does learning from demonstration by policy regression. A human
 * controls the agent and can switch off training at any time to see the learned
 * policy. 
 * 
 * @author bradknox
 *
 */
public class ImitationAgent extends TamerAgent{

	public int lastUserActI;
	public boolean okayToHang = true;
	public boolean takesHRew = false;

	/**
	 * Turns off the ability for the agent to choose actions, making this simply a
	 * heavyweight control interface for the agent. This is useful when you want to
	 * familiarize a trainer with the task before they train. 
	 */
	private boolean controlOnly = false; // i.e., no autonomy
    
    public static void main(String[] args){
    	ImitationAgent agent = new ImitationAgent();
    	agent.processPreInitArgs(args);
    	if (agent.glue) {
        	AgentLoader L=new AgentLoader(agent);
        	L.run();
    	}
    	else {
    		agent.runSelf();
    	}
    }  

    public void setControlOnlyBeforeStart(boolean controlOnly) {
    	this.controlOnly = controlOnly;
    	if (controlOnly)
    		this.TRAINING_BY_DEFAULT = true;
    }
    public boolean isControlOnly(){
    	return this.controlOnly;
    }
    
	public void receiveKeyInput(char c){
		//System.out.println("Key input rec'd by ImitationAgent: " + c);
		if (c == ' ' && !this.controlOnly && this.allowUserToggledTraining) {
			this.toggleInTrainSess();
			this.hLearner.credA.setInTrainSess(Stopwatch.getComparableTimeInSec(), this.inTrainSess);
		}
		else if (this.envName.equals("Puddle-World") ||
			this.envName.equals("Grid-World") ||
			this.envName.equals("Loop-Maze") ){
			if (c == 'j')
				this.lastUserActI = 1; // left
			else if (c == 'k')
				this.lastUserActI = 2; // down
			else if (c == 'l')
				this.lastUserActI = 0; // right
			else if (c == 'i')
				this.lastUserActI = 3; // up		
		}
		else if (this.envName.equals("NexiNav") ){
			if (c == 'j')
				this.lastUserActI = 2; // left
			else if (c == 'k')
				this.lastUserActI = 0; // stay
			else if (c == 'l')
				this.lastUserActI = 3; // right
			else if (c == 'i')
				this.lastUserActI = 1; // forward
		}
		else if (this.envName.equals("SimpleBot") ){
			if (c == 'k')
				this.lastUserActI = 0; // stay
			else if (c == 'i')
				this.lastUserActI = 1; // up
			else if (c == 'm')
				this.lastUserActI = 2; // down
			else if (c == 'l')
				this.lastUserActI = 3; // right
			else if (c == 'j')
				this.lastUserActI = 4; // left	
		}
		
		else if (this.envName.equals("Mario")){
			System.out.println("Mario input");
			if (c == 'j')
				this.lastUserActI = 1; // run left
			else if (c == 'k')
				this.lastUserActI = 4; // stop
			else if (c == 'l')
				this.lastUserActI = 9; // run right
			else if (c == 'u')
				this.lastUserActI = 3; // up left
			else if (c == 'i')
				this.lastUserActI = 7; // up center
			else if (c == 'o')
				this.lastUserActI = 11; // up right				
		}
		else if (this.envName.equals("CartPole")){
			if (c == 'j')
				this.lastUserActI = 0; // accelerate left
			else if (c == 'l')
				this.lastUserActI = 1; // accelerate right
		}
		else if (this.envName.equals("Mountain-Car")){
			if (c == 'j')
				this.lastUserActI = 0; // accelerate left
			else if (c == 'k')
				this.lastUserActI = 1; // don't accelerate
			else if (c == 'l')
				this.lastUserActI = 2; // accelerate right
		}
		else {
			if (c == '/') {
				this.lastUserActI = this.featGen.theActIntRanges[0][1]; // do the maximum act number
			}
			else if (c == 'z') {
				this.lastUserActI = this.featGen.theActIntRanges[0][0]; // do the minimum act number
			}
		}
	}

	
	// Called when the environment is loaded (when "Load Experiment" is clicked in RLViz)
    public void agent_init(String taskSpec) {
    	this.lastUserActI = -1;
		GeneralAgent.agent_init(taskSpec, this);

		//// CREATE CreditAssignParamVec
		CreditAssignParamVec credAssignParams = new CreditAssignParamVec(this.params.distClass, 
				this.params.creditDelay, 
				this.params.windowSize,
				this.params.extrapolateFutureRew,
				this.params.delayWtedIndivRew,
				this.params.noUpdateWhenNoRew);

		//// INITIALIZE HUMAN MODEL
		this.hLearner = new HLearner(model, credAssignParams);

		this.actSelector = new ActionSelect(this.model, this.params.selectionMethod, 
				this.params.selectionParams, this.currObsAndAct.getAct().duplicate());
		
		//LogTrainer.trainOnLog("/Users/bradknox/rl-library/data/cartpole_tamer/recTraj-wbknox-tamerOnly-1295030420.488000.log", this);
		if (!isApplet && enableGUI) {
			//Schedule a job for event dispatch thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					TrainerListener.createAndShowGUI(ImitationAgent.this);
				}
			});
		}
		
		this.endInitHelper();
	}

    public Action agent_step(double r, Observation o, double startTime, Action predeterminedAct) {
    	this.stepStartTime = startTime;
		//// GET BORDER TIME (ends last step and starts new step)
		this.stepStartHelper(r, o);
        //if (this.stepsThisEp % 500 == 0)
//		System.out.println("\nAgent on step: " + this.stepsThisEp);
//
//		System.out.println("o.intArray: " + Arrays.toString(o.intArray));
//		System.out.println("o.doubleArray: " + Arrays.toString(o.doubleArray));
		
		//// PROCESS PREVIOUS TIME STEP
		if (this.stepsThisEp > 1)
			processPrevTimeStep(this.stepStartTime);
		//// GET GREEDY ACTION OR USER ACTION
		//System.out.println("lastUserActI: " + lastUserActI);
		//System.out.println("inTrainSess: " + inTrainSess);
		//System.out.println("predeterminedAct: " + predeterminedAct);
		if (predeterminedAct == null) { 
			this.currObsAndAct.setAct(this.actSelector.selectAction(o, this.lastObsAndAct.getAct()));
			if (this.inTrainSess) {
				while (this.lastUserActI == -1 && this.okayToHang) {
					//System.out.println("Push an action button (j, k, or l) to start");
					GeneralAgent.sleep(500); // pause for 1/2 of a second
				}
				if (this.lastUserActI == -1)
					this.lastUserActI = 0; //// assuming 0 is an okay default action
				// lastUserActI now set
				this.currObsAndAct.setAct(this.featGen.actList.getActionList().get(this.lastUserActI));
				this.lastObsAndAct.setAct(this.currObsAndAct.getAct().duplicate()); //TODO this makes the current and last acts the same... check if there's a good reason for this
//				System.out.print("model's action overridden by user. ");
			}
		}
		else {
			this.lastUserActI = this.featGen.getActIntIndex(predeterminedAct.intArray);
			this.currObsAndAct.setAct(predeterminedAct);
		}

		this.lastStepStartTime = this.stepStartTime;
		this.stepEndHelper(r, o);
//		System.out.println("act chosen: " + this.action.intArray[0]);
        return this.currObsAndAct.getAct();
    }



	protected void processPrevTimeStep(double borderTime){ // if this does RL, it will need more: the observation and last reward
		if (inTrainSess && this.lastUserActI != -1) {
//			System.out.print("LbD update  ");
			for (Action act: this.featGen.actList.getActionList()) {
				int[] actIntArray = act.intArray;
				double label = (Arrays.equals(this.lastObsAndAct.getAct().intArray,actIntArray)) ? 1.0 : 0.0;
//				System.out.println("---Arrays.equals(this.lastAct.intArray,actIntArray): " + Arrays.equals(this.lastAct.intArray,actIntArray));
//				System.out.println("this.lastAct.intArray: " + this.lastAct.intArray);
//				System.out.println("actIntArray: " + actIntArray);
				double[] feats = this.featGen.getSAFeats(this.lastObsAndAct.getObs(), act);
//				System.out.println("label: " + label);
				//System.out.println("state-action feats from last step: " + Arrays.toString(feats));
				this.model.addInstance(new Sample(feats, label, 1.0));
			}
			this.model.buildModel();
		}		

	}
		
    public void agent_end(double reward, double time) {
    	super.agent_end(reward, time);
		if (this.stepsThisEp > 1)	//// PROCESS PREVIOUS TIME STEP
			processPrevTimeStep(this.stepStartTime);
		this.lastUserActI = -1;
	}
}

/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 */
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "General Imitation Agent";
    }

    public String getShortName() {
        return "Imitation Agent";
    }

    public String getAuthors() {
        return "Brad Knox";
    }

    public String getInfoUrl() {
        return "http://www.cs.utexas.edu/~bradknox";
    }

    public String getDescription() {
        return "RL-Library Java Version of a general imitation (LfD) agent, which bootstraps off of the TAMER code..";
	}
}


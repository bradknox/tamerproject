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
package edu.utexas.cs.tamerProject.agents.sarsaLambda;


import java.util.Arrays;
import java.util.HashMap;


import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;

import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.modeling.CombinationModel;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.IncModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;



public class SarsaLambdaAgent extends GeneralAgent {

	public RegressionModel qAugModel;
	public boolean takesHRew = false;


	public void processPreInitArgs(String[] args) {
		super.processPreInitArgs(args);
		System.out.println("\n[------Sarsa process pre-init args------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];
    		if ((argType.equals("-initialValue")) && (i+1) < args.length){
    			if (args[i+1].equals("zero")) 
    				this.params.initWtsValue = 0;
    			else {
    				System.out.println("\nIllegal SarsaAgent initial values type. Exiting.\n\n");
    				System.exit(1);
    			}
				System.out.println("Sarsa's Q-model's initial weights set to: " 
									+ this.params.initWtsValue);
			}
		}
	}
	public void processPostInitArgs(String[] args) {
		System.out.println("\n[------process post-init args------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];

    		if ((argType.equals("-discountFactor")) && (i+1) < args.length){
    			double discountFactor = Double.valueOf(args[i+1]);
    			setDiscountFactorForLearning(discountFactor); // override env discount factor for TD updates
    			actSelector.setDiscountParam(ActionSelect.discFactorToParam(discountFactor)); // override for planning-based action selection
				System.out.println("discount factor set to: " + discountFactor);
			}
		}
	}

    
    private void test(){
    	this.agent_init("VERSION RL-Glue-3.0 PROBLEMTYPE episodic DISCOUNTFACTOR 1.0 OBSERVATIONS DOUBLES (0.0 2.0)  " +
		"ACTIONS INTS (0 0)  REWARDS (-1.0 10.0)  EXTRA EnvName:HandFed");
		Observation o = new Observation();
		o.doubleArray = new double[1];
		o.doubleArray[0] = 0;
		this.agent_start(o);
		double reward = 10;
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 3; j++) {
				o.doubleArray[0] = j;
				this.agent_step(reward, o);
			}
		}
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n-------------------------------------\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		reward = -1;
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 3; j++) {
				o.doubleArray[0] = j;
				this.agent_step(reward, o);
			}
		}
    }

    
	// Called when the environment is loaded (when "Load Experiment" is clicked in RLViz)
    public void agent_init(String taskSpec) {
    	GeneralAgent.agent_init(taskSpec, this); // will sometimes be redundant

    	System.out.println("rlAgent params: " + params.toOneLineStr());
		//// INITIALIZE ActionSelect
//		String selectionMethod = "e-greedy"; // implemented greedy and e-greedy
//		HashMap<String,String> selectionParams = new HashMap<String,String>();
//		selectionParams.put("epsilon", "0.03"); // 0.2 works well for Sarsa alone in several enviros
//		selectionParams.put("annealRate", "0.9995");
    	System.out.println("model being given to actselect" + this.model);
		this.actSelector = new ActionSelect(this.model, this.params.selectionMethod, 
											this.params.selectionParams, this.currObsAndAct.getAct().duplicate());
		this.qAugModel = null;
    	this.numEpsBeforePause = -1;
		this.endInitHelper();
    }


    
    

	// Called at the beginning of each episode (in RLViz, it's first called when "Start" is first clicked)
    public Action agent_start(Observation o, double time, Action predeterminedAct) {
    	this.startHelper();
        return agent_step(0.0, o, time, predeterminedAct);
    }


    
    
    public Action agent_step(double r, Observation o, double startTime, Action predeterminedAct) {
    	//System.out.println("\n-----------------Sarsa step---------------\n");
    	this.stepStartTime = startTime;
//    	if (!this.isTopLevelAgent)
//    		System.out.println("Apparent reward: " + r);
    	this.stepStartHelper(r, o);
    	
//    	if (this.stepsThisEp == 10)
//    		System.out.println("\nobservation: " + Arrays.toString(o.doubleArray) + ", reward: " + r + 
    	//		", discountFactorForLearning: " + this.discountFactorForLearning.getValue());
		//// GET ACTION
    	if (predeterminedAct == null)
    		this.currObsAndAct.setAct(this.actSelector.selectAction(o, this.lastObsAndAct.getAct()));
		else {
			this.currObsAndAct.setAct(predeterminedAct);
		}
    	//if (this.stepsThisEp == 399)
    		//System.out.println("Sarsa act vals: " + Arrays.toString(this.model.getStateActOutputs(o, this.model.getPossActions(o))));
		 

		//// PROCESS PREVIOUS TIME STEP
		processPrevTimeStep(r, o);
		
		//System.out.println("SARSA action: " + this.action.intArray[0]);
//		if (this.lastAct != null)
//			System.out.println("SARSA last action: " + this.lastAct.intArray[0]);
		this.stepEndHelper(r, o);
        return this.currObsAndAct.getAct();
    }


    

    public void agent_end(double r, double time) {
    	this.stepStartTime = time;
    	this.endHelper(r);
		//// PROCESS PREVIOUS TIME STEP
    	processPrevTimeStep(r, null);
    	this.actSelector.anneal();
    }


    
    
    
	private void processPrevTimeStep(double r, Observation o) {
		if (this.stepsThisEp <= 1) // there is no previous time step
			return;
		double thisSAVal = 0.0;
		double supplModelWt = 0.0;
		if (this.qAugModel != null)
			supplModelWt = ((CombinationModel)this.actSelector.valFcnModel).hInf.getHInfluence(this.lastObsAndAct.getObs(), 
																					this.lastObsAndAct.getAct()); //.modelWts.get(1);
		if (o != null) { // i.e., not a terminating state
			thisSAVal = model.predictLabel(o, this.currObsAndAct.getAct());
			//System.out.println("thisSAVal w/o augmentation: " + thisSAVal);
			if (this.qAugModel != null) {
					thisSAVal += supplModelWt * this.qAugModel.predictLabel(o, this.currObsAndAct.getAct()); 
					//System.out.println("suppl contribution to thisSAVal: " + supplModelWt * this.qAugModel.predictLabel(o.intArray, o.doubleArray, 
					//   this.action.intArray, this.action.doubleArray)); 
			}
			//System.out.println("thisSAVal: " + thisSAVal);
		}
		
		/*
		 * GET FEATURES OF PREVIOUS TIME STEP 
		 */
		double[] lastStepFeats = this.featGen.getFeats(this.lastObsAndAct.getObs(), this.lastObsAndAct.getAct());
		//System.out.println("last obs and act: " + Arrays.toString(this.lastObsAndAct.getObs().doubleArray) + ", "
		//										+ Arrays.toString(this.lastObsAndAct.getAct().intArray));
		//System.out.println("current obs and act: " + Arrays.toString(o.doubleArray) + ", "
		//		+ Arrays.toString(this.currObsAndAct.getAct().intArray));

		/*
		 * CREATE SAMPLE with lastStepFeats and reward plus discounted thisSAVal
		 */
		Sample lastStepSample = new Sample(lastStepFeats,
									(r + (this.discountFactorForLearning.getValue() * thisSAVal)), 1.0);
		//System.out.println("Sarsa sample label: " + lastStepSample.label);
		
		/*
		 * Update Q
		 */
		if (this.qAugModel != null) {
			double augmentingPrediction = this.qAugModel.predictLabel(this.lastObsAndAct.getObs(), this.lastObsAndAct.getAct());
			((IncModel)this.model).addInstance(lastStepSample, augmentingPrediction * supplModelWt);
			//System.out.println("supplModelWt: " + supplModelWt);
			//System.out.println("augmenting pred in Sarsa: " + augmentingPrediction);
		}
		else {
			this.model.addInstance(lastStepSample);
		}
	}
	
    public static void main(String[] args){
    	SarsaLambdaAgent agent = new SarsaLambdaAgent();
    	agent.processPreInitArgs(args);
//    	agent.test();
        AgentLoader L=new AgentLoader(agent);
        L.run();
    }
	
}

/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 */
class DetailsProvider implements hasVersionDetails {
    public String getName() {return "Sarsa(Lambda) Agent";}
    public String getShortName() {return "Sarsa(Lambda) Agent";}
    public String getAuthors() {return "Brad Knox";}
    public String getInfoUrl() {return "http://www.cs.utexas.edu/~bradknox";}
    public String getDescription() {return "RL-Glue Java Version of a general Sarsa(Lambda) agent.";}
}



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

Common usage:
java -cp TamerProject.jar edu.utexas.cs.tamerProject.agents.combo.TamerRLAgent -expName cartpole_consecTAMERRL -unique 9%3.0%8 -\
combMethod 9 -combParam 3.0 -logPath /v/filer4b/v28q002/villasim/knox/rlglue-3.02/rl-library/data/cartpole_tamer/recTraj-wbknox-\
tamerOnly-1295247776.554000.log -numEps 150
 */
package edu.utexas.cs.tamerProject.agents.tamerrl;

import java.util.Arrays;
import java.util.Date;
import java.util.ArrayList;
import javax.swing.JFrame;


import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.AbstractRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.util.AgentLoader;

import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.agent.AgentMessageParser;
import rlVizLib.messaging.agent.AgentMessages;
import rlVizLib.messaging.agentShell.TaskSpecResponsePayload;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.HRew;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.trainInterface.TrainerListener;
import edu.utexas.cs.tamerProject.visualization.EligModDisplay;
import edu.utexas.cs.tamerProject.experimentTools.LogTrainer;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.featGen.FeatGen_RBFs;
import edu.utexas.cs.tamerProject.params.Params;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;



public class TamerRLAgent extends GeneralAgent implements AgentInterface {

	public SarsaLambdaAgent rlAgent;
	public TamerAgent tamerAgent;
	public HInfluence hInf;
	private String H_INFLUENCE_METHOD = "annealedParam"; // annealedParam or eligTrace
	private boolean SIMUL_LEARNING = false;
	
	public int COMBINATION_METHOD = 6;
	private double INITIAL_COMB_PARAM = 10.0;
	
	public boolean USING_PY_MC_MODEL = false;
	private int H_NUM = -1;

	private boolean tamerControl;
	
	// TODO Use an enum here instead.
	public final int RL_ON_H_AS_R = -2; // use the prediction of human reward as if it is MDP reward
	public final int TAMER_ONLY = -1;
	public final int RL_ONLY = 0;
	public final int REW_SHAPING = 1;
	public final int FEAT_ADD = 2;
	public final int Q_INIT = 3; // not implemented
	public final int Q_AUGM = 4;
	public final int EXTRA_ACT = 5; // not implemented
	public final int ACT_BIASING = 6; 
	public final int BERNOULLI_ACT = 7; // also called control sharing
	public final int STATE_POT_FCN_SHAPING = 8;

	public final int SA_POT_FCN_SHAPING = 9; // This is equivalent to Weiwora et al.'s Look-Ahead Advice for action selection that only depends 
											// on which action is greedy (greedy and e-greedy). For others, I'd need to add 
											// (discountFactor*H(s_{t-1}, a_{t-1})) to values during action selection. 
	public final int PROB_ACT_W_OSCILL_DAMP = 10;

	private final boolean DISPLAY_ELIG_MOD = true;
	
    public void processPreInitArgs(String[] args) {
    	super.processPreInitArgs(args);
    	for (int i = 0; i < args.length; i++) {
    		String argType = args[i];
    		if (argType.equals("-combMethod") && (i+1) < args.length){
    			this.COMBINATION_METHOD = Integer.valueOf(args[i+1]).intValue();
    			System.out.println("this.COMBINATION_METHOD set to: " + this.COMBINATION_METHOD);
    		}
    		else if (argType.equals("-combParam") && (i+1) < args.length){
    			this.INITIAL_COMB_PARAM = Double.valueOf(args[i+1]).doubleValue();
    			System.out.println("this.INITIAL_COMB_PARAM set to: " + this.INITIAL_COMB_PARAM);
    		}
    		else if (argType.equals("-eligTrace")){
    			this.H_INFLUENCE_METHOD = "eligTrace";
    			System.out.println("this.H_INFLUENCE_METHOD set to: " + this.H_INFLUENCE_METHOD);
    		}
    		else if (argType.equals("-simulLearning")){
    			this.SIMUL_LEARNING = true;
    			System.out.println("this.SIMUL_LEARNING set to: " + this.SIMUL_LEARNING);
    		}
    		else if (argType.equals("-pyMCModel") && (i+1) < args.length){
    			usePYMCModel(Integer.valueOf(args[i+1]).intValue());
    		}
    	}
    	rlAgent.processPreInitArgs(args);
    	tamerAgent.processPreInitArgs(args);
    	System.out.println("------------------");
    }

    public void usePYMCModel(int hNum) {
		this.USING_PY_MC_MODEL = true;
		this.H_NUM = hNum; 
		System.out.println("this.USING_PY_MC_MODEL set to: " + this.USING_PY_MC_MODEL);
		System.out.println("this.h_num set to: " + this.H_NUM);
    }
	
	public void initRecords() {
		super.initRecords();
		if (this.tamerAgent != null)
			this.tamerAgent.initRecords();
		if (this.rlAgent != null)
			this.rlAgent.initRecords();
	}
	
    public TamerRLAgent() {
    	super();
   		this.tamerAgent = new TamerAgent();
		this.rlAgent = new SarsaLambdaAgent();    	
    }

    public TamerRLAgent(ParameterHolder p) {
        this();
    }
    
    public void initParams(String envName){
    	super.initParams(envName);
    	rlAgent.initParams(envName);
    	tamerAgent.initParams(envName);
    }


    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());
        return p;
    }

    public static void main(String[] args){
    	TamerRLAgent agent = new TamerRLAgent();
    	agent.processPreInitArgs(args);
    	if (agent.glue) {
        	AgentLoader L=new AgentLoader(agent);
        	L.run();
    	}
    	else {
    		agent.runSelf();
    	}
    }  
    
    
    
    
    
	
	// Called when the environment is loaded (when "Load Experiment" is clicked in RLViz)
    public void agent_init(String taskSpec) {
    	System.out.println("Agent init started");
    	GeneralAgent.agent_init(taskSpec, this);
    	
//    	this.combinationParam = this.INITIAL_COMB_PARAM;
    	if (this.COMBINATION_METHOD == TAMER_ONLY)
        	this.tamerControl = true;
    	else 
    		this.tamerControl = false;
    	
    	if (this.SIMUL_LEARNING)
    		this.countTrainingEps = true;
    	
    	

    	/*
    	 * Initialize TAMER agent.
    	 */
    	this.tamerAgent.setIsTopLevelAgent(false);
    	this.tamerAgent.enableGUI = false;
    	if (this.trainFromLog && !this.SIMUL_LEARNING) {
    		if (this.COMBINATION_METHOD != RL_ONLY) {
	    		this.tamerAgent.trainFromLog = true;
	    		this.tamerAgent.trainLogPath = this.trainLogPath;
    		}
    		this.trainFromLog = false;
    	}

//    	boolean hInfEligTraces = this.H_INFLUENCE_METHOD.equals("eligTrace");
    	if (this.USING_PY_MC_MODEL) this.setTamerForPyMC();
    	this.tamerAgent.agent_init(taskSpec);
    	
    	if (this.USING_PY_MC_MODEL) this.loadPyMCWts();
    	
//    	// first feature test
//    	int[] intStateVars = new int[0];
//    	double[] doubleStateVars = {0.0, 0.0}; 
//		int[] intActVars = {0};
//		double[] doubleActVars = null;
//		System.out.println("Input: " + Arrays.toString(doubleStateVars) + ", " + Arrays.toString(intActVars));
//		double[] feats = this.tamerAgent.featGen.getSAFeats(intStateVars, doubleStateVars, intActVars, doubleActVars);
////		System.out.println("Feats: " + Arrays.toString(feats) + "\n\n\n");
//
//		// first test of model output
//		double modelOut = this.tamerAgent.model.predictLabel(feats);
//		System.out.println("Model output: " + modelOut);
//		
//		// second feature test
//		doubleStateVars[0] = 0.1; 
//		doubleStateVars[1] = 0.01;
//		intActVars[0] = 1;
//		System.out.println("Input: " + Arrays.toString(doubleStateVars) + ", " + Arrays.toString(intActVars));
//		feats = this.tamerAgent.featGen.getSAFeats(intStateVars, doubleStateVars, intActVars, doubleActVars);
//		System.out.println("Feats: " + Arrays.toString(feats) + "\n");
//		System.out.println("Num feats: " + feats.length);
//		
//		// second test of model output
//		modelOut = this.tamerAgent.model.predictLabel(feats);
//		System.out.println("Model output: " + modelOut);
    	
    	
    	
    	/*
    	 * Initialize determinants of the level of influence of H-hat on the RL algorithm.
    	 */
    	boolean hInfStateOnlyFeats = false;
    	boolean setDecayFactorsToOne = false;
    	
    	if (this.COMBINATION_METHOD == BERNOULLI_ACT) {
    		hInfStateOnlyFeats = true;
    	}
    	else if (this.COMBINATION_METHOD != ACT_BIASING && // all comb methods with constant scaling factors
    			this.COMBINATION_METHOD != REW_SHAPING) {
    		setDecayFactorsToOne = true;
    		this.H_INFLUENCE_METHOD = "annealedParam"; // don't want to use elig traces on methods with constants instead of weights
    	}
    	this.hInf = new HInfluence(this.H_INFLUENCE_METHOD, 
    				this.INITIAL_COMB_PARAM * ((this.COMBINATION_METHOD == SA_POT_FCN_SHAPING) ?
    											this.discountFactorForLearning.getValue() : 1.0), // TODO why is this if-then here?
    				getEnvName(taskSpecObj.getExtraString()), this.tamerAgent.params, this, hInfStateOnlyFeats);
    	if (setDecayFactorsToOne) {
    		this.hInf.setEpDecayFactor(1.0);
    		this.hInf.setStepDecayFactor(1.0);
    	}
    	
    	
    	
    	/*
    	 * Initialize RL agent.
    	 */	
  
    	this.rlAgent.setIsTopLevelAgent(false);
    	this.rlAgent.enableGUI = false;
    	if (this.USING_PY_MC_MODEL) this.setRLForPyMC();    	////////////////////////////////////////// check this

    	if (this.COMBINATION_METHOD == FEAT_ADD) // should stay before rlAgent.agent_init
    		this.rlAgent.addModelBasedFeat(taskSpec, tamerAgent.model, tamerAgent.featGen);
    	this.rlAgent.agent_init(taskSpec);
    	if (this.COMBINATION_METHOD == ACT_BIASING ||
    			this.COMBINATION_METHOD == SA_POT_FCN_SHAPING) 
    		this.rlAgent.actSelector.addModelForActBias(tamerAgent.model, this.hInf);
    	if (this.COMBINATION_METHOD == Q_AUGM) {
    		this.rlAgent.qAugModel = tamerAgent.model;
    		this.rlAgent.actSelector.addModelForActBias(this.rlAgent.qAugModel, this.hInf); 
    	}
    	if (this.COMBINATION_METHOD == RL_ON_H_AS_R) // TAMER model serves as reward function for RL agent
    		this.rlAgent.actSelector.setRewModel(this.tamerAgent.model);
    	
    	System.out.println("this.recordRew: " + this.recordRew);
    	if (!this.recordRew)
    		enableGUI = false;
		if (!GeneralAgent.isApplet && enableGUI) {
			try{ 		//Schedule a job for event dispatch thread
		        javax.swing.SwingUtilities.invokeLater(new Runnable() {
						public void run() { TrainerListener.createAndShowGUI(TamerRLAgent.this); }
				});
			}
			catch (java.awt.HeadlessException e) {
				System.out.println("Exception in TamerRLAgent while trying to create reinforcement window: " + e.toString());
			}
		}

		
		if (DISPLAY_ELIG_MOD && this.H_INFLUENCE_METHOD.equals("eligTrace")) {
			try{  		//Schedule a job for event dispatch thread
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						//EligModDisplay.createAndShowDisplay(TamerRLAgent.this);
						System.err.println("EligModDisplay.createAndShowDisplay(TamerRLAgent.this) disabled");
					}
				});
			}
			catch (Exception e) {
				System.out.println("Exception in TamerRLAgent while trying to create display for eligibility module display: " + e.toString());
			}
		}
		
		this.endInitHelper();
    }


    protected void endInitHelper() {
    	if (this.trainFromLog) {
 			System.out.println("Training from log for simul learning.");
 			LogTrainer.trainOnLog(this.trainLogPath, this, this.logTrainEpochs);
    	}
    	else {
    		System.out.println("Not training from log.");
    	}
    		
    }

	// Called at the beginning of each episode (in RLViz, it's first called when "Start" is first clicked)
    public Action agent_start(Observation o, double time, Action predeterminedAct) {
//    	System.out.println("---------------------------start TAMERRL ep " + this.currEpNum);
//    	System.out.println("Episode start");
    	this.startHelper();
    	if (this.COMBINATION_METHOD != RL_ONLY)	this.tamerAgent.agent_start(o, time, new Action());
    	this.currObsAndAct.setAct(this.agent_step(0.0, o, time, predeterminedAct));
    	this.rlAgent.agent_start(o, time, this.currObsAndAct.getAct());
    	if (this.COMBINATION_METHOD != RL_ONLY)	this.tamerAgent.lastObsAndAct.setAct(this.currObsAndAct.getAct().duplicate());
        return this.currObsAndAct.getAct();
    }



    
    
    public Action agent_step(double r, Observation o, double startTime, Action predeterminedAct) {
    	//System.out.println("---------------------------step " + this.stepsThisEp + " in ep " + this.currEpNum);
    	this.stepStartTime = startTime;
    	this.stepStartHelper(r, o);
    	//System.out.println("TAMERRL this.stepStartTime: " + String.format("%f", this.stepStartTime));
    	tamerAgent.hRewList = new ArrayList<HRew>(this.hRewThisStep);
    	this.hInf.recordTimeStepEnd(startTime);
    	
    	//if (this.stepsThisEp > 1) {
    	//	System.out.println("TamerRL lastO,lastA: " + Arrays.toString(this.lastObs.doubleArray) + Arrays.toString(this.lastAct.intArray));
    	//	System.out.println("Predicted human reinf for last step before TAMER update: " + this.tamerAgent.getValForLastStep());
    	//	System.out.println("Estimated Q-value for last step: " + this.rlAgent.getValForLastStep());
    	//}
    	
    	
    	/*
    	 * TAMER update
    	 */
    	if (this.stepsThisEp > 1 && this.COMBINATION_METHOD != RL_ONLY) {
    		this.currObsAndAct.setAct(new Action()); // sending an empty action instead of a null one to avoid the comp. cost of an extra action selection
    		tamerAgent.agent_step(r, o, this.stepStartTime, this.currObsAndAct.getAct());
    	}
    	//this.overwriteLastObsAndAct(this.tamerAgent); // the only reason to do this would be to use TamerAgent's lastObs and lastAct later in this method TODO remove 

    	this.hInf.stepUpdate(this.inTrainSess, this.stepStartTime);
    	
    	//System.out.println("MDP reward: " + r);
    	
		/*
		 *  GET ACTION (if RL is on-policy)
		 */
    	if (predeterminedAct == null) {
    		//System.out.print("rlAgent "); 		
	    	this.currObsAndAct.setAct(this.rlAgent.actSelector.selectAction(o, this.lastObsAndAct.getAct()));
	    	if ((this.COMBINATION_METHOD == BERNOULLI_ACT && this.random.nextDouble() < this.hInf.getHInfluence(o, null)) 
	    								|| this.tamerControl) {
	    		this.currObsAndAct.setAct(this.tamerAgent.actSelector.greedyActSelect(o, this.lastObsAndAct.getAct()));
	    	}
		}
		else {
			this.currObsAndAct.setAct(predeterminedAct);
		}
    	this.manualActIfFailed(o);
		this.tamerAgent.lastObsAndAct.setAct(this.currObsAndAct.getAct().duplicate()); // action is decided, so let TamerAgent know what it was (endHelper() has been called already in tamerAgent, so the currObsAndAct here is the lastObsAndAct there.) 
		
		//this.tamerAgent.hLearner.recordTimeStep(this.tamerAgent.featGen.getFeats(o, this.action), time);
		if (this.COMBINATION_METHOD != RL_ONLY) { // TODO make a common variable to replace this and its use above
			this.tamerAgent.hLearner.recordTimeStepStart(o, this.currObsAndAct.getAct(), this.tamerAgent.featGen, 
														this.stepStartTime); // called here b/c action was unknown at the time of tamerAgent's agent_step()
		}

    	double manipulatedR = this.getManipulatedRew(r, o);
    	
    	/*
    	 * RL update
    	 */
    	if (this.stepsThisEp > 1){
    		this.rlAgent.agent_step(manipulatedR, o, this.stepStartTime, this.currObsAndAct.getAct());
    		//this.overwriteLastObsAndAct(this.rlAgent); TODO remove
    	}
    	//    	this.hInf.recordTimeStep(o, this.action, this.stepStartTime); // out-of-date way of recording time step; eventually delete this line
    	
    	/*
    	 * GET ACTION if RL is off-policy
    	 */
    	
    	//System.out.println("TAMERRL action: " + this.action.intArray[0]);
//    	if (this.lastAct != null)
//			System.out.println("TAMERRL last action: " + this.lastAct.intArray[0]);
    	this.stepEndHelper(r, o);
		this.hInf.recordTimeStepStart(o, this.currObsAndAct.getAct(), this.stepStartTime);
        return this.currObsAndAct.getAct();
    }


//    protected void stepEndHelper(double r, Observation o) { I've commented this out because I don't see it as being needed, given that the predetermined action is given to each subagent.
//    	super.stepEndHelper(r, o);
//    	this.overwriteLastObsAndAct(this.tamerAgent);
//    	this.overwriteLastObsAndAct(this.rlAgent);
//    }
    


    public void agent_end(double r, double time) {
//    	System.out.println("---------------------------end");
    	this.stepStartTime = time;
//    	System.out.println("\n\nEpisode end");
    	this.endHelper(r);
    	tamerAgent.hRewList = new ArrayList<HRew>(this.hRewThisStep);
    	double manipulatedR = this.getManipulatedRew(r, null);
    	this.hInf.episodeEndUpdate();
    	if (this.COMBINATION_METHOD != RL_ONLY)
    		this.tamerAgent.agent_end(r, time);
    	this.rlAgent.agent_end(manipulatedR, time);
    }


    public void agent_cleanup() {
        
    }

    

    private double getManipulatedRew(double r, Observation o) {
		if (COMBINATION_METHOD == REW_SHAPING) {
			if (this.stepsThisEp > 1)
				r += this.hInf.getHInfluence(this.lastObsAndAct.getObs(), this.lastObsAndAct.getAct()) 
												* this.tamerAgent.getVal(this.lastObsAndAct.getObs(), 
																			this.lastObsAndAct.getAct());
		}
		else if (COMBINATION_METHOD == RL_ON_H_AS_R) {
			if (this.stepsThisEp > 1)
				r = this.tamerAgent.getVal(this.lastObsAndAct.getObs(), this.lastObsAndAct.getAct());
		}
		else if (COMBINATION_METHOD == STATE_POT_FCN_SHAPING) {
			r += this.INITIAL_COMB_PARAM * this.tamerAgent.getStatePotForTrans(this.lastObsAndAct.getObs(), o);
		}
		else if (COMBINATION_METHOD == SA_POT_FCN_SHAPING) {
			r += this.INITIAL_COMB_PARAM * this.tamerAgent.getSAPotForTrans(
							this.lastObsAndAct.getObs(), this.lastObsAndAct.getAct(), o, this.currObsAndAct.getAct());
			//System.out.println("r: " + r);
		}
		return r;
    }
    
	public void receiveKeyInput(char c){
		System.out.println(c);
		if (c == '/') {
			this.addHRew(1.0);
//			this.hRewCounter += 1;
		}
		else if (c == 'z') {
			this.addHRew(-1.0);
//			this.hRewCounter -= 1;
		}
		else if (c == ' ' && this.allowUserToggledTraining) {
			this.toggleInTrainSess();
		}
	}
	
	public void toggleInTrainSess() {
		this.tamerAgent.setInTrainSess(!this.tamerAgent.getInTrainSess());
		this.inTrainSess = !this.inTrainSess;
	}

	public void toggleTamerControl(){
		if (this.COMBINATION_METHOD != TAMER_ONLY) {
			this.tamerControl = !this.tamerControl;
			if (this.tamerControl)
				System.out.println("\n\nTAMER agent taking control.\n");
			else
				System.out.println("\n\nTAMER agent ceding control.\n");
		}
		else
			System.out.println("In TAMER-ONLY mode. TAMER agent cannot cede control.");
	}
    
	public void setMasterLogSwitch(boolean on) {
		this.masterLogSwitch = on;
		this.tamerAgent.setMasterLogSwitch(on);
		this.rlAgent.setMasterLogSwitch(on);
	}

	
	private void setTamerForPyMC() {
		this.tamerAgent.params = (Params.getParams(this.tamerAgent.getClass().toString(), 
				getEnvName(taskSpecObj.getExtraString())));
		this.tamerAgent.params.setPyMCParams(this.tamerAgent.getClass().toString(), false);
	}
	
	private void loadPyMCWts() {
		// load model weights from AAMAS-2010 TAMER+RL experiments; H1, the middling performer, has hNum 1; top performer H2 has hNum 2
		System.out.println("\nLoading H-hat_{" + this.H_NUM + "} from AAMAS-10 TAMER+RL Mountain Car experiments. ");
		String[] wtsPath = {RecordHandler.getPresentWorkingDir().replace("/bin", "") + "/src/edu/utexas/cs/tamerProject/agents/tamerrl/models/H1-100.model", 
				RecordHandler.getPresentWorkingDir().replace("/bin", "") + "/src/edu/utexas/cs/tamerProject/agents/tamerrl/models/H2-100.model"};
		double[] wtsArray = null;
		try{ 
			String wtsStr = RecordHandler.getStrArray(wtsPath[this.H_NUM - 1])[0];
			wtsArray = RecordHandler.getDoubleArrayFromStr(wtsStr);
//			System.out.println("weights: "+ Arrays.toString(wtsArray));
			System.out.println("numWts: " + wtsArray.length);
		}
		catch (Exception e){
			System.err.println("Error in TamerRLAgent.loadPyMCWts: " + e.getMessage() + "\nExiting.");
			System.err.println("If you have already created a data directory somewhere, consider creating a symbolic" +
					" link to it to make the above path valid.");
			System.exit(0); }
		((IncGDLinearModel)this.tamerAgent.model).setModelParams(wtsArray);
	}

	private void setRLForPyMC(){
		this.rlAgent.params = (Params.getParams(this.rlAgent.getClass().toString(), 
							getEnvName(taskSpecObj.getExtraString())));
		this.rlAgent.params.setPyMCParams(this.rlAgent.getClass().toString(), false);
	}
	
	
	public void manualActIfFailed(Observation o) {
		if (this.envName.equals("Mountain-Car") && this.numEpsBeforeStop != -1 
				&& this.totalRew < (this.numEpsBeforeStop * -200) && this.stepsThisEp > 150) {
			Action manualAct = new Action();
			manualAct.intArray = new int[1];
			if (o.doubleArray[1] < 0)
				manualAct.intArray[0] = 0;
			else
				manualAct.intArray[0] = 2;
			this.currObsAndAct.setAct(manualAct);
//			System.out.println("new action: " + Arrays.toString(this.action.intArray));
		}
	}
	
}

/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 */
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "TAMER+RL Agent";
    }

    public String getShortName() {
        return "TAMER+RL Agent";
    }

    public String getAuthors() {
        return "Brad Knox";
    }

    public String getInfoUrl() {
        return "http://www.cs.utexas.edu/~bradknox";
    }

    public String getDescription() {
        return "RL-Glue Java Version of an agent combining Sarsa(Lambda) and TAMER.";
	}
}


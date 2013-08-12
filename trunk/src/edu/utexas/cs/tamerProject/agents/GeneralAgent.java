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
package edu.utexas.cs.tamerProject.agents;



import java.net.URL;
import java.util.Random;
import java.util.Date;
import java.util.Arrays;
import java.util.ArrayList;
import java.io.*;
import java.util.concurrent.TimeUnit;
import java.net.URLClassLoader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;


import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpec;
import org.rlcommunity.rlglue.codec.taskspec.ranges.AbstractRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.DoubleRange;
import org.rlcommunity.rlglue.codec.taskspec.ranges.IntRange;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.agent.AgentMessageParser;
import rlVizLib.messaging.agent.AgentMessages;
import rlVizLib.messaging.agentShell.TaskSpecResponsePayload;

import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.modeling.*;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.modeling.weka.WekaModelPerActionModel;
import edu.utexas.cs.tamerProject.modeling.weka.WekaModelWrap;
import edu.utexas.cs.tamerProject.experimentTools.LogTrainer;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.featGen.*;
import edu.utexas.cs.tamerProject.params.Params;
import edu.utexas.cs.tamerProject.utils.MutableDouble;
import edu.utexas.cs.tamerProject.utils.Stopwatch;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndAct;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.HRew;

/**
 * This abstract class is the ancestor of all other major agent classes in this project. 
 * (ExtActionAgentWrap is an exception for now.) Much of the code that is used by the agents
 * is implemented here. Further, the TamerApplet class in the rl-applet codebase assumes that
 * all agents are children of GeneralAgent.
 */
public abstract class GeneralAgent implements AgentInterface{

	
	public static final int MAX_STEPS_SET_BY_EXP = 10000000;
	
	//// values that come directly from TaskSpec object
	protected TaskSpecVRLGLUE3 taskSpecObj = null;
	protected double discountFactorOfMDP;
    protected int[][] theObsIntRanges;
    protected double[][] theObsDoubleRanges;
    protected int[][] theActIntRanges;
    protected double[][] theActDoubleRanges;
    protected DoubleRange theRewardRange;
    public String envName;
    
    //// recent experience
	public volatile ObsAndAct currObsAndAct;
	public ObsAndAct lastObsAndAct;
	public ArrayList<HRew> hRewList;
	public ArrayList<HRew> hRewThisStep;
	public boolean takesHRew = true;
	protected double stepStartTime;
	protected double lastStepStartTime;
	public static boolean duringStepTransition = false;

	protected boolean isTopLevelAgent = true; // false when an agent object is a member of another agent object. 
	//public static boolean isApplet = false;
	public static boolean canWriteToFile = false; // set to true to allow logging to local file system
	public static boolean canWriteViaPHP = false; // set to true to allow logging to a server via PHP (must be set up in TamerApplet by setting member variable dataCollectURL)
	public boolean enableGUI = false; // true enables locally created GUIs
	
	protected boolean inTrainSess;
	protected MutableDouble discountFactorForLearning = null; // can differ from actual MDP discount factor; may not be used by some agents
	public boolean safeActionOnly = false;
	public boolean TRAINING_BY_DEFAULT = false; // code elsewhere assumes this is initially false
	protected boolean allowUserToggledTraining = true; 
	
	//// main modules
	public Params params;
	public volatile RegressionModel model;
	public FeatGenerator featGen;
	public ActionSelect actSelector;

	//// record keeping
	public int stepsThisEp;
	public int totalSteps;
	public double rewThisEp;
	public double totalRew;
	public int currEpNum;
	
	public boolean masterLogSwitch = true; // when false, no logs are written; used in LogTrainer

	public static String RLLIBRARY_PATH = ""; 
	
	//// writing a full log
	protected RecordHandler recHandler;
	protected boolean recordLog = false; // keep this false and reassign elsewhere to log
	protected String expName = "test";
	protected String trainerName = "wbknox";
	protected String unique = trainerName + "-tamerOnly-" + String.format("%f", Stopwatch.getWallTimeInSec());
	protected String writeLogDir; 
	protected String writeLogPath;
	
	//// writing just the reward per episode
	protected boolean recordRew = false; // keep this false and reassign elsewhere to log
	protected String writeRewDir; 
	protected String writeRewPath;
	
	//// training from a log file
	public boolean trainFromLog = false;
	public String trainLogPath = "";
	public int trainEpLimit = -1;
	public boolean countTrainingEps = false;
	public int logTrainEpochs = 1; // how many times to iterate through a log's data for learning

	//// variables for experiments
	public int numEpsBeforeStop = -1; // -1 means to never stop; don't change here
	protected int numEpsBeforePause = 0; // -1 means to never pause; don't change here
	public boolean pause = false;
	
	protected boolean verbose = false;
	
	protected Random random = new Random();

	// for non-RLGlue communication
	private int serverPort = 37564;
	private boolean startCalled = false; // agent_start() already called this episode; only used for non-glue environments
	protected boolean glue = true;
	protected boolean learningOnly = false;
    
	public void processPreInitArgs(String[] args) {
		System.out.println("\n[------Process pre-init args in " + this.getClass().getSimpleName() 
				+ "------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];
    		if (argType.equals("-expName") && (i+1) < args.length){
    			this.expName = args[i+1];
    			System.out.println("this.expName set to: " + this.expName);
    		}
    		else if (argType.equals("-unique") && (i+1) < args.length){
    			//this.recordLog = true;
    			this.unique = args[i+1];	
    			System.out.println("this.unique set to: " + this.unique);
    		}
    		else if (argType.equals("-rewLog")) {
    			this.recordRew = true;
    			System.out.println("Recording reward per episode.");
    		}
    		else if (argType.equals("-fullLog")) {
    			this.recordLog = true;
    			System.out.println("Recording full log (not just reward per episode).");
    		}
    		else if ((argType.equals("-trainLogPath") || argType.equals("-logPath"))
    						&& (i+1) < args.length){
    			this.trainLogPath = args[i+1];
    			this.trainFromLog = true;
    			System.out.println("this.trainFromLog set to: " + this.trainFromLog);
    			System.out.println("this.trainLogPath set to: " + this.trainLogPath);
    			if (argType.equals("-logPath"))
    				System.err.println("-logPath is no longer an accepted argument. Change whatever called it to -trainLogPath.");
    		}
    		else if (argType.equals("-trainEpLimit") && (i+1) < args.length){
    			this.trainEpLimit = Integer.valueOf(args[i+1]).intValue();
    			System.out.println("this.trainEpLimit set to: " + this.trainEpLimit);
    		}
    		else if (argType.equals("-numEps") && (i+1) < args.length){
    			this.numEpsBeforeStop = Integer.valueOf(args[i+1]).intValue();
    			System.out.println("this.numEpsBeforeStop set to: " + this.numEpsBeforeStop);
    		}
    		else if (argType.equals("-epsBeforePause") && (i+1) < args.length){
    			this.numEpsBeforePause = Integer.valueOf(args[i+1]).intValue();
    			System.out.println("this.numEpsBeforePause set to: " + this.numEpsBeforePause);
    		}
    		else if (argType.equals("-envName") && (i+1) < args.length){
    			this.envName = args[i+1];
    			System.out.println("this.envName set to: " + this.envName);
    		}
    		else if (argType.equals("-noGlue")) {
    			this.glue = false;
    			System.out.println("Communicating with environment using non-RLGlue interface.");
    		}
    		else if (argType.equals("-trainByDefault")){
    			this.TRAINING_BY_DEFAULT = true;
    			System.out.println("this.TRAINING_BY_DEFAULT set to: " + this.TRAINING_BY_DEFAULT);
    		}
		}
	}
	
	/**
	 * For parameters that require an initialized agent. Should be called immediately after
	 * after agent initialization.
	 * 
	 * @param args
	 * @param agent
	 */
	public void processPostInitArgs(String[] args) {
	}

    public GeneralAgent() {
        this(getDefaultParameters());
    }

    public GeneralAgent(ParameterHolder p) {
        super();
    }
    
    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        return p;
    }
    

    public double getDiscountFactorForLearning() {return this.discountFactorForLearning.getValue();}
    public void setDiscountFactorForLearning(double df) {
    	if (this.discountFactorForLearning == null)
			this.discountFactorForLearning = new MutableDouble(df);
    	else
    		this.discountFactorForLearning.setValue(df);
    }
    public boolean getIsTopLevelAgent() {return this.isTopLevelAgent;}
    public void setIsTopLevelAgent(boolean tla) {this.isTopLevelAgent = tla;}
    public boolean getRecordRew() {return this.recordRew;}
    public void setRecordRew(boolean recordRew) {this.recordRew = recordRew;}
    public boolean getRecordLog() {return this.recordLog;}
    public void setRecordLog(boolean recordLog) {this.recordLog = recordLog;}
    public void setUnique(String unique) {this.unique = unique;}
    public String getUnique() {return this.unique;}
    public void setExpName(String expName) {this.expName = expName;}
    public String getExpName() {return this.expName;}
    public void setAllowUserToggledTraining(boolean allowUserToggledTraining) {
    	this.allowUserToggledTraining = allowUserToggledTraining;}
    public RecordHandler getRecHandler() {return this.recHandler;}
    public boolean getInTrainSess() {return this.inTrainSess;}
    public void setInTrainSess(boolean inTrainSess) {this.inTrainSess = inTrainSess;}
    
    
    public static TaskSpecResponsePayload isCompatible(ParameterHolder P, String TaskSpec){
        return new TaskSpecResponsePayload(false,"");
    }

    public void initParams(String envName){
    	params = Params.getParams(getClass().getName(), envName);
    }
    
    public abstract void agent_init(String taskSpec);
    
    // Called when the environment is loaded (when "Load Experiment" is clicked in RLViz)
    protected static void agent_init(String taskSpec, GeneralAgent agent) {
    	System.out.println("\n\n\n----Agent " + agent.getClass().getName() + " is being initialized.----");
    	agent.startInitHelper(taskSpec); 
    	
		//// INITIALIZE FeatGenerator
    	System.out.println("featGen before initialization: " + agent.featGen);
		if (agent.featGen == null)
			agent.featGen = agent.getFeatGen(agent.params);
		else
			System.out.println("Keeping a previously instantiated featGen in agent_init()!!!!!");
		System.out.println("featGen after initialization: " + agent.featGen);
		
		//// INITIALIZE RegressionModel
    	System.out.println("model: " + agent.model);
		if (agent.model == null)
			agent.setModel();
		else
			System.out.println("Keeping a previously instantiated model in agent_init()!!!!!");
		System.out.println("model after initialization: " + agent.model);
		
		agent.recHandler = new RecordHandler(GeneralAgent.canWriteToFile);
		System.out.println(agent.getClass().getName() + " masterLogSwitch: " + agent.masterLogSwitch);
		
		/*
		 *  Write parameters to log files
		 */
		if (agent.masterLogSwitch) {
			if (agent.recordLog) {
	    		System.out.println("Log base path: " + agent.writeLogDir);
	    		if (agent.recHandler.canWriteToFile) {
	    			(new File(agent.writeLogDir)).mkdir();
	    		}
	    		System.out.println("agent.writeLogPath: " + agent.writeLogPath);
	    		agent.recHandler.writeParamsToFullLog(agent.writeLogPath, agent.params);
	    	}
			if (agent.recordRew) {
				System.out.println("Reward log base path: " + agent.writeRewDir);
				if (agent.recHandler.canWriteToFile)
					(new File(agent.writeLogDir)).mkdir();
				System.out.println("agent.writeRewPath: " + agent.writeRewPath);
	    		agent.recHandler.writeParamsToRewLog(agent.writeRewPath, agent.params);
			}
			
		}
    }
    
    protected void startInitHelper(String taskSpec) {
    	if (GeneralAgent.canWriteToFile) {
    		System.out.println("unique: " + unique);
    		
    		if (RLLIBRARY_PATH.equals(""))
    			RLLIBRARY_PATH = RecordHandler.getPresentWorkingDir().replace("/bin", "");
    		//RLLIBRARY_PATH = System.getenv("RLLIBRARY");
    		this.writeLogDir = RLLIBRARY_PATH + "/data/" + this.expName; 
    	
	    	this.writeLogPath = writeLogDir + "/recTraj-" + this.unique
	    					+ ".log";
	    	this.writeRewDir = RLLIBRARY_PATH + "/data/" + this.expName; 
	    	this.writeRewPath = writeRewDir + "/" + this.unique
	    					+ ".rew";
	    	
			//Get the System Classloader
	        ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
	
//	        //Get the URLs
//	        URL[] urls = ((URLClassLoader)sysClassLoader).getURLs();
//	        for(int i=0; i< urls.length; i++)
//			{
//				System.out.println("Classpath: "+ urls[i].getFile());
//			} 
    	}

    	System.out.println("Task specification string: " + taskSpec);
        taskSpecObj = new TaskSpecVRLGLUE3(taskSpec);
		System.out.println("Agent parsed the task spec.");
		this.envName = getEnvName(taskSpecObj.getExtraString());
		System.out.println("taskStr: " + taskSpecObj.toString());
		if (this.envName.equals("")) {
			taskSpecObj = new TaskSpecVRLGLUE3("VERSION RL-Glue-3.0 PROBLEMTYPE episodic DISCOUNTFACTOR 1.0" + 
						" OBSERVATIONS INTS (0 1) (0 1) (0 1) (0 3) (0 1)" + 
						" DOUBLES (-5.0 15.0) (-5.0 15.0) (-5.0 15.0) (-5.0 15.0)" +
						" (-5.0 15.0) (-5.0 15.0) (-5.0 15.0) (-5.0 15.0)" +
						" ACTIONS INTS (-1 1) (0 1) (0 1) " + 
						" REWARDS (-1.0 0.0)  EXTRA EnvName:Mario Revision:null");
			this.envName = getEnvName(taskSpecObj.getExtraString());
		}
		System.out.println("Environment name: " + envName);
		if (this.params == null)
			this.params = Params.getParams(this.getClass().getName(), envName);
		System.out.println("params: " + params.toOneLineStr());

		this.theObsIntRanges = getObsIntRanges(taskSpecObj); 
		
		this.theObsDoubleRanges = getObsDoubleRanges(taskSpecObj);

		this.theActIntRanges = getActIntRanges(taskSpecObj);
        
        this.theActDoubleRanges = getActDoubleRanges(taskSpecObj);
        
        
        
		this.theRewardRange = taskSpecObj.getRewardRange();
		System.out.println("Reward range is: " + this.theRewardRange.getMin() + " to " + this.theRewardRange.getMax());
		this.discountFactorOfMDP = taskSpecObj.getDiscountFactor();
		if (this.discountFactorForLearning == null)
			this.discountFactorForLearning = new MutableDouble(this.discountFactorOfMDP);
		
		System.out.println("Discount factor of MDP is: " + this.discountFactorOfMDP);
		System.out.println("Discount factor for learning is: " + this.discountFactorForLearning.getValue());
		
        this.currObsAndAct = new ObsAndAct();
        this.currObsAndAct.setAct(new Action(taskSpecObj.getNumDiscreteActionDims(), taskSpecObj.getNumContinuousActionDims()));
        this.initRecords();
    }
    
	public static int[][] getObsIntRanges(TaskSpecVRLGLUE3 taskSpecObj){ 
		int obsIntDims = taskSpecObj.getNumDiscreteObsDims();
		System.out.println("Observations have " + obsIntDims + " integer dimensions");
		int[][] theObsIntRanges = new int[obsIntDims][2]; 
		for (int i = 0; i < obsIntDims; i++) {
		    IntRange thisObsIntRange = taskSpecObj.getDiscreteObservationRange(i);
			//System.out.println("Observation integer " + i + " range is: " +thisObsIntRange.getMin()+ " to " +thisObsIntRange.getMax());
			theObsIntRanges[i][0] = thisObsIntRange.getMin();
			theObsIntRanges[i][1] = thisObsIntRange.getMax();
		}
		return theObsIntRanges;
	}
	public static double[][] getObsDoubleRanges(TaskSpecVRLGLUE3 taskSpecObj){
		int obsDoubleDims = taskSpecObj.getNumContinuousObsDims();
		System.out.println("Observations have " + obsDoubleDims + " double dimensions");
		double[][] theObsDoubleRanges = new double[obsDoubleDims][2]; 
	    for (int i = 0; i < obsDoubleDims; i++) {
	        DoubleRange thisObsDoubleRange = taskSpecObj.getContinuousObservationRange(i);
			//System.out.println("Observation double " + i + " range is: " +thisObsDoubleRange.getMin()+ " to " +thisObsDoubleRange.getMax());
			theObsDoubleRanges[i][0] = thisObsDoubleRange.getMin();
			theObsDoubleRanges[i][1] = thisObsDoubleRange.getMax();
		}
	    return theObsDoubleRanges;
	}
	public static int[][] getActIntRanges(TaskSpecVRLGLUE3 taskSpecObj){ 
		int actIntDims = taskSpecObj.getNumDiscreteActionDims();
		System.out.println("Actions have " + actIntDims + " integer dimensions");
		int[][] theActIntRanges = new int[actIntDims][2]; 
	    for (int i = 0; i < actIntDims; i++) {
	        IntRange thisActIntRange = taskSpecObj.getDiscreteActionRange(i);
			//System.out.println("Action integer " + i + " range is: " +thisActIntRange.getMin()+ " to " +thisActIntRange.getMax());
			theActIntRanges[i][0] = thisActIntRange.getMin();
			theActIntRanges[i][1] = thisActIntRange.getMax();
		}
	    return theActIntRanges;
	}
	public static double[][] getActDoubleRanges(TaskSpecVRLGLUE3 taskSpecObj){
		int actDoubleDims = taskSpecObj.getNumContinuousActionDims();
		System.out.println("Actions have " + actDoubleDims + " double dimensions");
		double[][] theActDoubleRanges = new double[actDoubleDims][2]; 
	    for (int i = 0; i < actDoubleDims; i++) {
	        DoubleRange thisActDoubleRange = taskSpecObj.getContinuousActionRange(i);
			//System.out.println("Action double " + i + " range is: " +thisActDoubleRange.getMin()+ " to " +thisActDoubleRange.getMax());
			theActDoubleRanges[i][0] = thisActDoubleRange.getMin();
			theActDoubleRanges[i][1] = thisActDoubleRange.getMax();
		}
	    return theActDoubleRanges;
	}
	
    
    protected void endInitHelper() {
		if (this.actSelector != null) {
			this.actSelector.setEnvTransModel(this.params.envTransModel);
			if (this.actSelector.getRewModel() == null)
				this.actSelector.setRewModel(this.params.envRewModel);
		}
    	if (this.trainFromLog)
    		LogTrainer.trainOnLog(this.trainLogPath, this, this.logTrainEpochs);
//    	if (this.getClass().getName().contains("TamerAgent")) {
//    		System.out.println("\n\n\n\t----------------------------");
//			System.out.print("\t-----To start, ");
//			if (this.glue)
//				System.out.print("press the \"Start\" button, expand the \"Environment Vizualizer\" window to the full size of your screen, bring the +/- window to the top, and ");
//			System.out.print("press \"p\" to unpause. The space bar begins and ends training sessions, during which learning occurs. Your feedback buttons are");	
//			System.out.println("\"/\"(\"?\") to reward and \"z\" to punish.-----");
//			System.out.println("\t----------------------------\n\n\n\n\n");
//		}
//    	else if (this.getClass().getName().contains("ImitationAgent")) {
//    		System.out.println("\n\n\n\t----------------------------");
//    		System.out.print("\t-----To start, ");
//    		if (this.glue)
//    			System.out.print("press the \"Start\" button, expand the \"Environment Vizualizer\" window to the full size of your screen, bring the \"TrainerListener\" window to the top, and ");
//    		System.out.print("press \"p\" to unpause. The space bar begins and ends training sessions, during which learning occurs. Your controls are ");
//    		if (this.envName.equals("Mountain-Car"))
//    			System.out.println("\"j\" to accelerate left, \"k\" to not accelerate, and \"l\" to accelerate right.-----");
//    		else if (this.envName.equals("CartPole"))
//    			System.out.println("\"j\" to accelerate left and \"l\" to accelerate right.-----");
//    		System.out.println("\t----------------------------\n\n\n\n\n");
//    	}
//    	if (this.params.safeAction == null)
//    		this.params.safeAction = this.featGen.getPossActions(this.featGen.getRandomObs()).get(0);
    }
    
    public void initRecords() {
		this.totalSteps = 0;
		this.totalRew = 0;
		this.currEpNum = 0;
		this.inTrainSess = TRAINING_BY_DEFAULT;
    	this.stepsThisEp = 0;
    	this.rewThisEp = 0;
    	this.lastObsAndAct = new ObsAndAct();
		this.hRewList = new ArrayList<HRew>();
//		this.hRewCounter = 0;
    }
    
    public FeatGenerator getFeatGen(Params params) {
		System.out.println("Creating feature generation class " + params.featClass + ".");
		FeatGenerator featGen = null;
		int[][] actIntRanges = this.theActIntRanges;
		double[][] actDoubleRanges = this.theActDoubleRanges;

		if (this.params.featClass.equals("FeatGen_DiscreteIndexer")) {
	    	featGen = new FeatGen_DiscreteIndexer(this.theObsIntRanges, this.theObsDoubleRanges, 
					this.theActIntRanges, this.theActDoubleRanges);;
		}
    	else if (params.featClass.equals("FeatGen_Discretize")) {
    		featGen = new FeatGen_Discretize(this.theObsIntRanges, this.theObsDoubleRanges, 
					actIntRanges, actDoubleRanges, 
					Integer.valueOf(params.featGenParams.get("numBinsPerDim")));
    	}
    	else if(params.featClass.equals("FeatGen_NoChange")){
    		featGen = new FeatGen_NoChange(this.theObsIntRanges, this.theObsDoubleRanges, 
    				actIntRanges, actDoubleRanges);
    	}
    	else if(params.featClass.equals("FeatGen_RBFs")){
    		featGen = new FeatGen_RBFs(this.theObsIntRanges, this.theObsDoubleRanges, 
    				actIntRanges, actDoubleRanges,
					Integer.valueOf(params.featGenParams.get("basisFcnsPerDim")), 
					Double.valueOf(params.featGenParams.get("relWidth")));
    		if (params.featGenParams.get("normMin") != null &&
    				params.featGenParams.get("normMax") != null)
    			((FeatGen_RBFs)featGen).setNormBounds(Float.valueOf(params.featGenParams.get("normMin")).floatValue(),
    												Float.valueOf(params.featGenParams.get("normMax")).floatValue());
    		if (params.featGenParams.get("biasFeatVal") != null)
    			((FeatGen_RBFs)featGen).setBiasFeatPerAct(Double.valueOf(params.featGenParams.get("biasFeatVal")).doubleValue());
		}
    	else if(params.featClass.equals("FeatGen_Tetris")){
    		featGen = new FeatGen_Tetris(this.theObsIntRanges, this.theObsDoubleRanges, 
    				actIntRanges, actDoubleRanges);
    	}
    	else {
	    		System.out.println("The current code doesn't support class " + params.featClass
    					+ " for feature generation. Adding support might be trivial. " +
    							"(Printed from GeneralAgent.getFeatGen().)");
    	}
    	return featGen;
    }
    
    
    private void setModel() {
		System.out.println("Creating model class " + this.params.modelClass + " for " + 
							this.getClass() + ".");
    	if (this.params.modelClass.equals("IncGDLinearModel")) {
    		System.out.println("featGen before initialization: " + featGen);
    		this.model = new IncGDLinearModel(this.featGen.getNumFeatures(), this.params.stepSize, 
    											this.featGen, this.params.initWtsValue, 
    											this.params.modelAddsBiasFeat);
    		((IncGDLinearModel)this.model).setEligTraceParams(this.params.traceDecayFactor, 
    														this.discountFactorForLearning,
    														this.params.traceType);
    	}
    	else if(this.params.modelClass.equals("WekaModelPerActionModel")) {
    		this.model = new WekaModelPerActionModel(this.params.wekaModelName, 
    												 this.featGen);
    	}
    	else if(this.params.modelClass.equals("WekaModel")) {
    		this.model = new WekaModelWrap(this.featGen, this.params.wekaModelName);
    	}
    	else if(this.params.modelClass.equals("TabularModel")) {
    		System.out.println(this.featGen);
    		if (!this.featGen.getClass().equals(FeatGen_DiscreteIndexer.class)) {
    			System.err.println("Agent's function approx model type TabularModel can " +
    					"only be used with feature generator FeatGen_DiscreteIndexer. " +
    					"Current feat. gen. set to " + this.featGen.getClass().getSimpleName()
    					+ " Exiting.");
    			System.exit(1);
    		}
    		this.model = new TabularModel(this.params.stepSize, 
    				(FeatGen_DiscreteIndexer)(this.featGen), 
    				this.params.initWtsValue);
    		((TabularModel)this.model).setEligTraceParams(this.params.traceDecayFactor, 
																this.discountFactorForLearning,
																this.params.traceType);
    	}
    	else {
	    		System.out.println("The current code doesn't support class " + this.params.modelClass
    					+ " for modeling. Adding support might be trivial." +
						"(Printed from GeneralAgent.getFeatGen().)");
    	}
		if (this.params.initModelWSamples)
			this.model.biasWGenSamples(this.params.numBiasingSamples, this.params.initSampleValue, 
										this.params.biasSampleWt);
    }
	
    
    public void addModelBasedFeat(String taskSpec, RegressionModel model, FeatGenerator featGen) {
    	this.startInitHelper(taskSpec);
    	//this.featGen = getFeatGen();
    	this.featGen = this.getFeatGen(this.params);
    	this.featGen.setSupplModel(model, featGen);
    	System.out.println("\n\nfeatGen in " + this.getClass().getName() + " after adding feature: " + featGen);
    }
    

    

    
    
    
    
    
 

	// Called at the beginning of each episode (in RLViz, it's first called when "Start" is first clicked)
    public Action agent_start(Observation o){
    	if (this.currEpNum == this.numEpsBeforeStop) {
    		System.out.println("Finished " + this.numEpsBeforeStop + " episodes. Exiting.");
    		System.exit(0);
    	}
//    	System.out.println("observation.intArray: " + Arrays.toString(o.intArray));
//    	System.out.println("observation.doubleArray: " + Arrays.toString(o.doubleArray));
//    	System.out.println("observation.charArray: " + Arrays.toString(o.charArray));
    	return this.agent_start(o, Stopwatch.getComparableTimeInSec(), null);
    }
    
    public abstract Action agent_start(Observation o, double time, Action predeterminedAct);

    protected void startHelper(){
    	//System.out.println("startHelper in " + this.getClass().getSimpleName());
    	this.currEpNum += 1;
//    	if (this.currEpNum == this.numEpsBeforePause + 1 && this.isTopLevelAgent)
//    		this.pause = true;
    		
    	while (pause){System.out.print("\n\n.\n\n"); GeneralAgent.sleep(2000);}
    	this.stepsThisEp = 0;
    	this.rewThisEp = 0;
		this.lastObsAndAct = new ObsAndAct();
		//this.hRewCounter = 0;
    }


    
    
    
    
    
    public Action agent_step(double r, Observation o) {
    	return this.agent_step(r, o, Stopwatch.getComparableTimeInSec());
    }
    
    public Action agent_step(double r, Observation o, double startTime){
    	return this.agent_step(r, o, Stopwatch.getComparableTimeInSec(), null);
    }
    
    public abstract Action agent_step(double r, Observation o, double startTime, Action predeterminedAct);
    
    protected void stepStartHelper(double r, Observation o){
    	this.totalSteps++;
    	GeneralAgent.duringStepTransition = true;
    	while (pause){GeneralAgent.sleep(2000); System.out.print("GenAgent.stepStart()");}
    	this.stepsThisEp++;
    	this.totalRew += r;
    	this.rewThisEp += r;
//    	if (!this.isTopLevelAgent)
//    		System.out.println("rewThisEp in " + this.getClass().getName() + ": " + this.rewThisEp);
//    	this.hRewThisStep = this.hRewCounter;
    	this.hRewThisStep = new ArrayList<HRew>(this.hRewList);
    	this.hRewList.clear();
    	ObsAndAct newCurrObsAndAct = new ObsAndAct();
    	newCurrObsAndAct.setObs(o.duplicate());
    	this.currObsAndAct = newCurrObsAndAct;
    	//this.currObsAndAct.setAct(new Action()); I don't think this is good coding. I've commented it out hoping no bugs will come.
    	if (this.verbose)
    		System.out.println("\nAgent on step: " + this.stepsThisEp);

    }
	
	protected void stepEndHelper(double r, Observation o){
		if (this.safeActionOnly) {
			if (this.inTrainSess) {
				System.err.println("Safe action mandated during training! This shouldn't happen!");
				System.exit(1);
			}
			if (this.params.safeAction != null)
				this.currObsAndAct.setAct(this.params.safeAction);
		}
		this.logStep(this.writeLogPath, o, this.currObsAndAct.getAct(), r, this.hRewThisStep, this.stepStartTime);
		this.lastObsAndAct.setAct(this.currObsAndAct.getAct().duplicate()); // might be overridden by TamerRLAgent. TODO seems like the following two lines should be done in stepStartHelper()
		this.lastObsAndAct.setObs(this.currObsAndAct.getObs().duplicate());
		if (verbose)
			System.out.println("act chosen: " + this.currObsAndAct.getAct().intArray[0]);
//		System.out.println("Steps this ep: " + this.stepsThisEp);
		if (this.stepsThisEp == GeneralAgent.MAX_STEPS_SET_BY_EXP) {
			if (this.isTopLevelAgent)
				System.out.println("At end of steps!!");
		   	this.logStep(this.writeLogPath, null, null, r, this.hRewThisStep, this.stepStartTime);
	    	if (this.recordRew && this.masterLogSwitch) {
				this.recHandler.writeLineToRewLog(this.writeRewPath, (this.rewThisEp + ""), true);
			}
		}
		this.lastStepStartTime = this.stepStartTime;
		GeneralAgent.duringStepTransition = false;
    }
	
	
	
	
	
	
	
    public abstract void agent_end(double r, double time);

    public void agent_end(double r){
    	agent_end(r, Stopwatch.getComparableTimeInSec());
    }
    
    public void endHelper(double r){
    	if (Double.isNaN(r)) {
    		System.err.println("Received NaN in agent_end()");
    	}
    	this.totalSteps++; 
    	this.stepsThisEp++;
    	this.totalRew += r;
    	this.rewThisEp += r;
    	this.hRewThisStep = new ArrayList<HRew>(this.hRewList);
    	this.hRewList.clear();
    	this.logStep(this.writeLogPath, null, null, r, this.hRewThisStep, this.stepStartTime);
    	this.currObsAndAct.setAct(new Action());
//    	if (!this.isTopLevelAgent)
//    		System.out.println("rewThisEp in " + this.getClass().getName() + ": " + this.rewThisEp);
    	if (this.recordRew && this.masterLogSwitch) {
			this.recHandler.writeLineToRewLog(this.writeRewPath, (this.rewThisEp + ""), true);
		}
    	if (verbose) {
    		System.out.println("Steps in episode: " + this.stepsThisEp);
        	System.out.println("Reward in episode: \t\t" + this.rewThisEp);
    	}
    	if (this.currEpNum % 50 == 0)
    		println("Mean reward after " + this.currEpNum + " episodes: " + (this.totalRew / this.currEpNum));
    }
    
    

    
    public void agent_cleanup() {System.out.println("Cleaning up " + this.getClass().getSimpleName() + ".");}
    
    

//	public double getValForLastStep(){
////		System.out.println("lastO,lastA in " + this.getClass() + ": " + Arrays.toString(this.lastObs.doubleArray) + Arrays.toString(this.lastAct.intArray));
//		return model.predictLabel(this.lastObsAndAct.getObs(), this.lastObsAndAct.getAct());
//	}
    
    public double getVal(ObsAndAct obsAct){
		return getVal(obsAct.getObs(), obsAct.getAct());
	}
	public double getVal(Observation obs, Action act){
		if (obs == null || act == null) {
			return 0.0;
		}		
		return model.predictLabel(obs, act);
	}
	
	public double getStatePotForTrans(Observation fromObs, Observation toObs){
		if (fromObs == null || toObs == null) {
			return 0.0;
		}
		Action lastGreedyAction = this.actSelector.greedyActSelect(fromObs, null);
		double[] lastStepGreedyFeats = this.featGen.getSAFeats(fromObs, lastGreedyAction);
		double lastStepGreedyVal = model.predictLabel(lastStepGreedyFeats);
		
		double thisStepGreedyVal = 0.0;
		if (toObs != null) { // i.e., not a terminating state
			Action thisGreedyAction = this.actSelector.greedyActSelect(toObs, null);
			double[] thisStepGreedyFeats = this.featGen.getSAFeats(toObs, thisGreedyAction);
			thisStepGreedyVal = model.predictLabel(thisStepGreedyFeats);
		}

		double changeInStatePot = (this.discountFactorForLearning.getValue() * thisStepGreedyVal) - lastStepGreedyVal;
		if (verbose)
			System.out.println("changeInStatePot: " + changeInStatePot);
		return changeInStatePot;
	}

	public double getSAPotForTrans(Observation fromObs, Action fromAct, Observation toObs, Action toAct){
		if (fromObs == null) {
			return 0.0;
		}
//		System.out.println("fromO,lastA: " + Arrays.toString(fromO.doubleArray) + ", " +  fromAct.intArray[0]);
		
		double lastStepSAVal = model.predictLabel(fromObs, fromAct);
//		System.out.println("lastStepSAVal: " + lastStepSAVal);
		
		double thisStepSAVal = 0.0;
		if (toObs != null) { // i.e., not a terminating state
			thisStepSAVal = model.predictLabel(toObs, toAct
					);
//			System.out.println("thisO,thisAct: " + Arrays.toString(o.doubleArray) + ", " + act.intArray[0]);
		}
		

//		for (int i = 0; i < 2; i++) {
//			int[] actIntArray = new int[1];
//			actIntArray[0] = i;
//			double thisActSAVal = (o == null) ? 0.0 : model.predictLabel(o.intArray, o.doubleArray, 
//					actIntArray, act.doubleArray);
////			System.out.println("thisActSAVal: " + thisActSAVal);
//			double changeInStatePotForAllActs = (this.discountFactor * thisActSAVal) - lastStepSAVal;
////			System.out.println("Change in SAPot if act " + i + " taken: " + changeInStatePotForAllActs);
//		}
			
//		System.out.println("action: " + act.intArray[0]);
//		System.out.println("thisStepSAVal: " + thisStepSAVal);
		double changeInStatePot = (this.discountFactorForLearning.getValue() * thisStepSAVal) - lastStepSAVal;
		if (verbose)
			System.out.println("changeInSAPot: " + changeInStatePot);
		return changeInStatePot;
	}


    public String agent_message(String theMessage) {
        AgentMessages theMessageObject;
        try {
            theMessageObject = AgentMessageParser.parseMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("Someone sent random agent a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }
        catch (Exception e){
        	System.err.println("Exception while parsing message: " + e);
        	return "There was a problem with this message.";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            return theMessageObject.handleAutomatically(this);
        }
//        System.err.println("Didn't know how to respond to message.");
        return null;
    }
    

    
    
	public void receiveKeyInput(char c){
		if (c == 'T') {
			this.actSelector.setTreeSearchFlag(!this.actSelector.getTreeSearchFlag());
			if (this.actSelector.getEnvTransModel() == null) {
				System.out.println("Can't do tree search. No environment model.");
				this.actSelector.setTreeSearchFlag(false);
			}
		}
		else if (c == 'C') {
			this.model.changeClassifier();
		}
	}
  

	public void toggleInTrainSess() {
			this.inTrainSess = !this.inTrainSess;
			if (this.inTrainSess)
				System.out.println("---Starting training session.---");
			else
				System.out.println("---Ending training session---");
	}
	
	public void togglePause() {
		this.pause = !this.pause;
		if (this.pause)
			System.out.println("Agent paused.");
		else
			System.out.println("Agent unpaused.");
	}
	
	public static void sleep(double milliseconds){
		try{
			TimeUnit.MILLISECONDS.sleep((int)milliseconds);
		}
		catch (java.lang.InterruptedException e){
			System.err.println("Exception while trying to sleep: " + e);
		}
	}

    public static String getEnvName(String extraStr) { 
    	String envName = "";
    	String[] extraStrComponents = extraStr.split(" ");
    	for (int i = 0; i < extraStrComponents.length; i++) {
    		if (extraStrComponents[i].startsWith("EnvName:")) {
    			envName = extraStrComponents[i].split(":")[1];
    		}
    	}
    	return envName;
    }
    
    public void setMasterLogSwitch(boolean on) {
		this.masterLogSwitch = on;
	}
    

    /**
     * Ensure that the observation fits expectations for the number of ints and doubles.
     * 
     * @param obs
     */
    public void checkObs(Observation obs) {
    	if (obs == null)
    		System.err.println("Observation is null.");
    	if (obs.intArray.length != this.theObsIntRanges.length)
    		System.err.println("Unexpected number of observation integers: " + obs.intArray.length + ". Expected: " + this.theObsIntRanges.length);
    	if (obs.doubleArray.length != this.theObsDoubleRanges.length)
    		System.err.println("Unexpected number of observation doubles: " + obs.doubleArray.length + ". Expected: " + this.theObsDoubleRanges.length);
    }
    
	protected void overwriteLastObsAndAct(GeneralAgent agent) {
		agent.lastObsAndAct.setObs(this.lastObsAndAct.obsIsNull() ? null : this.lastObsAndAct.getObs().duplicate());
		agent.lastObsAndAct.setAct(this.lastObsAndAct.actIsNull() ? null : this.lastObsAndAct.getAct().duplicate());
	}
    
	protected void logStep(String path, Observation o, Action a, 
			double rew, ArrayList<HRew> hRewThisStep, double timeStamp) {
		if (this.recordLog && this.masterLogSwitch){
			//System.out.println("logStep in " + this.getClass().getName());
			this.recHandler.writeTimeStep(path, o, a, rew, hRewThisStep, timeStamp, this.inTrainSess);
			if (o == null){
				this.recHandler.writeEpEnd(path);
			}
		}
	}
	
	public String makeEndInfoStr(){
		return "";
	}
	
    public URL getImageURL() {
        return this.getClass().getResource("/images/randomagent.png");
    }
    
    
    

    public void runSelf(){
    	TaskSpecVRLGLUE3 theTaskSpecObject = new TaskSpecVRLGLUE3();
    	if (this.envName == null)
    		this.envName = "CarStop";
    	if (this.envName.equals("CarStop")) {
	    	theTaskSpecObject.setEpisodic();
	    	theTaskSpecObject.setDiscountFactor(1.0);
	    	theTaskSpecObject.addContinuousObservation(new DoubleRange(-50, 100));
	    	theTaskSpecObject.addContinuousObservation(new DoubleRange(0, 12));
	    	theTaskSpecObject.addDiscreteAction(new IntRange(-1, 1)); //Specify that there will be an integer action [0,3]
	    	theTaskSpecObject.setRewardRange(new DoubleRange(-500d, 1000.0d)); //Specify the reward range [-100,10]
    	}
    	else if (this.envName.equals("FuelWorld")){
    		theTaskSpecObject.setEpisodic();
	    	theTaskSpecObject.setDiscountFactor(1.0);
	    	//theTaskSpecObject.addContinuousObservation(new DoubleRange(-50, 100));
	    	//theTaskSpecObject.addContinuousObservation(new DoubleRange(0, 12));
	    	//theTaskSpecObject.addDiscreteAction(new IntRange(0, 4)); //Specify that there will be an integer action [0,3]
	    	//theTaskSpecObject.setRewardRange(new DoubleRange(-500d, 1000.0d)); //Specify the reward range [-100,10]
    	}
    	else {
    		System.err.println("Environment " + this.envName + " not supported. Agent exiting.");
    		System.exit(1);
    	}
    	theTaskSpecObject.setExtra("EnvName:" + this.envName);
    	String taskSpecString = theTaskSpecObject.toTaskSpec();
    	TaskSpec.checkTaskSpec(taskSpecString);
    	agent_init(taskSpecString);

    	try {
    		DatagramPacket incomingPacket;
    		DatagramSocket dataSocket = new DatagramSocket(serverPort);

    		while (true) {
    			byte[] buffer = new byte[1024];
    			incomingPacket = new DatagramPacket(buffer, buffer.length);
    			try {
    				dataSocket.receive(incomingPacket);
    				processTimeStepMessage(dataSocket, incomingPacket);
    			}
    			catch (IOException e) {
    				System.err.println(e);
    			}      
    		} // end while
    	}  // end try
    	catch (Exception e) {
    		System.err.println(e);
    	}  // end catch
    }

    public void processTimeStepMessage(DatagramSocket dataSocket, DatagramPacket dataPacket) {
    	String dataStr = new String(dataPacket.getData());
    	String[] dataStrs = dataStr.split(",");
    	
    	float reward = Float.valueOf(dataStrs[0]).floatValue();
    	float[] state = new float[dataStrs.length - 2];
    	for (int i = 0; i < dataStrs.length - 2; i++) {
    		state[i] = Float.valueOf(dataStrs[i + 1]).floatValue();
    	}	
    	boolean terminal = (Integer.valueOf(dataStrs[dataStrs.length - 1].trim()).intValue() != 0);
    	
    	Observation o = new Observation(0, state.length, 0);
    	for (int i = 0; i < state.length; i++) {
	    	o.setDouble(i, state[i]);
    	}

    	Reward_observation_terminal rewardObs = new Reward_observation_terminal();
    	rewardObs.setObservation(o);
    	rewardObs.setTerminal(terminal);
    	rewardObs.setReward(reward);
    	
    	Action act = null;
		//if (!learningOnly)
		//	; // get predetermined action from message
		respondWithAction(rewardObs, act);
    }
    	
	public void respondWithAction(Reward_observation_terminal rewardObs, Action act) {
    	boolean respondWH = false;
		if (act != null)
    		respondWH = true;
    	if (!startCalled) {
    		act = agent_start(rewardObs.getObservation(), Stopwatch.getComparableTimeInSec(), act);
    		if (!respondWH)
    			this.sendAction(act);
    		//else
    			//this.sendH();
    		startCalled = true;
    	} 
    	else { // agent_start() already called this episode
    		if (!rewardObs.isTerminal()) {
    			act = agent_step(rewardObs.getReward(), rewardObs.getObservation(), Stopwatch.getComparableTimeInSec(), act);
    			if (!respondWH)
        			this.sendAction(act);
        		//else
        			//this.sendH();	
    		} 
    		else {
    			agent_end(rewardObs.getReward());
    			startCalled = false;
    			if (this.envName.equals("FuelWorld")){
        			double[] linModelWts = ((IncGDLinearModel)this.model).getWeights(); 
        			//// Chiu: save H parameters to file for Todd's code to use 
        		}
    		}
    	}
    }
	
	public void addHRew(double feedbackVal) {
		this.hRewList.add(new HRew(feedbackVal, Stopwatch.getComparableTimeInSec()));
	}
	


    public void sendAction(Action act) {
    	try {
    		InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
    		//// Chiu: this needs to be able to pass the whole action vector, act.intArray
    		ByteBuffer byteBuff = ByteBuffer.allocate(4);
    		byteBuff.putInt(act.getInt(0));

    		DatagramSocket socket = new DatagramSocket();
    		DatagramPacket packet = new DatagramPacket(byteBuff.array(), byteBuff.capacity(), serverAddr, serverPort + 1);

    		/* Send out the packet */
    		socket.send(packet);
    	} 
    	catch (Exception e) {
    	}
    }

//    public void sendH() {
//    	try {
//    		InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
//    		
//    		//// get model parameters string
//    		
//    		ByteBuffer byteBuff = ByteBuffer.allocate(4);
//    		//byteBuff.(modelParamsStr);
//
//    		DatagramSocket socket = new DatagramSocket();
//    		DatagramPacket packet = new DatagramPacket(byteBuff.array(), byteBuff.capacity(), serverAddr, serverPort + 1);
//
//    		/* Send out the packet */
//    		socket.send(packet);
//    	} 
//    	catch (Exception e) {
//    	}
//    }

    
    public void println(String str){
    	if (verbose)
    		System.out.println(str);
    }

}
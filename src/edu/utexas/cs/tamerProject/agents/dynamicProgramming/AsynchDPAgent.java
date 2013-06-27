package edu.utexas.cs.tamerProject.agents.dynamicProgramming;

import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.tamer.HRew;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.envModels.rewModels.LoopMazeRewModel;
import edu.utexas.cs.tamerProject.envModels.transModels.LoopMazeTransModel;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.featGen.FeatGen_DiscreteIndexer;
import edu.utexas.cs.tamerProject.featGen.FeatGen_Discretize;
import edu.utexas.cs.tamerProject.featGen.FeatGen_NoChange;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.TabularModel;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.planning.TreeSearchNode;
import edu.utexas.cs.tamerProject.planning.UCTNode;
import edu.utexas.cs.tamerProject.trainInterface.TrainerListener;
import edu.utexas.cs.tamerProject.utils.Stopwatch;

/**
 * Agent that plans and updates value function by Upper Confidence Trees
 * Monte Carlo search.
 * 
 * This agent assumes that the state and action space are discrete and that 
 * transitions are deterministic.
 * 
 * @author bradknox
 *
 */
public class AsynchDPAgent extends GeneralAgent {

	
	public int PLANNING_DEPTH = 10;
	public TamerAgent tamerAgent;
	protected EnvTransModel envTransModel;
	protected volatile ObsActModel rewReadOnlyModel;

	protected TreeSearchNode planningTree = null;
	protected Observation planningStartObs = null;
	public boolean deterministicTransitions;
	
//	Action dummyActForFeats;
	public boolean useTamer = true;
	public boolean giveTieToLastAct = false;
	public int startActIndex = -1;
	public boolean planAtOwnPace = true;
//	Object rewModelLock = new Object();
	Object valModelLock = new Object();
	Object treeLock = new Object();
	
	Observation[] legalObservations;
	Observation[] nonTermLegalObservations;
	Action[] possibleActions;
	
	protected String writePredHRewDir;
	protected String writePredHRewPath;
	protected double predHRewThisEp = 0;
	Stopwatch agentStopwatch = new Stopwatch();
	
//	Timer mcSampleTimer;
	Thread planningThread = null;
	//public long timeBtwnMCSamples = 1000; // in milliseconds	
	//public long timeMCSamplingTilWait = 20; // in milliseconds
	//public long waitTimeForMCSampling = 2; // in milliseconds
	int numMCSamples = 0;
	public boolean printPostSample = false;
	
	
	
	
	public void setEnvTransModel(EnvTransModel model){this.envTransModel = model;}
	public void setRewModel(ObsActModel model){this.rewReadOnlyModel = model;}
	public void setDiscountFactorForLearning(double df) {
		super.setDiscountFactorForLearning(df);
		UCTNode.setDiscountFactor(this.discountFactorForLearning.getValue());
	}

	public void processPreInitArgs(String[] args) {
		super.processPreInitArgs(args);
		System.out.println("\n[------Sarsa process pre-init args in AsynchDPAgent------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];
    		if ((argType.equals("-initialValue")) && (i+1) < args.length){
    			if (args[i+1].equals("zero")) 
    				this.params.initWtsValue = 0;
    			else {
    				System.out.println("\nIllegal AsynchDPAgent initial values type. Exiting.\n\n");
    				System.exit(1);
    			}
				System.out.println("Sarsa's Q-model's initial weights set to: " 
									+ this.params.initWtsValue);
			}
		}
	}
	public void processPostInitArgs(String[] args) {
		System.out.println("\n[------process post-init args in AsynchDPAgent------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];

    		if ((argType.equals("-discountParam")) && (i+1) < args.length){
    			double discountFactor = Double.valueOf(args[i+1]);
    			setDiscountFactorForLearning(discountFactor); // override env discount factor for TD updates
    			//actSelector.setDiscountParam(ActionSelect.discFactorToParam(discountFactor)); // override for planning-based action selection
				System.out.println("discount factor set to: " + discountFactor);
			}
		}
	}
	
	

	public void agent_init(String taskSpec) {
	   	System.out.println("\n\n\n----Agent " + getClass().getName() + " is being initialized.----");
    	startInitHelper(taskSpec); 
    	this.agentStopwatch.startTimer();
    	
		/*
		 * INITIALIZE FeatGenerator for state value function (no action input) TODO change to general feat gen
		 */
    	FeatGen_DiscreteIndexer discIndFeatGen = new FeatGen_DiscreteIndexer(this.theObsIntRanges, this.theObsDoubleRanges, 
				this.theActIntRanges, this.theActDoubleRanges);
    	this.featGen = discIndFeatGen;
    	if (!this.params.featClass.equals("FeatGen_DiscreteIndexer") && !this.params.featClass.equals("None")) {
    		System.err.println("AsynchDPAgent hard-codes FeatGenerator child class to be FeatGen_DiscreteIndexer, but the agent params ask for something else (" + this.params.featClass.toString() + ").");
    		System.exit(1);
    	}
    	
//    	this.featGen = new FeatGen_Discretize(this.theObsIntRanges, this.theObsDoubleRanges, TODO Get rid of this later
//				this.theActIntRanges, this.theActDoubleRanges, 
//				Integer.valueOf(params.featGenParams.get("numBinsPerDim")));
    	

		/*
		 * INITIALIZE RegressionModel - step size of 1 for DP
		 */
    	this.model = new TabularModel(this.params.stepSize, 
				discIndFeatGen, this.params.initWtsValue);

 //    	this.model = new IncGDLinearModel(this.featGen.getNumFeatures(), this.params.stepSize, TODO Get rid of this later 
//				this.featGen, this.params.initWtsValue, 
//				this.params.modelAddsBiasFeat);
//    	((IncGDLinearModel)this.model).setDiscountFactor(0);

    	
    	

		/*
		 * INITIALIZE TAMER AGENT, which creates the human model that acts as an MDP reward function
		 */    	
    	if (this.tamerAgent == null)
    		this.tamerAgent = new TamerAgent();
    	this.tamerAgent.setIsTopLevelAgent(false);
    	this.tamerAgent.enableGUI = false;
    	if (this.trainFromLog) {
    		this.tamerAgent.trainFromLog = true;
	    	this.tamerAgent.trainLogPath = this.trainLogPath;
    		this.trainFromLog = false;
    	}
    	this.tamerAgent.agent_init(taskSpec);
    	
    	
    	
    	/*
    	 * INITIALIZE REWARD MODEL if using TAMER to make model
    	 */
		if (this.useTamer)
       		this.rewReadOnlyModel = this.tamerAgent.model.makeFullCopy();
		
    	
		/*
		 * Set static fields of class for Upper Confidence Trees
		 */
		UCTNode.setDiscountFactor(this.discountFactorForLearning.getValue());
		UCTNode.setConfidenceConstant(1);
		UCTNode.setDeterministicTransitions(this.deterministicTransitions);
		ArrayList<Action> possActionsList = this.featGen.getPossActions(null);
		UCTNode.setPossibleActions(possActionsList.toArray(new Action[possActionsList.size()]));
		
		/*
		 * INITIALIZE ActionSelect
		 */
		this.possibleActions = getPossibleActions();
		this.legalObservations = getLegalObservations();
		this.nonTermLegalObservations = getNonTermLegalObservations();
		
    	
		/*
		 *  Write parameters to log files
		 */
		this.recHandler = new RecordHandler(GeneralAgent.canWriteToFile);
    	this.writePredHRewDir = RLLIBRARY_PATH + "/data/" + this.expName; 
    	this.writePredHRewPath = writePredHRewDir + "/" + "HRew-" + this.unique
    					+ ".rew";
		if (this.masterLogSwitch) {
			if (this.recordLog) {
	    		System.out.println("Log base path: " + this.writeLogDir);
	    		if (this.recHandler.canWriteToFile) {
	    			(new File(this.writeLogDir)).mkdir();
	    		}
	    		this.recHandler.writeParamsToFullLog(this.writeLogPath, this.params);
	    	}
			if (this.recordRew) {
				System.out.println("Reward log base path: " + this.writePredHRewDir);
				if (this.recHandler.canWriteToFile)
					(new File(this.writeLogDir)).mkdir();
				System.out.println("this.writePredHRewPath: " + this.writePredHRewPath);
	    		this.recHandler.writeParamsToRewLog(this.writePredHRewPath, this.params);
			}
			
		}
		
		
		if (!GeneralAgent.isApplet && enableGUI) {
			//Schedule a job for event dispatch thread:
	        //creating and showing this application's GUI.
	        javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					TrainerListener.createAndShowGUI(AsynchDPAgent.this);
				}
			});
		}
		else {
			System.out.println("No " + this.getClass().getSimpleName() + " GUI for you!");
			System.out.println("GeneralAgent.isApplet: " + GeneralAgent.isApplet);
			System.out.println("enableGUI: " + enableGUI);
		}
		
		
		/*
		 * Start "planning" thread
		 */
		System.out.println("Planning depth at agent initialization: " + this.PLANNING_DEPTH);
		if(this.planAtOwnPace)
			createPlanningThread();
		
		this.endInitHelper();
	}

	public void createPlanningThread(){
		//killPlanningThread(); // TODO update

		planningThread = new Thread(new PlanningRunnable());
		planningThread.start();
		
//		mcSampleTimer = new Timer();
//        mcSampleTimer.schedule(new TimerTask() {
//            public void run() {
//            	synchronized(treeLock) {
//    				createMCSample();    	            
//            	}
//            }
//		}, new Date(), timeBtwnMCSamples);
	}
	
	public class PlanningRunnable implements Runnable {
	    public void run() {
	    	Thread.currentThread().setName("PlanningThread-AsynchDPAgent");
    		Stopwatch treeSearchStopWatch = new Stopwatch();
    		treeSearchStopWatch.startTimer();
    		while (true) {
    			//System.out.println("\n\nTime tree search rollout started at: " + AsynchDPAgent.this.agentStopwatch.getTimeElapsed()); //((System.currentTimeMillis() / 1000.0) % 1000))
    			//System.out.flush();
    			createMCSample();
//    			if (treeSearchStopWatch.getTimeElapsed() / 1000.0 < AsynchDPAgent.this.timeMCSamplingTilWait) {
//               		createMCSample();
//    			}
//    			else {
//    				GeneralAgent.sleep(AsynchDPAgent.this.waitTimeForMCSampling);
//    				treeSearchStopWatch.startTimer();
//    			}
    		}
	    }
	}
	
	
	
	public void createMCSample() {
		//System.out.println("Time createMCSample() started at: " + ((System.currentTimeMillis() / 1000.0) % 1000));//this.agentStopwatch.getTimeElapsed());
		//System.out.flush();
		if (AsynchDPAgent.this.currObsAndAct.getObs() != null) {
			// If current state has changed, remove or cut tree
        	if (AsynchDPAgent.this.planningTree == null ||
        				AsynchDPAgent.this.planningStartObs != AsynchDPAgent.this.currObsAndAct.getObs()) {
        		//System.out.println("Setting new observation in root");
        		System.out.println("Num MC samples so far: " + this.numMCSamples);
        		System.out.flush();
	        		

        		
           		RegressionModel valFcnModel;
           		if (AsynchDPAgent.this.planningTree == null) { 		// first planning tree ever for this agent
           			valFcnModel = AsynchDPAgent.this.model.makeFullCopy(); // this should be the only thread to modify this.model (atomically), so no synchronization needed 
           		}
           		else { // planning has occured before and obs is new
           				valFcnModel = AsynchDPAgent.this.planningTree.qFunction;
           		}
        		 
    			
        		AsynchDPAgent.this.planningTree = new UCTNode(null, AsynchDPAgent.this.currObsAndAct.getObs(), 
        												false, AsynchDPAgent.this.envTransModel,
        												AsynchDPAgent.this.rewReadOnlyModel, valFcnModel, 
        												AsynchDPAgent.this.featGen);
        		AsynchDPAgent.this.planningStartObs = AsynchDPAgent.this.currObsAndAct.getObs();
    		
        	}
	        	
        	//System.out.println("Time past root node creation: " + ((System.currentTimeMillis() / 1000.0) % 1000));//this.agentStopwatch.getTimeElapsed());
        	//System.out.flush();
        	
        	// TODO? tree search could be called concurrently
        	//Stopwatch pathSampleStopwatch = new Stopwatch();
    		//pathSampleStopwatch.startTimer();
       		
    		//System.out.println();
    		
        	UCTNode.nodeStopWatch.startTimer();
    		this.planningTree.createTreePathSample(PLANNING_DEPTH);
    		//System.out.println("Time after sample created. making val fcn copy: " + ((System.currentTimeMillis() / 1000.0) % 1000));//this.agentStopwatch.getTimeElapsed());
    		//System.out.flush();
    		RegressionModel newValFcnModel = this.planningTree.qFunction.makeFullCopy();
    		this.model = newValFcnModel; // atomic action w/ write to volatile variable shouldn't need synchronization
    		//System.out.println("Time after val fcn copy: " + ((System.currentTimeMillis() / 1000.0) % 1000));//this.agentStopwatch.getTimeElapsed());
    		//System.out.flush();
    		
    		//System.out.println("MC path total sample time: " + pathSampleStopwatch.getTimeElapsed());
    		//System.out.flush();
    		this.numMCSamples++;
    		//System.out.println("Num samples so far: " + this.numMCSamples);
    		//if (this.printPostSample) {
    			//System.out.println("\n" + stateActValsToStr());
    		//}
    		//System.out.println("Time at end of tree search iteration: " + ((System.currentTimeMillis() / 1000.0) % 1000));

    	}
					
	}
	
	

	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Action agent_start(Observation o, double time,
			Action predeterminedAct) {
		System.out.println("------------------AsynchDPAgent start------------------");
    	this.startHelper();
    	System.out.println("discount factor: " + this.discountFactorForLearning);
    	predHRewThisEp = 0;
    	this.tamerAgent.agent_start(o, time, new Action());
    	this.currObsAndAct.setAct(agent_step(0.0, o, time, predeterminedAct));
    	this.tamerAgent.lastObsAndAct.setAct(this.currObsAndAct.getAct().duplicate());
    	return this.currObsAndAct.getAct();
	}

	
	
	public Action agent_step(double r, Observation o, double startTime,
			Action predeterminedAct) {
    	System.out.println("\n-----------------AsynchDPAgent step " + this.totalSteps + "---------------\n");
    	System.out.println("observation: " + Arrays.toString(o.intArray));
    	System.out.println("reward: " + r);
    	//System.out.println("state values: " + this.stateValsToStr());
    	this.stepStartTime = startTime;
    	//synchronized(treeLock) { // to keep this.currObsAndAct from being read to create a new tree node while it's being changed
    		this.stepStartHelper(r, o);
    	//}
    	/*
    	 * TAMER UPDATE
    	 */
    	tamerAgent.hRewList = new ArrayList<HRew>(this.hRewThisStep);
    	if (this.stepsThisEp > 1) {
    		this.currObsAndAct.setAct(new Action()); // sending an empty action instead of a null one to avoid the comp. cost of an extra action selection. OK b/c TamerAgent's apparent action isn't used for update if !tamerAgent.isTopLevelAgent. 
       		tamerAgent.agent_step(r, o, this.stepStartTime, this.currObsAndAct.getAct());
        	ObsActModel newRewReadOnlyModel = tamerAgent.model.makeFullCopy();
        	this.rewReadOnlyModel = newRewReadOnlyModel;
    	}
    	
    	
    	/*
    	 * Create look-up table for reward function; only useful for discrete state-action spaces
    	 */
//    	if (this.useTamer) {
//    		synchronized(this.rewModelLock) {
//    			this.rewModel = makeFastModel(this.tamerAgent.model);
//    		}
//    	}
    	
		/*
		 * GET ACTION
		 */
    	if (predeterminedAct == null) {
    		Action dummyAction = new Action();
    		int[] dummyActInt = {0};
    		dummyAction.intArray = dummyActInt;
    		//this.currObsAndAct.setAct(this.actSelector.selectAction(o, this.lastObsAndAct.getAct()));
    		this.currObsAndAct.setAct(this.chooseGreedyAct(o));
    	}
		else {
			this.currObsAndAct.setAct(predeterminedAct);
		}
    	
    	/*
    	 * ACTION-SPECIFIC TAMER UPDATES
    	 */
    	this.tamerAgent.hLearner.recordTimeStepStart(this.tamerAgent.featGen.getFeats(o, this.currObsAndAct.getAct()), startTime); // called here b/c action was unknown at the time of tamerAgent's agent_step()
    	

	    // TODO UPDATE with actual experience? This would complicate code with a trivial performance impact unless I use eligibility traces.
    	
		this.stepEndHelper(r, o);
		
		System.out.println("Features for this state-action: " + Arrays.toString(this.featGen.getSAFeats(o, this.currObsAndAct.getAct())));
		System.out.println("Predicted reward for next state-action: " + this.tamerAgent.model.predictLabel(o, this.currObsAndAct.getAct()));
		System.out.println("Value for next state-action: " + this.model.predictLabel(o, this.currObsAndAct.getAct()));
		//System.out.println(this.agentStopwatch.getTimeElapsed());
		
        return this.currObsAndAct.getAct();
	}
	


	
    protected void stepEndHelper(double r, Observation o) {
    	super.stepEndHelper(r, o);
    	this.overwriteLastObsAndAct(this.tamerAgent);
		if (this.stepsThisEp == GeneralAgent.MAX_STEPS_SET_BY_EXP) {
			if (this.isTopLevelAgent)
				System.out.println("At end of steps!!");
	    	if (this.recordRew && this.masterLogSwitch) {
				this.recHandler.writeLineToRewLog(this.writePredHRewPath, (this.predHRewThisEp + ""), true);
			}
		}
    }
    


    
    

    public void agent_end(double r, double time) {
    	System.out.println("----------------AsynchDPAgent end---------------");
    	System.out.println("reward at end: " + r);
    	this.stepStartTime = time;
    	this.endHelper(r);
    	
    	/*
    	 * TAMER UPDATE
    	 */
    	tamerAgent.hRewList = new ArrayList<HRew>(this.hRewThisStep);
    	this.tamerAgent.agent_end(r, time);
    }
    
    public void endHelper(double r){
    	super.endHelper(r);
    	if (this.recordRew && this.masterLogSwitch) {
			this.recHandler.writeLineToRewLog(this.writePredHRewPath, (this.predHRewThisEp + ""), true);
		}
    }
    

	public String makeEndInfoStr(){
		String endInfoStr = "numMCSamples, " + this.numMCSamples;
		endInfoStr += "\n" + "agent total time steps, " + this.totalSteps;
		endInfoStr += "\n" + "agent run time, " + this.agentStopwatch.getTimeElapsed();
		//endInfoStr += "\n\nvalue function,\n" + this.stateValsToStr();
		endInfoStr += "\n\nrew vals,";
		for (Observation obs : nonTermLegalObservations) {
			for (Action act: possibleActions) {
				double reward = Double.NEGATIVE_INFINITY;
				reward = this.rewReadOnlyModel.predictLabel(obs, act);
				endInfoStr += "\n" + Arrays.toString(obs.intArray) + ", " 
							+ Arrays.toString(act.intArray) + ", "
							+ reward;
			}
		}
		endInfoStr += "\n";
		return endInfoStr;
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	
	
	/**
	 * Helper/minor methods
	 */
    public void initParams(String envName){
    	super.initParams(envName);
    	tamerAgent.initParams(envName);
    }
	public void receiveKeyInput(char c){
		System.out.println("char in AsynchDPAgent: " + c);
		if (c == '/') {
			this.addHRew(1.0);
			System.out.println("+1: " + this.agentStopwatch.getTimeElapsed());
		}
		else if (c == 'z') {
			this.addHRew(-1.0);
			System.out.println("-1: " + this.agentStopwatch.getTimeElapsed());
		}
		else if (c == ' ' && this.allowUserToggledTraining) {
			this.toggleInTrainSess();
		}
	}
	
	public void toggleInTrainSess() {
		this.tamerAgent.setInTrainSess(!this.tamerAgent.getInTrainSess());
		this.inTrainSess = !this.inTrainSess;
	}
	
	private Action[] getPossibleActions(){
		ArrayList<Action> possActionsList = (new FeatGen_NoChange(this.theObsIntRanges, this.theObsDoubleRanges, 
				this.theActIntRanges, this.theActDoubleRanges)).getPossActions(null);
		Action[] possActionsArray = new Action[possActionsList.size()];
		for (int i = 0; i < possActionsList.size(); i++) {
			possActionsArray[i] = possActionsList.get(i);
		}
		return possActionsArray;
	}
	
	/**
	 * Make a list of observations (states, really) that are not terminal.
	 */
	private Observation[] getNonTermLegalObservations(){
		ArrayList<Observation> nonTermLegalObsList = new ArrayList<Observation>();
		for (Observation obs : this.legalObservations) {
			if (!this.envTransModel.isObsTerminal(obs)) {
				nonTermLegalObsList.add(obs.duplicate());
			}
		}
		
		Observation[] nonTermLegalObsArray = new Observation[nonTermLegalObsList.size()];
		for (int i = 0; i < nonTermLegalObsList.size(); i++){
			nonTermLegalObsArray[i] = nonTermLegalObsList.get(i);
			//	System.out.println("Non-term legal obs: " + nonTermLegalObsArray[i]);
		}
		
		return nonTermLegalObsArray;
	}
	
	private Observation[] getLegalObservations(){
		ArrayList<int[]> possObsIntArrays = getPossObsIntArrays();
		ArrayList<Observation> possObsList = new ArrayList<Observation>();
		
		/*
		 * Make each possible observation in array into an observation. Check if it
		 * is legal. If so, add to possObsList.
		 */
		for (int[] obsIntArray : possObsIntArrays) {
			Observation obs = new Observation();
			obs.intArray = obsIntArray;
			//System.out.println(Arrays.toString(obsIntArray));
			if (this.envTransModel.isObsLegal(obs))
				possObsList.add(obs);
		}
		
		Observation[] possObsArray = new Observation[possObsList.size()];
		for (int i = 0; i < possObsList.size(); i++){
			possObsArray[i] = possObsList.get(i);
			//System.out.println("Legal obs: " + possObsArray[i]);
		}
		
		return possObsArray;
	}
	
	
	
	protected ArrayList<int[]> getPossObsIntArrays() {
		return this.recurseForPossObsIntArrays(new int[0]);
	}
	protected ArrayList<int[]> recurseForPossObsIntArrays(int[] actSoFar){
		if (actSoFar.length ==  this.theObsIntRanges.length) { // base case
			ArrayList<int[]> list = new ArrayList<int[]>();
			list.add(actSoFar);
			return list;
		}
		int currObsIndex = actSoFar.length;
//		int a = this.theObsIntRanges[currObsIndex][1];
//		int b = this.theObsIntRanges[currObsIndex][0];
		int numPossibleValues = (this.theObsIntRanges[currObsIndex][1] 
		                          - this.theObsIntRanges[currObsIndex][0]) + 1;
		ArrayList<int[]> fullObss = new ArrayList<int[]>();
		// iterate through all possible values of the next action integer
		for (int i = 0; i < numPossibleValues; i++){
			int currVal = theObsIntRanges[currObsIndex][0] + i;
			int[] newObsSoFar = new int[currObsIndex + 1];
			for (int j = 0; j < actSoFar.length; j++){
				newObsSoFar[j] = actSoFar[j];
			}
			newObsSoFar[currObsIndex] = currVal;
			fullObss.addAll(this.recurseForPossObsIntArrays(newObsSoFar));
		}
		return fullObss;
	}
	
	
	
	
	/**
	 * Calculate state action value for an observation and action. Obs-act features are passed in
	 * to save computation.
	 * 
	 * @param obs
	 * @param act
	 * @return
	 */
	private double getStateActVal(Observation obs, Action act) {
		if (deterministicTransitions) {
			//System.out.println("act: " + act.intArray[0]);
			double reward;
			reward = this.rewReadOnlyModel.predictLabel(obs, act);

			//System.out.println("reward: " + reward);
			Observation newObs = this.envTransModel.sampleNextObs(obs, act).getObs();
			double newVal;
			
			Action nextOptAct;
			
			nextOptAct= this.model.getMaxAct(newObs, this.featGen.getPossActions(newObs));
			//System.out.println("newObs: " + Arrays.toString(newObs.intArray));
			newVal = reward + this.discountFactorForLearning.getValue() *
							this.model.predictLabel(featGen.getSAFeats(newObs, 
																nextOptAct));
		
			//System.out.println("nextObs val: " + this.model.predictLabel(featGen.getSAFeats(newObs, 
			//													dummyActForFeats)));
			//System.out.println("newVal: " + newVal);
			return newVal;
			
		}
		else { 
			return this.model.predictLabel(obs, act);
		}
	}
	
	
	public String stateValsToStr() {
		/*
		 * Print state values
		 */
		double[][] stateVals = new double[6][6];
		String stateValsStr = "";
		for (int i = 0; i < stateVals.length; i++) {
			for (int j = 0; j < stateVals[0].length; j++) {
				stateVals[i][j] = Double.NaN;
			}
		}
		for (Observation obs : this.legalObservations) {
			if (this.envTransModel.isObsTerminal(obs)) {
				stateVals[obs.intArray[0]][obs.intArray[1]] = Double.NaN;
			}
			else {
				Action optAct = this.model.getMaxAct(obs, this.featGen.getPossActions(obs));
				stateVals[obs.intArray[0]][obs.intArray[1]] = this.model.predictLabel(featGen.getSAFeats(obs, optAct));
			}			 
		}
		
		for (int j = 0; j < stateVals[0].length; j++) {
			if (j > 0)
				stateValsStr += "\n";
			for (int i = 0; i < stateVals.length; i++) {
				stateValsStr += stateVals[i][j] + "\t";
			}
		}
		return stateValsStr;
	}
	
	
	private String stateActValsToStr() {
		/*
		 * Print state values
		 */
		double[][] stateActs = new double[6][6];
		String stateActsStr = "";
		for (int i = 0; i < stateActs.length; i++) {
			for (int j = 0; j < stateActs[0].length; j++) {
				stateActs[i][j] = 5;
			}
		}
		for (Action act : this.possibleActions) {
			for (Observation obs : this.legalObservations) {
				if (this.envTransModel.isObsTerminal(obs)) {
					stateActs[obs.intArray[0]][obs.intArray[1]] = 0;
				}
				else {						
					stateActs[obs.intArray[0]][obs.intArray[1]] = this.model.predictLabel(featGen.getSAFeats(obs,
																											act));
				}			 
			}
		
			for (int j = 0; j < stateActs[0].length; j++) {
				if (j > 0)
					stateActsStr += "\n";
				for (int i = 0; i < stateActs.length; i++) {
					stateActsStr += stateActs[i][j] + "\t";
				}
			}
			stateActsStr += "\n----------------\n";
		}
		return stateActsStr;
	}
	
	
    /**
     * Chooses the action with the highest value. A tie randomly broken. 
     * 
     * Optionally, by class member giveTieToLastAct, this will choose the last 
     * action if it's tied for best.
     * 
     * @param obs
     * @return
     */
    public Action chooseGreedyAct(Observation obs) {

    	System.out.println("steps: " + this.totalSteps);
    	if (this.totalSteps == 1 && startActIndex >= 0)
    		return possibleActions[startActIndex];
    	
    	/*
    	 * Get list of maximizing actions
    	 */
    	//System.out.println("--Act vals--");
		double maxVal = Double.NEGATIVE_INFINITY;
		ArrayList<Action> maxActs = new ArrayList<Action>(); 
		maxActs.add(this.lastObsAndAct.getAct()); // I believe this will be replaced unless all actions have negative infinity value or there are no actions.
		for (Action act: possibleActions) {
			//System.out.print(act.intArray[0] + ": ");
			double stateActVal = getStateActVal(obs, act);
			//System.out.println(stateActVal);
			if (stateActVal >  maxVal) {
				maxVal = stateActVal;
				maxActs.clear();
				maxActs.add(act);
			}
			else if (stateActVal == maxVal) {
				maxActs.add(act);
			}
			//System.out.flush();
		}
		
		/*
		 * Break ties. If last action is in tie, it is taken. Otherwise choose randomly.
		 */
		Action lastAct = this.lastObsAndAct.getAct();
		
		boolean lastActIsGreedy = false;
		if (this.giveTieToLastAct && lastAct != null) {
			for (Action act: maxActs){
				if (Arrays.equals(act.intArray, lastAct.intArray) &&
						Arrays.equals(act.doubleArray, lastAct.doubleArray) &&
						Arrays.equals(act.charArray, lastAct.charArray)) { 
					lastActIsGreedy = true; 
					break; 
				} 
			}
		}
		if (lastActIsGreedy)
			return lastAct.duplicate();
		else {
			int actIndex = FeatGenerator.staticRandGenerator.nextInt(maxActs.size());
			return maxActs.get(actIndex);
		}
    }
	
	private void test(){
		// TODO create test() for main()
	}

	

	
	
	class HashModel extends RegressionModel {

		public Hashtable<DoubleArrayWrapper, Double> hashMap;
		
		public HashModel(int numSlots){
			hashMap = new Hashtable<DoubleArrayWrapper, Double>(numSlots);
		}

		public void addInstance(Sample sample) {
//			hashMap.put(Arrays.asList(sample.feats), sample.label);
			hashMap.put(new DoubleArrayWrapper(sample.feats), sample.label);
			//System.out.println("in hash: " + hashMap.get(Arrays.asList(sample.feats)));
		}
		public void addInstance(double[] feats, double label){
//			hashMap.put(Arrays.asList(feats), label);
			hashMap.put(new DoubleArrayWrapper(feats), label);
			//System.out.println("in hash: " + hashMap.get(Arrays.asList(feats)));
		}

		public void addInstances(Sample[] samples) {
			for (Sample sample : samples)
				addInstance(sample);
		}

		public void addInstancesWReplacement(Sample[] samples) {
			System.out.println("This method not implemented. Exiting.");
			System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
			System.exit(0);
		}

		public void buildModel() {
			;
		}

		public double predictLabel(double[] feats) {
//			System.out.println("stack trace: " + Arrays.toString(Thread.currentThread().getStackTrace()));
//			System.out.println("hashMap: " + hashMap);
//			System.out.println("feats: " + Arrays.toString(feats));
//			List<double[]> list = Arrays.asList(feats);
//			System.out.println(list);
//			double labelPred = hashMap.get(list);
//			System.out.println("labelPred: " + labelPred);
//			return hashMap.get(Arrays.asList(feats));
			return hashMap.get(new DoubleArrayWrapper(feats));
		}

		public void clearSamplesAndReset() {
			hashMap = new Hashtable<DoubleArrayWrapper, Double>(hashMap.size());
		}
		
	}
	
	public final class DoubleArrayWrapper
	{
	    private final double[] data;

	    public DoubleArrayWrapper(double[] data)
	    {
	        if (data == null)
	        {
	            throw new NullPointerException();
	        }
	        this.data = data;
	    }

	    @Override
	    public boolean equals(Object other)
	    {
	        if (!(other instanceof DoubleArrayWrapper))
	        {
	            return false;
	        }
	        return Arrays.equals(data, ((DoubleArrayWrapper)other).data);
	    }

	    @Override
	    public int hashCode()
	    {
	        return Arrays.hashCode(data);
	    }
	}
	
	
}

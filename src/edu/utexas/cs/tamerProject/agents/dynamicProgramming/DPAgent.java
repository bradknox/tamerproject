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
import edu.utexas.cs.tamerProject.featGen.FeatGen_Discretize;
import edu.utexas.cs.tamerProject.featGen.FeatGen_NoChange;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.utils.Stopwatch;

/**
 * Value iteration (through dynamic programming) agent
 * 
 * This agent assumes that the state and action space are discrete and that 
 * transitions are deterministic.
 * 
 * Note that this algorithm is technically also asynchronous since
 * it updates a single state at a time during the sweep. However, it
 * much closer to synchronous than the algorithm called asynchronous
 * DP (in class AsynchDPAgent), which updates different states with 
 * very different frequencies.
 * 
 * @author bradknox
 *
 */
public class DPAgent extends GeneralAgent {

	
	public TamerAgent tamerAgent;
	public EnvTransModel envTransModel;
	protected ObsActModel rewModel;
	public boolean doCreateFastModel = true;
	Action dummyActForFeats;
	public boolean useTamer = true;
	boolean giveTieToLastAct = false;
	protected Object rewModelLock = new Object();
	Object valModelLock = new Object();
	
	protected Observation[] legalObservations;
	protected Observation[] nonTermLegalObservations;
	protected Action[] possibleActions;
	
	protected String writePredHRewDir;
	protected String writePredHRewPath;
	protected double predHRewThisEp = 0;
	Stopwatch agentStopwatch = new Stopwatch();
	
	Timer sweepTimer;
	public double timeBtwnDPSweeps = 1000; // in milliseconds
	int numSweepsPerformed = 0;
	public boolean printSweeps = false;
	
	public void setEnvTransModel(EnvTransModel model){this.envTransModel = model;}
	public void setRewModel(ObsActModel model){this.rewModel = model;}
	public ObsActModel getRewModel(){ return this.rewModel; } 
	
	public void replaceRewFcnWFastVersion(){
		synchronized(this.rewModelLock) {
			this.rewModel = makeFastModel(this.tamerAgent.model);
		}
	}

	public void processPreInitArgs(String[] args) {
		super.processPreInitArgs(args);
		System.out.println("\n[------Sarsa process pre-init args in DPAgent------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];
    		if ((argType.equals("-initialValue")) && (i+1) < args.length){
    			if (args[i+1].equals("zero")) 
    				this.params.initWtsValue = 0;
    			else {
    				System.out.println("\nIllegal DPAgent initial values type. Exiting.\n\n");
    				System.exit(1);
    			}
				System.out.println("Sarsa's Q-model's initial weights set to: " 
									+ this.params.initWtsValue);
			}
		}
	}
	public void processPostInitArgs(String[] args) {
		System.out.println("\n[------process post-init args in DPAgent------] " + Arrays.toString(args));
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
		 * INITIALIZE FeatGenerator for state value function (no action input)
		 */
    	int[][] singleActIntRanges = {{0,0}};
    	this.featGen = new FeatGen_Discretize(this.theObsIntRanges, this.theObsDoubleRanges, 
				singleActIntRanges, this.theActDoubleRanges, 
				Integer.valueOf(params.featGenParams.get("numBinsPerDim")));
		dummyActForFeats = new Action();
		int[] dummyIntArray = {0};
		dummyActForFeats.intArray = dummyIntArray;
		dummyActForFeats.doubleArray = new double[0];

		/*
		 * INITIALIZE RegressionModel - step size of 1 for DP
		 */
    	this.model = new IncGDLinearModel(this.featGen.getNumFeatures(), 1.0, 
				this.featGen, this.params.initWtsValue, 
				this.params.modelAddsBiasFeat);
    	((IncGDLinearModel)this.model).setDiscountFactor(0);
    	
    	

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
    	 * INITIALIZE EnvTransModel
    	 */
    	if (this.envTransModel == null) {
    		System.err.println("envTransModel in DPAgent has not been set. Call setEnvTransModel() before agent_init().");
    		System.exit(1);  		
    	}
//    		this.envTransModel = new LoopMazeTransModel();
    	
    	/*
    	 * INITIALIZE REWARD MODEL
    	 */
		synchronized(this.rewModelLock) {
			if (this.useTamer)
        		this.rewModel = this.tamerAgent.model;
//			else if (this.rewModel == null) 
//				this.rewModel = new LoopMazeRewModel();
		}
    	
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
    	System.out.println("this.expName: " + this.expName);
    	System.out.println("RLLIBRARY_PATH: " + RLLIBRARY_PATH);
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
		
		/*
		 * Start dynamic programming thread
		 */
		createDPUpdateThread();
		
		this.endInitHelper();
	}

	public void createDPUpdateThread(){
		killDPUpdateThread();
		sweepTimer = new Timer();
        sweepTimer.schedule(new TimerTask() {
            public void run() {
            	Thread.currentThread().setName("DPUpdateThread");
            	dynamicProgSweep();
            }
		}, new Date(), (long)timeBtwnDPSweeps);
	}
	
	public void killDPUpdateThread() {
		if (sweepTimer != null)
			sweepTimer.cancel();
	}
	
	public void dynamicProgSweep() {
		// TODO grab boolean?
		edu.utexas.cs.tamerProject.utils.Stopwatch sweepStopwatch = new Stopwatch();
		sweepStopwatch.startTimer();
		//System.out.println("sweep start time: " + edu.utexas.cs.tamerProject.utilities.Stopwatch.getComparableTimeInSec());
		//System.out.println("Reward model: " + this.rewModel);
		int i = 0;
		double getStateValTime = 0;
		double sampleCreationTime = 0;
		double addInstanceTime = 0;
		edu.utexas.cs.tamerProject.utils.Stopwatch innerStopwatch = new Stopwatch();
		for (Observation obs : nonTermLegalObservations) {
			innerStopwatch.startTimer();
			//System.out.println("\nobs: " + Arrays.toString(obs.intArray));
			double newStateVal = getStateVal(obs);
			getStateValTime += innerStopwatch.getTimeElapsed();
			
			
			Sample sample = (new Sample(featGen.getSAFeats(obs, dummyActForFeats), 1));
			sampleCreationTime += innerStopwatch.getTimeElapsed();
			
			sample.label = newStateVal;
			synchronized(valModelLock) {
				this.model.addInstance(sample);
			}
			
			addInstanceTime += innerStopwatch.getTimeElapsed();			
			i++;
		}
		//System.out.println("sweep time: " + sweepStopwatch.getTimeElapsed());
		//System.out.println("mean time til getStateVal done: " + getStateValTime / i);
		//System.out.println("mean time til sampleCreation done: " + sampleCreationTime / i);
		//System.out.println("mean time til addInstance done: " + addInstanceTime / i);
		this.numSweepsPerformed++;
		if (this.printSweeps)
			System.out.println("\n" + stateValsToStr());		
	}
	
	

	
	
	
	
	
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public Action agent_start(Observation o, double time,
			Action predeterminedAct) {
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
//    	System.out.println("\n-----------------DPAgent step---------------\n");
//    	System.out.println("observation: " + Arrays.toString(o.intArray));
    	this.stepStartTime = startTime;
    	this.agentStopwatch.startTimer();
    	this.stepStartHelper(r, o);
    	
    	/*
    	 * TAMER UPDATE
    	 */
    	tamerAgent.hRewList = new ArrayList<HRew>(this.hRewThisStep);
    	if (this.stepsThisEp > 1) {
    		this.currObsAndAct.setAct(new Action()); // sending an empty action instead of a null one to avoid the comp. cost of an extra action selection
    		tamerAgent.agent_step(r, o, this.stepStartTime, this.currObsAndAct.getAct());
    	}
    	
    	//System.out.println("Step duration after update: " + this.agentStopwatch.getTimeElapsed());

    	/*
    	 * Create look-up table for reward function
    	 */
    	if (this.useTamer && this.doCreateFastModel ) {
    		synchronized(this.rewModelLock) {
    			this.rewModel = makeFastModel(this.tamerAgent.model);
    		}
    	}
    	
    	//System.out.println("Step duration after fast reward table creation: " + this.agentStopwatch.getTimeElapsed());
    	
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
    	this.tamerAgent.hLearner.recordTimeStepStart(this.tamerAgent.featGen.getFeats(o, this.currObsAndAct.getAct()), this.stepStartTime); // called here b/c action was unknown at the time of tamerAgent's agent_step()
    	

	    	
    	
		this.stepEndHelper(r, o);
		
//		// TODO remove this printout
		Action act = this.currObsAndAct.getAct();
//		System.out.println("action: " + act.intArray[0]);
//		System.out.println("sweeps performed: " + this.numSweepsPerformed);
//		System.out.println("Total step duration: " + this.agentStopwatch.getTimeElapsed());
		
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

    public void agent_cleanup() {
    	System.out.println("Cleaning up DPAgent.");
    	this.sweepTimer.cancel();
    }
    

	public String makeEndInfoStr(){
		String endInfoStr = "numSweepsPerformed, " + this.numSweepsPerformed;
		endInfoStr += "\n" + "agent total time steps, " + this.totalSteps;
		endInfoStr += "\n" + "agent run time, " + this.agentStopwatch.getTimeElapsed();
		endInfoStr += "\n\nvalue function,\n" + this.stateValsToStr();
		endInfoStr += "\n\nrew vals,";
		for (Observation obs : nonTermLegalObservations) {
			for (Action act: possibleActions) {
				double reward = Double.NEGATIVE_INFINITY;
				synchronized(this.rewModelLock) {
					reward = this.rewModel.predictLabel(obs, act);
				}
				endInfoStr += "\n" + Arrays.toString(obs.intArray) + ", " 
							+ Arrays.toString(act.intArray) + ", "
							+ reward;
			}
		}
		endInfoStr += "\n";
		return endInfoStr;
	}
    
	/**
	 * Returns a string representation of the discount factor and the maximum state value
	 * @return
	 */
    public double getMaxVal(){
    	double maxVal = Double.NEGATIVE_INFINITY;
		for (Observation obs : this.legalObservations) {
			if (!this.envTransModel.isObsTerminal(obs)){
				synchronized(this.valModelLock) {
					double val = this.model.predictLabel(featGen.getSAFeats(obs, dummyActForFeats));
					maxVal = Math.max(val, maxVal);
				}
			}			 
		}
		return maxVal;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	
	
	/**
	 * Helper/minor methods
	 */
    public void initParams(String envName){
    	super.initParams(envName);
    	tamerAgent.initParams(envName);
    }
	public void receiveKeyInput(char c){
		System.out.println("char in DPAgent: " + c);
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
	 * Calculate value for max-val action
	 */
	private double getStateVal(Observation obs) {
		double maxVal = Double.NEGATIVE_INFINITY;
		for (Action act: possibleActions) {			
			maxVal = Math.max(getStateActVal(obs, act), maxVal);
		}
		//System.out.println("maxVal: " + maxVal);
		return maxVal;
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
		//System.out.println("act: " + act.intArray[0]);
		double reward;
		synchronized(this.rewModelLock) {
			reward = this.rewModel.predictLabel(obs, act);
		}
		//System.out.println("reward: " + reward);
		Observation newObs = this.envTransModel.sampleNextObs(obs, act).getObs();
		//System.out.println("newObs: " + Arrays.toString(newObs.intArray));
		
		double newVal;
		synchronized(this.valModelLock) {
			newVal = reward + this.discountFactorForLearning.getValue() *
							this.model.predictLabel(featGen.getSAFeats(newObs, 
																dummyActForFeats));
		}
		//System.out.println("nextObs val: " + this.model.predictLabel(featGen.getSAFeats(newObs, 
		//													dummyActForFeats)));
		//System.out.println("newVal: " + newVal);
		return newVal;
	}
	
	
	public String stateValsToStr() {
		/*
		 * Print state values 
		 * 
		 * TODO remove magic numbers
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
				synchronized(this.valModelLock) {
					stateVals[obs.intArray[0]][obs.intArray[1]] = this.model.predictLabel(featGen.getSAFeats(obs,
																					dummyActForFeats));
				}
			}			 
		}
		
		for (int j = 0; j < stateVals[0].length; j++) {
			if (j > 0)
				stateValsStr += "\n";
			for (int i = 0; i < stateVals.length; i++) {
				stateValsStr += stateVals[i][j] + "\t";
			}
		}
		
//		for (int i = 0; i < stateVals.length; i++) {
//			if (i > 0)
//				stateValsStr += "\n";
//			for (int j = 0; j < stateVals[0].length; j++) {
//				stateValsStr += stateVals[i][j] + "\t";
//			}
//		}
		return stateValsStr;
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
       	/*
    	 * Get list of maximizing actions
    	 */
    	//System.out.println("--Act vals--");
		double maxVal = Double.NEGATIVE_INFINITY;
		ArrayList<Action> maxActs = new ArrayList<Action>(); 
		maxActs.add(this.lastObsAndAct.getAct());
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
		//System.out.println("max action val: " + maxVal);
		
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

	
	
	
	
	
	protected RegressionModel makeFastModel(RegressionModel origModel) {
		edu.utexas.cs.tamerProject.utils.Stopwatch fastModelTimer = new edu.utexas.cs.tamerProject.utils.Stopwatch();
		fastModelTimer.startTimer();
		HashModel fastModel = new HashModel(this.possibleActions.length * this.legalObservations.length);
		fastModel.setFeatGen(new FeatGen_NoChange(this.theObsIntRanges, this.theObsDoubleRanges, 
				this.theActIntRanges, this.theActDoubleRanges));
    	
		for (Observation obs : this.legalObservations) {
			for (Action act : this.possibleActions) {
				double[] feats = fastModel.getFeatGen().getFeats(obs, act);
				//System.out.println("\norigModel predict: " + origModel.predictLabel(obs, act));
				double label = origModel.predictLabel(obs, act);
				fastModel.addInstance(feats, label);
				//System.out.println("fastModel predict: " + fastModel.predictLabel(feats));
			}
		}
		//System.out.println("Time to create fastModel: " + fastModelTimer.getTimeElapsed());
		return fastModel;
	}
	
	
	
	
	
	public class HashModel extends RegressionModel {

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

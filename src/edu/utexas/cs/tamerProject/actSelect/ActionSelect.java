package edu.utexas.cs.tamerProject.actSelect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.agents.tamerrl.HInfluence;
import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.CombinationModel;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;

/**
 * ActionSelect implements action selection methods and is used by agent classes.
 *  After initial setup, selectAction() is typically the only method that will be 
 *  used.
 * 
 * @author bradknox
 *
 */
public class ActionSelect{
	
	public String selectionMethod = "greedy";
	//private Action baseAction;
	private HashMap<String,String> selectionParams;
	
	/**
	 * valFcnModel is a time-independent value function (as in RL) only if exponential 
	 * discounting is being used (including a discount factor of 0). For hyperbolic 
	 * discounting, valFcnModel might be used for the value for a state and action 
	 * being taken immediately, but if it's used for the value of a future state and 
	 * action (as in its use in path planning), it's being used as a biased approximation 
	 * of the actual value from the current time.
	 */
	public RegressionModel valFcnModel;
	
	private ObsActModel rewModel = null; // see comment above setRewModel
	private EnvTransModel envTransModel = null;
	 
	boolean treeSearch = false;
	public enum DiscountTypes {EXPON, HYPER} // exponential or hyperbolic discounting
	private DiscountTypes discountType = DiscountTypes.EXPON;
	private static double discountParam = Double.MAX_VALUE; // 0 for df=1. 1.0 for df=0.5
	
	private static int greedyLeafPathLength = 0;
	private static int exhaustiveSearchDepth = 1; //5;
	private static boolean randomizeSearchDepth = true;
	
	public Action[] forbiddenActs = null;
	
	public ActionSelect(RegressionModel valFcnModel, String selectionMethod, 
						HashMap<String,String> selectionParams, Action action){
		this.valFcnModel = valFcnModel;
		this.selectionMethod = selectionMethod;
		this.selectionParams = selectionParams;
		
		this.treeSearch = Boolean.valueOf(selectionParams.get("treeSearch"));
		ActionSelect.greedyLeafPathLength = Integer.valueOf(selectionParams.get("greedyLeafPathLength"));
		ActionSelect.exhaustiveSearchDepth = Integer.valueOf(selectionParams.get("exhaustiveSearchDepth"));
		ActionSelect.randomizeSearchDepth = Boolean.valueOf(selectionParams.get("randomizeSearchDepth"));
		System.out.println("selectionParams in ActionSelect: " + selectionParams.toString());
	}

	
	
	/**
	 * Adds an additional RegressionModel to the main Q, H, etc. valFcnModel. The output of the two
	 * models is added together and acted upon as if it is the single output of a meta-model.
	 * For TAMER+RL (via the TamerRLAgent class), this is used for action biasing and Q 
	 * augmentation.
	 * 
	 * @param supplModel
	 * @param hInf
	 */
    public void addModelForActBias(RegressionModel supplModel, HInfluence hInf) {
    	this.valFcnModel = new CombinationModel(this.valFcnModel, supplModel, hInf);
    }

    
    public void setEnvTransModel(EnvTransModel envTransModel){ this.envTransModel = envTransModel;}
    public EnvTransModel getEnvTransModel(){ return this.envTransModel; }
    
	/**
	 * rewModel is interpreted in an RL sense, where the reward function differs
	 * from the value function. Thus, when the human-reward model H-hat is used
	 * only as if it is a value-function model in RL (as in TAMER), it differs from 
	 * rewModel here. However, in algorithms beyond TAMER, H-Hat could be used as 
	 * the reward model.
	 */
    public void setRewModel(ObsActModel rewModel){ 
    	if (this.rewModel != null && rewModel == null) {
    		System.err.println("Attempting to change reward model to null in ActionSelect. Exiting.");
    		System.exit(1);
    	}
    	this.rewModel = rewModel;
    }
    /** See comment for setRewModel(). */
    public ObsActModel getRewModel(){ return this.rewModel; }
    
    public void setTreeSearchFlag(boolean treeSearch) { this.treeSearch = treeSearch; }
    public boolean getTreeSearchFlag() { return this.treeSearch; }
	
	public void setDiscountParam(double param) {
		this.discountParam = param;
		System.out.println("Discount parameter in ActionSelect set to: " + this.discountParam);
	}
	public void setDiscountType(DiscountTypes discountType) {
		this.discountType = discountType;
	}
	
	
	
	
	
    
	public Action selectAction(Observation obs, Action lastAct){
		//System.out.println("selectionMethod is "+selectionMethod);
		if (this.treeSearch) {
			if (this.selectionMethod.equals("greedy") || 
					(new Random()).nextDouble() > Double.valueOf(selectionParams.get("epsilon"))){
				return ActionSelect.treeSearchBasedExploitSelect(this.valFcnModel, 
						this.rewModel, this.envTransModel, obs, 
						lastAct, this.discountType);
			}
			else {
				return valFcnModel.getRandomAction();		
			}
		}
		
		
		
		if (this.selectionMethod.equals("greedy")) {
			return ActionSelect.greedyActSelect(this.valFcnModel, obs, lastAct);
		}
		else if (this.selectionMethod.equals("e-greedy")) {
			double epsilon = Double.valueOf(selectionParams.get("epsilon"));
			Action chosenAction = ActionSelect.eGreedyActSelect(epsilon, 
					this.valFcnModel, obs, lastAct);
			return chosenAction;
		}
		else {
			System.err.println("Action selection method " + this.selectionMethod + " not supported. Exiting.");
			System.exit(0);
			return null;
		}
	}
	
	
	public void anneal(){
		if (this.selectionMethod.equals("greedy")) {
			;
		}
		else if (this.selectionMethod.equals("e-greedy")) {
			double annealRate = Double.valueOf(this.selectionParams.get("epsilonAnnealRate"));
			double priorEpsilon = Double.valueOf(this.selectionParams.get("epsilon"));
			selectionParams.put("epsilon", Double.toString(priorEpsilon * annealRate));
		}
		else {
			System.err.println("Action selection method " + this.selectionMethod + " not supported. Exiting.");
			System.exit(0);
		}
//		if (this.valFcnModel.getClass().getName().equals("edu.utexas.cs.tamerProject.modeling.CombinationModel"))
//			((CombinationModel)this.valFcnModel).annealNonPrimaryWts();
		
	}
	
	public Action greedyActSelect(Observation obs, Action lastAct) {
		return ActionSelect.greedyActSelect(this.valFcnModel, obs, lastAct);
	}
	
	private static Action greedyActSelect(RegressionModel valFcnModel, Observation obs, Action lastAct){
		ArrayList<Action> maxActs =  valFcnModel.getMaxActs(obs, null);
		if (maxActs.size() == 0) {
			System.err.println("A list of zero maximum acts was returned by RegressionModel.getMaxActs(). Exiting.");
			System.err.println("state-action values: " + Arrays.toString(valFcnModel.getStateActOutputs(obs, 
																		valFcnModel.getFeatGen().getPossActions(obs))));
			System.exit(1);
		}
		boolean lastActIsGreedy = false;
		if (lastAct != null) {
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
		
	private static Action eGreedyActSelect(double epsilon,
								RegressionModel valFcnModel, Observation obs,
								Action lastAct){
		//System.out.println("epsilon: " + epsilon);
		if ((new Random()).nextDouble() > epsilon){
			return ActionSelect.greedyActSelect(valFcnModel, obs, lastAct);
		}
		else {
			return valFcnModel.getRandomAction();
		}
	}	
	
	
	
	
	
	
	
	

	/**
	 * Searches exhaustively to exhaustiveSearchDepth (or to a depth drawn uniformly from 
	 * {0, ... exhaustiveSearchDepth} if randomizeSearchDepth is true) and then from each
	 * leaf does a greedy rollout to greedyLeafPathLength. Of the searched paths, chooses
	 * a path with the highest estimated return, using the reward during the path as output
	 * by rewFcnModel and the value at the end of the path as output by valFcnModel.
	 */
	private static Action treeSearchBasedExploitSelect(RegressionModel valFcnModel, 
							ObsActModel rewFcnModel, EnvTransModel envTransModel, 
							Observation obs, Action lastAct, DiscountTypes discountType){
		//System.out.println("\nvalFcnModel: " + valFcnModel + ", rewFcnModel: " + rewFcnModel);
//		System.out.println("\n");
		if (envTransModel == null) {
			System.err.println("Attempting to treeSearch without setting an environment model.");
			System.exit(1);
		}
		if (rewFcnModel == null) {
			System.err.println("Attempting to treeSearch without setting ActionSelect.rewModel.");
			System.exit(1);
		}
		
		/*
		 * Get array of possible actions.
		 */
		ArrayList<Action> possActions = valFcnModel.getPossActions(obs);
		double maxPathVal = Double.NEGATIVE_INFINITY;
		ArrayList<PathAndVal> maxValPaths = new ArrayList<PathAndVal>();
		
		int searchDepth = exhaustiveSearchDepth;
		if (ActionSelect.randomizeSearchDepth) // choose a depth uniformly from [0, exhaustiveSearchDepth]
			searchDepth = FeatGenerator.staticRandGenerator.nextInt(exhaustiveSearchDepth + 1);
			
		
		/*
		 * Get state-action values for each action.
		 */
		//System.out.print("\nAct path: ");
		for (int actI = 0; actI < possActions.size(); actI++) {
			//System.out.print("\n" + actI + "|");
			PathAndVal thisPathAndVal = planWithStartAct(obs, possActions.get(actI), valFcnModel, rewFcnModel, 
													envTransModel, 0, searchDepth, lastAct, discountType);
			//System.out.print(thisPathAndVal);
			
			
			if (thisPathAndVal.getVal() > maxPathVal){
				maxValPaths.clear();
				maxPathVal = thisPathAndVal.getVal();
			}
			if (thisPathAndVal.getVal() == maxPathVal){
				maxValPaths.add(thisPathAndVal);
			}
		}
		//System.out.println();
		
		PathAndVal chosenMaxValPath = null;
		// TODO this should be uncommented or merged in for TAMER 
//		if (lastAct != null) { //// choose last action if it maximizes expected return 
//			for (PathAndVal path: maxValPaths){
//				if (Arrays.equals(path.pathActs.get(0).intArray, lastAct.intArray) &&
//						Arrays.equals(path.pathActs.get(0).doubleArray, lastAct.doubleArray) &&
//						Arrays.equals(path.pathActs.get(0).charArray, lastAct.charArray)) { 
//					chosenMaxValPath = path; 
//					break; 
//				} 
//			}
//		}
		if (chosenMaxValPath == null) {
			int pathIndex = FeatGenerator.staticRandGenerator.nextInt(maxValPaths.size());
			chosenMaxValPath=  maxValPaths.get(pathIndex);
		}
		
		
		
		//PathAndVal greedyPath = ActionSelect.sampleGreedyPath(obs, valFcnModel, rewFcnModel, envTransModel, 0, 
		//		exhaustiveSearchDepth + greedyLeafPathLength, lastAct, discountType);
//		if ( !(Arrays.equals(chosenMaxValPath.pathActs.get(0).intArray, greedyPath.pathActs.get(0).intArray) &&
//				Arrays.equals(chosenMaxValPath.pathActs.get(0).doubleArray, greedyPath.pathActs.get(0).doubleArray) &&
//				Arrays.equals(chosenMaxValPath.pathActs.get(0).charArray, greedyPath.pathActs.get(0).charArray)))
//		{
			//System.out.print("Chosen path: " + chosenMaxValPath); 
			//System.out.print("Obs: ");
			//ArrayList<Observation> pathObservations = chosenMaxValPath.pathObs;
			//for (Observation pathObs : pathObservations){
			//	System.out.print(" " + Arrays.toString(pathObs.doubleArray));
			//}
			//System.out.println();
			//System.out.println("Greedy path from start: " + greedyPath);
			//System.out.println("Greedy act vals: " + Arrays.toString(valFcnModel.getStateActOutputs(obs, possActions)));
//		}
		
		//// return first action in path
		return chosenMaxValPath.getFirstAct();
	}
	
	
	
	
	
	
	/**
	 * This recursive method exhaustively enumerates all possible paths of depth "depthToGo",
	 * searching depth-first. Once it is called with depthToGo of 0, plan() performs a rollout
	 * using greedy action selection. The path that is finally returned includes the discounted 
	 * value of the rollout but not the actions and individual rewards from the rollout.  
	 */
	private static PathAndVal plan(Observation obs, RegressionModel valFcnModel, 
										ObsActModel rewFcnModel, EnvTransModel envTransModel, int depthSoFar, 
										int depthToGo, Action lastAct, DiscountTypes discountType) {
		//System.out.print(depthSoFar);
		
		////////////////////////////////////////////////////////////////////////
		/* 
		 * Base case 
		 * 
		 * Perform rollout from obs and return empty path with rollout value at leaf.
		 */
		////////////////////////////////////////////////////////////////////////
		
		if (depthToGo == 0) {		
			PathAndVal greedyPath = ActionSelect.sampleGreedyPath(obs, valFcnModel, rewFcnModel, 
																envTransModel, depthSoFar + 1, greedyLeafPathLength, 
																lastAct, discountType); // rollout
			PathAndVal pathAndVal = new PathAndVal(greedyPath.getVal());
			return pathAndVal;
		}
		
		
		////////////////////////////////////////////////////////////////////////
		/* 
		 * Recursive case
		 * 
		 * For every possible action at this step, get value by discounted immediate 
		 * reward plus recursive call.
		 */
		////////////////////////////////////////////////////////////////////////
		
		/*
		 * Get array of possible actions.
		 */
		ArrayList<Action> possActions = valFcnModel.getPossActions(obs);
		ArrayList<PathAndVal> maxValPaths = new ArrayList<PathAndVal>();
		double maxPathVal = Double.NEGATIVE_INFINITY;
		
		/*
		 * Recursively get stateActVals for each possible action from the current obs.
		 */
		for (int actI = 0; actI < possActions.size(); actI++)  {
			Action currentAct = possActions.get(actI);
			
			/*
			 * Take action.
			 */
			ObsAndTerm nextObsAndTerm = envTransModel.sampleNextObs(obs, currentAct); 
			
			/*
			 * Recursive call if not in terminal state.
			 */
			PathAndVal thisPathAndVal;
			if (!nextObsAndTerm.getTerm())
				thisPathAndVal = ActionSelect.plan(nextObsAndTerm.getObs(), valFcnModel, rewFcnModel, 
														envTransModel, depthSoFar + 1, depthToGo - 1, 
														currentAct, discountType);
			else
				thisPathAndVal = new PathAndVal();
			
			
			/*
			 * Add this action and its resultant discounted reward.
			 */
			double rawImmedRew = rewFcnModel.predictLabel(obs, currentAct);
			
			double discountedImmedRew = ActionSelect.getDiscount(depthSoFar, discountType) * rawImmedRew;
			thisPathAndVal.addObsAndActBeforePath(obs, currentAct, discountedImmedRew, rawImmedRew);
			
			/*
			 * Add action's path if its value is as high or higher than the current maximum.
			 */
			if (thisPathAndVal.getVal() > maxPathVal){
				maxValPaths.clear();
				maxPathVal = thisPathAndVal.getVal();
			}
			if (thisPathAndVal.getVal() == maxPathVal){
				maxValPaths.add(thisPathAndVal);
				
			}
			

		}
		
		/*
		 * Randomly choose from tied acts.
		 */
		int chosenMaxActIndex = FeatGenerator.staticRandGenerator.nextInt(maxValPaths.size());
		PathAndVal chosenMaxValPath = maxValPaths.get(chosenMaxActIndex);
		
		/*
		 * Return the path with the highest value (or in the case of a tie, a randomly 
		 * chosen maximizing path).
		 */
		return chosenMaxValPath;
	}
	
	
	
	private static PathAndVal planWithStartAct(Observation obs, Action act, RegressionModel valFcnModel, 
			ObsActModel rewFcnModel, EnvTransModel envTransModel, int depthSoFar, 
			int depthToGo, Action lastAct, DiscountTypes discountType) {
	
		if (depthToGo == 0) {
			double stateActVal = valFcnModel.predictLabel(obs, act);
			return new PathAndVal(stateActVal, act);
		}
		
		/*
		 * General case 
		 * TODO every time an observation is sampled, check for terminal state. If so, stop recursive search and return current path.
		 */
		ObsAndTerm nextObsAndTerm = envTransModel.sampleNextObs(obs, act); 
		
		PathAndVal thisPathAndVal = new PathAndVal();
		if (!nextObsAndTerm.getTerm()){
			thisPathAndVal = ActionSelect.plan(nextObsAndTerm.getObs(), valFcnModel, 
					rewFcnModel, envTransModel, depthSoFar + 1, 
					depthToGo - 1, act, discountType);
		}

		double rawImmedRew = rewFcnModel.predictLabel(obs, act);
		double discountedImmedRew = ActionSelect.getDiscount(depthSoFar, discountType) * rawImmedRew;
		thisPathAndVal.addObsAndActBeforePath(obs, act, discountedImmedRew, rawImmedRew);
		
		return thisPathAndVal;
		
	}
	
	
	
	
	
	
	
	
	
	/*
	 * At start, the argument act is not yet taken. This action creates a transition before considering the 
	 * possible subsequent actions.
	 */
	private static PathAndVal planWithStartAct2(Observation obs, Action act, RegressionModel valFcnModel, 
										ObsActModel rewFcnModel, EnvTransModel envTransModel, int depthSoFar, 
										int depthToGo, Action lastAct, DiscountTypes discountType) {
//		System.out.print(depthSoFar);
		double rawImmedRew = rewFcnModel.predictLabel(obs, act);
		double discountedImmedRew = ActionSelect.getDiscount(depthSoFar, discountType) * rawImmedRew;
		
		/*
		 * Base cases - Perform rollout from (obs, act) and return value of the rollout path.
		 */
//		if (depthToGo == 0) {
//			PathAndVal greedyPath = ActionSelect.sampleGreedyPath(obs, valFcnModel, rewFcnModel, 
//					envTransModel, depthSoFar + 1, greedyLeafPathLength, 
//					act, discountType); // rollout
//			PathAndVal pathAndVal = new PathAndVal(greedyPath.getVal(), act);
//		}
		if (depthToGo == 1) {
			
			ObsAndTerm nextObsAndTerm = envTransModel.sampleNextObs(obs, act);
			PathAndVal pathAndVal;
			
			if (!nextObsAndTerm.getTerm()) {
				PathAndVal greedyPath = ActionSelect.sampleGreedyPath(nextObsAndTerm.getObs(), valFcnModel, rewFcnModel, 
																	envTransModel, depthSoFar + 1, greedyLeafPathLength, 
																	act, discountType); // rollout
				pathAndVal = new PathAndVal(greedyPath.getVal());
			}
			else { // terminal state has no value
				pathAndVal = new PathAndVal();
			}
			
			pathAndVal.addObsAndActBeforePath(obs, act, discountedImmedRew, rawImmedRew);
			return pathAndVal;
		}
		
		/*
		 * Recursive case - for every possible action at this step get value by discounted immediate reward plus recursive call
		 */
		ObsAndTerm nextObsAndTerm = envTransModel.sampleNextObs(obs, act);
		
		PathAndVal chosenMaxValPath;
		if (!nextObsAndTerm.getTerm()) {
			////get array of possible actions
			ArrayList<Action> possActions = valFcnModel.getPossActions(nextObsAndTerm.getObs());
			ArrayList<PathAndVal> maxValPaths = new ArrayList<PathAndVal>();
			double maxPathVal = Double.NEGATIVE_INFINITY;
			//// recursively get stateActVals
			for (int actI = 0; actI < possActions.size(); actI++)  {
				Action nextAct = possActions.get(actI);
				PathAndVal thisPathAndVal = ActionSelect.planWithStartAct(nextObsAndTerm.getObs(), nextAct, valFcnModel, 
																	rewFcnModel, envTransModel, depthSoFar + 1, 
																	depthToGo - 1, act, discountType);
				if (thisPathAndVal.getVal() > maxPathVal){
					maxValPaths.clear();
					maxPathVal = thisPathAndVal.getVal();
				}
				if (thisPathAndVal.getVal() == maxPathVal){
					maxValPaths.add(thisPathAndVal);
				}
			}
			
			int chosenMaxActIndex = FeatGenerator.staticRandGenerator.nextInt(maxValPaths.size());
			chosenMaxValPath = maxValPaths.get(chosenMaxActIndex);
		}
		else { // terminal state has no value
			chosenMaxValPath = new PathAndVal();
		}
			
		//// add this level's action and discounted reward
		chosenMaxValPath.addObsAndActBeforePath(obs, act, discountedImmedRew, rawImmedRew);
		
		//// return the path with the highest value (or in the case of a tie, a randomly chosen maximizing path)
		return chosenMaxValPath;
	}
	
	


	/**
	 * This method returns a greedy path from a starting observation. The method is used to do
	 * rollouts for tree search.
	 * 
	 * @param obs
	 * @param valFcnModel
	 * @param rewFcnModel
	 * @param envTransModel
	 * @param startDepth
	 * @param addtlDepthLimit
	 * @param lastAct
	 * @param discountType
	 * @return
	 */
	private static PathAndVal sampleGreedyPath(Observation obs, RegressionModel valFcnModel, 
							ObsActModel rewFcnModel, EnvTransModel envTransModel, 
							int startDepth, int addtlDepthLimit, Action lastAct, DiscountTypes discountType){
		
		PathAndVal path = new PathAndVal();
		Action act;
		ObsAndTerm nextObsAndTerm;
		for (int addtlDepth = 0; addtlDepth < addtlDepthLimit; addtlDepth++){
			/*
			 * Get greedy action.
			 */
			act = greedyActSelect(valFcnModel, obs, lastAct);
			
			/*
			 * Get reward expected for (obs,act) after transition.
			 */
			double rawImmedRew = rewFcnModel.predictLabel(obs, act);
			double discountedImmedRew = ActionSelect.getDiscount(startDepth + addtlDepth, discountType) 
											* rawImmedRew;
			//System.out.println("Discount in sampleGreedyPath at depth " + (addtlDepth+ startDepth) 
			//				+ ": " + ActionSelect.getDiscount(startDepth + addtlDepth, discountType));
			/*
			 * Add (obs,act) and reward info to path.
			 */
			path.addObsAndActToPathEnd(obs, act, discountedImmedRew, rawImmedRew);
			
			lastAct = act;
			/*
			 *  Get next observation and check whether terminal.
			 */
			nextObsAndTerm = envTransModel.sampleNextObs(obs, act);
			if (nextObsAndTerm.getTerm()) { // end path with leaf value still at 0
				return path;
			}
			obs = nextObsAndTerm.getObs();
			
		}
		/*
		 * Get value of leaf state. This value will be discounted one step beyond the last recorded step
		 * (i.e., startDepth + addtlDepthLimit).
		 */
		act = greedyActSelect(valFcnModel, obs, lastAct);
		//System.out.println("leaf act: " + act.intArray[0]);
		path.leafValue = ActionSelect.getDiscount(startDepth + addtlDepthLimit, discountType) 
							* valFcnModel.predictLabel(obs, act);
		//System.out.println("path.leafValue: " + path.leafValue);
		
		return path;
	}

	
	
	
	
	/*			*****************************
	 * 			****** Utility methods ******
	 * 			*****************************
	 */
	private static double getDiscount(int depthSoFar, DiscountTypes discountType) {
		double paramTimesTime = discountParam * depthSoFar;
		if ((discountParam == Double.MAX_VALUE || Double.isInfinite(discountParam)) && depthSoFar == 0)
			paramTimesTime = 0;
		if (discountType == DiscountTypes.EXPON)
			return Math.exp(-1 * paramTimesTime); //exponential discounting
		else if (discountType == DiscountTypes.HYPER)
			return 1.0 / (1 + (paramTimesTime)); //hyperbolic discounting
		else {
			System.err.println("Illegal discount type in ActionSelect.getDiscount(). Exiting.");
			System.exit(1);
			return Double.NaN;
		}
	}
	
	/**
	 * Converts from discount factor, expressing the step-by-step discount,
	 * from what I'm calling the discount parameter, which is used to calculate
	 * the discount at t steps in the future: 1 / (e^{-1 * discountParam * t}) for exponential
	 * discounting. This conversion only applies for exponential discounting.
	 * There is no equivalent of a discount factor for hyperbolic discounting.
	 * 
	 * @param param
	 * @return

	 */
	public static double discParamToFactor(double param){
		return Math.exp(-1 * param);
	}
	
	/**
	 * Reverses conversion of discParamToFactor().
	 * 
	 * @param factor
	 * @return
	 */
	public static double discFactorToParam(double factor){
		double param = -1 * Math.log(factor);
		if (param == Double.NaN)
			param = Double.MAX_VALUE;
		return param;
	}
	
}
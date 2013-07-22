package edu.utexas.cs.tamerProject.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.utils.Stopwatch;
import edu.utexas.cs.tamerProject.utils.encapsulation.IndexAndVal;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndAct;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;

public class UCTNode extends TreeSearchNode{
	//HashMap<Action, Double> actionValues;
	
	int totalVisitsHere = 0;
	//HashMap<Action, Integer> timesActTaken;
	int[] timesActsTaken;
	int numActsTakenAtLeastOnce = 0;
	double stateVal = Double.NEGATIVE_INFINITY;
	
	public static Stopwatch nodeStopWatch = new Stopwatch();

		
	// ArrayList<TreeSearchNode> pathToRoot;
	//int depth; // root is at 0
	
	private static double confConst = 1; // TODO Is there a rigorous way to decide this value?
	private static boolean deterministicTransitions = true;
	
	
	
	
	
	
	public UCTNode(UCTNode parentNode, Observation obs, boolean terminal, EnvTransModel transModel,	
													ObsActModel rewModel, RegressionModel qFunction,
													FeatGenerator featGen){
		if (TreeSearchNode.possActions == null) {
			System.err.println("Instantiating TreeSearchNode without first assigning object to possActions. Exiting.");
			System.exit(1);
		}
		if (TreeSearchNode.discountFactor == -1) {
			System.err.println("Instantiating TreeSearchNode without first assigning value to discountFactor. Exiting.");
			System.exit(1);
		}
		
		this.parent = parentNode;
		this.obs = obs;
		this.terminal = terminal;
		
		this.childrenByObsAndAct = new TreeMap<ObsAndAct, TreeSearchNode>();
		this.timesActsTaken = new int[TreeSearchNode.possActions.length];
		
		this.transModel = transModel;
		this.rewModel = rewModel;
		this.qFunction = qFunction;
		this.featGen = featGen;
		
		UCTNode.confConst = 2.0 * (10.0) / (1 - TreeSearchNode.discountFactor); // 10 is a rough estimate of (R_MAX - R_MIN)
	}
	
	
	
	
	
	
	
	
	
	public double createMCPathSample(int depthToGo){
		//System.out.println("path depth to go: " + depthToGo);
		//System.out.println("time elapsed: " + nodeStopWatch.getTimeElapsed());
//		System.out.println("node address: " + this);
//		System.out.println(loopMazeStateToString());
		double nextStateVal = 0;
		Action act;
		/*
		 * Base cases - make leaf node
		 */
		if (depthToGo == 0) { 
			/*
			 * // TODO - I think the argument should be true below, so that a 
			 * Bellman update is occurring (assuming deterministic transitions). 
			 * As is, if this node has been reached previously before being a leaf, 
			 * it will return the value of a non-greedy action. I don't think this 
			 * is a big issue for the aVI-TAMER experiments though, since such a 
			 * case would be a small fraction of the backups 
			 * (i.e., [1 / (rollout length)] * 
			 * (proportion of leaves that have been visited previously)). 
			 * I could check how often this situation occurs in practice. Also, 
			 * the issue with aVI-TAMER of looping on previously encountered (s,a)
			 * pairs might actually be alleviated slightly by this coding error, 
			 * which would tend to lower the value of states that are being 
			 * encountered more often in the search tree, driving higher exploration.
			 */
					
			return getMaxActNotTaken(false).getVal(); // return estimate of value     
		}
		else if (this.terminal) {
			return 0.0;
		}
		
		/*
		 * If not all of the actions have been tried from this node at least once,
		 * take the best-valued action not yet taken (which defines the rollout 
		 * policy). (Should rollout policy use lookahead to use latest reward function?)
		 */
		else if(numActsTakenAtLeastOnce < TreeSearchNode.possActions.length) {	
//			System.out.println("numActsTakenAtLeastOnce: " + numActsTakenAtLeastOnce);
//			System.out.println("TreeSearchNode.possActions.length: " + TreeSearchNode.possActions.length);
//			System.out.println("Choosing by rollout");
			//System.out.println("Time elapsed before getMaxActNotTaken(): " + nodeStopWatch.getTimeElapsed());
			int actI = getMaxActNotTaken(false).getIndex();
			//System.out.println("Time elapsed after getMaxActNotTaken(): " + nodeStopWatch.getTimeElapsed());
			act = TreeSearchNode.possActions[actI];				
			
			ObsAndTerm nextObsAndTerm = transModel.sampleNextObs(obs, act);
			this.timesActsTaken[actI] = 1;
			this.numActsTakenAtLeastOnce++;

			// create node for action
			TreeSearchNode childNode = new UCTNode(this, nextObsAndTerm.getObs(), nextObsAndTerm.getTerm(),
															this.transModel, this.rewModel, this.qFunction, this.featGen);
			
			// add node to HashMap
			ObsAndAct keySrc = new ObsAndAct();
			keySrc.setAct(act);
			if (deterministicTransitions)
				keySrc.setObs(new Observation()); // use generic observation since we want to be able to use action alone for lookup
			else
				keySrc.setObs(nextObsAndTerm.getObs());
				
			this.childrenByObsAndAct.put(keySrc, childNode);
				
			/*
			 *  Call createMCPathSample on node
			 */
			//System.out.println("------\nTime elapsed before UCT-led createTreePathSample(): " + nodeStopWatch.getTimeElapsed());
			nextStateVal = childNode.createTreePathSample(depthToGo - 1);
			//System.out.println("Time elapsed after UCT-led createTreePathSample(): " + nodeStopWatch.getTimeElapsed());
		}
		/*
		 * If all of the actions have been tried at least once, use UCT policy to determine action 
		 */
		else {
			// choose action by UCT method
			//System.out.println("Choosing by UCT");
			int actI = getMaxUCTActI();
			act = TreeSearchNode.possActions[actI];	 
			this.timesActsTaken[actI]++;
			
			// make HashMap key
			ObsAndAct keySrc = new ObsAndAct();
			ObsAndTerm nextObsAndTerm = null;
			keySrc.setAct(act);
			if (deterministicTransitions)
				keySrc.setObs(new Observation()); // use generic observation since we want to be able to use action alone for lookup
			else {
				nextObsAndTerm = transModel.sampleNextObs(obs, act);
				keySrc.setObs(nextObsAndTerm.getObs());
			}
//			System.out.println("this.childrenByObsAndAct.size(): " + this.childrenByObsAndAct.size());
			//System.out.println("this.childrenByObsAndAct: " + this.childrenByObsAndAct.toString());
			TreeSearchNode childNode = this.childrenByObsAndAct.get(keySrc);
			// if node doesn't exist yet, create it 
			if (childNode == null) {
//				System.out.println("No child node found.");
				if (deterministicTransitions) {
					System.err.println("Missing child node with deterministic transitions! Exiting.");
					System.exit(1);
				}
				childNode = new UCTNode(this, nextObsAndTerm.getObs(), nextObsAndTerm.getTerm(),
													this.transModel, this.rewModel, this.qFunction, this.featGen);
				this.childrenByObsAndAct.put(keySrc, childNode);
				//System.out.println("this.childrenByObsAndAct.size() after add: " + this.childrenByObsAndAct.size());
			}
			
			/*
			 * Call createMCPathSample on node
			 */
			//System.out.println("Time elapsed before UCT-led createTreePathSample(): " + nodeStopWatch.getTimeElapsed());
			nextStateVal = childNode.createTreePathSample(depthToGo - 1);
			//System.out.println("Time elapsed after UCT-led createTreePathSample(): " + nodeStopWatch.getTimeElapsed());
		}
		
		/*
		 * Perform update to qFunction for action taken. This update should involve current and next state's q fcn's, not the MC path value, which would violate the off-policy update.
		 */
		//System.out.println("Time elapsed before reward lookup: " + nodeStopWatch.getTimeElapsed());
		double reward = this.rewModel.predictLabel(obs, act);
		//System.out.println("Time elapsed after reward lookup: " + nodeStopWatch.getTimeElapsed());
		
		double targetQVal = reward + TreeSearchNode.discountFactor * nextStateVal;
//		System.out.println("Target Q-value for (" + obs.intArray[0] + ", " + obs.intArray[1] + "; " 
//														+ act.intArray[0] + "): " + targetQVal);
		Sample qFcnSample = new Sample(featGen.getFeats(obs, act), targetQVal, 1.0);
		
		this.qFunction.addInstance(qFcnSample);
		//System.out.println("Time elapsed after Q update: " + nodeStopWatch.getTimeElapsed());
		
		totalVisitsHere++; // occurs at end to avoid affecting UCT choice
		
		double myValue = getMaxActNotTaken(true).getVal(); // own state value from max_a of q function(for now, don't use estimate of q value from MC path sampling. i'm not sure if that's the right choice though. i suppose it's a bias/variance trade-off issue.)
		//System.out.println("Time elapsed after get max act and value lookup: " + nodeStopWatch.getTimeElapsed());
		
		return myValue;
	}
	
	
	
	
	
	
	
	
	
	
	
	public IndexAndVal getMaxActNotTaken(boolean considerAllActs) {
		ArrayList<Integer> maxActIndices = new ArrayList<Integer>();
		double maxActVal = Double.NEGATIVE_INFINITY;
		
		for (int actI = 0; actI < TreeSearchNode.possActions.length; actI++) {
			//System.out.println("Time elapsed before getMaxActNotTaken() loop iteration " + actI 
					//						+ ": " + nodeStopWatch.getTimeElapsed());
			if (considerAllActs || this.timesActsTaken[actI] == 0) {
				//System.out.println("obs: " + obs);
				//System.out.println("act: " + TreeSearchNode.possActions[actI]);
				//System.out.println("Time elapsed before predictLabel: " + nodeStopWatch.getTimeElapsed());
				double actVal = this.qFunction.predictLabel(obs, TreeSearchNode.possActions[actI]);
				//System.out.println("Time elapsed after predictLabel: " + nodeStopWatch.getTimeElapsed());
				if (actVal == maxActVal) {
					maxActIndices.add(actI);
				}
				else if (actVal > maxActVal) {
					maxActVal = actVal;
					maxActIndices.clear();
					maxActIndices.add(actI);
				}
			}
		}
		if (maxActIndices.size() <= 0)
			System.out.println("num max act indices: " + maxActIndices.size());
		int maxActI = maxActIndices.get(TreeSearchNode.random.nextInt(maxActIndices.size()));
		//System.out.println("Time elapsed after getMaxActNotTaken(): " + nodeStopWatch.getTimeElapsed());
		return new IndexAndVal(maxActI, maxActVal);
	}
	
	
	
	
	
	
	
	
	

	public int getMaxUCTActI(){
		ArrayList<Integer> maxActIndices = new ArrayList<Integer>();
		double maxActVal = Double.NEGATIVE_INFINITY;
		
		for (int actI = 0; actI < TreeSearchNode.possActions.length; actI++) {
			
			/*
			 * UCB calculation: 
			 * action value = Q(s,a) + confBound, where
			 * confBound = confConst * [log(visits(s) / visits(s,a))]^2
			 */
			
			double actVal= this.qFunction.predictLabel(obs, TreeSearchNode.possActions[actI]);
			double confBound = UCTNode.confConst * Math.sqrt(Math.log(this.totalVisitsHere) / this.timesActsTaken[actI]);
//			System.out.println("act: " + actI +", Q: " + actVal + ", n(s): " + this.totalVisitsHere 
//								+ ", n(s,a): " + this.timesActsTaken[actI] + ", confBound: " + confBound
//								+ ", sumVal: " + (actVal+confBound));
			actVal += confBound;
			
			if (actVal == maxActVal) {
				maxActIndices.add(actI);
			}
			else if (actVal > maxActVal) {
				maxActVal = actVal;
				maxActIndices.clear();
				maxActIndices.add(actI);
			}
		}
		int maxActI = maxActIndices.get(TreeSearchNode.random.nextInt(maxActIndices.size()));
		return maxActI;		
	}

	
	public static void setConfidenceConstant(double confConst) { UCTNode.confConst = confConst;}
	public static void setDeterministicTransitions(boolean deterministicTransitions) {
		UCTNode.deterministicTransitions  = deterministicTransitions;}


	public double createTreePathSample(int depthToGo) {
		return this.createMCPathSample(depthToGo);
	}
	
	// remove this
	public String loopMazeStateToString() {
		String str = "";
		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 6; col++) {
				if (this.obs.intArray[0] == col && this.obs.intArray[1] == row)
					str += "X";
				else if (row == 0 && col == 5)
					str += "!";
				else
					str += "-";
			}
			str += "\n";
		}
		return str;
	}
	
}

package edu.utexas.cs.tamerProject.featGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;

/**
 * This class's children serve primarily to take observations and possibly actions 
 * as input and output features (at this point, always as an array of doubles).
 * 
 * Also, because it has possStaticActions as a class variable, this class has a
 * number of methods that serve functions beyond feature extraction. Important
 * methods of this type are getPossActions(), getRandomAction(), getActIndex(),
 * and getMaxActs().
 * 
 * @author W. Bradley Knox
 *
 */
public abstract class FeatGenerator{
	
	protected boolean verbose = false;
	public Random randGenerator = new Random();
	public static final Random staticRandGenerator = new Random();
	public int[][] theObsIntRanges;
	public double[][] theObsDoubleRanges; 
	public int[][] theActIntRanges;
	public double[][] theActDoubleRanges;
	//public static ArrayList<int[]> possActIntArrays;
	public static ArrayList<Action> possStaticActions = new ArrayList<Action>();;
	public static ArrayList<Action> forbiddenActions = new ArrayList<Action>();

	////protected RegressionModel model;
	protected int numFeatures;
	
	private RegressionModel modelForSupplFeatGen;
	private FeatGenerator featGenForSupplModel;
	private String featSource = "state-action";
	
	public FeatGenerator(int[][] theObsIntRanges, double[][] theObsDoubleRanges, 
			int[][] theActIntRanges, double[][] theActDoubleRanges){
		//this.randGenerator = new Random();
		this.theActIntRanges = theActIntRanges;
		this.theActDoubleRanges = theActDoubleRanges;
		this.theObsIntRanges = theObsIntRanges;
		this.theObsDoubleRanges = theObsDoubleRanges;
		//FeatGenerator.possActIntArrays = getPossActIntArrays();
		ArrayList<int[]> possActIntArrays = this.recurseForPossActIntArrays(new int[0]);//getPossActIntArrays();
		possStaticActions.clear();
		for (int[] possActIntArray: possActIntArrays){
			Action possStaticAction = new Action();
			possStaticAction.intArray = possActIntArray;
			possStaticActions.add(possStaticAction);
		}
	}


	
	public void setVerbose(boolean verbose) {this.verbose = verbose;}
	public void setFeatSource(String source) {
		this.featSource = source;
	}
	public static void forbidAction(Action act){System.out.println("Forbidding action " + act); forbiddenActions.add(act);}
	public static void allowAction(Action act){
		if (!forbiddenActions.remove(act)) {
			System.out.println("Attempted to allow an action that wasn't forbidden.");
		}
	}
	public void setRandSeed(long seed) {this.randGenerator = new Random(seed);}
	public int getNumFeatures() {return this.numFeatures;}
	public abstract int[] getActionFeatIndices(); // when applicable, this tells which features in the returned double[] indicate the action taken
	public abstract int[] getNumFeatValsPerFeatI();
	public double[] getFeats(Observation obs, Action act) {
		double[] feats = null;
		if (this.featSource.equals("state-action"))
			feats = getSAFeats(obs, act);
		//else if (this.featSource.equals("state-state"))
		//	feats = getSSFeats(intVars1, doubleVars1, intVars2, doubleVars2);
		else {
			System.out.println(this.featSource + " is not a valid source for features. Exiting.");
			System.exit(0);
		}
		if (this.modelForSupplFeatGen != null) {
			feats = this.addSupplFeat(feats, obs, act);
		}
		return feats;
	}

	public abstract double[] getSAFeats(Observation obs, Action act);
	public abstract double[] getSFeats(Observation obs);
	public ArrayList<Action> getMaxActs(RegressionModel model, Observation obs){
		return getMaxActs(model, obs, null);
	}

	
	public ArrayList<Action> getMaxActs(RegressionModel model, Observation obs, ArrayList<Action> possActions){
		ArrayList<Action> bestActions = new ArrayList<Action>();
		if (model.noRealValFeats()){
			double bestActionVal = Double.NEGATIVE_INFINITY;
			if (possActions == null) {
				possActions = this.getPossActions(obs);
				if (possActions.size() == 0)
					System.err.println("Zero possible actions returned for obs " + Arrays.toString(obs.doubleArray));
			}
			
			double[] stateActVals = model.getStateActOutputs(obs, possActions);
			//System.out.println("\t****************Predicted value by action: " + Arrays.toString(stateActVals) + "**********************");	
			for (int actI = 0; actI < possActions.size(); actI++) {
				if (stateActVals[actI] > bestActionVal){
					bestActions.clear();
					bestActionVal = stateActVals[actI];
				}
				if (stateActVals[actI] >= bestActionVal) {
					bestActions.add(possActions.get(actI).duplicate());
				}
			}
		}
		else {
			System.err.println("FeatGenerator.getMaxAct() does not yet support real-valued actions variables. Exiting.");
			System.exit(1);
		}
		//System.out.println("num max acts in FeatGen: " + bestActions.size());
		return bestActions;
	}


	public Action getRandomAction(){
		int actI;
		do {actI = this.randGenerator.nextInt(FeatGenerator.possStaticActions.size());}
		while(FeatGenerator.forbiddenActions.contains(FeatGenerator.possStaticActions.get(actI)));
		return FeatGenerator.possStaticActions.get(actI).duplicate();
	}
	public int[] getRandomActIntArray(){
		int actI;
		do {actI = this.randGenerator.nextInt(FeatGenerator.possStaticActions.size());}
		while(FeatGenerator.forbiddenActions.contains(FeatGenerator.possStaticActions.get(actI)));
		return FeatGenerator.possStaticActions.get(actI).intArray.clone();
	}
	public ArrayList<double[]> genRandomFeats(int numSamples){
		ArrayList<double[]> randSamples = new ArrayList<double[]>();
		Observation o;
		Action a = new Action();
		double[] feats;
		for (int j = 0; j < numSamples; j++) {
			o = getRandomObs();
			a.intArray = getRandomActIntArray();
			feats = getFeats(o, a);
			randSamples.add(feats);
		}
		return randSamples;
	}
	public Observation getRandomObs(){
		Observation o = new Observation();
		o.intArray = new int[this.theObsIntRanges.length];	
		o.doubleArray = new double[this.theObsDoubleRanges.length];
		for (int i = 0; i < this.theObsIntRanges.length; i++) {
			o.intArray[i] = (int)getRandomVal(this.theObsIntRanges[i][0], this.theObsIntRanges[i][1], true);
		}
		for (int i = 0; i < this.theObsDoubleRanges.length; i++) {
			o.doubleArray[i] = getRandomVal(this.theObsDoubleRanges[i][0], this.theObsDoubleRanges[i][1], false);
		}
		return o;
	}
	
	
	public ArrayList<Action> getPossActions(Observation obs) {
		ArrayList<Action> possActionDupes = new ArrayList<Action>();
		for (Action a: FeatGenerator.possStaticActions) {
			if (!FeatGenerator.forbiddenActions.contains(a))
				possActionDupes.add(a.duplicate());
			else
				System.out.println("A forbidden action has been ignored.");
		}
		return possActionDupes;
	}
//	protected ArrayList<int[]> getPossActIntArrays() {
//		return this.recurseForPossActIntArrays(new int[0]);
//	}
	
	/*
	 * TODO Fix: If this is used anywhere but in this class's constructor, forbidden actions will 
	 * not be reflected.
	 */
	protected ArrayList<int[]> recurseForPossActIntArrays(int[] actSoFar){
		if (actSoFar.length ==  this.theActIntRanges.length) { // base case
			ArrayList<int[]> list = new ArrayList<int[]>();
			list.add(actSoFar);
			return list;
		}
		int currActIndex = actSoFar.length;
		int a = this.theActIntRanges[currActIndex][1];
		int b = this.theActIntRanges[currActIndex][0];
		int numPossibleValues = (this.theActIntRanges[currActIndex][1] 
		                          - this.theActIntRanges[currActIndex][0]) + 1;
		ArrayList<int[]> fullActs = new ArrayList<int[]>();
		// iterate through all possible values of the next action integer
		for (int i = 0; i < numPossibleValues; i++){
			int currVal = theActIntRanges[currActIndex][0] + i;
			int[] newActSoFar = new int[currActIndex + 1];
			for (int j = 0; j < actSoFar.length; j++){
				newActSoFar[j] = actSoFar[j];
			}
			newActSoFar[currActIndex] = currVal;
			fullActs.addAll(this.recurseForPossActIntArrays(newActSoFar));
		}
		return fullActs;
	}
	
	public int getActIntIndex(int[] actIntArray){
		return FeatGenerator.getActIntIndex(actIntArray, FeatGenerator.possStaticActions);
	}
	public static int getActIntIndex(int[] actIntArray, ArrayList<Action> possStaticActions){
		int i = 0;
		while (i < possStaticActions.size()) {
			if (Arrays.equals(possStaticActions.get(i).intArray, actIntArray))
				break;
			i++;
		}
		if (i == possStaticActions.size()) {
			System.err.println("\n\nNo act match found for act int array: " + Arrays.toString(actIntArray));
			System.err.print("Possible act int arrays to match: ");
			for (Action act: possStaticActions)
				System.err.print("  " + Arrays.toString(act.intArray));
			System.err.println("Stack:\n" + Arrays.toString(Thread.currentThread().getStackTrace()));
			System.err.println("Killing agent process\n\n");
			System.exit(1);
		}
		return i;
	}
	
	public double getRandomVal(double inclLowerBound, double inclUpperBound, boolean isInt) {
		double randVal;
		if (isInt) {
			int numPossVals = (int)((inclUpperBound - inclLowerBound) + 1);
			int unshiftedVal = randGenerator.nextInt(numPossVals);
			randVal = (unshiftedVal + inclLowerBound);
		}
		else {
			double rangeFactor = inclUpperBound - inclLowerBound;
			double unshiftedVal = randGenerator.nextDouble() * rangeFactor;
			randVal = (unshiftedVal + inclLowerBound);
		}
		return randVal;
	}


	//// Below here are methods needed only for TAMER+RL combination techniques
	public void setSupplModel(RegressionModel model, FeatGenerator featGen) {
		this.modelForSupplFeatGen = model;
		this.featGenForSupplModel = featGen;
		this.numFeatures++;
	}
	protected double[] addSupplFeat(double[] feats, Observation obs, Action act){
		double supplFeat = getValFromSupplModel(obs, act);
		feats = Arrays.copyOf(feats, feats.length + 1);
		feats[feats.length - 1] = supplFeat;
		return feats;
	}
	public double getValFromSupplModel(Observation obs, Action act){
		double[] feats = this.featGenForSupplModel.getSAFeats(obs, act);
		return this.modelForSupplFeatGen.predictLabel(feats);
	}

	// These currently do not support feature generators with a supplemental model added.
	// Such support is unnecessary as long as this is only used by HInfluence.
	public abstract double[] getMaxPossFeats(); 
	public abstract double[] getMinPossFeats();
	public abstract double[] getMaxPossSFeats(); 
	public abstract double[] getMinPossSFeats();
	
}



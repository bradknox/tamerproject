package edu.utexas.cs.tamerProject.featGen;

import java.lang.Double;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.modeling.weka.WekaModelPerActionModel;


public class FeatGen_NoChange extends FeatGenerator{
	
	int[] actionFeatIndices;
	int[] numFeatValsPerFeatI;
	int firstActIndex;
	
	public FeatGen_NoChange(int[][] theObsIntRanges, double[][] theObsDoubleRanges, 
							int[][] theActIntRanges, double[][] theActDoubleRanges) {
		super(theObsIntRanges, theObsDoubleRanges, theActIntRanges, theActDoubleRanges);
		int actionDims = theActIntRanges.length + theActDoubleRanges.length;
		int stateDims = theObsIntRanges.length + theObsDoubleRanges.length;
		this.actionFeatIndices = new int[actionDims];
		for (int i = 0; i < actionDims; i++){
			this.actionFeatIndices[i] = stateDims + i;
		}
		int totalDims = stateDims + actionDims;
		this.firstActIndex = stateDims;
		this.numFeatValsPerFeatI = new int[totalDims];
		for (int i = 0; i < totalDims; i++){
			if (i < stateDims || i >= (stateDims + theActIntRanges.length))
				numFeatValsPerFeatI[i] = -1; // currently not filling cells out for state variables, 
			                                 // since it's not yet using them and they might be continuous
			else
				numFeatValsPerFeatI[i] = (theActIntRanges[i - stateDims][1] - theActIntRanges[i - stateDims][0]) + 1;
		}
		this.numFeatures = theObsIntRanges.length + theObsDoubleRanges.length 
								+ theActIntRanges.length + theActDoubleRanges.length;

	}

	
	public int[] getActionFeatIndices() {
		if (verbose) {
			System.out.println("Getting act feats indices: " + Arrays.toString(this.actionFeatIndices));
		}
		return this.actionFeatIndices;
	}

	public int[] getNumFeatValsPerFeatI() {
		if (verbose) {
			System.out.println("Getting num feat vals per feat index: " + Arrays.toString(this.numFeatValsPerFeatI));
		}
		return this.numFeatValsPerFeatI;
	}

	public double[] getSAFeats(Observation obs, Action act){
		return getSAFeats(obs.intArray, obs.doubleArray, act.intArray, act.doubleArray);
	}
	public double[] getSAFeats(int[] intObsVals, double[] doubleObsVals, int[] intActionVals, double[] doubleActVals){
		double[] feats = new double[intObsVals.length + doubleObsVals.length + intActionVals.length + doubleActVals.length];
		for (int i = 0; i < intObsVals.length; i++) {
			feats[i] = intObsVals[i];
		}
		int numIntObsVals = intObsVals.length;
		for (int i = 0; i < doubleObsVals.length; i++) {
			feats[numIntObsVals + i] = doubleObsVals[i];
		}
		for (int i = 0; i < intActionVals.length; i++) {
			feats[this.firstActIndex + i] = intActionVals[i];
		}
		// not bothering to implement double action values portion for now
		return feats;
	}

	public double[] getSSFeats(int[] intObsVals, double[] doubleObsVals, int[] intNextObsVals, double[] doubleNextObsVals){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}

	public double[] getSFeats(Observation obs) {
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}

	public double[] getMaxPossSFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}
	public double[] getMinPossSFeats(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}
	
	public void setSupplModel(RegressionModel model, FeatGenerator featGen) {
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
	}

	
	
//
//	// currently assumes that there are no real-valued action variables
//	public ArrayList<int[]> findMaxActs(RegressionModel model, int[] intObsVals, double[] doubleObsVals){
//		ArrayList<int[]> bestActions = new ArrayList<int[]>(); //new int[actionFeatIndices.length];
//		double bestActionVal = Double.NEGATIVE_INFINITY;
//		ArrayList<int[]> possibleActs = recurseForPossActIntArrays(new int[0]);
//		Iterator<int[]> actIter = possibleActs.iterator();
//		while (actIter.hasNext()){
//			int[] currAct = actIter.next();
//			double[] feats = getSAFeats(intObsVals, doubleObsVals, currAct, new double[0]);
//			double valForAct = model.predictLabel(feats);
//			if (verbose) {
//				System.out.println("currAct: " + Arrays.toString(currAct));
//				System.out.println("valForAct: " + valForAct);
//			}
//			if (valForAct > bestActionVal){
//				bestActions.clear();
//				bestActions.add(currAct);
//				bestActionVal = valForAct;
//			}
//			else if (valForAct == bestActionVal) {
//				bestActions.add(currAct);
//			}
//		}
//		return bestActions;
//	}

//	// gets all actions tied for best from findMaxActs and then returns one of them
//	public Action getMaxAct(RegressionModel model, int[] intObsVals, 
//							double[] doubleObsVals, Action baseAction){
//		if (verbose) {
//			System.out.println("Getting greedy action.");
//		}
//		ArrayList<int[]> maxActs = findMaxActs(model, intObsVals, doubleObsVals);
//		int actIndex = this.randGenerator.nextInt(maxActs.size());
//		baseAction.intArray = maxActs.get(actIndex);
//		return baseAction;
//	}


	public ArrayList<double[]> genRandomFeats(int numSamples){
		ArrayList<double[]> randSamples = new ArrayList<double[]>();
		double[] feats;
		for (int j = 0; j < numSamples; j++) {
			feats = new double[this.theObsIntRanges.length + this.theObsDoubleRanges.length 
							   + this.theActIntRanges.length + theActDoubleRanges.length];
			for (int i = 0; i < this.theObsIntRanges.length; i++) {
				feats[i] = (int)getRandomVal(this.theObsIntRanges[i][0], this.theObsIntRanges[i][1], true);
			}
			int numFeatsOfDiffTypeFinished = this.theObsIntRanges.length;
			for (int i = 0; i < this.theObsDoubleRanges.length; i++) {
				feats[numFeatsOfDiffTypeFinished + i] = getRandomVal(this.theObsDoubleRanges[i][0], this.theObsDoubleRanges[i][1], false);
			}
			numFeatsOfDiffTypeFinished = numFeatsOfDiffTypeFinished + this.theObsDoubleRanges.length;
			for (int i = 0; i < this.theActIntRanges.length; i++) {
				feats[numFeatsOfDiffTypeFinished + i] = (int)getRandomVal(this.theActIntRanges[i][0], this.theActIntRanges[i][1], true);
			}
			numFeatsOfDiffTypeFinished = numFeatsOfDiffTypeFinished + this.theActIntRanges.length;
			for (int i = 0; i < this.theActDoubleRanges.length; i++) {
				feats[numFeatsOfDiffTypeFinished + i] = getRandomVal(this.theActDoubleRanges[i][0], this.theActDoubleRanges[i][1], false);
			}
			randSamples.add(feats);
		}
		return randSamples;
	}

	
	public double[] getMaxPossFeats(){
		double[] maxPossFeats = new double[this.theObsIntRanges.length + this.theObsDoubleRanges.length 
						   + this.theActIntRanges.length + theActDoubleRanges.length];
		for (int i = 0; i < this.theObsIntRanges.length; i++) {
			maxPossFeats[i] = this.theObsIntRanges[i][1];
		}
		int numFeatsOfDiffTypeFinished = this.theObsIntRanges.length;
		for (int i = 0; i < this.theObsDoubleRanges.length; i++) {
			maxPossFeats[numFeatsOfDiffTypeFinished + i] = this.theObsDoubleRanges[i][1];
		}
		numFeatsOfDiffTypeFinished = numFeatsOfDiffTypeFinished + this.theObsDoubleRanges.length;
		for (int i = 0; i < this.theActIntRanges.length; i++) {
			maxPossFeats[numFeatsOfDiffTypeFinished + i] = this.theActIntRanges[i][1];
		}
		numFeatsOfDiffTypeFinished = numFeatsOfDiffTypeFinished + this.theActIntRanges.length;
		for (int i = 0; i < this.theActDoubleRanges.length; i++) {
			maxPossFeats[numFeatsOfDiffTypeFinished + i] = this.theActDoubleRanges[i][1];
		}
		return maxPossFeats;
	}
	
	public double[] getMinPossFeats(){
		double[] minPossFeats = new double[this.theObsIntRanges.length + this.theObsDoubleRanges.length 
		        						   + this.theActIntRanges.length + theActDoubleRanges.length];
		for (int i = 0; i < this.theObsIntRanges.length; i++) {
			minPossFeats[i] = this.theObsIntRanges[i][0];
		}
		int numFeatsOfDiffTypeFinished = this.theObsIntRanges.length;
		for (int i = 0; i < this.theObsDoubleRanges.length; i++) {
			minPossFeats[numFeatsOfDiffTypeFinished + i] = this.theObsDoubleRanges[i][0];
		}
		numFeatsOfDiffTypeFinished = numFeatsOfDiffTypeFinished + this.theObsDoubleRanges.length;
		for (int i = 0; i < this.theActIntRanges.length; i++) {
			minPossFeats[numFeatsOfDiffTypeFinished + i] = this.theActIntRanges[i][0];
		}
		numFeatsOfDiffTypeFinished = numFeatsOfDiffTypeFinished + this.theActIntRanges.length;
		for (int i = 0; i < this.theActDoubleRanges.length; i++) {
			minPossFeats[numFeatsOfDiffTypeFinished + i] = this.theActDoubleRanges[i][0];
		}
		return minPossFeats;
	}

	
	public static void main(String[] args){
		int[][] theObsIntRanges = {};
		double[][] theObsDoubleRanges = {{0.0, 1.0}, {-1.0, 0.0}};
		int[][] theActIntRanges = {{0, 2}};
		double[][] theActDoubleRanges = {};
		FeatGenerator featGen = new FeatGen_NoChange(theObsIntRanges, theObsDoubleRanges, 
													 theActIntRanges, theActDoubleRanges);

		ArrayList<double[]> randFeats = featGen.genRandomFeats(100);
		for (int i = 0; i < randFeats.size(); i++) {
			System.out.println(Arrays.toString(randFeats.get(i)));
		}
		
		Observation obs = new Observation();
		Action act = new Action();
		obs.intArray = new int[0];
		double[] doubleObsVals = {0.0, 1.0 };
		obs.doubleArray = doubleObsVals;
		int[] intActionVals = {1};
		act.intArray = intActionVals;
		act.doubleArray = new double[0];
		double[] feats = featGen.getSAFeats(obs, act);
		System.out.println("feats: " + Arrays.toString(feats));
		//for (int i = 0; i < feats.length; i++) {
		//	System.out.println(feats[i]);
		///}

		String s = "";
		int[] actIs = new int[1];
		actIs[0] = 2;
		int[] numFeatVals = {0,0,3};
		RegressionModel model = new WekaModelPerActionModel(s, featGen);

		double[] attr = {1.0, 1.0, 1.0};
		Sample sample = new Sample(attr, 20.0, 1.0, 1);
		model.addInstance(sample);
		model.addInstance(sample);
		model.addInstance(sample);
		double[] attr2 = {0.0, 1.0, 1.0};
		sample = new Sample(attr2, 10.0, 1.0, 1);
		model.addInstance(sample);
		model.addInstance(sample);
		double[] attr3 = {1.0, 0.0, 1.0};
		sample = new Sample(attr3, 10.0, 1.0, 1);
		model.addInstance(sample);
		double[] attr4 = {0.0, 0.0, 2.0};
		sample = new Sample(attr4, 20.0, 1.0, 1);
		model.addInstance(sample);
		model.addInstance(sample);
		model.addInstance(sample);
		
		model.buildModel();
		double[] attr5 = {0.0, 1.0, 1.0};
		System.out.println("predicted label: " + model.predictLabel(attr5));
	   
		Action maxAct = model.getMaxAct(obs, null);
		System.out.println("greedy action: " + maxAct.intArray);

	}
}



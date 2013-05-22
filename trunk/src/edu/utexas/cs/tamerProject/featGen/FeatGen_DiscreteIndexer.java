package edu.utexas.cs.tamerProject.featGen;

/**
 * FeatGen_Discretize both discretizes state and converts it into a 
 * boolean array indicating which bin the state falls in; separating 
 * the two might improve the code base.
 * 
 * FeatGen_Discretize might not be compatible with WekaModelPerActionModel
 * 
 * To only get an array from state (not action) initialize with theActIntRanges = {{0,0}}
 */

import java.lang.Double;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.modeling.weka.WekaModelWrap;


public class FeatGen_DiscreteIndexer extends FeatGenerator{
	

//	private int numActions;
//	private int numObsFeats;
//	
	int maxObsIndex;
	int maxActIndex;
	int[] featCount; // TODO What is this for? Keep?

	public FeatGen_DiscreteIndexer(int[][] theObsIntRanges, double[][] theObsDoubleRanges, 
							int[][] theActIntRanges, double[][] theActDoubleRanges)
	{
		super(theObsIntRanges, theObsDoubleRanges, theActIntRanges, theActDoubleRanges);
		
		/*
		 * Throw error if there are non-discrete observations or actions
		 */
		if (theObsDoubleRanges.length > 0 || theActDoubleRanges.length > 0) {
			System.err.println("FeatGen_DiscreteIndexer cannot handle continuous acts or observations. Exiting");
			System.exit(1);
		}
		
		

		
		//// find number of features
		int numPossObservations = 1;
		for (int i = 0; i < theObsIntRanges.length; i++){
			numPossObservations *= (theObsIntRanges[i][1] - theObsIntRanges[i][0]) + 1; 
		}
		
		this.maxObsIndex = numPossObservations - 1;
		
		this.maxActIndex = FeatGen_DiscreteIndexer.possStaticActions.size() - 1;		
		this.numFeatures = 2; // state index and action index
		featCount = new int[this.numFeatures];
	}

//	public void setSupplModel(RegressionModel model, FeatGenerator featGen) {
//		super.setSupplModel(model, featGen);
//		System.out.println("num features inside featGen_Disc: "+ this.numFeatures);
//		featCount = Arrays.copyOf(featCount, featCount.length + 1);
//	}

	
	public int getNumObsFeats(){return 1;}
	public int getNumActFeats(){return 1;}
	public int getNumActs(){return (this.maxActIndex + 1);}
	public int getNumObs(){return this.maxObsIndex + 1;}
	

	
	public int getActNum(int[] intActionVals){
		int actNum = 0;
		int actMultiplicand = 1;
		for (int i = 0; i < intActionVals.length; i++) {
			int numActValsThisDim = this.theActIntRanges[i][1] - this.theActIntRanges[i][0] + 1;
			int thisDimActVal = intActionVals[i] - this.theActIntRanges[i][0];
			actNum += actMultiplicand * thisDimActVal;
			actMultiplicand *= numActValsThisDim;
		}
		return actNum;
	}
	
	
	
	
	
//	
//	public int[] getIntActionVals(int actNum){
//		int[] intActionVals = new int[theActIntRanges.length];
//		int actDivisor = this.numActions;
//		for (int i = intActionVals.length - 1; i >= 0; i--) {
//			int numActValsThisDim = this.theActIntRanges[i][1] - this.theActIntRanges[i][0] + 1;
//			actDivisor /= numActValsThisDim;
//			int thisDimActVal = actNum / actDivisor; 
//			intActionVals[i] = thisDimActVal + this.theActIntRanges[i][0];
//			actNum %= actDivisor;
//		}
//		return intActionVals;
//	}

	public int getDiscObsNum(int[] intObsVals, double[] doubleObsVals){
		int obsNum = 0;
		int obsMultiplicand = 1;
		for (int obsI = 0; obsI < intObsVals.length; obsI++) {
			int numObsValsThisDim = this.theObsIntRanges[obsI][1] - this.theObsIntRanges[obsI][0] + 1;
			int thisDimObsVal = intObsVals[obsI] - this.theObsIntRanges[obsI][0];
			obsNum += obsMultiplicand * thisDimObsVal;
			obsMultiplicand *= numObsValsThisDim;
		}
		return obsNum;
	}
	
	
//	public double[] getSAFeatsFromIndices(int discObsNum, int actNum){
//		double[] feats = new double[this.numObsFeats * this.numActions];
//		for (int actI = 0; actI < this.numActions; actI++){
//			for (int obsI = 0; obsI < this.numObsFeats; obsI++){
//				int featsI = (actI * this.numObsFeats) + obsI;
//				if (actI == actNum && discObsNum == obsI){
//					feats[featsI] = 1;
//					this.featCount[featsI]++;
//					if (verbose)
//						System.out.println("visits to feat " + featsI + ": " + this.featCount[featsI]);
//				}
//				else {
//					feats[featsI] = 0;
//				}
//			}
//		}
//		// not bothering to implement double action values portion for now
//		return feats;		
//	}
	
	public double[] getSAFeats(Observation obs, Action act){
		return getSAFeats(obs.intArray, obs.doubleArray, act.intArray, act.doubleArray);
	}
	public double[] getSAFeats(int[] intObsVals, double[] doubleObsVals, int[] intActionVals, double[] doubleActVals){
		int actNum = getActNum(intActionVals);
		int discObsNum = getDiscObsNum(intObsVals, doubleObsVals);
		if (verbose) {
			System.out.println("actNum: " + actNum);
			System.out.println("discObsNum: " + discObsNum);
		}
		
		double[] feats = new double[this.numFeatures];
		feats[0] = discObsNum;
		feats[1] = actNum;
		return feats;
	}




	public ArrayList<double[]> genRandomFeats(int numSamples){
		ArrayList<double[]> randSamples = new ArrayList<double[]>();
		double[] feats;
		for (int j = 0; j < numSamples; j++) {
			int discObsNum =  randGenerator.nextInt(this.maxObsIndex + 1);
			int actNum = randGenerator.nextInt(this.maxActIndex + 1);
			feats = new double[this.numFeatures];
			feats[0] = discObsNum;
			feats[1] = actNum;
			randSamples.add(feats);
		}
		return randSamples;
	}
	
	public double[] getMaxPossFeats(){
		double[] maxPossFeats = new double[this.numFeatures];
		maxPossFeats[0] = this.maxObsIndex;
		maxPossFeats[1] = this.maxActIndex;
		return maxPossFeats;
	}
	public double[] getMinPossFeats(){
		return new double[this.numFeatures];
	}

	

	
	public static void main(String[] args){
		int[][] theObsIntRanges = {{0,2},{0,1}};
		double[][] theObsDoubleRanges = {};
		int[][] theActIntRanges = {{0, 2}};
		double[][] theActDoubleRanges = {};
		FeatGen_DiscreteIndexer featGen = new FeatGen_DiscreteIndexer(theObsIntRanges, theObsDoubleRanges, 
													 theActIntRanges, theActDoubleRanges);

		System.out.println("Printing a bunch of random features.");
		ArrayList<double[]> randFeats = featGen.genRandomFeats(100);
		for (int i = 0; i < randFeats.size(); i++) {
			System.out.println(Arrays.toString(randFeats.get(i)));
		}
		System.out.println("Done.");
		
		int[] intObsVals = {1, 1};
		double[] doubleObsVals = {};
		int[] intActionVals = {2};
		double[] doubleActVals = {};
		double[] feats = featGen.getSAFeats(intObsVals, doubleObsVals, intActionVals, doubleActVals);
		System.out.println("feats: " + Arrays.toString(feats));
		//for (int i = 0; i < feats.length; i++) {
		//	System.out.println(feats[i]);
		///}

		RegressionModel model = new WekaModelWrap(featGen);

		double[] doubleObsVals1 = {};
		int[] intActionVals1 = {1};
		int[] intObsVals1 = {1, 1};
		feats = featGen.getSAFeats(intObsVals1, doubleObsVals1, intActionVals1, doubleActVals);
		Sample sample = new Sample(feats, 20.0, 1.0, 1);
		model.addInstance(sample);
		model.addInstance(sample);
		model.addInstance(sample);

		double[] doubleObsVals2 = {};
		int[] intActionVals2 = {1};
		int[] intObsVals2 = {0, 1};
		feats = featGen.getSAFeats(intObsVals2, doubleObsVals2, intActionVals2, doubleActVals);
		sample = new Sample(feats, 10.0, 1.0, 1);
		model.addInstance(sample);
		model.addInstance(sample);
		
		double[] doubleObsVals3 = {};
		int[] intActionVals3 = {1};
		int[] intObsVals3 = {2, 1};
		feats = featGen.getSAFeats(intObsVals3, doubleObsVals3, intActionVals3, doubleActVals);
		sample = new Sample(feats, 10.0, 1.0, 1);
		model.addInstance(sample);
		
		double[] doubleObsVals4 = {};
		int[] intActionVals4 = {2};
		int[] intObsVals4 = {2, 0};
		feats = featGen.getSAFeats(intObsVals4, doubleObsVals4, intActionVals4, doubleActVals);
		sample = new Sample(feats, 20.0, 1.0, 1);
		model.addInstance(sample);
		model.addInstance(sample);
		model.addInstance(sample);
		
		model.buildModel();
		double[] doubleObsVals5 = {};
		int[] intActionVals5 = {1};
		int[] intObsVals5 = {1, 1};
		feats = featGen.getSAFeats(intObsVals5, doubleObsVals5, intActionVals5, doubleActVals);
		System.out.println("predicted label: " + model.predictLabel(feats));
		Observation o = new Observation();
		o.intArray = intObsVals;
		o.doubleArray = doubleObsVals;
	   
		Action maxAct = model.getMaxAct(o, null);
		System.out.println("greedy action: " + maxAct.intArray);

	}
	
	
	public int[] getNumFeatValsPerFeatI(){
		System.err.println("This method, getNumFeatValsPerFeatI(), is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new int[0];
	}
	
	public int[] getActionFeatIndices(){
		System.err.println("This method, getActionFeatIndices(), is not implemented in " + this.getClass() + ". Exiting.");
		System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
		System.exit(1);
		return new int[0];
	}
	
	public double[] getSSFeats(int[] intObsVals, double[] doubleObsVals, int[] intNextObsVals, double[] doubleNextObsVals){
		System.err.println("This method, getSSFeats(), is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}

	public double[] getSFeats(Observation obs) {
		System.err.println("This method, getSFeats(), is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}
	
	public double[] getMaxPossSFeats(){
		System.err.println("This method, getMaxPossSFeats(), is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}
	public double[] getMinPossSFeats(){
		System.err.println("This method, getMinPossSFeats(), is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}
}



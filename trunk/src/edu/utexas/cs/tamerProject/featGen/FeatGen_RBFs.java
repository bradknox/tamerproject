package edu.utexas.cs.tamerProject.featGen;

/**
 * This could be sped by removing the exponential and replacing it with a 
 * table look-up.
 * 
 * This feature generator could be made more general if it could 
 * accept a set of sets that indicate the RBF dims; e.g. {{1}, {2,3}, {4,5,6}} 
 * would create 1D RBFs on dim 1, 2D RBFs over dims 2 and 3, and 3D RBFs over 
 * 4, 5, and 6.
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
/**import java.util.Random;
import java.util.Iterator;**/

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.utils.Stopwatch;

public class FeatGen_RBFs extends FeatGenerator{
	
	private double relWidth;
	
	/*
	 * width is the Gaussian RBF's "width" (as it is called in Sutton and Barto),
	 * where a unit is the distance between adjacent RBF means.	The width would
	 * be the variance if this were scaled to integrate to one, but the constant
	 * 1/sqrt(2*pi*variance) has been removed.			 
	 */
	private double width; 
	
	private int basisFcnsPerDim;
	private int numObsDims;
	ArrayList<double[]> means; // RBF means are unnormalized 
	
	double[] theObsRangeSizes;
	double[] dimDistNormFactor;

	boolean addBiasFeatPerAct = false;
	double biasFeatVal = 0;
	
	boolean approxFeats = true;

		
	public FeatGen_RBFs(int[][] theObsIntRanges, double[][] theObsDoubleRanges, 
			int[][] theActIntRanges, double[][] theActDoubleRanges,
			int basisFcnsPerDim, double relWidth){
		super(theObsIntRanges, theObsDoubleRanges, theActIntRanges, theActDoubleRanges);
		
		this.basisFcnsPerDim = basisFcnsPerDim;
		this.relWidth = (double)relWidth;
//		this.width = relWidth / (basisFcnsPerDim - 1); // features are normalized between zero and one
		double[] normBounds = {0, 1}; // features are normalized between zero and one by default
		this.width = (normBounds[1] - normBounds[0]) * this.relWidth / (basisFcnsPerDim - 1); 
		System.out.println("width: " + width);
		this.numObsDims = theObsIntRanges.length + theObsDoubleRanges.length;
		this.means = this.getRBFMeans(this.getTheObsRangesAndSetNormalization(normBounds));

		this.numFeatures = this.means.size() * this.possStaticActions.size();
//		System.out.println("numFeatures: " + this.numFeatures);
//		System.out.print("RBF Means:  ");
//		for (double[] mean: this.means) {
//			System.out.print(Arrays.toString(mean) + ",  ");
//		}
//		System.out.println();
//		System.out.println("this.means.size(): " + this.means.size());
//		System.out.println("this.possActIntArrays.size(): " + this.possActIntArrays.size());
//		for (int[] actIntArray: this.possActIntArrays)
//			System.out.print("possActIntArray: " + Arrays.toString(actIntArray));
//		System.out.println();
	}
	

		
	private double[][] getTheObsRangesAndSetNormalization(double[] normBounds) {
		double[][] theObsRanges = new double[this.numObsDims][2];
		this.theObsRangeSizes = new double[this.numObsDims];
		this.dimDistNormFactor = new double[this.numObsDims];
		int i = 0;
		for (int[] range: this.theObsIntRanges){
			theObsRanges[i][0] = (double)range[0];
			theObsRanges[i][1] = (double)range[1];
			this.theObsRangeSizes[i] = range[1] - range[0];
			this.dimDistNormFactor[i] = (normBounds[1] - normBounds[0]) / this.theObsRangeSizes[i];
			i++;
		}
		for (double[] range: this.theObsDoubleRanges){
			theObsRanges[i][0] = (double)range[0];
			theObsRanges[i][1] = (double)range[1];
			this.theObsRangeSizes[i] = (double)(range[1] - range[0]);
			this.dimDistNormFactor[i] = (normBounds[1] - normBounds[0]) / this.theObsRangeSizes[i];
			i++;
		}
		return theObsRanges;
	}
	
	public void setNormBounds(double min, double max){
		System.out.print("Setting norm bounds to [" + min + ", " + max + "]. ");
		double[] normBounds = {min, max};
		this.width = (normBounds[1] - normBounds[0]) * this.relWidth / (this.basisFcnsPerDim - 1);
		System.out.println("**New width: " + this.width + "**");
		this.getTheObsRangesAndSetNormalization(normBounds);
	}
	
	public void setSupplModel(RegressionModel model, FeatGenerator featGen) {
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
	}
	
	
	
	
	
	
	private ArrayList<double[]> getRBFMeans(double[][] theObsRanges){
		return this.recurseForRBFMeans(theObsRanges, new double[0]);
	}

	protected ArrayList<double[]> recurseForRBFMeans(double[][] theObsRanges , double[] meanSoFar){
		if (meanSoFar.length ==  theObsRanges.length) { // base case
			ArrayList<double[]> list = new ArrayList<double[]>();
			list.add(meanSoFar);
			return list;
		}
		int currDimIndex = meanSoFar.length;
		//// for current obs index, add every possible mean component in this dimension and recurse
		ArrayList<double[]> fullMeans = new ArrayList<double[]>();
		for (int i = 0; i < this.basisFcnsPerDim; i++){
			double normVal = ((double)i) / (this.basisFcnsPerDim - 1);
			double rawVal = (this.theObsRangeSizes[currDimIndex] * normVal) + (double)theObsRanges[currDimIndex][0];
			double[] newMeanSoFar = Arrays.copyOf(meanSoFar, meanSoFar.length + 1);
			newMeanSoFar[currDimIndex] = rawVal;
			fullMeans.addAll(this.recurseForRBFMeans(theObsRanges, newMeanSoFar));
		}
		return fullMeans;
	}
	
	
	
	public void setBiasFeatPerAct(double val) {
		this.addBiasFeatPerAct = true;
		this.biasFeatVal =  val;
		this.numFeatures = (this.means.size() + (addBiasFeatPerAct?1:0)) * this.possStaticActions.size();
	}
	
	private double[] getStateVars(int[] intStateVars, double[] doubleStateVars) {
		double[] stateVars = new double[this.numObsDims];
		int i = 0;
		for (int val: intStateVars){
			stateVars[i] = (double)val;
			i++;
		}
		for (double val: doubleStateVars){
			stateVars[i] = (double)val;
			i++;
		}
		return stateVars;
	}
	
	
	public double[] getSAFeats(Observation obs, Action act) {
		double[] feats = new double[this.numFeatures];
		int actI = this.getActIntIndex(act.intArray);
		int i = (this.means.size() + (addBiasFeatPerAct?1:0)) * actI;
		this.fillWithStateFeats(feats, i, obs.intArray, obs.doubleArray);
			
		return feats;
	}

	
	/**
	 * This method calculates the Gaussian distance from the state variables to each RBF mean.
	 * 
	 * @param feats
	 * @param startI
	 * @param intStateVars
	 * @param doubleStateVars
	 */
	private void fillWithStateFeats(double[] feats, int startI, int[] intStateVars, double[] doubleStateVars) {
		double[] stateVars = this.getStateVars(intStateVars, doubleStateVars);
		int i = startI;
		double sqrdEucDist;
		if (this.approxFeats) { // code redundancy below allows this if statement to only be evaluated once.
			for (double[] currMean: this.means) {
				/*
				 * Get square of Euclidean distance between RBF mean and state in normalized space.
				 */
				sqrdEucDist = this.getSqrdEucDist(currMean, stateVars);
				/*
				 * Gaussian distance (some constants are absorbed into the width, including the variance).
				 */	
				feats[i] = exp((-0.5f * sqrdEucDist) / this.width);
				i++;
			}
		}
		else {
			for (double[] currMean: this.means) {
				/*
				 * Get square of Euclidean distance between RBF mean and state in normalized space.
				 */
				sqrdEucDist = this.getSqrdEucDist(currMean, stateVars);
				/*
				 * Gaussian distance (some constants are absorbed into the width, including the variance).
				 */
				feats[i] = Math.exp((-0.5 * sqrdEucDist) / this.width);
				i++;
			}
		}
		// generally any bias feature should be added by the model, not here; Only add bias here to make compatible with Python TAMER
		if (addBiasFeatPerAct)
			feats[i] = this.biasFeatVal;
	}
	

	
	
	public double[] getSFeats(Observation obs) {
		double[] feats = new double[this.means.size() + (addBiasFeatPerAct?1:0)];
		int i = 0;		
		this.fillWithStateFeats(feats, i, obs.intArray, obs.doubleArray);
		return feats;
	}
	
	

	private double getSqrdEucDist(double[] currMean, double[] stateVars) {
		double sqrdEucDist = 0;
		double rawDimDist;
		double normDimDist;
		for (int i = 0; i < currMean.length; i++){
			rawDimDist = stateVars[i] - currMean[i];
			//System.out.print("\n\n, rawDimDist: " + rawDimDist);
			normDimDist = (rawDimDist * this.dimDistNormFactor[i]); // TODO normalize means and stateVars, not here (should give a ~10% decrease in computational costs)
			//System.out.print(", normDimDist: " + normDimDist);
			sqrdEucDist += normDimDist * normDimDist;
		}
		return sqrdEucDist;
	}
	
	public int[] getNumFeatValsPerFeatI(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new int[0];
	}
	
	public int[] getActionFeatIndices(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new int[0];
	}
	
	public double[] getSSFeats(int[] intObsVals, double[] doubleObsVals, int[] intNextObsVals, double[] doubleNextObsVals){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(1);
		return new double[0];
	}
	
	public double[] getMaxPossFeats(){
		double[] maxPossFeats = new double[this.numFeatures];
		for (int i = 0; i < maxPossFeats.length; i++) {
			maxPossFeats[i] = 1.0;
		}
		if (addBiasFeatPerAct) {
			for (int actI = 0; actI < FeatGenerator.possStaticActions.size(); actI++) {
				maxPossFeats[((this.means.size() + 1) * (actI + 1)) - 1] = this.biasFeatVal;
			}
		}
		return maxPossFeats;
	}
	public double[] getMinPossFeats(){
		double[] minPossFeats = new double[this.numFeatures];
		if (addBiasFeatPerAct) {
			for (int actI = 0; actI < FeatGenerator.possStaticActions.size(); actI++) {
				minPossFeats[((this.means.size() + 1) * (actI + 1)) - 1] = this.biasFeatVal;
			}
		}
		return minPossFeats;
	}
	
	public double[] getMaxPossSFeats(){
		double[] maxPossFeats = new double[this.means.size() + (addBiasFeatPerAct?1:0)];
		for (int i = 0; i < maxPossFeats.length; i++) {
			maxPossFeats[i] = 1.0;
		}
		if (addBiasFeatPerAct) {
			maxPossFeats[this.means.size()] = this.biasFeatVal;
		}
		return maxPossFeats;
	}
	public double[] getMinPossSFeats(){
		double[] minPossFeats = new double[this.means.size() + (addBiasFeatPerAct?1:0)];
		if (addBiasFeatPerAct) {
			minPossFeats[this.means.size()] = this.biasFeatVal;
		}
		return minPossFeats;
	}
	
	
	
	
	
	
	public static void main(String[] args){
		Observation obs = new Observation();
		Action act = new Action();
		int[][] theObsIntRanges = new int[0][0];
		double[][] theObsDoubleRanges = new double[2][2]; 
		theObsDoubleRanges[0][0] = 0;
		theObsDoubleRanges[0][1] = 2;
		theObsDoubleRanges[1][0] = 0;
		theObsDoubleRanges[1][1] = 2;
		System.out.println("theObsDoubleRanges: " + Arrays.toString(theObsDoubleRanges));
		int[][] theActIntRanges = new int[1][2];
		theActIntRanges[0][0] = 0;
		theActIntRanges[0][1] = 1;
		double[][] theActDoubleRanges = new double[0][0];
		int basisFcnsPerDim = 2;
		double relWidth = 0.08; //1.0;
		
		FeatGen_RBFs featGen = new FeatGen_RBFs(theObsIntRanges, theObsDoubleRanges, 
			theActIntRanges, theActDoubleRanges,
			basisFcnsPerDim, relWidth);
		featGen.setNormBounds(-1, 1);
		
		obs.intArray = new int[0];
		double[] doubleStateVars = {0.8, 0.8};
		obs.doubleArray = doubleStateVars;
		int[] intActVars = {0};
		act.intArray = intActVars;
		act.doubleArray = null;
		System.out.println("Input: " + Arrays.toString(doubleStateVars) + ", " + Arrays.toString(intActVars));
		System.out.println("Feats: " + Arrays.toString(featGen.getSAFeats(obs, act)) + "\n");
		
		obs.doubleArray[0] = 1.2; 
		obs.doubleArray[1] = 1.0;
		intActVars[0] = 1;
		System.out.println("Input: " + Arrays.toString(doubleStateVars) + ", " + Arrays.toString(intActVars));
		System.out.println("Feats: " + Arrays.toString(featGen.getSAFeats(obs, act)) + "\n");
		
		
		
		/*
		 * Compare computation time for a large number of RBF evaluations, using both exact
		 * and approximate exp().
		 */
		Random r = new Random(21l);
		Stopwatch timer = new Stopwatch();
		int numTests = 5000000;
		int numObs = 1000;
		Observation[] obsArray = new Observation[numObs];
		for (int i = 0; i < numObs; i++) {
			obsArray[i] = new Observation();
			obsArray[i].doubleArray = new double[2];
			obsArray[i].doubleArray[0] = r.nextDouble() * theObsDoubleRanges[0][1];
			obsArray[i].doubleArray[1] = r.nextDouble() * theObsDoubleRanges[1][1];
			
		}
		boolean approx = featGen.approxFeats; //save setting
		// Test for exact exp()
		featGen.approxFeats = false;
		timer.startTimer();
//		for (int i = 0; i < numTests; i++) {
//			featGen.getSAFeats(obsArray[i % numObs], act);
//		}
//		System.out.println("\n\nTime for " + (2 * numTests) + " RBF evaluations: " + timer.getTimeElapsed());
		// Test for approx. exp()
		featGen.approxFeats = true;
		timer.startTimer();
		for (int i = 0; i < numTests; i++) {
			featGen.getSAFeats(obsArray[i % numObs], act);
		}
		System.out.println("Time for " + (2 * numTests) + " approximate RBF evaluations: " + timer.getTimeElapsed());
		
		featGen.approxFeats = approx; //set to original value		
		
		
		
		//// Python TAMER's Mountain car features
		System.out.println("\n\nPython TAMER's Mountain car features and model test");
		
		// load model weights
		String wtsPath = "/Users/bradknox/projects/rl-library-data/mc_tamer/models/ikarpov-1228858017.78-100.model";
		double[] wtsArray = null;
		try{ 
			String wtsStr = RecordHandler.getStrArray(wtsPath)[0];
			wtsArray = RecordHandler.getDoubleArrayFromStr(wtsStr);
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage() + "\nExiting.");
			System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
			System.exit(0); }
		
		System.out.println("\nLoading saved features from Python code for comparison.");
		String pyFeatsPath = "/Users/bradknox/projects/rl-library-data/mc_tamer/models/feats.python";
		double[] pyFeatsArray = null;
		try{ 
			String pyFeatsStr = RecordHandler.getStrArray(pyFeatsPath)[0];
			pyFeatsArray = RecordHandler.getDoubleArrayFromStr(pyFeatsStr);
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage() + "\nExiting.");
			System.err.println(Arrays.toString(Thread.currentThread().getStackTrace()));
			System.exit(0); }
		
		System.out.println("model weights: " + Arrays.toString(wtsArray));
		
		
		// create Mountain Car's RBF-based feature generator
		theObsDoubleRanges[0][0] = -1.2;
		theObsDoubleRanges[0][1] = 0.6;
		theObsDoubleRanges[1][0] = -0.07;
		theObsDoubleRanges[1][1] = 0.07;
		//System.out.println("theObsDoubleRanges: " + Arrays.toString(theObsDoubleRanges));
		theActIntRanges = new int[1][2];
		theActIntRanges[0][0] = 0;
		theActIntRanges[0][1] = 2;
		basisFcnsPerDim = 40;
		relWidth = 0.08;
		
		System.out.println("Creating new FeatGen_RBFs instance.");
		featGen = new FeatGen_RBFs(theObsIntRanges, theObsDoubleRanges, 
			theActIntRanges, theActDoubleRanges,
			basisFcnsPerDim, relWidth);
		System.out.println("Created.");
		featGen.setNormBounds(-1, 1);
		featGen.setBiasFeatPerAct(0.1);
		
		
		// first feature test
		obs.doubleArray[0] = 0.0; 
		obs.doubleArray[1] = 0.0;
		act.intArray[0] = 0;
		System.out.println("Input: " + Arrays.toString(doubleStateVars) + ", " + Arrays.toString(intActVars));
		double[] feats = featGen.getSAFeats(obs, act);
//		System.out.println("Feats: " + Arrays.toString(feats) + "\n\n\n");

		// first test of model output
		double modelOut = 0;
		for (int i = 0; i < wtsArray.length; i++) {		
			modelOut += feats[i] * wtsArray[i];
		}
		System.out.println("Model output: " + modelOut);
		
		// second feature test
		obs.doubleArray[0] = 0.1; 
		obs.doubleArray[1] = 0.01;
		act.intArray[0] = 1;
		System.out.println("Input: " + Arrays.toString(doubleStateVars) + ", " + Arrays.toString(intActVars));
		feats = featGen.getSAFeats(obs, act);
		System.out.println("Feats: " + Arrays.toString(feats) + "\n");
		System.out.println("Num feats: " + feats.length);
		System.out.println("width: " + featGen.width);
		System.out.println("Num weights: " + wtsArray.length);
		
		// second test of model output
		modelOut = 0;
		for (int i = 0; i < wtsArray.length; i++) {	
			if (!FeatGen_RBFs.areAlmostTheSame(feats[i], pyFeatsArray[i])) {
				System.out.println("Mistmatch at index " + i + ". Python: " + pyFeatsArray[i] + ". Java: " + feats[i]);
			}
			modelOut += feats[i] * wtsArray[i];
		}
		System.out.println("Model output: " + modelOut);

		
	}
	
	private static boolean areAlmostTheSame(double a, double b) {
		if (b == 0) {
			if (a == 0)
				return true;
			else
				return false;
		}
		double quotient = a / b;
		double diffMetric = Math.abs(quotient - 1);
		if (diffMetric < 0.0001)
			return true;
		else
			return false;
		
	}
	
//	public static double exp(double x) {
//		x = 1f + x / 256f;
//		x *= x; x *= x; x *= x; x *= x;
//		x *= x; x *= x; x *= x; x *= x;
//		return x;
//	}
	
	/**
	 * This fast approximation of the exponential function is from an 
	 * implementation of Schraudolph's (1999) algorithm from A Fast, 
	 * Compact Approximation of the Exponential Function. The 
	 * implementation can be found at 
	 * 
	 * http://martin.ankerl.com/2007/02/11/optimized-exponential-functions-for-java/#comment-5122. 
	 */
	public static double exp(double val) {
		if (val < -709.0) // for overflow
			return 0.0;
		else if (val > 709.0) // for overflow
			return Double.MAX_VALUE;
	    final long tmp = (long)(1512775 * val + 1072632447);
	    return Double.longBitsToDouble(tmp << 32);
	}
	
}



package edu.utexas.cs.tamerProject.modeling;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.IncModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.utils.MutableDouble;
import edu.utexas.cs.tamerProject.featGen.FeatGen_DiscreteIndexer;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;

import java.lang.Math;
import java.util.Arrays;

public class TabularModel extends IncModel implements Cloneable{


	private double[] weights;
	double initValue;
	int numObservations;
	int numActs;
	
	private double[] traces;
	private double decayFactor = 0.0; // in Sutton and Barto terminology, decay factor is lambda; set to 0 for no traces by default
	private MutableDouble discountFactor;
	private String traceStyle = "replacing"; // replacing or accumulating
	
//	final double APPROX_ONE = 0.99999;
	

	/**
	 * This class assumes that the input to the model is {obsIndex, actIndex}.
	 * FeatGen_DiscreteIndexer follows this convention.
	 * 
	 * @param stepSize
	 * @param featGen
	 * @param initValue
	 * @param useBiasWt
	 */
	public TabularModel(double stepSize, FeatGen_DiscreteIndexer featGen, 
						double initValue){

		this.numObservations = featGen.getNumObs();
		this.numActs = featGen.getNumActs();
		this.initValue = initValue;
		
		weights = new double[this.numObservations * this.numActs];
		traces = new double[this.numObservations * this.numActs];
		
		clearSamplesAndReset();
		this.stepSize = stepSize;
		this.featGen = featGen;
		for (int i = 0; i < weights.length; i++) {
			this.weights[i] = this.initValue;
		}
	
	}

	public double[] getWeights(){return weights;}
	public void setWeights(double[] newWts) {this.weights = newWts.clone();}

	
	
	private void update(Sample sample) {
		if (verbose) {
			System.out.println("---- before -----");
			System.out.println("this.weights:" + Arrays.toString(this.weights));
			System.out.println("Prediction before update: " + predictLabel(sample.feats)); 
		}
		
		int stateActI = (int) ((sample.feats[1] * this.numObservations) + sample.feats[0]);
		double prediction = predictLabel(sample.feats);
		//System.out.println("Prediction: " + prediction);
		if (this.decayFactor != 0) {
			updateEligTraces(sample.feats, stateActI );
			if (this.traces[stateActI] * this.stepSize * sample.weight > 1) {
				System.err.println("Overshooting target in TabularModel.update().");
			}	
		}
		
		double err = sample.label - prediction;
		double wtedErr = this.stepSize * err * sample.weight;
		if (verbose) {
			System.out.println("Label: " + sample.label);
			System.out.println("Error: " + err);
			System.out.println("wtedErr: " + wtedErr);
		}

		
		if (this.decayFactor != 0) {
			wtedErr *= this.traces[stateActI];
		}
		this.weights[stateActI] += wtedErr;
		
		if (Double.isInfinite(weights[stateActI])) {
			System.err.println("weight is infinite from err: " + wtedErr);
		}


	}
	
	
	
	
	
	public void addInstance(Sample sample){
		if (sample.feats.length != 2) {
			System.err.println("Number of features does not fit assumptions of TabularModel. "
					+ "See the class description.");
		}
		update(sample);
		
		if (verbose) {
			System.out.println("Prediction after update: " + predictLabel(sample.feats));
			System.out.println("---- after -----");
			System.out.println("this.weights:" + Arrays.toString(this.weights));
		}

	}
	
	
	
	public void addInstances(Sample[] samples){ 
		//System.out.println("\n\n****** addInstances()\n");
		for (int i = 0; i < samples.length; i++) {
			addInstance(samples[i]);
		}
	}


	

	public void buildModel(){
		;
	}
	
	
	
	
	public double predictLabel(double[] sampleFeats){
//		System.out.println("weights.length: " + weights.length);
//		System.out.println("sampleFeats.length: " + sampleFeats.length);
		
		int stateActI = (int) ((sampleFeats[1] * this.numObservations) + sampleFeats[0]); 
		return this.weights[stateActI];
	}
	
	
	
	
	public void setModelParams(double[] params) {
		if (this.weights.length != params.length) {
			System.err.println("Mismatch in number of weights in TabularModel.setModelParams(). Exiting");
			System.exit(1);
		}
		for (int i = 0; i < this.weights.length; i++) {
			this.weights[i] = params[i];
		}
	}
	
	public void clearSamplesAndReset(){
		for (int i = 0; i < weights.length; i++) {
			this.weights[i] = this.initValue;
		}
	}
	
	public void setEligTraceParams(double decayFactor,  MutableDouble discountFactor, String traceStyle){
		this.decayFactor = decayFactor;
		this.discountFactor = discountFactor;
		this.traceStyle = traceStyle;
	}
	
	
	private void updateEligTraces(double[] feats, int stateActI){
		for (int i = 0; i < feats.length; i++){
			/*
			 *  decay previous traces	
			 */
			this.traces[i] *= this.decayFactor * this.discountFactor.getValue();
			//System.out.println("decay rate: " + this.decayFactor * this.discountFactor.getValue());
		}

		/* 
		 *  update current state-action pair's trace
		 */
		if (this.decayFactor*this.discountFactor.getValue() == 0.0 || this.traceStyle.equals("accumulating")){ // dF == 0 allows negative features
			this.traces[stateActI] += 1;
		}
		else if (this.traceStyle.equals("replacing")){
			this.traces[stateActI] = Math.max(1, this.traces[stateActI]);
		}
		else {
			System.err.println("Trace style " + this.traceStyle + "  is not supported in " 
									+ this.getClass() + ". Exiting.");
			System.exit(0);
		}	
	}

	
	
	public TabularModel makeFullCopy(){
		TabularModel cloneModel = null;
		try {
			cloneModel = (TabularModel) (this.clone());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		cloneModel.weights = Arrays.copyOf(this.weights, this.weights.length);
		
		return cloneModel;
	}
	
	
	public TabularModel duplicate() {
		TabularModel model = new TabularModel(this.stepSize, 
						(FeatGen_DiscreteIndexer)this.featGen, this.initValue);
		model.setWeights(this.weights);
		return model;
	}
	
	

	
	public static void main(String[] args) {
		int[][] theObsIntRanges = {{0,2},{0,1}};
		double[][] theObsDoubleRanges = {};
		int[][] theActIntRanges = {{0, 2}};
		double[][] theActDoubleRanges = {};
		FeatGen_DiscreteIndexer featGen = new FeatGen_DiscreteIndexer(theObsIntRanges, theObsDoubleRanges, 
													 theActIntRanges, theActDoubleRanges);
		
		RegressionModel model = new TabularModel(0.2, featGen, 0.0);

		Sample[] samples = new Sample[2];
		double[] attr = {3,1};
		Sample sample1 = new Sample(attr, 20.0, 1.0, 1);
		sample1.usedCredit = 1.0;
		double[] attr2 = {1,1};
		Sample sample2 = new Sample(attr2, 10.0, 1.0, 1);
		sample2.usedCredit = 1.0;
		samples[0] = sample2;
		samples[1] = sample1;
		double[] attr6 = {1,1};
		for (int i = 0; i < 24; i++) {
			model.addInstances(samples);
			System.out.println("predicted label: " + model.predictLabel(attr6));
		}
		/**double[] attr3 = {1.0, 0.0, 1.0};
		sample = new Sample(attr3, 10.0, 1.0, 1);
		model.addInstance(sample);
		double[] attr4 = {0.0, 0.0, 2.0};
		sample = new Sample(attr4, 20.0, 1.0, 1);
		model.addInstance(sample);**/
		double[] attr5 = {2,1};
		System.out.println("predicted label: " + model.predictLabel(attr5));
		
		System.out.println("predicted label: " + model.predictLabel(attr6));
		double[] attr7 = {3,1};
		System.out.println("predicted label: " + model.predictLabel(attr7));
	}

	@Override
	public void addInstance(Sample sample, double predictionAugmentation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInstancesWReplacement(Sample[] samples) {
		// TODO Auto-generated method stub
		
	}
}
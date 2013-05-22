package edu.utexas.cs.tamerProject.modeling;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.IncModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.utils.MutableDouble;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;

import java.lang.Math;
import java.util.Arrays;

/**
 * A linear model updated by incremental gradient descent, as in Sutton and Barto (1998).
 * 
 * @author bradknox
 *
 */
public class IncGDLinearModel extends IncModel implements Cloneable{

	private double decayFactor = 0.0; // in Sutton and Barto terminology, decay factor is lambda; set to 0 for no traces by default
	private MutableDouble discountFactor;
	private String traceStyle = "replacing"; // replacing or accumulating
	private double[] weights;
	private double[] complSampleWts; // For a base model built only from complete samples. The regular weights can reflect learning from samples with labels extrapolated by the credit assignment module.
	private double biasWt = 0;
	public double complSampleBiasWt;
	private double[] traces;
	private boolean useBiasWt = false;
	final double APPROX_ONE = 0.99999;
	private double regL2Wt = 0;
	
	public IncGDLinearModel makeFullCopy(){
		IncGDLinearModel cloneModel = null;
		try {
			cloneModel = (IncGDLinearModel) (this.clone());
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		cloneModel.weights = Arrays.copyOf(this.weights, this.weights.length);
		cloneModel.complSampleWts = Arrays.copyOf(this.complSampleWts, this.complSampleWts.length);
		cloneModel.traces = Arrays.copyOf(this.traces, this.traces.length);
		cloneModel.discountFactor = new MutableDouble(this.discountFactor.getValue());
		
		return cloneModel;
	}
	
	
	
	public IncGDLinearModel(int numFeatures, double stepSize, FeatGenerator featGen, 
						double initValue, boolean useBiasWt){
		weights = new double[numFeatures];
		complSampleWts = new double[numFeatures];
		traces = new double[numFeatures];
		//System.out.println("numfeatures in IncGDLinearModel: " + numFeatures);
		clearSamplesAndReset();
		this.stepSize = stepSize;
		this.featGen = featGen;
		for (int i = 0; i < weights.length; i++) {
			this.weights[i] = initValue;
			this.complSampleWts[i] = initValue;
		}
		this.useBiasWt = useBiasWt;
		this.biasWt = initValue;
		this.complSampleBiasWt = initValue;
	}
	
	public double[] getWeights() {return Arrays.copyOf(this.weights, this.weights.length);}
	public void setWeights(double[] newWts) {this.weights = newWts.clone();}
	public void setComplSampleWts(double[] newCompWts){this.complSampleWts = newCompWts.clone();}
	public void setBiasWt(double biasWt) {this.biasWt = biasWt;}
	public double getBiasWt() {return this.biasWt;}
	public void setL2RegWt(double wt) {this.regL2Wt = wt;}
	public double getL2RegWt() {return this.regL2Wt;}
	public void setTraces(double[] newTraces) {this.traces = newTraces.clone();}
	public void setDiscountFactor(double factor) {
		//System.out.println("Setting IncGDLinearModel discount factor to " + factor);
		this.discountFactor = new MutableDouble(factor);
	}
	public void setDiscountFactor(MutableDouble factor){
		//System.out.println("Setting discount factor to " + factor);
		this.discountFactor = factor;
	}
	
	public IncGDLinearModel duplicate() {
		IncGDLinearModel model = new IncGDLinearModel(weights.length, this.stepSize, this.featGen, 0, this.useBiasWt);
		model.setEligTraceParams(this.decayFactor, this.discountFactor, this.traceStyle);
		model.setWeights(this.weights);
		model.setComplSampleWts(this.complSampleWts);
		model.setTraces(this.traces);
		return model;
	}
	
	public void setEligTraceParams(double decayFactor,  MutableDouble discountFactor, String traceStyle){
		this.decayFactor = decayFactor;
		this.discountFactor = discountFactor;
		this.traceStyle = traceStyle;
		//System.out.println("Set elig trace params. Decay factor: " + decayFactor 
		//		+ ", discount factor: " + discountFactor.getValue() 
		//		+ ", traceStyle: " + traceStyle);
	}
		
	
	private void gradDescUpdate(Sample sample, double predictionAugmentation) {
		if (verbose) {
			System.out.println("---- before -----");
			System.out.println("observation: " + Arrays.toString(sample.feats));
			System.out.println("this.weights:" + Arrays.toString(this.weights));
			System.out.println("this.complSampleWts:" + Arrays.toString(this.complSampleWts));
			System.out.println("this.biasWt:" + this.biasWt);
			System.out.println("this.complSampleBiasWt:" + this.complSampleBiasWt);
			System.out.println("Prediction before update: " + predictLabel(sample.feats));
			System.out.println("Prediction augmentation: " + predictionAugmentation); 
		}
		
		double prediction = predictLabel(sample.feats) + predictionAugmentation;
		//System.out.println("Prediction: " + prediction);
		updateEligTraces(sample.feats); // sets traces equal to features if decayFactor*discountFactor is 0
		double err = sample.label - prediction;
		double wtForErr = this.stepSize * sample.weight;

		if (verbose) {
			System.out.println("Label: " + sample.label);
			System.out.println("Error: " + err);
//			System.out.println("wtedErr: " + wtedErr);
		}
		//System.out.println("Traces: " + Arrays.toString(traces));
		double traceSum = 0;
		for (double a: traces)
			traceSum += a;
		if (traceSum * this.stepSize * sample.weight > 1) {
			//System.err.println("Overshooting target in IncGDLinearModel.gradDescUpdate().");
		}
		// TODO regularization component of error shouldn't be the deciding factor in changing the sign of a weight
		for (int i = 0; i < complSampleWts.length; i++){
			double wtedErr = wtForErr * (err - (this.regL2Wt * this.weights[i])); // from derivation at http://cseweb.ucsd.edu/~elkan/250B/logreg.pdf
			this.weights[i] += this.traces[i] * wtedErr; // this is the same as updating complSampleWts if they point to same array
			if (Double.isInfinite(weights[i])) {
				System.err.println("weight is infinite from trace and err: " + this.traces[i] + ", " + wtedErr);
				System.err.println("Common culprits: large features, large step size, or large weight for L2 regularization.");
			}
		}
		if (this.useBiasWt) {
			if (this.weights == this.complSampleWts) { // if they're the same, then a complete sample has been added
				this.complSampleBiasWt += wtForErr * (err - (this.regL2Wt * this.complSampleBiasWt));;
				this.biasWt = this.complSampleBiasWt;
			}
			else
				this.biasWt += wtForErr * (err - (this.regL2Wt * this.complSampleBiasWt));
		}

	}
	
	
	
	
	public void addInstance(Sample sample){ addInstance(sample, 0.0); }
	public void addInstance(Sample sample, double predictionAugmentation){
		//System.out.println("\n\n\nAdding sample: " + sample);
		//System.out.println("\n\n-----------\n-----------Adding finished sample");
		/*
		 *  Align pointers to base (complete samples) model and temporary in-use 
		 *  model, thus obliterating any temporary samples.
		 */
		this.weights = this.complSampleWts; 
		this.biasWt = this.complSampleBiasWt;
		gradDescUpdate(sample, predictionAugmentation);
		
		if (verbose) {
			System.out.println("Prediction after update: " + predictLabel(sample.feats));
			System.out.println("---- after -----");
			System.out.println("this.weights:" + Arrays.toString(this.weights));
			System.out.println("this.complSampleWts:" + Arrays.toString(this.complSampleWts));
			System.out.println("this.biasWt:" + this.biasWt);
			System.out.println("this.complSampleBiasWt:" + this.complSampleBiasWt);
		}

	}
	
	

	public void addBiasInstance(Sample sample) {
		double realStepSize = this.stepSize;
		this.stepSize = 1.0;
		addInstance(sample);
		this.stepSize = realStepSize;
	}
	
	public void addInstances(Sample[] samples){ 
		//System.out.println("\n\n****** addInstances()\n");
		for (int i = 0; i < samples.length; i++) {
			addInstance(samples[i]);
		}
	}


	public void addInstanceWReplacement(Sample sample){
		System.err.println("addInstanceWReplacement is not supported for IncGDLinearModel. Exiting.");
		System.exit(1);
	}
	
	/**
	 * Adds complete samples to a base model, copies that model to a temporary model for use, 
	 * and adds incomplete samples to the temporary model. 
	 */
	public void addInstancesWReplacement(Sample[] samples){
		//// add all completed instances to base model
		//System.out.println("\n\n****** addInstancesWReplacement()\n");
		for (int i = 0; i < samples.length; i++) {
			if (samples[i].usedCredit > APPROX_ONE || samples[i].unique == -1) { 
				//System.out.println("adding permanent instance, used credit: " + samples[i].usedCredit);
				addInstance(samples[i]);
			}
		}
		
		//// copy base model and point to that model for predictions
		this.weights = Arrays.copyOf(this.complSampleWts, this.complSampleWts.length);
		this.biasWt = this.complSampleBiasWt;
		
		//// add unfinished samples to copy
		//System.out.println("\n\n-----------\n-----------Adding unfinished samples (w replacement)");
		for (int i = 0; i < samples.length; i++) {
			if (samples[i].usedCredit <= APPROX_ONE && samples[i].weight != 0 && samples[i].unique != -1) {
				if (this.decayFactor != 0.0) {
					System.err.println(this.getClass().getName() + " does not support both eligibility traces and temporary samples. Exiting.");
					System.err.println("traceDecayFactor: " + decayFactor);
					System.exit(1);
				}
				//System.out.println("adding instance that can be replaced(). Used credit: " + samples[i].usedCredit +
				//		". Unique: " + samples[i].unique);
				gradDescUpdate(samples[i], 0.0);	
			}
		}
		//System.out.println("\n\n****** FINISHED addInstancesWReplacement()\n");
	}

	public void buildModel(){
		;
	}
	
	
	
	
	public double predictLabel(double[] sampleFeats){
		double prediction = 0.0;
		//System.out.println("weights: " + Arrays.toString(weights));
		//System.out.println("sampleFeats: " + Arrays.toString(sampleFeats));
		for (int i = 0; i < weights.length; i++){
			prediction += weights[i] * sampleFeats[i];
		}
		if (this.useBiasWt){
			prediction += this.biasWt;
			//System.out.println("using bias weight");
		}
		if (Double.isInfinite(prediction)) {
			System.err.println(this.getClass().getSimpleName() + " calculating infinite prediction.");		
		}
		return prediction;
	}
	
	
	
	
	public void setModelParams(double[] params) {
		if (this.complSampleWts.length != params.length) {
			System.err.println("Mismatch in number of complSampleWts in IncGDLinearModel.setModelParams(). Exiting");
			System.exit(1);
		}
		for (int i = 0; i < this.complSampleWts.length; i++) {
			this.complSampleWts[i] = params[i];
		}
		this.weights = this.complSampleWts;
	}
	
	public void clearSamplesAndReset(){
		for (int i = 0; i < this.complSampleWts.length; i++){
			complSampleWts[i] = 0.0;
			traces[i] = 0.0;
		}
		this.weights = this.complSampleWts; 
	}
	
	private void updateEligTraces(double[] feats){
		for (int i = 0; i < feats.length; i++){
			//// decay previous traces	
			this.traces[i] *= this.decayFactor * this.discountFactor.getValue();
			//System.out.println("decay rate: " + this.decayFactor * this.discountFactor.getValue());
			//// update traces with feats
			if (this.decayFactor*this.discountFactor.getValue() == 0.0 || this.traceStyle.equals("accumulating")){ // dF == 0 allows negative features
				this.traces[i] += feats[i];
			}
			else if (this.traceStyle.equals("replacing")){
				this.traces[i] = Math.max(feats[i], this.traces[i]);
			}
			else {
				System.err.println("Trace style " + this.traceStyle + "  is not supported in " 
										+ this.getClass() + ". Exiting.");
				System.exit(0);
			}
		}	
	}
	
	
	public static void main(String[] args) {
		RegressionModel model = new IncGDLinearModel(3, 0.2, null, 0.0, true);
		((IncGDLinearModel)model).setDiscountFactor(1.0);
		((IncGDLinearModel)model).setL2RegWt(.1);
//		model.verbose = true;

		Sample[] samples = new Sample[2];
		double[] attr = {1.0, 1.0, 1.0};
		Sample sample1 = new Sample(attr, 20.0, 1.0, 1);
		sample1.usedCredit = 1.0;
		double[] attr2 = {0.0, 1.0, 1.0};
		Sample sample2 = new Sample(attr2, 10.0, 1.0, 1);
		sample2.usedCredit = 1.0;
		samples[0] = sample2;
		samples[1] = sample1;
		double[] attr6 = {1.0, 1.0, 1.0};
		System.out.println("predicted label: " + model.predictLabel(attr6));
		for (int i = 0; i < 24; i++) {
//			model.addInstancesWReplacement(samples);
			model.addInstance(sample1);
			System.out.println("predicted label: " + model.predictLabel(attr6));
		}
		/**double[] attr3 = {1.0, 0.0, 1.0};
		sample = new Sample(attr3, 10.0, 1.0, 1);
		model.addInstance(sample);
		double[] attr4 = {0.0, 0.0, 2.0};
		sample = new Sample(attr4, 20.0, 1.0, 1);
		model.addInstance(sample);**/
		double[] attr5 = {0.0, 1.0, 1.0};
		System.out.println("predicted label: " + model.predictLabel(attr5));
		
		System.out.println("predicted label: " + model.predictLabel(attr6));
		double[] attr7 = {0.0, 1.0, 1.0};
		System.out.println("predicted label: " + model.predictLabel(attr7));
	}
}
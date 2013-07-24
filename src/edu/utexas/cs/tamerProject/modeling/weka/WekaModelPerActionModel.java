package edu.utexas.cs.tamerProject.modeling.weka;

import java.util.ArrayList;
import java.util.Arrays;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.modeling.weka.WekaRegressor;
import edu.utexas.cs.tamerProject.actSelect.ActionList;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;

public class WekaModelPerActionModel extends RegressionModel{
	private final boolean verbose = false;

	WekaRegressor[] wMIs;
	int numAttributes;
	int[] actionFeatIndices;
	int[] numFeatValsPerFeatI;

	public WekaModelPerActionModel(String wekaModelName, FeatGenerator featGen) {
		this.actionFeatIndices = featGen.getActionFeatIndices();
		this.numFeatValsPerFeatI = featGen.getNumFeatValsPerFeatI();
		this.numAttributes = numFeatValsPerFeatI.length 
								- actionFeatIndices.length;
		this.featGen = featGen;
		int numActions = 1;
		
		for (int i = 0; i < actionFeatIndices.length; i++) {
			numActions *= numFeatValsPerFeatI[actionFeatIndices[i]];
		}
		wMIs = new WekaRegressor[numActions];
		for (int i = 0; i < numActions; i++) {
			wMIs[i] = new WekaRegressor(wekaModelName, numAttributes);
		}
		System.out.println("Initiating model with " + numAttributes + " attributes.");
	}
	
	public WekaModelPerActionModel(FeatGenerator featGen,
								WekaRegressor[] wMIs, int numAttributes, 
								int[] actionFeatIndices, int[] numFeatValsPerFeatI) {
		this.featGen = featGen;
		this.wMIs = wMIs;
		this.numAttributes = numAttributes;
		this.actionFeatIndices = actionFeatIndices;
		this.numFeatValsPerFeatI = numFeatValsPerFeatI;
	}
	
	// returns the action number; useful when there is more than one action feature
	private int getActI(double[] feats){
		int[] actFeats = getActFeats(feats);
		return ActionList.getActIntIndex(actFeats, this.featGen.actList.getActionList());
//		
//		int actI = 0;
//		int multiplicand = 1;
//		for (int i = 0; i < actionFeatIndices.length; i++) {
//			int actionFeatI = actionFeatIndices[i];
//			actI += feats[actionFeatI] * multiplicand;
//			multiplicand *= this.numFeatValsPerFeatI[actionFeatI];
//		}
//		return actI;
	}
	
	// assumes that action features are all integers
	private int[] getActFeats(double[] feats) {
		int[] actFeats = new int[actionFeatIndices.length];
		for (int i = 0; i < actionFeatIndices.length; i++) {
			actFeats[i] = (int)feats[actionFeatIndices[i]];
		}
		return actFeats;
	}
	
	private double[] removeActFeats(double[] feats) {
		ArrayList<Double> filteredFeatsList = new ArrayList<Double>();
		for (int i = 0; i < feats.length; i++) {
			if (!inIntArray(actionFeatIndices, i)) {
				filteredFeatsList.add(new Double(feats[i]));
			}
		}
		double[] filteredFeatsArray = new double[filteredFeatsList.size()];
		for (int i = 0; i < filteredFeatsList.size(); i++) {
			filteredFeatsArray[i] = filteredFeatsList.get(i).doubleValue();
		}
		return filteredFeatsArray;
	}
	
	private boolean inIntArray(int[] array, int a){
		boolean in = false;
		for (int i = 0; i < array.length; i++) {
			if (a == array[i])
				in = true;
		}
		return in;
	}		

	public void addInstance(Sample sample){
		
		int actI = getActI(sample.feats);
		double[] filteredFeats = removeActFeats(sample.feats);
		if (verbose) {
			System.out.print("filtered feats: " + Arrays.toString(filteredFeats));
		}
		Sample filteredSample = new Sample(filteredFeats, sample.label,
											sample.weight, sample.unique);
		this.wMIs[actI].addInstance(filteredSample);
	}
	
	public void addInstances(Sample[] samples){
		for (int i = 0; i < samples.length; i++) {
			addInstance(samples[i]);
		}
	}
	
	public void addInstanceWReplacement(Sample sample){
		int actI = getActI(sample.feats);
		double[] filteredFeats = removeActFeats(sample.feats);
		Sample filteredSample = new Sample(filteredFeats, sample.label,
									sample.weight, sample.unique);
		//System.out.print("Adding sample to act " + actI + ". ");
		this.wMIs[actI].addInstanceWReplacement(filteredSample);
	}
	
	public void addInstancesWReplacement(Sample[] samples){
		for (int i = 0; i < samples.length; i++) {
			addInstanceWReplacement(samples[i]);
		}
	}
	
	public void buildModel() {
		for (int i = 0; i < wMIs.length; i++){
			this.wMIs[i].buildModel();
		}
	}
	public double predictLabel(double[] feats) {
		int actI = getActI(feats);
		if (verbose)
			System.out.println("act index: " + actI);
		double[] filteredFeats = removeActFeats(feats);
		if (verbose) {
			System.out.println("filtered feats: " + Arrays.toString(filteredFeats));
		}
		return this.wMIs[actI].classifyInstance(filteredFeats);
	}
	public void clearSamplesAndReset(){
		for (int i = 0; i < wMIs.length; i++){
			this.wMIs[i].clearSamplesAndReset();
		}
	}
	
	public void changeClassifier() {
		for (int i = 0; i < wMIs.length; i++){
			this.wMIs[i].changeClassifier();
		}
	}

	public void loadDataAsArff(String envName, String timeStamp, String furtherID) {
		for (int i = 0; i < wMIs.length; i++){
			this.wMIs[i].loadDataAsArff(envName, timeStamp, furtherID + i);
		}
	}
	public void saveDataAsArff(String envName, double timeStamp, String furtherID) {
		for (int i = 0; i < wMIs.length; i++){
			this.wMIs[i].saveDataAsArff(envName, timeStamp, furtherID + i);
		}
		
	}
	
	/*
	 * This is almost a deep copy. featGen is copied shallowly, which should be
	 * okay since models shouldn't modify it (though I don't see an easy way to
	 * enforce that).
	 * 
	 * (non-Javadoc)
	 * @see edu.utexas.cs.tamerProject.modeling.RegressionModel#makeFullCopy()
	 */
	@Override
	public RegressionModel makeFullCopy() {
		WekaRegressor[] wMIsCopy = new WekaRegressor[this.wMIs.length]; 
		 
		int[] actionFeatIndicesCopy = (int[]) this.actionFeatIndices.clone(); 
		int[] numFeatValsPerFeatICopy = (int[]) this.numFeatValsPerFeatI.clone();
		
		for (int i = 0; i < this.wMIs.length; i++){
			wMIsCopy[i] = this.wMIs[i].deepCopy();
		}
		
		return new WekaModelPerActionModel(this.featGen, wMIsCopy, this.numAttributes, 
						actionFeatIndicesCopy, numFeatValsPerFeatICopy);
	}

	public static void main(String[] args) {
		String s = "";
		int[] actIs = new int[1];
		actIs[0] = 2;
		int[] numFeatVals = {0,0,3};
		RegressionModel model = new WekaModelPerActionModel(s, null);

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
		model.buildModel();
		double[] attr5 = {0.0, 1.0, 1.0};
		System.out.println("predicted label: " + model.predictLabel(attr5));
		
	}
	
}
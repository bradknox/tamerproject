package edu.utexas.cs.tamerProject.modeling.weka;

import weka.classifiers.Classifier;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;

public class WekaModelWrap extends RegressionModel{
	WekaRegressor wMI;
	
	public WekaModelWrap(FeatGenerator featGen){
		System.out.println("Features in " + featGen.getClass().getName() + ": " + featGen.getNumFeatures());
		wMI = new WekaRegressor("", featGen.getNumFeatures());
		this.featGen = featGen;
	}
	
	public WekaModelWrap(FeatGenerator featGen, String wekaModelName){
		System.out.println("Features in " + featGen.getClass().getName() + ": " + featGen.getNumFeatures());
		wMI = new WekaRegressor(wekaModelName, featGen.getNumFeatures());
		this.featGen = featGen;
	}
	
	public void addInstance(Sample sample){
		wMI.addInstance(sample);
	}
	
	public void addInstances(Sample[] samples){
		for (int i = 0; i < samples.length; i++) {
			addInstance(samples[i]);
		}
	}
	
	public void addInstanceWReplacement(Sample sample){
		wMI.addInstanceWReplacement(sample);
	}
	
	public void addInstancesWReplacement(Sample[] samples){
		for (int i = 0; i < samples.length; i++) {
			addInstanceWReplacement(samples[i]);
		}
	}
	
	public Classifier getWekaClassifier() {
		return this.wMI.classifier;
	}
	
	public void buildModel() {
		wMI.buildModel();
	}
	public double predictLabel(double[] sampleFeats) {
		return wMI.classifyInstance(sampleFeats);
	}
	public void clearSamplesAndReset(){
		wMI.clearSamplesAndReset();
	}
	public void changeClassifier() {	
		this.wMI.changeClassifier();
	}
	public void loadDataAsArff(String envName, String timeStamp, String furtherID) {
		this.wMI.loadDataAsArff(envName, timeStamp, furtherID);
	}
	public void saveDataAsArff(String envName, double timeStamp, String furtherID) {
		this.wMI.saveDataAsArff(envName, timeStamp, furtherID);
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
		WekaModelWrap newModel = new WekaModelWrap(this.featGen);
		newModel.wMI = this.wMI.deepCopy();
		
		return newModel;
	}
	
}
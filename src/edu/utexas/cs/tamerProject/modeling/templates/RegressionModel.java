package edu.utexas.cs.tamerProject.modeling.templates;

import java.util.ArrayList;
import java.util.Arrays;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 * RegressionModel is an abstract class for models that perform regression. It
 * built mostly for regression of the form F: O X A -> real number, where 
 * F is the function to be learned, O is the set of possible observations, and A
 * is the set of actions, but it can handle more general regression, with a vector
 * of real-valued inputs. 
 * 
 * @author bradknox
 *
 */
public abstract class RegressionModel implements ObsActModel{
	
	protected FeatGenerator featGen;
	public boolean verbose = false;

	public void setVerbose(boolean verbose) {this.verbose = verbose;}
	public abstract void addInstance(Sample sample);
	public abstract void addInstances(Sample[] samples);
	public abstract void addInstancesWReplacement(Sample[] samples);
	public abstract void buildModel();
	public abstract double predictLabel(double[] feats);
	public double predictLabel(Observation obs, Action act){
		double[] feats = this.featGen.getFeats(obs, act);
		double label = this.predictLabel(feats);
		return (this.predictLabel(this.featGen.getFeats(obs, act)));
	}
	public abstract void clearSamplesAndReset();
	
	public void addBiasInstance(Sample sample){ this.addInstance(sample);}
	
	public FeatGenerator getFeatGen(){return this.featGen;}
	public void setFeatGen(FeatGenerator featGen) {this.featGen = featGen;}
	
	public void biasWGenSamples(int numSamples, double label, double weight) {
		int unique = -1;	
		this.featGen.setRandSeed((long)100);
		for (int i = 0; i < numSamples; i++) {
			double[] feats = this.featGen.genRandomFeats(1).get(0);
			addBiasInstance(new Sample(feats, label, weight, unique));
		unique--;
	}
		buildModel();
		System.out.println("Finished biasing initial model with " + numSamples + " samples.");
	}
	
	
	public ArrayList<Action> getMaxActs(Observation obs, ArrayList<Action> possActions){
		return featGen.getMaxActs(this, obs, possActions);	
	}
	public Action getMaxAct(Observation obs, ArrayList<Action> possActions){
		Action maxAct;
		ArrayList<Action> maxActs = this.featGen.getMaxActs(this, obs, possActions);
		try{
			int actIndex = FeatGenerator.staticRandGenerator.nextInt(maxActs.size());
			maxAct = maxActs.get(actIndex);
		}
		catch (IllegalArgumentException e){
			if (e.toString().contains("n must be positive"))
				System.out.print("Exception: " + e);
			else 
				System.out.println("Error: " + e + ". Returning null action.");
			maxAct = new Action();
			maxAct.intArray = null;
		}
		return maxAct.duplicate();
	}
	
	public double[] getStateActOutputs(Observation obs, ArrayList<Action> actions)
	{
		double[] stateActOutputs = new double[actions.size()];
		for (int actI = 0; actI < actions.size(); actI++) {
			Action currAct = actions.get(actI);
			stateActOutputs[actI] = this.predictLabel(obs, currAct);
		}
		return stateActOutputs;
	}

	public Action getRandomAction(){
		return this.featGen.actList.getRandomAction();
	}
	public boolean noRealValFeats(){
		return (this.featGen.theActDoubleRanges.length == 0);
	}
	public ArrayList<Action> getPossActions(Observation obs){
		return this.featGen.getPossActions(obs);
	}
	public void changeClassifier() {
		System.out.println("Model in use doesn't support classifier change (by pushing 'C').");
	}
	
	public void loadDataAsArff(String envName, String timeStamp, String furtherID) {
		System.out.println("Model in use doesn't support loading data from a .arff file.");
	}
	public void saveDataAsArff(String envName, double timeStamp, String furtherID) {
		System.out.println("Model in use doesn't support saving data to a .arff file.");
	}
	public RegressionModel makeFullCopy() {
		System.out.println("Model in use (" + this.getClass().getSimpleName() + ") doesn't support cloning itself.");
		System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
		return null;
	}
}
package edu.utexas.cs.tamerProject.modeling;

import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Double;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.agents.tamerrl.HInfluence;

/** Repository Revision 429 holds a very different but functional version that has
 * model weights as class variables and can handle any number of models.
 * 
 */

public class CombinationModel extends RegressionModel{

	String comboType = "sum"; // mean or sum
	
	private ArrayList<RegressionModel> models = new ArrayList<RegressionModel>();
//	public ArrayList<Double> modelWts = new ArrayList<Double>();
	private double annealFactor;
	public HInfluence hInf;
	
//	public CombinationModel(RegressionModel firstModel, double annealFactor, 
//							RegressionModel secondModel, double secondWt){
	public CombinationModel(RegressionModel firstModel, RegressionModel secondModel, 
							HInfluence hInf){
		models.add(firstModel);
		this.featGen = firstModel.getFeatGen(); //// This should work for its later use, but it's bad coding practice.
		
//		modelWts.add(new Double(1.0));
		models.add(secondModel);
		System.out.println("second model: " + secondModel);
//		modelWts.add(new Double(secondWt));
//		this.annealFactor = annealFactor;
		System.out.println("supplementary model weight: " + hInf.COMB_PARAM);
		this.hInf = hInf;
	}
	
	public Action getRandomAction(){
		return this.models.get(0).getFeatGen().actList.getRandomAction();
	}
////	public void annealNonPrimaryWts() {
////		if (verbose)
////			System.out.println("num wts: :" + this.modelWts.size());
////		for (int i = 1; i < this.modelWts.size(); i++){
////			double oldWt = this.modelWts.remove(i);
////			if (verbose) {
////				System.out.println("oldWt: :" + oldWt);
////				System.out.println("this.annealFactor: :" + this.annealFactor);
////			}
////			this.modelWts.add(i, oldWt * this.annealFactor);
////		}
////		if (verbose)
////			
////			System.out.println("num wts: :" + this.modelWts.size());
////	}
//	public void addModel(RegressionModel model, double weight){
//		models.add(model);
//		modelWts.add(new Double(weight));
//	}
	public void addInstance(Sample sample){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(0);
	}
	public void addInstances(Sample[] samples){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(0);
	}
	public void addInstancesWReplacement(Sample[] samples){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(0);
	}
	public void buildModel(){
		for (int i = 0; i < this.models.size(); i++){
			this.models.get(i).buildModel();
		}
	}
	
	public double predictLabel(double[] sampleFeats) {
		System.err.println("This method is not compatible with in " + this.getClass() + 
					". The different models may use different features and so must take" +
					" the raw state variables as input. Exiting.");
		System.exit(0);
		return 0.0;
	}
	public double predictLabel(Observation obs, Action act){
		double prediction = 0;
		double hInfWt = this.hInf.getHInfluence(obs, act);
		if (verbose) {
			System.out.println("suppl model weight: " + hInfWt);
			System.out.println("suppl model prediction: " + (this.models.get(1)).predictLabel(obs, act));
		}
		prediction += (this.models.get(0)).predictLabel(obs, act);
		prediction += hInfWt * (this.models.get(1)).predictLabel(obs, act);

		if (verbose) {
				System.out.println("Main model's contribution to prediction: " + 
						(this.models.get(0)).predictLabel(obs, act));
				System.out.println("Suppl model's contribution to prediction: " +
						hInfWt * (this.models.get(1)).predictLabel(obs, act));
		}
		if (comboType.equals("mean"))
			prediction /= this.models.size();
		return prediction;
	}
//	public Action getMaxAct(int[] intObsVals, double[] doubleObsVals, 
//							char[] charObsVals, Action baseAction){
//		if (featGen == null)
//			System.err.println("featGen in " + this.getClass().getName() + ": " + featGen);
//		return featGen.getMaxAct(this, intObsVals, doubleObsVals, charObsVals, baseAction);
//	}	
	
	public void clearSamplesAndReset(){
		System.err.println("This method is not implemented in " + this.getClass() + ". Exiting.");
		System.exit(0);
	}
	public boolean noRealValFeats(){
		boolean noRealValFeats = true;
		for (int i = 0; i < this.models.size(); i++){
			if (!models.get(i).noRealValFeats())
				noRealValFeats = false;
		}
		return noRealValFeats;
	}

}
package edu.utexas.cs.tamerProject.params;
	
import java.lang.reflect.Field;
import java.util.HashMap;

import org.rlcommunity.rlglue.codec.types.Action;

import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.envModels.rewModels.MountainCarRewModel;
import edu.utexas.cs.tamerProject.envModels.transModels.CartPoleTransModel;
import edu.utexas.cs.tamerProject.envModels.transModels.MountainCarTransModel;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;

/**
 * Params is where the parameters are set that are specific to an agent type,
 * an agent's approach to an environment (e.g., representation of Q or H),
 * experimental conditions, and anything else that calls for changes in the
 * agent algorithm.
 * 
 * @author bradknox
 *
 */
/*
 * Parameters for some past experiments are documented here, but they can often 
 * also be found in the .log files produced by the experiments.   
 */
public class Params{
	
	/*
	 * DO NOT CHANGE THESE DEFAULT VALUES HERE AT THEIR DECLARATION. 
	 * 
	 * Change them in getParams(). Or better, keep a class for each experiment
	 * that sets parameters. That way, all of the relevant parameters for the
	 * data set created in that experiment will be in one place and in less
	 * danger of being changed. (Some of the messy commenting below gives 
	 * evidence for why a separate file is preferable.)
	 */
	
    //// FeatGenerator params
    public String featClass = "None";
	public HashMap<String,String> featGenParams = new HashMap<String,String>();
    
	
	//// RegressionModel params
	public String modelClass = "None";
	public boolean initModelWSamples = false; // use learning samples to initially bias the model towards a certain constant output
	public double initSampleValue = 0.0; // constant target value for initial model output 
	public int numBiasingSamples = 0; // number of sample to use for biasing; higher creates better results but takes longer
	public double biasSampleWt = 0.5; // if biasing initial model output, this determines the weight of the sample (for learning algs that use weight); for example, this will lower the step size of incremental gradient descent, reducing overshoot 
	// below this are non-universal params for specific models
	public double initWtsValue = 0.0; // for models with weights
	public boolean modelAddsBiasFeat = false; // for linear model, add a feature with value 1 (in model, not in feature selection). (was "modelAddsBiasWt") 
	public String wekaModelName = "";
	public double stepSize = 0.05; // for incremental models
	public String traceType = "replacing";
	public double traceDecayFactor = 0.0;
	public Action safeAction = null;
	
	
	//// ActionSelect params	
	public String selectionMethod = "e-greedy"; // implemented greedy, e-greedy, vals-as-probs
	public HashMap<String,String> selectionParams = new HashMap<String,String>();
	public EnvTransModel envTransModel = null;
	public ObsActModel envRewModel = null;


	//// CreditAssign Params
	public String distClass = "uniform"; //// previousStep or uniform
	public double creditDelay = 0.2;
	public double windowSize = 0.6;
	public boolean extrapolateFutureRew = true;
	// TODO add extrapolation threshold
	public boolean delayWtedIndivRew = false; // this uses delay-weighted, individual reward (named in the original TAMER journal paper 
												// and first used in the K-CAP09 paper) instead of the current d-w, aggregate reward
	public boolean noUpdateWhenNoRew = false; // this is carried out differently for different credit assignment schemes
	
	//// HInfluence-only Params
	public HashMap<String,String> hInflParams = new HashMap<String,String>();
	
	public Params(){
		selectionParams.put("epsilon", "0.03"); // 0.2 works well for Sarsa alone in several enviros
		selectionParams.put("epsilonAnnealRate", "0.9995");
		// next four params are for tree search
		selectionParams.put("treeSearch", "false");
		selectionParams.put("greedyLeafPathLength", "0");
		selectionParams.put("exhaustiveSearchDepth", "1");
		selectionParams.put("randomizeSearchDepth", "true");
		
		featGenParams.put("numBinsPerDim", "10"); // used by FeatGen_Discretize
		featGenParams.put("basisFcnsPerDim", "41"); //41; used by FeatGen_RBFs
		featGenParams.put("relWidth", "0.062"); //0.2; used by FeatGen_RBFs
		featGenParams.put("biasFeatVal", "0.1"); // used by FeatGen_RBFs; for most use cases, lowering this value reduces generalization across all states for an action sample
	}
	
	public String toString(){
		return "";
	}
	
	public static Params getParams(String agentClassName, String envName){
		System.out.println("***in getParams(), agentClassName: " + agentClassName);
		Params params = new Params();
		
		if (envName.equals("Mountain-Car")){
			params.envTransModel = new MountainCarTransModel();
			params.envRewModel = new MountainCarRewModel();
		}
		else if (envName.equals("CartPole")){
			params.envTransModel = new CartPoleTransModel();
		}
		
		if (agentClassName.contains("TamerAgent") || 
				agentClassName.contains("ImitationAgent") || agentClassName.contains("Lfd") ){
			params.modelClass = "WekaModelPerActionModel";
			params.featClass = "FeatGen_NoChange";
			params.selectionMethod = "greedy";
			params.traceDecayFactor = 0.0;
			
			if (envName.equals("Mountain-Car")){
				params.initModelWSamples = false; //true; //// no biasing in MC for ALIHT paper and ICML workshop paper
				params.numBiasingSamples = 100;
				params.biasSampleWt = 0.1;
				params.wekaModelName = "IBk"; // "KDTree"; //"IBk"; //"M5P"; //// IBk for ALIHT paper and ICML workshop paper
//				params.modelClass = "IncGDLinearModel";
//				params.featClass = "FeatGen_RBFs";
//				params.featGenParams.put("basisFcnsPerDim", "8");
//				params.featGenParams.put("relWidth", "0.08");
//				params.traceDecayFactor = 0.0;
			}
			else if (envName.equals("CartPole")){
				params.initModelWSamples = false; //// was true for ALIHT paper and ICML workshop paper
				params.numBiasingSamples = 50000;
				params.biasSampleWt = 0.1;
				params.wekaModelName = "IBk"; //"M5P"; //// IBk for ALIHT paper and ICML workshop paper
			}
			else if (envName.equals("Puddle-World")){
				params.initModelWSamples = false;
				params.numBiasingSamples = 50000;
				params.biasSampleWt = 0.1;
			}
			else if (envName.equals("Grid-World")){
				params.modelClass = "IncGDLinearModel";
				params.featClass = "FeatGen_RBFs";
				params.featGenParams.put("basisFcnsPerDim", "5");
				params.featGenParams.put("relWidth", "0.08");
			}
			else if (envName.equals("Loop-Maze")){
				params.modelClass = "IncGDLinearModel";
				params.featClass = "FeatGen_RBFs";
				params.featGenParams.put("basisFcnsPerDim", "6");
				params.featGenParams.put("relWidth", "0.05");
				params.stepSize = 0.2;
				params.featGenParams.put("biasFeatVal", "0.1");
				
				// credit assignment parameters
				params.distClass = "uniform"; //// immediate, previousStep, or uniform
				params.creditDelay = 0.15; // these bounds are because the typical event being reward is the action shown at the beginning of the step
				params.windowSize = 0.25;
			}
			else if (envName.equals("Mario")){
				params.featClass = "FeatGen_Mario";
				params.modelClass =  "WekaModelPerActionModel"; // "WekaModel"; 
				params.wekaModelName = "M5P";
			}
			else if (envName.equals("Tetris")){
				params.distClass = "previousStep";
				params.extrapolateFutureRew = false;
				params.traceDecayFactor = 0.0;
				params.featClass = "FeatGen_Tetris";
				params.modelClass =  "IncGDLinearModel"; 
				params.stepSize = 0.000005; // 0.02;
			}
			else if (envName.equals("Acrobot")){
				;
			}
			else if (envName.equals("CarStop")){
 				params.windowSize = 0.7;
 				params.creditDelay = 0.2;
			}
			else if (envName.equals("FuelWorld")){
				params.modelClass = "IncGDLinearModel";
				params.featClass = "FeatGen_RBFs";
				params.featGenParams.put("numBinsPerDim", "10");
				params.featGenParams.put("basisFcnsPerDim", "41"); //41
				params.featGenParams.put("relWidth", "0.062"); //0.2
			}
			else if (envName.equals("NexiNav")){
				params.wekaModelName = "KDTree"; 
				params.safeAction = new Action();
				int[] safeActArray = {0};
				params.safeAction.intArray = safeActArray;
			}
		}
		
		
		
		else if (agentClassName.contains("SarsaLambdaAgent") || 
				agentClassName.contains("HyperSarsaLambdaAgent")){
			params.modelClass = "IncGDLinearModel";
			params.traceDecayFactor = 0.95;
			params.featClass = "FeatGen_Discretize";
			if (envName.equals("Mountain-Car")){
				params.featClass = "FeatGen_RBFs"; // "FeatGen_Discretize";
				if (params.featClass.equals("FeatGen_Discretize")){
					params.featGenParams.put("numBinsPerDim", "20");
					params.initWtsValue = -120.0;
				}
				else if (params.featClass.equals("FeatGen_RBFs")){
//					params.initModelWSamples = true;
//					params.initSampleValue = -120.0;
//					params.numBiasingSamples = 100000;
//					params.biasSampleWt = 0.01;
//					params.initWtsValue = 0; //// just for testing. this shouldn't stay here.
					
					//params.setPyMCParams(agentClassName, false);
					
					/*
					 * parameters below from java-based parameter search 
					 */
					params.stepSize = 0.05;
					params.traceDecayFactor = 0.9;
					params.selectionParams.put("epsilon", "0.0");
					params.featGenParams.put("basisFcnsPerDim", "34"); //41
					params.featGenParams.put("relWidth", "0.062"); //0.2
				}
			}
			else if (envName.equals("CartPole")){
				params.featClass = "FeatGen_RBFs"; // "FeatGen_Discretize";
				params.selectionParams.put("epsilon", "0.085"); // "0.0");
				params.selectionParams.put("epsilonAnnealRate", "0.9995");
				if (params.featClass.equals("FeatGen_Discretize")){
					params.featGenParams.put("numBinsPerDim", "10");
				}
				else if (params.featClass.equals("FeatGen_RBFs")){
					params.stepSize = 0.05; //0.19;
					params.traceDecayFactor = 0.86;

					params.featGenParams.put("basisFcnsPerDim", "8");
					params.featGenParams.put("relWidth", "0.13"); 
				}
			}
			else if (envName.equals("Acrobot")){
				;
			}
			else if (envName.equals("HandFed")){ // Used for testing agents with predetermined transitions
				params.featGenParams.put("numBinsPerDim", "3");
				params.featClass = "FeatGen_RBFs";
				params.featGenParams.put("basisFcnsPerDim", "3");
			}
		}
		else if (agentClassName.contains("TamerRLAgent")){
//			params.selectionParams.put("epsilon", "0.03"); 
//			params.selectionParams.put("epsilonAnnealRate", "0.9995");
//			params.featGenParams.put("numBinsPerDim", "10"); // used by FeatGen_Discretize
//			params.featGenParams.put("basisFcnsPerDim", "41"); // used by FeatGen_RBFs
//			params.featGenParams.put("relWidth", "0.062"); // used by FeatGen_RBFs
		}
			
		// This was true for AAMAS 2012 experiments. It's not technically wrong, but I 
		// think bias features should be action-specific (see warning below).	
//		if (params.featClass.equals("FeatGen_RBFs") &&
//				params.modelClass.equals("IncGDLinearModel")) {
//			params.modelAddsBiasWt = true;
//		}		
		if (params.modelAddsBiasFeat && Integer.valueOf(params.featGenParams.get("biasFeatVal")) != 0)
			System.err.println("\n\nWarning. A bias weight is being added both in the model (one always " +
					"active) and in the features (one per action, active when that action occurs)");
		
		return params;
	}
	
	public static Params getHInflParams(String envName, boolean hInfEligTraces){
		Params params = new Params();
		params.hInflParams.put("accumFactor", "1.0");
		
		/*
		 * These default values may change and should not be used in an experiment without specifying
		 * them somewhere else.
		 */
		if (hInfEligTraces) {
//			params.hInflParams.put("stepDecayFactor", "0.995"); // used for MC in icml workshop paper
			params.hInflParams.put("stepDecayFactor", "0.95");
			params.hInflParams.put("epDecayFactor", "0.98");
		}
		else {
			params.hInflParams.put("stepDecayFactor", "1.0");
			params.hInflParams.put("epDecayFactor", "0.97");
		}
		params.hInflParams.put("traceStyle", "accumulating");
		
		
		
		params.featClass = "FeatGen_RBFs";	
		if (envName.equals("Mountain-Car")){
			params.setPyMCParams("HInfluence", hInfEligTraces);
		}
		else if (envName.equals("CartPole")){
			if (hInfEligTraces) {
				params.hInflParams.put("stepDecayFactor", "0.99996");
				params.hInflParams.put("epDecayFactor", "0.98");
			}
			else {
				params.hInflParams.put("stepDecayFactor", "1.0");
				params.hInflParams.put("epDecayFactor", "0.97");
			}
			params.hInflParams.put("accumFactor", "0.2");
			
			params.featGenParams.put("basisFcnsPerDim", "8");
			params.featGenParams.put("relWidth", "0.08"); 
		}
		else if (envName.equals("Acrobot")){
			;
		}
		return params;		
	}
	
	
	public void setPyMCParams(String agentClassName, boolean hInfEligTraces) {
		//// This sets the RBF features and Sarsa parameters to match what was used
		//// in my Python code for the AAMAS-2010 paper for pessimistic learning.
		
		this.featClass = "FeatGen_RBFs";
		this.modelClass = "IncGDLinearModel";
		
		this.initModelWSamples = false;
		this.initWtsValue = -10.0; //-4.0 "was used in AAMAS[2010] paper" though I think that might have just been the submission, not the camera ready
		
		this.stepSize = 0.15;
				 
		this.selectionParams.put("epsilon", "0.00625");
		this.selectionParams.put("epsilonAnnealRate", "0.99");
		
		this.featGenParams.put("basisFcnsPerDim", "40");
		if (agentClassName.contains("TamerAgent")){
			this.featGenParams.put("relWidth", "0.08");
			this.traceDecayFactor = 0.0; // non-zero in AAMAS2012 paper?
		}
		else if (agentClassName.contains("SarsaLambdaAgent")){
			this.traceDecayFactor = 0.84;
			this.featGenParams.put("relWidth", "0.08"); //same as TAMER for pessimistic
		}
		else if (agentClassName.contains("HInfluence")){
			this.traceDecayFactor = 0.84;
			this.featGenParams.put("relWidth", "0.08"); 
		}
		this.featGenParams.put("normMin", "-1");
		this.featGenParams.put("normMax", "1");
		this.featGenParams.put("biasFeatVal", "0.1");
		
		if (hInfEligTraces) {
			this.hInflParams.put("stepDecayFactor", "0.9998");
			this.hInflParams.put("epDecayFactor", "1.0");
		}
		else {
			this.hInflParams.put("stepDecayFactor", "1.0");
			this.hInflParams.put("epDecayFactor", "0.98");
		}
				
	}
	
	public String toOneLineStr(){
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");

//		result.append( this.getClass().getName() );
//		result.append( " Object {" );
//		result.append(newLine);

		//determine fields declared in this class only (no fields of superclass)
		Field[] fields = this.getClass().getDeclaredFields();

		//print field names paired with their values
		for (Field field : fields) {
			result.append(";");
			try {
				result.append(field.getName());
				result.append("=");
				//requires access to private field:
				result.append(field.get(this));
			} 
			catch (IllegalAccessException ex) {
				System.out.println(ex);
			}
//			result.append(newLine);
		}
//		result.append("}");
	
		return result.toString().replace("\n", "<n>");
	}
}



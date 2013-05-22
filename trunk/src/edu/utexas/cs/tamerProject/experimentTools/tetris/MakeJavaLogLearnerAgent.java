package edu.utexas.cs.tamerProject.experimentTools.tetris;

import java.io.BufferedReader;
import java.util.Arrays;

import org.rlcommunity.environments.mountaincar.MountainCar;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import weka.classifiers.functions.LinearRegression;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.agents.specialty.ExtActionAgentWrap;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.demos.tetris.TetrisTamerExpHelper;
import edu.utexas.cs.tamerProject.experimentTools.LogTrainer;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.experimentTools.TimeStep;
import edu.utexas.cs.tamerProject.experiments.GeneralExperiment;
import edu.utexas.cs.tamerProject.featGen.FeatGen_NoChange;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;
import edu.utexas.cs.tamerProject.modeling.Sample;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.modeling.weka.WekaModelWrap;
import edu.utexas.cs.tamerProject.params.Params;
import edu.utexas.cs.tamerProject.utils.MutableDouble;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndAct;

public class MakeJavaLogLearnerAgent {

	

	static boolean debug = false;
	
	public static double stepSize = 0.00001; ///0.000005 / 47;  // python code takes input value and divides by number of features
	public static double REG_WT = 0;
	
		
	public static void runOneExp(String[] args) {
		RunLocalExperiment runLocal = new RunLocalExperiment();
		RunLocalExperiment.numEpisodes = 1000;
		RunLocalExperiment.stepDurInMilliSecs = 0;
		
		ExtActionAgentWrap agent = MakeJavaLogLearnerAgent.createAgent(args);
		runLocal.theAgent = agent;
		runLocal.theEnvironment = new MountainCar();
		
		runLocal.init();
		runLocal.initExp(); // where agent_init() is called
		agent.setInTrainSess(false);
		//System.out.println("weights of tamer model: " + Arrays.toString(((IncGDLinearModel)agent.model).weights));
		System.out.println("tamer in training? " + agent.getInTrainSess());
		
		runLocal.startExp();
		while (!runLocal.expFinished) {
			GeneralAgent.sleep(10000);
		}
	}
	
	public static String makeUnique(String[] args) {
		return "trash";
	}
	
	public static ExtActionAgentWrap createAgent(String[] args) {
		GeneralExperiment exp = new TetrisTamerExpHelper();
		EnvironmentInterface env = exp.createEnv();
		ExtActionAgentWrap agent = (ExtActionAgentWrap)exp.createAgent(args, env);
		return agent;
		
//		TamerAgent agent = new TamerAgent();
//		String unique = MakeJavaLogLearnerAgent.makeUnique(args);
//		
//		agent.setUnique(unique);	
//		agent.setRecordRew(true);
//		agent.setRecordLog(false);
//		
//		agent.envName = "Tetris";
//		agent.enableGUI = false;
//		agent.EP_END_PAUSE = 0;
//				
//		Params agentParams = Params.getParams(agent.getClass().getName(), agent.envName);
//		agentParams.traceDecayFactor = 0.0; // setting this to .84 instead of 0 in past experiments might be why it did slightly better
//		agentParams.extrapolateFutureRew = false;
//		MakeJavaLogLearnerAgent.setParams(agentParams);
//		
//		//agent.trainFromLog = false; // for debugging only
//		agent.params = agentParams;
//		
//		return agent;
	}
	
	public static void setParams(Params params) {
		params.distClass = "previousStep"; //// immediate, previousStep, or uniform
		params.extrapolateFutureRew = false;
		params.traceDecayFactor = 0.0;
		params.featClass = "FeatGen_NoChange";
		params.modelClass =  "IncGDLinearModel"; // "WekaModel"; //
		//params.wekaModelName = "LinearRegression"; 
		params.modelAddsBiasFeat = true;
		params.stepSize = MakeJavaLogLearnerAgent.stepSize;
		
		params.delayWtedIndivRew = false; 
		params.noUpdateWhenNoRew = true; 
		params.selectionMethod = "greedy";
	
		params.initModelWSamples = false;
		params.numBiasingSamples = 0;
		params.biasSampleWt = 0.1;
		params.traceDecayFactor = 0.0;
	}
	
	
	public static double[] getHWeightsFromLog(String logFilePath, int maxSteps, 
												int numEpochs, String taskSpec) {
		return MakeJavaLogLearnerAgent.getHWeightsFromLog(logFilePath, maxSteps, 
																numEpochs, false, taskSpec);
	}
	
	public static double[] getHWeightsFromLog(String logFilePath, int maxSteps, 
						int numEpochs, boolean weka, String taskSpec) {
		
		String[] args = TetrisTamerExpHelper.getDebugArgsStrArray();
		ExtActionAgentWrap shellAgent = MakeJavaLogLearnerAgent.createAgent(args);		
		shellAgent.agent_init(taskSpec);
		TamerAgent logAgent = (TamerAgent)shellAgent.coreAgent;
		TamerAgent.verifyObsFitsEnvDesc = false;
		
		MakeJavaLogLearnerAgent.trainAgentFromLog(maxSteps, logFilePath, logAgent, shellAgent);
		
		double[] weights = getHWts(logAgent, weka);
		return weights; 
	}
	

	public static double[] getHWeightsFromLog(String logFilePath, int maxSteps, String taskSpec) {
		return MakeJavaLogLearnerAgent.getHWeightsFromLog(logFilePath, maxSteps, 1, taskSpec);
	}
	
	
	
	
	
	public static void trainAgentFromLog(int maxSteps, String logFilePath, TamerAgent logAgent, ExtActionAgentWrap shellAgent){
		
		System.out.println("Max steps: " + maxSteps);
		LogTrainer ltrain = new LogTrainer(logFilePath, logAgent);
		if (maxSteps == -1) // train off of whole log
			ltrain.trainOneEpoch();
		else
			ltrain.trainForNSteps(maxSteps);
		
		//LogTrainer.trainOnLog(logFilePath, logAgent, numEpochs);
		
		if (shellAgent.getInTrainSess())
			shellAgent.toggleInTrainSess(); // toggle ensures that member agents are also toggled off
		TamerAgent.verifyObsFitsEnvDesc = true;
	}
	
	
	
	public static double[] getHWts(TamerAgent logAgent, boolean weka){
		double[] weights;
		if (weka) {
			double[] weightsWEmptySpotForClass = ((LinearRegression)((WekaModelWrap)logAgent.model).getWekaClassifier()).coefficients();
			weights = new double[weightsWEmptySpotForClass.length - 1];
			System.arraycopy(weightsWEmptySpotForClass, 0, weights, 0, weights.length - 1);
			weights[weights.length - 1] = 
						weightsWEmptySpotForClass[weightsWEmptySpotForClass.length - 1];
			System.out.println("weights: " + weights);
			
			
		} else {
			IncGDLinearModel coreModel = ((IncGDLinearModel)logAgent.model); 
			double[] learnedWts = coreModel.getWeights();
			weights = new double[learnedWts.length + 1];
			System.arraycopy(learnedWts, 0, weights, 0, weights.length - 1);
			weights[weights.length - 1] = ((IncGDLinearModel)logAgent.model).getBiasWt();
		}
		return weights;
	}
	

	public static String[] getDebugArgsStrArray() {
		String[] args = new String[0];
		return args;
	}
	
	public static void main(String[] args) {
		if (MakeJavaLogLearnerAgent.debug) {
			args = getDebugArgsStrArray();
		}
		
		MakeJavaLogLearnerAgent.runOneExp(args);
	}
	
	
}

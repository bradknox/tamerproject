package edu.utexas.cs.tamerProject.experiments;

import java.util.Arrays;
import java.util.HashMap;

import org.rlcommunity.environments.mountaincar.MountainCar;
import org.rlcommunity.rlglue.codec.types.Action;

import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.experimentTools.LogTrainer;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.params.Params;

/**
 * This class creates an experiment that tests learning from predictions of
 * human reward as if it is MDP reward, calculating and acting upon a return
 * with varying discount factors. When the discount factor is 0, the condition
 * corresponds to TAMER's actions selection strategy. When the discount
 * factor is 1, predictions of human reward are being treated equivalently to
 * MDP reward (since the domain(s) tested here is/are episodic).
 * 
 * The learning algorithm is Sarsa with a several-step lookahead, which aids
 * learning. The key comparison here is performance at plateau. The results
 * are essentially correct for discountFactor=0, since that requires almost no
 * learning (it only requires any at all because we keep the learning algorithm
 * static among the conditions), and completely correct for discountFactor=1, 
 * with which all human models lead to infinite loops of unlimited reward, since
 * the existence of one such loop demonstrates that the optimal set of polices
 * with respect to human reward is to never reach the goal.
 * 
 * @author bradknox
 *
 */
public class ToyExp {

	static boolean debug = true;
	
	//double discountFactor = 1;
	
	public String trainLogPath = "";
	public int logTrainEpochs = 1;
	
	private final int TRAIN_EPOCHS_FOR_LINEAR = 100;

	
	public ToyExp(){
	}
	
	
	public GeneralAgent createAgent(String[] args) {
		this.processArgs(args);
		return createTamerRLAgent(args);
		//return createHyperSarsaAgent(args);
	}
	
	
	
	
	public TamerRLAgent createTamerRLAgent(String[] args) {
		
		TamerRLAgent agent = new TamerRLAgent();
		System.out.println("\n\nAgent in ToyExp: " + agent);

		String unique = ToyExp.makeUnique(args);
		agent.setUnique("tamerrl%" + unique);
		agent.rlAgent.setUnique("sarsa%" + unique);

		agent.setRecordRew(true);
		agent.rlAgent.setRecordRew(true);
		
		/*
		 * Set combination method for TAMER+RL. Will be RL_ON_H_AS_R in experiments.
		 */
		//agent.COMBINATION_METHOD = agent.RL_ON_H_AS_R;
		//agent.COMBINATION_METHOD = agent.RL_ONLY;
		agent.COMBINATION_METHOD = agent.TAMER_ONLY;
		
		agent.envName = "Loop-Maze";//"Mountain-Car";
		agent.enableGUI = false;
		agent.tamerAgent.EP_END_PAUSE = 0;
				
		agent.initParams(agent.envName);
		setTamerRLAgentParams(agent); // should be done before processPreInitArgs(), which might override some assignments done by this call 	
		agent.processPreInitArgs(args);
		
		//agent.usePYMCModel(1); don't use without tagging with todo and "remove" remark
		
		return agent;
	}
	
	

		
	public void runOneExp(String[] args) {
		RunLocalExperiment runLocal = new RunLocalExperiment();
		setRunLocalExpOptions();
		
		GeneralAgent agent = createAgent(args);
		runLocal.theAgent = agent;
		runLocal.theEnvironment = new LoopMaze();//MountainCar();
		
		runLocal.init();
		runLocal.initExp(); // where agent_init() is called
		
		adjustAgentAfterItsInit(args, agent, this.logTrainEpochs, this.trainLogPath);
		
		System.out.println("About to start experiment");
		runLocal.startExp();
		while (!runLocal.expFinished) {
			GeneralAgent.sleep(10000);
		}
	}
	
	
	public void adjustAgentAfterItsInit(String[] args, GeneralAgent agent, 
											int logTrainEpochs,	String trainLogPath) {
		agent.processPostInitArgs(args);
		
		//LogTrainer.trainOnLog(trainLogPath, tamerRLAgent.tamerAgent, logTrainEpochs);
		if (agent.getInTrainSess())
			agent.toggleInTrainSess(); // toggle ensures that member agents are also toggled off
		//System.out.println("weights of tamer model: " 
		//		+ Arrays.toString(((IncGDLinearModel)agent.tamerAgent.model).weights));
		System.out.println("in training? " + agent.getInTrainSess());
	}
	
	
	/**
	 * Create unique string for saving the log of this experiment.
	 * 
	 * @param args
	 * @return
	 */
	public static String makeUnique(String[] args) {
		String[] logFilePathParts = args[5].split("/");
		String logFileName = logFilePathParts[logFilePathParts.length - 1].replace(".log", "").replace("recTraj-", "");
		System.out.println("logFileName: " + logFileName);	
		return args[1] + "%" + args[3] + "%" + logFileName;
	}
	
	
	public static void setRunLocalExpOptions() {
		RunLocalExperiment.numEpisodes = 1000;
		RunLocalExperiment.maxStepsPerEpisode= 400;
		RunLocalExperiment.stepDurInMilliSecs = 0;
	}
	
	

	
	
	public void processArgs(String[] args) {
		System.out.println("\n[------process pre-init args------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];
    		if (argType.equals("-tamerModel") && (i+1) < args.length){
    			if (args[i+1].equals("linear")) {
    				this.logTrainEpochs = TRAIN_EPOCHS_FOR_LINEAR;
    			}
    		}
			else if ((argType.equals("-trainLogPath")) && (i+1) < args.length){
				this.trainLogPath = args[i+1];
				System.out.println("this.trainLogPath set to: " + this.trainLogPath);
			}
		}
	}
	

	
	
	
	
	


	

	
	
	/*
	 * Set the parameters for the Sarsa and TAMER agents. This should be done before,
	 * processPreInitArgs(), which may be used to overwrite these valuse.
	 */
	private static void setTamerRLAgentParams(TamerRLAgent agent) {
		agent.params.extrapolateFutureRew = false;
		setSarsaAgentParams(agent.rlAgent);
		setTamerAgentParams(agent.tamerAgent);
	}
	
	private static void setSarsaAgentParams(GeneralAgent agent) {
		/*
		 * Sarsa params
		 */
    
		// from setPyMCParams()
		agent.params.featClass = "FeatGen_RBFs";
		agent.params.modelClass = "IncGDLinearModel";	
		agent.params.initModelWSamples = false;
		agent.params.traceDecayFactor = 0.84;
		agent.params.featGenParams.put("basisFcnsPerDim", "40");
		agent.params.featGenParams.put("relWidth", "0.08");
		agent.params.featGenParams.put("normMin", "-1");
		agent.params.featGenParams.put("normMax", "1");
		agent.params.featGenParams.put("biasFeatVal", "0.1"); // used by FeatGen_RBFs
		//agent.params.featGenParams.put("biasFeatVal", "0.0"); // used by FeatGen_RBFs
		
		// from Params defaults
		agent.params.initSampleValue = 0.0;
		agent.params.numBiasingSamples = 0;
		agent.params.biasSampleWt = 0.5;
		agent.params.modelAddsBiasFeat = false; // 
		agent.params.traceType = "replacing";
 
		
		// params I've set for this experiment
		agent.params.initWtsValue = 0.0; // When predictions of human reward are given, 
		// this is not clearly optimistic or pessimistic.	
		agent.params.stepSize = 0.01; 

		agent.params.selectionMethod = "e-greedy"; // treesearch used as well
		agent.params.selectionParams.put("epsilon", "0.1");
		agent.params.selectionParams.put("epsilonAnnealRate", "0.998");
		/*
		 *      *********Tree search parameters*********
		 */
		agent.params.selectionParams.put("treeSearch", "true");
		agent.params.selectionParams.put("greedyLeafPathLength", "0");
		agent.params.selectionParams.put("exhaustiveSearchDepth", "3");
		agent.params.selectionParams.put("randomizeSearchDepth", "true");
	}

	public static void setTamerAgentParams(TamerAgent agent) {		
		/*
		 * Tamer params
		 */
		agent.params.distClass = "uniform"; //// previousStep or uniform
		agent.params.creditDelay = 0.2;
		agent.params.windowSize = 0.6;
		agent.params.extrapolateFutureRew = false;
		agent.params.delayWtedIndivRew = false; 
		agent.params.noUpdateWhenNoRew = false; 
		
		agent.params.modelClass = "WekaModelPerActionModel";
		agent.params.featClass = "FeatGen_NoChange";
		agent.params.selectionMethod = "greedy";
	
		agent.params.initModelWSamples = false;
		agent.params.numBiasingSamples = 100;
		agent.params.biasSampleWt = 0.1;
		agent.params.wekaModelName = "IBk";
		agent.params.traceDecayFactor = 0.0;
	}
	
	
	
	public static String[] getDebugArgsStrArray() {
		String[] args = new String[12];
		args[0] = "";//-tamerModel";
		args[1] = "linear";
		args[2] = ""; //"-credType"; // indivRewOnly, aggregate, indivAlways, or aggregRewOnly
		//args[3] = "aggregate"; 
		args[3] = "aggregRewOnly";
		//args[3] = "indivAlways";
		//args[3] = "indivRewOnly";
		args[4] = "";//-trainLogPath";
		args[5] = RecordHandler.getPresentWorkingDir().replace("/bin", "") + "/src/edu/utexas/cs/tamerProject/agents/tamerrl/models/recTraj-H2.log";		
		//args[5] = RecordHandler.getPresentWorkingDir().replace("/bin", "") + "/src/edu/utexas/cs/tamerProject/agents/tamerrl/models/recTraj-H1.log";
		args[6] = "-expName";
		args[7] = "test";
		args[8] = "-trainEpLimit";
		args[9] = "20";
		args[10] = "-discountFactor";
		args[11] = "0.8";
		return args;
	}
	
	
	
	
	public static void main(String[] args) {
		if (ToyExp.debug) {
			args = getDebugArgsStrArray();
		}
		
		ToyExp exp = new ToyExp();
		exp.runOneExp(args);
	}
	
}

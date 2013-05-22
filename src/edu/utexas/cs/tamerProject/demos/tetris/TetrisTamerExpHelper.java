package edu.utexas.cs.tamerProject.demos.tetris;

import java.util.Arrays;
import java.util.HashMap;

import org.rlcommunity.environments.mountaincar.MountainCar;
import org.rlcommunity.environments.tetris.Tetris;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.Environments.SampleableEnvBase;



import edu.utexas.cs.tamerProject.actSelect.ActionSelect;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.AsynchDPAgent;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.AsynchDPAgentForceCont;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPForceContAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLForceContAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.specialty.ExtActionAgentWrap;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.envModels.EnvWrapper;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.experimentTools.LogTrainer;
import edu.utexas.cs.tamerProject.experimentTools.RecordHandler;
import edu.utexas.cs.tamerProject.experiments.GeneralExperiment;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;
import edu.utexas.cs.tamerProject.modeling.templates.ObsActModel;
import edu.utexas.cs.tamerProject.params.Params;

/**
 * (Needs to be adapted for this class.)
 * 
 * This class creates an experiment that tests learning from predictions of
 * human reward as if it is MDP reward, calculating and acting upon a return
 * with hyperbolic discounting at varying parameters. When the discount factor
 * is 0, the condition corresponds to TAMER's actions selection strategy. When 
 * the discount factor is very high, predictions of human reward are almost 
 * being treated equivalently to MDP reward (since the domain(s) tested here 
 * is/are episodic).
 * 
 * @author bradknox
 *
 */
public class TetrisTamerExpHelper extends GeneralExperiment {

	static boolean debug = true;
	public final String expPrefix = "tetriszamsterdam";
	
	/*
	 * Hard-coding indices for args String (this might be removed eventually)
	 */
	static final int EXP_NAME_I = 2;

		
	
	public void setRunLocalExpOptions() {
		RunLocalExperiment.numEpisodes = 1000;
		RunLocalExperiment.maxStepsPerEpisode= 10000000;
		RunLocalExperiment.stepDurInMilliSecs = 10;
	}
	
	
	public TetrisTamerExpHelper(){
	}
	

	/**
	 * EnvironmentInterface object that is input here should not be the same as
	 * the object used by RLGlue through RunLocalExperiment (to avoid multi-threading
	 * issues).
	 */
	public GeneralAgent createAgent(String[] args, EnvironmentInterface env) {
		this.processArgs(args);
		return createTetrisAgent(args);
	}
	
	
	
	

	public ExtActionAgentWrap createTetrisAgent(String[] args) {
		
		ExtActionAgentWrap agent;
		agent = new ExtActionAgentWrap();
		
		agent.coreAgent = new TamerAgent();

		String unique = TetrisTamerExpHelper.makeUnique(args);
		//agent.setRecordRew(true); // records predictions of human reward
				
		agent.envName = "Tetris";
		agent.enableGUI = false;
		agent.coreAgent.envName = "Tetris";
		agent.coreAgent.enableGUI = false;
		((TamerAgent)agent.coreAgent).EP_END_PAUSE = 0;
		
		agent.params = Params.getParams(agent.getClass().getName(), agent.envName);
		agent.coreAgent.params = Params.getParams(agent.coreAgent.getClass().getName(), agent.envName);

		
		agent.initParams(agent.envName);
		setTamerAgentParams((TamerAgent)agent.coreAgent); // should be done before processPreInitArgs(), which might intentionally override some assignments done by this call
		agent.processPreInitArgs(args);
		
		return agent;
	}
	
	
	
		
	public void runOneExp(String[] args) {
		RunLocalExperiment runLocal = new RunLocalExperiment();
		setRunLocalExpOptions();
		
		EnvironmentInterface env = createEnv();
		GeneralAgent agent = createAgent(args, null);
		runLocal.theAgent = agent;
		runLocal.theEnvironment = env;
		
		runLocal.init();
		runLocal.initExp(); // where agent_init() is called
		
		adjustAgentAfterItsInit(args, agent);
		
		System.out.println("About to start experiment");
		runLocal.startExp();
		while (!runLocal.expFinished) {
			GeneralAgent.sleep(100);
		}
	}
	
	
	public EnvironmentInterface createEnv(){
		return new Tetris();
	}
	
	public void adjustAgentAfterItsInit(String[] args, GeneralAgent agent) {
		agent.processPostInitArgs(args);
		System.out.println("args in adjust: " + Arrays.toString(args));
		if (!agent.getInTrainSess())
			agent.toggleInTrainSess(); // toggle ensures that member agents are also toggled on
		System.out.println("in training in " + this.getClass().getSimpleName() + "? " + agent.getInTrainSess());
	}
	
	
	/**
	 * Create unique string for saving the log of this experiment.
	 * 
	 * @param args
	 * @return
	 */
	public static String makeUnique(String[] args) {
		return "testUnique";
//		String[] logFilePathParts = args[TRAIN_PATH_I].split("/");
//		String logFileName = logFilePathParts[logFilePathParts.length - 1].replace(".log", "").replace("recTraj-", "");
//		System.out.println("logFileName: " + logFileName);	
//		String epOrCont = args[TASK_CONT_I].equals("-makeTaskCont") ? "cont" : "epis" ;
//		return args[DISC_PARAM_I] + "%" + args[INIT_VALUE_I] + "%" + epOrCont + "%" + logFileName;
	}

	
	public void processArgs(String[] args) {
		System.out.println("\n[------process pre-init args------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
    		String argType = args[i];
    		// process any arguments here
		}
	}
	

	
		
	
	/*
	 * Set the parameters for the TAMER agent. This should be done before
	 * processPreInitArgs(), which may be used to overwrite these valuse.
	 */
	public static void setTamerAgentParams(TamerAgent agent) {		
		/*
		 * Set all TamerAgent params here, not in Params class. That way you have
		 * a single location that's specific to this experiment that contains
		 * all of your algorithmic information when it's time for writing.
		 */
		System.out.println(agent.params);
		agent.params.distClass = "previousStep"; //// immediate, previousStep, or uniform
		agent.params.extrapolateFutureRew = false;
		agent.params.traceDecayFactor = 0.0;
		agent.params.featClass = "FeatGen_Tetris";
		agent.params.modelClass =  "IncGDLinearModel"; 
		agent.params.modelAddsBiasFeat = true;
		agent.params.stepSize = 0.000005 / 47; // python code takes input value and divides by number of features // 0.02;
		
		agent.params.delayWtedIndivRew = false; 
		agent.params.noUpdateWhenNoRew = false; 
		agent.params.selectionMethod = "greedy";
	
		agent.params.initModelWSamples = false;
		agent.params.numBiasingSamples = 0;
		agent.params.biasSampleWt = 0.1;
		agent.params.traceDecayFactor = 0.0;
	}

	
	public static String[] getDebugArgsStrArray() {
		String[] args = new String[0];
		return args;
	}
	
	
	
	
	public static void main(String[] args) {
		if (TetrisTamerExpHelper.debug) {
			args = getDebugArgsStrArray();
		}
		
		TetrisTamerExpHelper exp = new TetrisTamerExpHelper();
		exp.runOneExp(args);
	}


	public void processTrainerUnique(GeneralAgent agent, String trainerUnique) {
		// only one condition in this Tetris exp
	}
	
}

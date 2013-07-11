package edu.utexas.cs.tamerProject.demos;

import java.util.Arrays;
import java.util.HashMap;

import org.rlcommunity.environments.mountaincar.MountainCar;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.Environments.SampleableEnvBase;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.AsynchDPAgent;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.AsynchDPAgentForceCont;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPForceContAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLForceContAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.envModels.EnvWrapper;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.experiments.GeneralExperiment;
import edu.utexas.cs.tamerProject.params.Params;

/**
 * Helper class for D10LoopmazeVITamerExp, used to initialize the
 * agent and environment as well as handle some experimental
 * details (like parsing and using the trainer's "unique").
 * 
 * This class can also be run by itself if human interaction and
 * visualization are not needed. One such use case is when the 
 * agent is learning from logs of past interaction. Without 
 * visualization, time steps can go much faster than 1000 steps
 * per second (the limit in user-controlled speed in the applet).
 * 
 * @author bradknox
 *
 */
public class D10LoopmazeVITamerHelper extends GeneralExperiment {

	static boolean debug = true; // set to false for experiment

	public final String expPrefix = "discountingzloopmazezexpon";

	protected boolean makeTaskContinuous = false; // keep default false


	/*
	 * DP sweeps occurred every 20 ms in IncGD modeled experiment on 2012/4/13 and 2012/4/14  
	 */
	public long timeBtwnDPSweeps = 10; // in milliseconds

	/*
	 * Sets discounting parameter (gamma if exponential discounting) if running
	 * in debug mode.
	 */
	public static double discParam = 0.99;

	/*
	 * Determines whether to use dynamic programming or AsynchDP. Should be true 
	 * unless debugging AsynchDPAgent.
	 */
	public boolean dynProg = true;

	/*
	 * How deeply down the search tree the UCT planner goes before starting a new iteration.
	 */
	private int depthForUCT = 40;

	/*
	 * Hard-coding indices for args String (this might be removed eventually)
	 */
	static final int DISC_PARAM_I = 1;
	static final int TRAIN_PATH_I = 3;
	static final int TASK_CONT_I = 4;
	static final int EXP_NAME_I = 6;


	public D10LoopmazeVITamerHelper(){
	}


	public void setRunLocalExpOptions() {
		RunLocalExperiment.numEpisodes = 10;
		RunLocalExperiment.maxStepsPerEpisode= 400;
		RunLocalExperiment.stepDurInMilliSecs = 100;
	}



	public void runOneExp(String[] args) {
		RunLocalExperiment runLocal = new RunLocalExperiment();
		setRunLocalExpOptions();

		SampleableEnvBase agentUsableEnv = createEnv();
		SampleableEnvBase env = createEnv();
		GeneralAgent agent = createAgent(args, agentUsableEnv);
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
	
	

	public SampleableEnvBase createEnv(){
		return new LoopMaze();
	}




	/**
	 * Creates the agent (either value iteration or "asynchronous" value iteration).
	 * 
	 * EnvironmentInterface object that is input here should not be the same as
	 * the object used by RLGlue through RunLocalExperiment (to avoid multi-threading
	 * issues).
	 */
	public GeneralAgent createAgent(String[] args, EnvironmentInterface env) {
		this.processArgs(args);
		SampleableEnvBase sampleableEnv = null;
		try{
			sampleableEnv = (SampleableEnvBase)env;
		}
		catch (Exception e) {
			System.err.println("Exception while trying to cast EnvironmentInterface to SampleableEnvBase: \n" + e);
			System.exit(1);
		}


		if (this.dynProg) 
			return createDPAgent(args, sampleableEnv);
		else
			return createAsynchDPAgent(args, sampleableEnv);
	}




	/**
	 * Create agent that performs highly asynchronous value iteration, where
	 * the state-action pair used to update in dynamic programming is chosen
	 * by UCT planning from the agent's current state. Calling this algorithm
	 * dynamic programming (or value iteration) assumes that the there is no
	 * stochasticity in the agent's policy or the environment's transitions.
	 * 
	 * @param args
	 * @param env
	 * @return
	 */
	public AsynchDPAgent createAsynchDPAgent(String[] args, SampleableEnvBase env) {

		AsynchDPAgent agent;
		if (makeTaskContinuous)
			agent = new AsynchDPAgentForceCont();
		else
			agent = new AsynchDPAgent();

		agent.tamerAgent = new TamerAgent();		
		System.out.println("\n\nAgent in " + this.getClass().getSimpleName() + ": " + agent);
		System.out.println("Stack: " + Arrays.toString(Thread.currentThread().getStackTrace()));

		String unique = D10LoopmazeVITamerHelper.makeUnique(args);
		agent.setUnique(this.getClass().getSimpleName() + "%" + unique);
		//agent.setRecordRew(true); // records predictions of human reward

		agent.envName = "Loop-Maze";
		agent.enableGUI = false;
		agent.tamerAgent.EP_END_PAUSE = 0;
		agent.useTamer = true;
		agent.setDiscountFactorForLearning(D10LoopmazeVITamerHelper.discParam);

		agent.PLANNING_DEPTH = this.depthForUCT;
		agent.printPostSample = true;

		/*
		 * Set agent's models of the environment
		 */
		EnvWrapper envWrapper = new EnvWrapper(env);
		agent.setEnvTransModel(envWrapper.transModel);
		agent.setRewModel(envWrapper.rewModel);

		agent.initParams(agent.envName);
		agent.params.stepSize = 1; // stepSize of 1 okay b/c this is really the same as asynchronous VI as long as env and policy are deterministic
		setTamerAgentParams(agent.tamerAgent); // should be done before processPreInitArgs(), which might intentionally override some assignments done by this call
		setRLAgentParams(agent);
		agent.processPreInitArgs(args);

		return agent;
	}




	/**
	 * Create agent that does one value iteration sweep over all states
	 * at a time, with sweeps occurring at regular intervals.
	 * 
	 * Note that this algorithm is technically also asynchronous since
	 * it updates a single state at a time during the sweep. However, it
	 * much closer to synchronous than the algorithm called asynchronous
	 * DP, which updates different states with very different frequencies.
	 */
	public DPAgent createDPAgent(String[] args, SampleableEnvBase env) {
		DPAgent agent;
		if (makeTaskContinuous )
			agent = new DPForceContAgent();			
		else
			agent = new DPAgent();


		agent.tamerAgent = new TamerAgent();
		System.out.println("\n\nAgent in " + this.getClass().getSimpleName() + ": " + agent);
		System.out.println("Stack: " + Arrays.toString(Thread.currentThread().getStackTrace()));

		String unique = D10LoopmazeVITamerHelper.makeUnique(args);
		agent.setUnique(this.getClass().getSimpleName() + "%" + unique);
		//agent.setRecordRew(true); // records predictions of human reward

		agent.envName = "Loop-Maze";
		agent.enableGUI = false;
		agent.tamerAgent.EP_END_PAUSE = 0;
		agent.useTamer = true;

		agent.setDiscountFactorForLearning(D10LoopmazeVITamerHelper.discParam);
		agent.timeBtwnDPSweeps = this.timeBtwnDPSweeps;
		agent.printSweeps = false;

		/*
		 * Set agent's models of the environment
		 */
		EnvWrapper envWrapper = new EnvWrapper(env);
		agent.setEnvTransModel(envWrapper.transModel);
		agent.setRewModel(envWrapper.rewModel);

		agent.initParams(agent.envName);
		setTamerAgentParams(agent.tamerAgent); // should be done before processPreInitArgs(), which might intentionally override some assignments done by this call
		agent.processPreInitArgs(args);

		return agent;
	}







	/**
	 * Create unique string for saving the log of this experiment.
	 * 
	 * @param args
	 * @return
	 */
	public static String makeUnique(String[] args) {
		return "testUnique";
	}




	public void adjustAgentAfterItsInit(String[] args, GeneralAgent agent) {
		agent.processPostInitArgs(args);
		System.out.println("args in adjust: " + Arrays.toString(args));
		if (!agent.getInTrainSess())
			agent.toggleInTrainSess(); // toggle ensures that member agents are also toggled on
		System.out.println("in training in " + this.getClass().getSimpleName() + "? " + agent.getInTrainSess());
	}

	public void processArgs(String[] args) {
		System.out.println("\n[------process pre-init args------] " + Arrays.toString(args));
		for (int i = 0; i < args.length; i++) {
			String argType = args[i];
			if (argType.equals("-makeTaskCont")){
				this.makeTaskContinuous = true;
				System.out.println("forcing task to be continuous");
			}
		}
	}


	/*
	 * Set the parameters for the RL and TAMER agents. This should be done before
	 * processPreInitArgs(), which may be used to overwrite these valuse.
	 */
	private static void setRLAgentParams(GeneralAgent agent) {
		// from Params defaults
		agent.params.initSampleValue = 0.0;
		agent.params.numBiasingSamples = 0;
		agent.params.biasSampleWt = 0.5;
		agent.params.modelAddsBiasFeat = false; // 
		agent.params.initWtsValue = 0;

	}

	public static void setTamerAgentParams(TamerAgent agent) {		
		/*
		 * Tamer params
		 */
		System.out.println(agent.params);
		agent.params.distClass = "uniform"; //// immediate, previousStep, or uniform
		agent.params.creditDelay = 0.15; // these bounds are because the typical event being reward is the action shown at the beginning of the step
		agent.params.windowSize = 0.25;
		agent.params.extrapolateFutureRew = false;
		agent.params.delayWtedIndivRew = false; 
		agent.params.noUpdateWhenNoRew = false; 

		agent.params.modelClass = "IncGDLinearModel"; // this seems to do better than KNN for preventing unwanted avoidance behaviors
		agent.params.featClass = "FeatGen_RBFs";
		agent.params.stepSize = 0.2;
		agent.params.featGenParams.put("basisFcnsPerDim", "6");
		agent.params.featGenParams.put("relWidth", "0.05");

		//		agent.params.modelClass = "WekaModelPerActionModel";
		//		agent.params.featClass = "FeatGen_NoChange";
		agent.params.selectionMethod = "greedy";

		agent.params.initModelWSamples = false;
		agent.params.numBiasingSamples = 0;
		agent.params.biasSampleWt = 0.0;
		agent.params.initSampleValue = 0.0;
		agent.params.wekaModelName = "LinearNNSearch"; //"KDTree"; //"IBk"; // using biasStrength=0.1, baselineBias=0.0, and cube root; with this biasStrength, only the linear bias towards zero is used
		agent.params.traceDecayFactor = 0.0;
	}


	public static String[] getDebugArgsStrArray() {
		String[] args = new String[2];
		args[DISC_PARAM_I - 1] = "-discountParam";
		args[DISC_PARAM_I] = 0.99 + "";
		return args;
	}




	/**
	 * The trainerUnique string can be used to specify agent parameters. One (admittedly
	 * clunky) use for this method is to give specify the experimental condition in a string 
	 * that a subject types into their browser. This string could, for instance, be provided 
	 * in Mechanical Turk instructions.
	 */
	public void processTrainerUnique(GeneralAgent agent, String trainerUnique) {
		// get discount factor
		String[] uniqueComponents = trainerUnique.split("z"); // TODO splitting params by z is limiting; PHP needs modification to allow better separators
		System.out.println("uniqueComponents: " + Arrays.toString(uniqueComponents));
		if (uniqueComponents.length >= 2) {
			String agentTypeStr = uniqueComponents[1];
			if (agentTypeStr.equals("u")) {
				this.dynProg = false;
			}
			else if (agentTypeStr.equals("d")) {
				this.dynProg = true;
			}

			String contOrEpis = uniqueComponents[2];
			if (contOrEpis.equals("c")) {
				this.makeTaskContinuous = true;
			}
			else if (contOrEpis.equals("e")) {
				this.makeTaskContinuous = false;
			}			
		}
	}




	public static void main(String[] args) {
		if (D10LoopmazeVITamerHelper.debug) {
			args = getDebugArgsStrArray();
		}

		D10LoopmazeVITamerHelper exp = new D10LoopmazeVITamerHelper();
		exp.dynProg = false;
		exp.runOneExp(args);
	}

}



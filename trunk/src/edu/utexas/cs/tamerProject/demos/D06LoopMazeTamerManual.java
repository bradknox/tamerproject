package edu.utexas.cs.tamerProject.demos;

import java.util.Arrays;

import org.rlcommunity.agents.random.RandomAgent;


import edu.utexas.cs.tamerProject.envModels.EnvWrapper;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.environments.robotarm.RobotArm;
import edu.utexas.cs.tamerProject.featGen.FeatGen_RBFs;
import edu.utexas.cs.tamerProject.featGen.FeatGenerator;
import edu.utexas.cs.tamerProject.modeling.IncGDLinearModel;
import edu.utexas.cs.tamerProject.modeling.templates.RegressionModel;
import edu.utexas.cs.tamerProject.utils.MutableDouble;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.taskspec.TaskSpecVRLGLUE3;

import rlVizLib.Environments.SampleableEnvBase;
import rlVizLib.messaging.environmentShell.TaskSpecPayload;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.applet.RLApplet;
import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.applet.TamerApplet;

/**
 * This class runs a TAMER agent. It differs from the basic demonstration
 * of a TAMER agent (D5LoopMazeTamer) in that the feature generation and 
 * agent's reward model algorithm are set in this class rather than in 
 * GeneralAgent's agent_init(). If you create your own feature generation
 * or modeling classes, this class provides a simple demonstration of how
 * you would use your classes instead.
 * 
 * If you create a new environment, you'll need to set various parameters
 * of the agent manually, as is done below. The Params class has numerous
 * parameters that are set according to the agent's envName variable, so 
 * new environments (with new names) simply use default values.
 * 
 * @author bradknox
 *
 */
public class D06LoopMazeTamerManual extends TamerApplet {
	private static final long serialVersionUID = 672112553565074878L;

	public void initPanel() {

		
		/*
		 * Instantiate the environment
		 * 
		 * Simply change the name of the environment to one imported above to 
		 * view random behavior in another environment (a couple environments
		 * may not visualize because they are experimental/unfinished code 
		 * from RL-Library. TAMER currently should work on mountain car, 
		 * cart pole, and loop maze. Tetris is a special case that requires
		 * a different approach (see edu.utexas.cs.tamerProject.demos.tetris
		 * package).
		 * 
		 */
		SampleableEnvBase env = new LoopMaze();
		TaskSpecPayload taskSpecHolder = LoopMaze.getTaskSpecPayload(LoopMaze.getDefaultParameters());
		String taskSpec = taskSpecHolder.getTaskSpec();
		TaskSpecVRLGLUE3 taskSpecObj = new TaskSpecVRLGLUE3(taskSpec);
		
		
		/*
		 * Instantiate the agent
		 */
		TamerAgent agent = new TamerAgent();


		/*
		 * Set some agent parameters
		 */
		agent.setRecordRew(false); // records predictions of human reward to file on a hard drive or on a server via PHP
		agent.setRecordLog(false); // records top-level agent's full experience to log file on a hard drive or on a server via PHP
		GeneralAgent.canWriteToFile = false; // must be set to true for writing to file
		GeneralAgent.canWriteViaPHP = false; // must be set to true for writing to PHP
		agent.envName = "Loop-Maze";	
		agent.initParams(agent.envName);
		
		
		
		/* 
		 * Manually set agent's feature generation module
		 */
		int basisFcnsPerDim = 6; //Integer.valueOf(agent.params.featGenParams.get("basisFcnsPerDim"));
		double relWidth = 0.05; //Double.valueOf(agent.params.featGenParams.get("relWidth"));
		FeatGenerator featGen = new FeatGen_RBFs(GeneralAgent.getObsIntRanges(taskSpecObj),
					GeneralAgent.getObsDoubleRanges(taskSpecObj),
					GeneralAgent.getActIntRanges(taskSpecObj),
					GeneralAgent.getActDoubleRanges(taskSpecObj),
					basisFcnsPerDim, 
					relWidth); 
		agent.featGen = featGen;
		
		if (agent.params.featGenParams.get("normMin") != null &&
				agent.params.featGenParams.get("normMax") != null)
			((FeatGen_RBFs)agent.featGen).setNormBounds(Float.valueOf(agent.params.featGenParams.get("normMin")).floatValue(),
												Float.valueOf(agent.params.featGenParams.get("normMax")).floatValue());
		
		if (agent.params.featGenParams.get("biasFeatVal") != null)
			((FeatGen_RBFs)agent.featGen).setBiasFeatPerAct(Double.valueOf(agent.params.featGenParams.get("biasFeatVal")).doubleValue());
		
		/*
		 * Manually set agent's reward-modeling or value-modeling module (the usage of which is determined 
		 * by the agent).
		 */
		RegressionModel model = new IncGDLinearModel(agent.featGen.getNumFeatures(), agent.params.stepSize, 
				agent.featGen, agent.params.initWtsValue, 
				agent.params.modelAddsBiasFeat); 
		agent.model = model;
		
		double agentDiscountFactor = 0; //always 0 for TAMER
		((IncGDLinearModel)agent.model).setEligTraceParams(agent.params.traceDecayFactor, 
				new MutableDouble(agentDiscountFactor),
				agent.params.traceType);
		
		if (agent.params.initModelWSamples)
			agent.model.biasWGenSamples(agent.params.numBiasingSamples, agent.params.initSampleValue, 
					agent.params.biasSampleWt);
		
		
		
		
		/*
		 *  If you want to change some the parameters for the agent, this is a good place 
		 *  (must be after initParams() and should be before agent_init(), which is called 
		 *  by initExp() below)
		 *  
		 *  Example: agent.params.stepSize = 0.5;
		 */
		agent.params.modelClass = "IncGDLinearModel";
		agent.params.featClass = "FeatGen_RBFs";
		agent.params.featGenParams.put("basisFcnsPerDim", "6");
		agent.params.featGenParams.put("relWidth", "0.05");
		agent.params.stepSize = 0.2;
		agent.params.featGenParams.put("biasFeatVal", "0.1");
		
		// credit assignment parameters
		agent.params.distClass = "uniform"; //// immediate, previousStep, or uniform
		agent.params.creditDelay = 0.15; // these bounds are because the typical event being reward is the action shown at the beginning of the step
		agent.params.windowSize = 0.25;
		agent.params.extrapolateFutureRew = false;

		
		
		/*
		 * Set experimental parameters
		 */
		RunLocalExperiment.stepDurInMilliSecs = 800;
		RLPanel.enableSpeedControls = true; // typically false for user studies
		RLPanel.enableSingleStepControl = false; // typically false for user studies
		
		/*
		 * Initialize and start applet panel and experiment.
		 */
		rlPanel.init(agent, env);
		this.getContentPane().add(rlPanel);
		this.rlPanel.runLocal.addObserver(this);
		rlPanel.runLocal.initExp(); // among other things, this calls agent_init()
		rlPanel.runLocal.startExp(); // among other things, this calls agent_start() and begins the progression of time steps
	}
	
}

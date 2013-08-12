package edu.utexas.cs.tamerProject.demos;

import java.util.Arrays;

import org.rlcommunity.agents.random.RandomAgent;


import edu.utexas.cs.tamerProject.envModels.EnvWrapper;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.environments.robotarm.RobotArm;
import edu.utexas.cs.tamerProject.experimentTools.LogTrainer;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.Environments.SampleableEnvBase;

import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.applet.RLApplet;
import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.applet.TamerApplet;

/**
 * This class uses a log from a previous training session with a TAMER 
 * agent to train the agent, making it start with the knowledge from the
 * experience gained in the previous session.
 * 
 * @author bradknox
 *
 */
public class D08LoopMazeTamerLearnFromTrainingLog extends TamerApplet {
	private static final long serialVersionUID = 672112553565074878L;

	public void initPanel() {

		/*
		 * Instantiate the agent
		 */
		TamerAgent agent = new TamerAgent();


		/*
		 * Set some agent parameters
		 */
		agent.setRecordRew(false); // records predictions of human reward to file
		agent.setRecordLog(false); // records top-level agent's full experience to log file
		agent.envName = "Loop-Maze";	
		agent.initParams(agent.envName);
		
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
		rlPanel.runLocal.initExp();
		
		/*
		 * Train agent from a log file of previous training (easiest to do after 
		 * initExp() above, which call agent_init()).
		 */
		String logFilePath = "./edu/utexas/cs/tamerProject/demos/expTamerLoopMaze.log";
		LogTrainer.trainOnLog(logFilePath, agent, 1);
		
		rlPanel.runLocal.startExp();
	}
	
}

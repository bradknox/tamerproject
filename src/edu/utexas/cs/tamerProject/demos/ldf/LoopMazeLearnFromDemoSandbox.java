package edu.utexas.cs.tamerProject.demos.ldf;

import java.util.Arrays;

import org.rlcommunity.agents.random.RandomAgent;
import org.rlcommunity.environments.cartpole.CartPole;
import org.rlcommunity.environments.mountaincar.MountainCar;


import edu.utexas.cs.tamerProject.envModels.EnvWrapper;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.environments.robotarm.RobotArm;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.Environments.SampleableEnvBase;

import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.imitation.ImitationAgent;
import edu.utexas.cs.tamerProject.applet.RLApplet;
import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.applet.TamerApplet;

/**
 * This class runs a simple learning-from-demonstration agent. The agent
 * is performing regression to map from states to action from the 
 * demonstrated samples. (It uses regression instead of classification
 * both because it's been used to debug TAMER algorithms, which use
 * regression, and because it roughly learns the proportion of the time
 * that the user inputs each action and then chooses the action taken
 * most often in the current state. These proportions may not add to 1 
 * exactly.) 
 * 
 * As with the TamerAgent, the space bar turns training on and off (i.e., 
 * the agent will only update its reward model when training is on). 
 * Also, the arrow keys will control the agent when training is on. Turn
 * off training to see the policy the agent has learned. This agent works
 * in the same environments as TamerAgent.
 * 
 * @author bradknox
 *
 */
public class LoopMazeLearnFromDemoSandbox extends TamerApplet {
	private static final long serialVersionUID = 672112553565074878L;

	public void initPanel() {

		/*
		 * Instantiate the agent
		 */
		ImitationAgent agent = new ImitationAgent();


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
		// agent.params.selectionMethod = "greedy";
		agent.params.selectionMethod = "vals-as-probs";
		
		
		agent.params.modelClass = "IncGDLinearModel";
		agent.params.featClass = "FeatGen_RBFs";
		agent.params.featGenParams.put("basisFcnsPerDim", "6");
		agent.params.featGenParams.put("relWidth", "0.05"); // Change the width of Guassian RBFs here
		agent.params.stepSize = 0.2;
		
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
		RunLocalExperiment.stepDurInMilliSecs = 300;
		RLPanel.enableSpeedControls = true; // typically false for user studies
		RLPanel.enableSingleStepControl = true; // typically false for user studies
		
		/*
		 * Initialize and start applet panel and experiment.
		 */
		rlPanel.init(agent, env);
		this.getContentPane().add(rlPanel);
		this.rlPanel.runLocal.addObserver(this);
		rlPanel.runLocal.initExp();
		rlPanel.runLocal.startExp();
	}
	
}

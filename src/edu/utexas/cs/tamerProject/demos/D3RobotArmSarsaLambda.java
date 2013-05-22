package edu.utexas.cs.tamerProject.demos;

import java.util.Arrays;

import org.rlcommunity.agents.random.RandomAgent;


import edu.utexas.cs.tamerProject.envModels.EnvWrapper;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.environments.robotarm.RobotArm;
import org.rlcommunity.environments.acrobot.Acrobot;
import org.rlcommunity.environments.continuousgridworld.ContinuousGridWorld;
import org.rlcommunity.environments.discretegridworld.DiscreteGridWorld;
import org.rlcommunity.environments.stochasticdiscretegridworld.StochasticDiscreteGridWorld;
import org.rlcommunity.environments.helicopter.Helicopter;
import org.rlcommunity.environments.octopus.Octopus;
import org.rlcommunity.environments.puddleworld.PuddleWorld;
import org.rlcommunity.environments.keepAway.KeepAway;
import org.rlcommunity.environments.skeleton.SkeletonEnvironment;
import org.rlcommunity.environments.hotplate.HotPlate;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import edu.utexas.cs.tamerProject.agents.GeneralAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.applet.RLApplet;
import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;

/**
 * This class shows how to run Sarsa(lambda). It should 
 * generally learn somewhat faster than the Sarsa agent.
 * 
 * @author bradknox
 *
 */
public class D3RobotArmSarsaLambda extends RLApplet {
	private static final long serialVersionUID = 672112553565074878L;

	public void initPanel() {
		
		/*
		 * Instantiate the agent
		 */
		SarsaLambdaAgent agent = new SarsaLambdaAgent();
		
		/*
		 * Set some agent parameters
		 */
		agent.setRecordRew(false); // records predictions of human reward to file
		agent.setRecordLog(false); // records top-level agent's full experience to log file		
		agent.envName = "Robot-Arm";
		
		agent.initParams(agent.envName);
		setAgentParams(agent);
		
		
		/*
		 * Instantiate the environment
		 * 
		 * Simply change the name of the environment to one imported above to 
		 * view random behavior in another environment (a couple environments
		 * may not visualize because they are experimental/unfinished code 
		 * from RL-Library.
		 */
		EnvironmentInterface env = new RobotArm(); 
		
		/*
		 * Initialize the JPanel to be used by the applet.
		 */
		rlPanel.init(agent, env);
		this.getContentPane().add(rlPanel);
		this.rlPanel.runLocal.addObserver(this);
		
		/*
		 * Add a 100 ms pause after each episode.
		 */
		RunLocalExperiment.PAUSE_DUR_AFTER_EP = 100;
		
		/*
		 * When experiment is paused by 0 key, the 1 key will increment a single
		 * time step.
		 */
		RLPanel.enableSingleStepControl = true;
		
		/*
		 * Initialize experiment, which initializes both the environment and 
		 * the agent.
		 */
		rlPanel.runLocal.initExp();

		/*
		 * Start experiment.
		 */
		rlPanel.runLocal.startExp();
	}
	
	
	/**
	 * Set parameters of the agent that differ from the default in Params.
	 * Whenever an agent will create data to be used for analysis, I recommend
	 * setting all parameters here so that a record is kept independent of 
	 * Params.
	 * 
	 * @param agent
	 */
	public static void setAgentParams(SarsaLambdaAgent agent) {			
		agent.params.modelClass = "TabularModel";
		agent.params.featClass = "FeatGen_DiscreteIndexer";
		agent.params.traceDecayFactor = 0.95; // Default is 0, which would be Sarsa (a simpler special case of Sarsa(lambda)).
		agent.params.stepSize = 0.2;
		agent.params.selectionMethod = "greedy";
		agent.params.initWtsValue = 0.0;
		
		System.out.println(agent.params);
	}
	
}

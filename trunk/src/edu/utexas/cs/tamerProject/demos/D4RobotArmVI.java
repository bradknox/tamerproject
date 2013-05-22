package edu.utexas.cs.tamerProject.demos;

import java.util.Arrays;

import org.rlcommunity.agents.random.RandomAgent;


import edu.utexas.cs.tamerProject.envModels.EnvWrapper;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.environments.robotarm.RobotArm;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.Environments.SampleableEnvBase;

import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.applet.RLApplet;

/**
 * This class runs value iteration, which learns MUCH faster than
 * either Sarsa or Sarsa(lambda) but requires a model of the environment
 * and may not run quickly enough on complex problems.
 * 
 * Value iteration will only work with environments that have discrete 
 * states and actions.
 * 
 * @author bradknox
 *
 */
public class D4RobotArmVI extends RLApplet {
	private static final long serialVersionUID = 672112553565074878L;

	public void initPanel() {

		/*
		 * Instantiate the environment (done before the agent here because 
		 * the agent uses the environment to create a module that allows it 
		 * to sample the next state and action)
		 * 
		 * Simply change the name of the environment to one imported above to 
		 * view random behavior in another environment (a couple environments
		 * may not visualize because they are experimental/unfinished code 
		 * from RL-Library.
		 * 
		 * This agent will only work with environments that have discrete 
		 * states and actions.
		 */
		SampleableEnvBase env = new RobotArm(); 
		
		

		/*
		 * Instantiate the agent
		 */
		DPAgent agent = new DPAgent();


		/*
		 * Set some agent parameters
		 */
		agent.setRecordRew(false); // records predictions of human reward to file
		agent.setRecordLog(false); // records top-level agent's full experience to log file
		agent.envName = "Loop-Maze";
		
		// TAMER could be used to provide the reward function (learned from human reward), but the pre-coded environment's reward will be used instead.
		agent.tamerAgent = new TamerAgent(); 
		agent.useTamer = false;
		
		agent.setDiscountFactorForLearning(1.0); // can be used to override the discount factor set by the environment (most episodic environments have discount factors of 1)
		agent.timeBtwnDPSweeps = 1000; // in milliseconds, interval between a single dynamic programming sweep across each state
		agent.printSweeps = false; // if true, print information about each sweep to the console
		
		// Set agent's models of the environment
		EnvWrapper envWrapper = new EnvWrapper(env);
		agent.setEnvTransModel(envWrapper.transModel);
		agent.setRewModel(envWrapper.rewModel);
	
		agent.initParams(agent.envName);
		
		
		
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

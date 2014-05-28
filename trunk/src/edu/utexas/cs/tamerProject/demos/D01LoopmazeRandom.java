package edu.utexas.cs.tamerProject.demos;

import org.rlcommunity.agents.random.RandomAgent;


import edu.utexas.cs.tamerProject.environments.cartarm.CartArm;
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
import edu.utexas.cs.tamerProject.applet.RLApplet;

/**
 * This class is the simplest demo (hence the prefix D1), 
 * a randomly acting agent in a grid world (called LoopMaze).
 * 
 * When the applet is shown, you can use the following controls:
 * - +/- change the frequency of time steps
 * - 0 pauses
 * - 2 unpauses
 * 
 * 
 * @author bradknox
 *
 */
public class D01LoopmazeRandom extends RLApplet {
	private static final long serialVersionUID = 672112553565074878L;

	public void initPanel() {
		
		/*
		 * Instantiate the agent
		 */
		AgentInterface agent = new RandomAgent();
		
		/*
		 * Instantiate the environment
		 * 
		 * Simply change the name of the environment to one imported above to 
		 * view random behavior in another environment (a couple environments
		 * may not visualize because they are experimental/unfinished code 
		 * from RL-Library.
		 * 
		 * This agent should work with any discrete state and action environment
		 * and at least a few environments with continuous state and discrete
		 * actions (e.g., mountain car, cart pole, and acrobot).
		 */
		EnvironmentInterface env = new CartArm(); 
		
		
		/*
		 * Initialize the JPanel to be used by the applet.
		 */
		rlPanel.init(agent, env);
		this.getContentPane().add(rlPanel);
		this.rlPanel.runLocal.addObserver(this);
		
		/*
		 * Initialize experiment, which initializes both the environment and 
		 * the agent.
		 * 
		 * RunLocalExperiment (which runLocal below is an instance of) controls
		 * the timing, calling RLGlue.RL_init() (which calls agent_init() and 
		 * env_init()) and step() on the TinyGlueExtended instance that calls 
		 * agent_start(), env_start(), agent_step(), and so on.
		 */
		rlPanel.runLocal.initExp();
		
		/*
		 * Start experiment. In each episode, agent_start() and env_start() are
		 * called first. Then agent_step() and env_step() are called repeatedly
		 * until the end of the episode, at which point agent_end() and env_end()
		 * are called. See TinyGlueExtended for exact ordering between agent and 
		 * env.
		 */
		rlPanel.runLocal.startExp();
	}
	
}

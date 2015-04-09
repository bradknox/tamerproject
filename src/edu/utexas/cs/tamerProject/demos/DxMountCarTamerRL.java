package edu.utexas.cs.tamerProject.demos;

import java.util.Arrays;

import org.rlcommunity.agents.random.RandomAgent;
import org.rlcommunity.environments.mountaincar.MountainCar;


import edu.utexas.cs.tamerProject.envModels.EnvWrapper;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.environments.robotarm.RobotArm;

import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.Environments.EnvironmentBase;
import rlVizLib.Environments.SampleableEnvBase;

import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;
import edu.utexas.cs.tamerProject.agents.sarsaLambda.SarsaLambdaAgent;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.tamerrl.TamerRLAgent;
import edu.utexas.cs.tamerProject.applet.RLApplet;
import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.applet.TamerApplet;

/**
 * This class runs a simultaneous TAMER+RL agent. It was created with small
 * changes to D05LoopMazeTamer.
 * 
 * In addition to the controls that worked before (when enabled), you
 * can now give reward. '/' key gives +1. 'z' key gives -1. Also, the
 * space bar turns training on and off (i.e., the agent will only update
 * its reward model when training is on). 
 *
 * When the applet is shown, you can use the following controls:
 * - +/- change the frequency of time steps
 * - 0 pauses
 * - 2 unpauses
 * 
 * @author bradknox
 *
 */
public class DxMountCarTamerRL extends TamerApplet {
	private static final long serialVersionUID = 672112553565074878L;

	public void initPanel() {

		/*
		 * Instantiate the agent
		 */
		TamerRLAgent agent = new TamerRLAgent();


		/*
		 * Set some agent parameters
		 */
		agent.setRecordRew(false); // records predictions of human reward to file
		agent.setRecordLog(false); // records top-level agent's full experience to log file
		agent.envName = "Mountain-Car";	
		agent.initParams(agent.envName);
		
		/*
		 *  If you want to change some the parameters for the agent, this is a good place 
		 *  (must be after initParams() and should be before agent_init(), which is called 
		 *  by initExp() below)
		 *  
		 *  Example: agent.params.stepSize = 0.5;
		 */
		
		
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
		EnvironmentBase env = new MountainCar(); 
		
		
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
		rlPanel.runLocal.startExp();
	}
	
}

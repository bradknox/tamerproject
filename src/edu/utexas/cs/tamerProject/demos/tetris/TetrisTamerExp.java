package edu.utexas.cs.tamerProject.demos.tetris;

import java.util.Arrays;

import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.applet.TamerApplet;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMaze;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMazeSecretPath;
import edu.utexas.cs.tamerProject.environments.loopmaze.LoopMazeState;
import edu.utexas.cs.tamerProject.experiments.GeneralExperiment;

import edu.utexas.cs.tamerProject.agents.dynamicProgramming.DPAgent;


public class TetrisTamerExp extends TamerApplet{
	
	GeneralExperiment exp;
	
	public void initPanel() {
		/*
		 * Init experiment class
		 */
		exp = new TetrisTamerExpHelper();
		
		/*
		 * Init environment
		 */
		env = exp.createEnv();
		
		/*
		 * Init agent
		 */
		String[] args = TetrisTamerExpHelper.getDebugArgsStrArray();
		agent = exp.createAgent(args, env);

		/*
		 * Set agent parameters
		 */
		agent.setAllowUserToggledTraining(false);
		agent.setRecordLog(true);
		agent.setRecordRew(true);
	
		/*
		 * Set experimental parameters
		 */
		RunLocalExperiment.stepDurInMilliSecs = 150;
		RLPanel.DISPLAY_SECONDS_FOR_TIME = true;
		RLPanel.DISPLAY_REW_THIS_EP = true;
		RLPanel.PRINT_REW_AS_INT = true;
		RLPanel.nameForRew = "Lines cleared";
		RLPanel.enableSpeedControls = true;
		RLPanel.enableSingleStepControl = false;
		
		super.initPanel();
	}
	
	
	protected void prepForStartTask(){
		prepPanelsForStartTask();
		rlPanel.runLocal.initExp();
		
		// Experiment-specific code below.
		if (exp != null) {
			exp.adjustAgentAfterItsInit(TetrisTamerExpHelper.getDebugArgsStrArray(), agent);
			exp.processTrainerUnique(agent, TetrisTamerExp.this.trainerUnique);
		}
	}
	
}
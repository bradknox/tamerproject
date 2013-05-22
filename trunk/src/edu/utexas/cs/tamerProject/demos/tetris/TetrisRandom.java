package edu.utexas.cs.tamerProject.demos.tetris;

import org.rlcommunity.agents.random.RandomAgent;
import org.rlcommunity.environments.tetris.Tetris;
import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import edu.utexas.cs.tamerProject.applet.RLApplet;
import edu.utexas.cs.tamerProject.applet.RLPanel;
import edu.utexas.cs.tamerProject.applet.RunLocalExperiment;
import edu.utexas.cs.tamerProject.environments.robotarm.RobotArm;

public class TetrisRandom extends RLApplet {
	
	public void initPanel() {
		AgentInterface agent = new RandomAgent();
		EnvironmentInterface env = new Tetris();
		
		rlPanel.init(agent, env);
		this.getContentPane().add(rlPanel);
		this.rlPanel.runLocal.addObserver(this);
		rlPanel.runLocal.initExp();
		rlPanel.runLocal.startExp();
	}
	
}

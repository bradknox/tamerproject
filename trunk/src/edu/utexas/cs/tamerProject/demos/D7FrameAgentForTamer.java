package edu.utexas.cs.tamerProject.demos;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import org.rlcommunity.rlglue.codec.AgentInterface;
import edu.utexas.cs.tamerProject.agents.tamer.TamerAgent;
import edu.utexas.cs.tamerProject.agents.GeneralAgent;


/**
 * This class demonstrates how to use a TamerAgent instance as a module rather
 * as a top-level agent. Any agent class that extends GeneralAgent can be used
 * similarly.
 * 
 * This class's extension of GeneralAgent is only to allow it to be compatible
 * with the applet environment that is used in the demo class 
 * DxLoopMazeTamerAsModule. It could instead extend AgentInterface to be generally
 * RLGlue compatible, or it could take any other form to be compatible with
 * another system such as a physical robot. Essentially, the code in agent_init()
 * should be called once at the start of execution; agent_start()'s code would be 
 * called at the beginning of a learning episode (or just after agent_init() if
 * the learning environment is continuing); agent_step() is called at each time
 * step (or update opportunity, ideally at near-regular intervals); and 
 * agent_end() is called at the end of a learning episode. 
 * 
 * @author bradknox
 *
 */
public class D7FrameAgentForTamer extends GeneralAgent {

	TamerAgent tamerModule = new TamerAgent();
	

	
	@Override
	public void agent_init(String taskSpecification) {
		/*
		 * Set some agent parameters
		 */
		tamerModule.setRecordRew(false); // records predictions of human reward to file
		tamerModule.setRecordLog(false); // records top-level tamerModule's full experience to log file
		tamerModule.envName = "Loop-Maze";	
		tamerModule.initParams(tamerModule.envName);

		tamerModule.agent_init(taskSpecification);
	}

	@Override
	public Action agent_start(Observation observation) {
		return tamerModule.agent_start(observation);
	}

	@Override
	public Action agent_step(double reward, Observation observation) {
		return tamerModule.agent_step(reward, observation);
	}

	@Override
	public void agent_end(double reward) {
		tamerModule.agent_end(reward);
	}

	@Override
	public void agent_cleanup() {
		tamerModule.agent_cleanup();
	}

	@Override
	public String agent_message(String message) {
		return tamerModule.agent_message(message);
	}

	@Override
	public Action agent_start(Observation o, double time,
			Action predeterminedAct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Action agent_step(double r, Observation o, double time,
			Action predeterminedAct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void agent_end(double r, double time) {
		// TODO Auto-generated method stub
		
	}


}

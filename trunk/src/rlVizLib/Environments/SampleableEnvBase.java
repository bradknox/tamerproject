package rlVizLib.Environments;

import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

public abstract class SampleableEnvBase extends EnvironmentBase {

   	public abstract boolean isObsLegal(Observation obs);
	public abstract boolean isObsTerminal(Observation obs);
	
	public abstract Reward_observation_terminal sampleNextObs(Observation obs, Action act);
	public abstract Reward_observation_terminal sampleStartObs(); // corresponds to environment's env_start() method
}

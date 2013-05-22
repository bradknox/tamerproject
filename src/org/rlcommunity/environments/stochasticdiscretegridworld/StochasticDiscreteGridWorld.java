/* 
 * Copyright (C) 2007, Brian Tanner
 * 
http://rl-library.googlecode.com/
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.rlcommunity.environments.stochasticdiscretegridworld;

import java.awt.geom.Point2D;
import java.util.Random;
import org.rlcommunity.environments.discretegridworld.DiscreteGridWorld;
import rlVizLib.general.ParameterHolder;
import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;
import rlVizLib.dynamicLoading.Unloadable;

/**
 * This is very much like the Continuous Grid World, but we have a discretized, 
 * labelled integer state representation instead of continuous state variables, and movement
 * is restricted to be a fixed distance in x,y giving us a more traditional 
 * discrete grid world.  
 * <p>
 * Good time to mention, this code isn't actually meant to work with world that
 * don't start at (0,0).  Positive starting positions will just falsely mean
 * more states.  Negative starting positions will break.
 * 
 * @author Brian Tanner
 */
public class StochasticDiscreteGridWorld extends DiscreteGridWorld implements Unloadable {

	double randomProb=.1;
	Random theRandom=new Random();

	public StochasticDiscreteGridWorld(){
		this(getDefaultParameters());
	}
	
	public StochasticDiscreteGridWorld(ParameterHolder theParams){
		super(theParams);
		this.randomProb=theParams.getDoubleParam("stochastic-randomprob");
	}

	public static ParameterHolder getDefaultParameters(){
		ParameterHolder p = DiscreteGridWorld.getDefaultParameters();
		p.addDoubleParam("stochastic-randomprob",.1);
		return p;

	}

	@Override
	public Reward_observation_terminal env_step(Action action) {
		int theAction = action.intArray[0];

		boolean chooseRandom=theRandom.nextDouble()<=randomProb;

		if(chooseRandom)theAction=theRandom.nextInt(4);

		double dx = 0;
		double dy = 0;

		if (theAction == 0) {
			dx = xDiscFactor;
		}
		if (theAction == 1) {
			dx = -xDiscFactor;
		}
		if (theAction == 2) {
			dy = yDiscFactor;
		}
		if (theAction == 3) {
			dy = -yDiscFactor;
		}

		Point2D nextPos = new Point2D.Double(agentPos.getX() + dx, agentPos.getY() + dy);

		nextPos = updateNextPosBecauseOfWorldBoundary(nextPos);
		nextPos = updateNextPosBecauseOfBarriers(nextPos);

		agentPos = nextPos;
		discretizeAgentPos();
		updateCurrentAgentRect();
		boolean inResetRegion = false;

		for (int i = 0; i < resetRegions.size(); i++) {
			if (resetRegions.get(i).contains(currentAgentRect)) {
				inResetRegion = true;
			}
		}

		return makeRewardObservation(getReward(), inResetRegion);
	}

}

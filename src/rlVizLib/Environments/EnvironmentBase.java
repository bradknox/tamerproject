/*
Copyright 2007 Brian Tanner
brian@tannerpages.com
http://brian.tannerpages.com

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

  
package rlVizLib.Environments;

import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.types.Observation;
import org.rlcommunity.rlglue.codec.types.Reward_observation_terminal;

public abstract class EnvironmentBase implements EnvironmentInterface {

	abstract protected Observation makeObservation();
	
	protected Reward_observation_terminal makeRewardObservation(double reward, boolean isTerminal){
		Reward_observation_terminal RO=new Reward_observation_terminal();
		RO.o=makeObservation();
		RO.r=reward;
		
		RO.terminal=1;
		if(!isTerminal)
			RO.terminal=0;

		return RO;
	}
}

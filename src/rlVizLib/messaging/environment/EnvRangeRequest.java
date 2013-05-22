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

  
package rlVizLib.messaging.environment;

import java.util.Vector;

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.interfaces.getEnvMaxMinsInterface;

public class EnvRangeRequest extends EnvironmentMessages{

	public EnvRangeRequest(GenericMessage theMessageObject) {
		super(theMessageObject);
	}

	public static EnvRangeResponse Execute(){
		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kEnv.id(),
				MessageUser.kBenchmark.id(),
				EnvMessageType.kEnvQueryVarRanges.id(),
				MessageValueType.kNone.id(),
		"NULL");

		String responseMessage=RLGlue.RL_env_message(theRequest);

		EnvRangeResponse theResponse;
		try {
			theResponse = new EnvRangeResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			System.err.println("In EnvRangeRequest, the response was not RL-Viz Compatible");
			theResponse = null;
		}

		return theResponse;

	}

	@Override
	public boolean canHandleAutomatically(Object theReceiver) {
		return (theReceiver instanceof getEnvMaxMinsInterface);
	}

	@Override
	public String handleAutomatically(EnvironmentInterface theEnvironment) {
		
		getEnvMaxMinsInterface castedEnv = (getEnvMaxMinsInterface)theEnvironment;
		//			//Handle a request for the ranges
		Vector<Double> mins = new Vector<Double>();
		Vector<Double> maxs = new Vector<Double>();

		int numVars=castedEnv.getNumVars();

		for(int i=0;i<numVars;i++){
			mins.add(castedEnv.getMinValueForQuerableVariable(i));
			maxs.add(castedEnv.getMaxValueForQuerableVariable(i));
		}

		EnvRangeResponse theResponse=new EnvRangeResponse(mins, maxs);

		return theResponse.makeStringResponse();

	}

}

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

  
package rlVizLib.messaging.agent;


import org.rlcommunity.rlglue.codec.AgentInterface;
import org.rlcommunity.rlglue.codec.RLGlue;

import rlVizLib.rlVizCore;
import rlVizLib.general.RLVizVersion;
import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;


public class AgentVersionSupportedRequest extends AgentMessages{

	public AgentVersionSupportedRequest(GenericMessage theMessageObject){
		super(theMessageObject);
	}

	public static AgentVersionSupportedResponse Execute(){
		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kAgent.id(),
				MessageUser.kBenchmark.id(),
				AgentMessageType.kAgentQuerySupportedVersion.id(),
				MessageValueType.kNone.id(),
				"NULL");

		String responseMessage=RLGlue.RL_agent_message(theRequest);

		AgentVersionSupportedResponse theResponse;
		try {
			theResponse = new AgentVersionSupportedResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			//if we didn't get back anything good from the Agent, we'll assume its supporting version 0.0 of rlViz :P
			theResponse= new AgentVersionSupportedResponse(RLVizVersion.NOVERSION);
		}
		return theResponse;
	}

	@Override
	public String handleAutomatically(AgentInterface theAgent) {
                RLVizVersion theVersion=rlVizCore.getRLVizSpecVersionOfClassWhenCompiled(theAgent.getClass());
		AgentVersionSupportedResponse theResponse=new AgentVersionSupportedResponse(theVersion);
		return theResponse.makeStringResponse();
	}

	@Override
	public boolean canHandleAutomatically(Object theAgent) {
            return true;
	}
	
	
}

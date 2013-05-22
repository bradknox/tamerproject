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

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.interfaces.HasAVisualizerInterface;

public class AgentVisualizerNameRequest extends AgentMessages{

	public AgentVisualizerNameRequest(GenericMessage theMessageObject){
		super(theMessageObject);
	}

	public static AgentVisualizerNameResponse Execute(){
		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kAgent.id(),
				MessageUser.kBenchmark.id(),
				AgentMessageType.kAgentQueryVisualizerName.id(),
				MessageValueType.kNone.id(),
				"NULL");

		String responseMessage=RLGlue.RL_agent_message(theRequest);

		AgentVisualizerNameResponse theResponse;
		try {
			theResponse = AgentVisualizerNameResponse.AgentVisualizerNameResponseFromResponseString(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			//if we didn't get back anything good from the agent, we'll assume its supporting version 0.0 of rlViz :P
			theResponse= new AgentVisualizerNameResponse("org.rlcommunity.visualizers.generic.GenericAgentVisualizer");
		}
		return theResponse;
	}

	@Override
	public String handleAutomatically(AgentInterface theAgent) {
		HasAVisualizerInterface castedAgent = (HasAVisualizerInterface)theAgent;
		AgentVisualizerNameResponse theResponse=new AgentVisualizerNameResponse(castedAgent.getVisualizerClassName());
		return theResponse.makeStringResponse();
	}

	@Override
	public boolean canHandleAutomatically(Object theAgent) {
		return (theAgent instanceof HasAVisualizerInterface);
	}
	
	
}

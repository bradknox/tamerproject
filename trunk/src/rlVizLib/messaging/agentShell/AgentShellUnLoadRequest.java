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

  
package rlVizLib.messaging.agentShell;


import org.rlcommunity.rlglue.codec.RLGlue;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;

public class AgentShellUnLoadRequest extends AgentShellMessages{
	public AgentShellUnLoadRequest(GenericMessage theMessageObject) {
		super(theMessageObject);
	}




	public static AgentShellUnLoadResponse Execute(){
		String theRequestString=AbstractMessage.makeMessage(
				MessageUser.kAgentShell.id(),
				MessageUser.kBenchmark.id(),
				AgentShellMessageType.kAgentShellUnload.id(),
				MessageValueType.kNone.id(),
				"NULL");

		String responseMessage=RLGlue.RL_agent_message(theRequestString);

		AgentShellUnLoadResponse theResponse;
		try {
			theResponse = new AgentShellUnLoadResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			System.err.println("In AgentShellUnLoadResponse: response was not an RLViz Message");
			return null;
		}	
		return theResponse;


	}
}

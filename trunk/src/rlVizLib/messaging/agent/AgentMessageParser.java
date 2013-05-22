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

import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.GenericMessageParser;
import rlVizLib.messaging.NotAnRLVizMessageException;

public class AgentMessageParser extends GenericMessageParser{
	public static AgentMessages parseMessage(String theMessage) throws NotAnRLVizMessageException{
		GenericMessage theGenericMessage = new GenericMessage(theMessage);

		int cmdId=theGenericMessage.getTheMessageType();

		if(cmdId==AgentMessageType.kAgentQueryValuesForObs.id())return new AgentValueForObsRequest(theGenericMessage);
		if(cmdId==AgentMessageType.kAgentQueryVisualizerName.id())return new AgentVisualizerNameRequest(theGenericMessage);
		if(cmdId==AgentMessageType.kAgentQuerySupportedVersion.id())return new AgentVersionSupportedRequest(theGenericMessage);
		if(cmdId==AgentMessageType.kAgentGetGraphic.id())return new AgentGraphicRequest(theGenericMessage);
                if(cmdId==AgentMessageType.kAgentCustom.id())return new AgentCustomRequest(theGenericMessage);
                
	System.out.println("AgentMessageParser - unknown query type: "+theMessage);
	Thread.dumpStack();
	System.exit(1);
	return null;
}
}

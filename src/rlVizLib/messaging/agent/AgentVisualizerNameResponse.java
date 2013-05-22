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



import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;

public class AgentVisualizerNameResponse extends AbstractResponse{
	String theVisualizerClassName=null;
	

	private void setVisualizerName(String theVisualizerClassName){
		this.theVisualizerClassName=theVisualizerClassName;
	}
	
		
	public AgentVisualizerNameResponse(String theVisualizerClassName){
		this.theVisualizerClassName=theVisualizerClassName;
	}
	

	static public AgentVisualizerNameResponse AgentVisualizerNameResponseFromResponseString(String responseMessage) throws NotAnRLVizMessageException {
		GenericMessage theGenericResponse = new GenericMessage(responseMessage);
		String theVisualizerClassName=theGenericResponse.getPayload();
		
		AgentVisualizerNameResponse theResponse=new AgentVisualizerNameResponse(theVisualizerClassName);
		return theResponse;
	}


	@Override
	public String makeStringResponse() {
		StringBuffer thePayLoadBuffer= new StringBuffer();


		thePayLoadBuffer.append(theVisualizerClassName);
		
		String theResponse=AbstractMessage.makeMessage(
				MessageUser.kBenchmark.id(),
				MessageUser.kAgent.id(),
				AgentMessageType.kAgentResponse.id(),
				MessageValueType.kStringList.id(),
				thePayLoadBuffer.toString());
		
		return theResponse;		
	
}

	public String getTheVisualizerClassName() {
		return theVisualizerClassName;
	}

};
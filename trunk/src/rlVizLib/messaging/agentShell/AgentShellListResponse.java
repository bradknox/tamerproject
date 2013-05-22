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

import java.util.StringTokenizer;
import java.util.Vector;

import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;



public class AgentShellListResponse extends AbstractResponse{
	private Vector<String> theAgentList = new Vector<String>();
	private Vector<ParameterHolder> theParamList = new Vector<ParameterHolder>();

	public AgentShellListResponse(String responseMessage) throws NotAnRLVizMessageException {
		GenericMessage theGenericResponse=new GenericMessage(responseMessage);

		String thePayLoadString=theGenericResponse.getPayload();

		StringTokenizer agentListTokenizer = new StringTokenizer(thePayLoadString, ":");

		String numAgentsToken=agentListTokenizer.nextToken();

		int numAgents=Integer.parseInt(numAgentsToken);

		for(int i=0;i<numAgents;i++){
			theAgentList.add(agentListTokenizer.nextToken());
			theParamList.add(new ParameterHolder(agentListTokenizer.nextToken()));
		}

	}


	public AgentShellListResponse(Vector<String> agentNameVector, Vector<ParameterHolder> agentParamVector) {
		this.theAgentList=agentNameVector;
		this.theParamList=agentParamVector;
	}


	@Override
	public String makeStringResponse() {

		String thePayLoadString=theAgentList.size()+":";

		for(int i=0;i<theAgentList.size();i++){
			thePayLoadString+=theAgentList.get(i)+":";
			if(theParamList.get(i)!=null)
				thePayLoadString+=theParamList.get(i).stringSerialize()+":";
			else
				thePayLoadString+="NULL:";//When we pass this into a parameter holder constructor it will just create an empty param holder
				
		}

		String theResponse=AbstractMessage.makeMessage(
				MessageUser.kBenchmark.id(),
				MessageUser.kAgentShell.id(),
				AgentShellMessageType.kAgentShellResponse.id(),
				MessageValueType.kStringList.id(),
				thePayLoadString);


		return theResponse;
	}

	public String toString() {
		String theString= "AgentShellList Response: "+theAgentList.toString();
		return theString;
	}


	public Vector<String> getTheAgentList() {
		return theAgentList;
	}
	public Vector<ParameterHolder> getTheParamList() {
		return theParamList;
	}

};
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

import java.util.StringTokenizer;
import java.util.Vector;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;



public class AgentValueForObsResponse extends AbstractResponse{
	private Vector<Double> theValues=null;
	

	public AgentValueForObsResponse(Vector<Double> theValues) {
		this.theValues=theValues;
	}




	public AgentValueForObsResponse(String responseMessage) throws NotAnRLVizMessageException {
		GenericMessage theGenericResponse = new GenericMessage(responseMessage);

		String thePayLoadString=theGenericResponse.getPayload();

		StringTokenizer valueTokenizer = new StringTokenizer(thePayLoadString, ":");
		theValues = new Vector<Double>();
		String numValuesToken=valueTokenizer.nextToken();
		int numValues=Integer.parseInt(numValuesToken);

		for(int i=0;i<numValues;i++){
			theValues.add(Double.parseDouble(valueTokenizer.nextToken()));
		}
	}




	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String theResponse="AgentValuesForOBs: " + theValues.size()+" values, they are: ";
		for(int i=0;i<theValues.size();i++){
			theResponse+=theValues.get(i)+" ";
		}
		return theResponse;
	}




	/**
	 * @return the theValues
	 */
	public Vector<Double> getTheValues() {
		return theValues;
	}

//	@Override
	
	//So, when you create on of these in an environment, this gives you the response to send
	public String makeStringResponse() {
		StringBuffer thePayLoadBuffer= new StringBuffer();

		thePayLoadBuffer.append(theValues.size());
		thePayLoadBuffer.append(":");

		
		for(int i=0;i<theValues.size();i++){
			thePayLoadBuffer.append(theValues.get(i));
			thePayLoadBuffer.append(':');
		}
		
		String theResponse=AbstractMessage.makeMessage(
				MessageUser.kAgent.id(),
				MessageUser.kBenchmark.id(),
				AgentMessageType.kAgentResponse.id(),
				MessageValueType.kStringList.id(),
				thePayLoadBuffer.toString());

		return theResponse;
	}
};
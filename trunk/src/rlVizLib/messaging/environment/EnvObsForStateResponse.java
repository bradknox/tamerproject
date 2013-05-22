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

import java.util.StringTokenizer;
import java.util.Vector;

import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.utilities.UtilityShop;

public class EnvObsForStateResponse extends AbstractResponse{
	private Vector<Observation> theObservations=null;

	
	public EnvObsForStateResponse(Vector<Observation> theObservations) {
		this.theObservations=theObservations;
	}

	
	public EnvObsForStateResponse(String responseMessage) throws NotAnRLVizMessageException {
		GenericMessage theGenericResponse = new GenericMessage(responseMessage);

		String thePayLoadString=theGenericResponse.getPayload();

		StringTokenizer obsTokenizer = new StringTokenizer(thePayLoadString, ":");

		theObservations = new Vector<Observation>();
		String numValuesToken=obsTokenizer.nextToken();
		int numValues=Integer.parseInt(numValuesToken);

		for(int i=0;i<numValues;i++){
			String thisObsString=obsTokenizer.nextToken();
			theObservations.add(UtilityShop.buildObservationFromString(thisObsString));
		}
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer bufferedResponse=new StringBuffer();

		bufferedResponse.append("EnvObsForStateResponse: " + theObservations.size()+" observations, they are: (serialized for fun)");

		for(int i=0;i<theObservations.size();i++){
			UtilityShop.serializeObservation(bufferedResponse,theObservations.get(i));
			bufferedResponse.append(" ");
		}

		return bufferedResponse.toString();
	}


	/**
	 * @return the theObservations
	 */
	public Vector<Observation> getTheObservations() {
		return theObservations;
	}

	
	//So, when you create on of these in an environment, this gives you the response to send
	public String makeStringResponse() {
		StringBuffer thePayLoadBuffer= new StringBuffer();

		//Tell them how many
		thePayLoadBuffer.append(theObservations.size());

		for(int i=0;i<theObservations.size();i++){
			thePayLoadBuffer.append(":");
			UtilityShop.serializeObservation(thePayLoadBuffer,theObservations.get(i));
		}

		String theResponse=AbstractMessage.makeMessage(
				MessageUser.kBenchmark.id(),
				MessageUser.kEnv.id(),
				EnvMessageType.kEnvResponse.id(),
				MessageValueType.kStringList.id(),
				thePayLoadBuffer.toString());

		return theResponse;
	}
};
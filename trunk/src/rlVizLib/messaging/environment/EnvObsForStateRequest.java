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

import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;
import org.rlcommunity.rlglue.codec.types.Observation;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.interfaces.getEnvObsForStateInterface;
import rlVizLib.utilities.UtilityShop;

public class EnvObsForStateRequest extends EnvironmentMessages{
	Vector<Observation> theRequestStates=new Vector<Observation>();

	public EnvObsForStateRequest(GenericMessage theMessageObject) {
		super(theMessageObject);

		String thePayLoad=super.getPayload();

		StringTokenizer obsTokenizer = new StringTokenizer(thePayLoad, ":");

		String numValuesToken=obsTokenizer.nextToken();
		int numValues=Integer.parseInt(numValuesToken);
		assert(numValues>=0);
		for(int i=0;i<numValues;i++){
			String thisObsString=obsTokenizer.nextToken();
			theRequestStates.add(UtilityShop.buildObservationFromString(thisObsString));
		}

	}

	public static EnvObsForStateResponse Execute(Vector<Observation> theQueryStates){
		StringBuffer thePayLoadBuffer= new StringBuffer();

		//Tell them how many
		thePayLoadBuffer.append(theQueryStates.size());

		for(int i=0;i<theQueryStates.size();i++){
			thePayLoadBuffer.append(":");
			UtilityShop.serializeObservation(thePayLoadBuffer,theQueryStates.get(i));
		}

		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kEnv.id(),
				MessageUser.kBenchmark.id(),
				EnvMessageType.kEnvQueryObservationsForState.id(),
				MessageValueType.kStringList.id(),
				thePayLoadBuffer.toString());




		String responseMessage=RLGlue.RL_env_message(theRequest);

		EnvObsForStateResponse theResponse;
		try {
			theResponse = new EnvObsForStateResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			System.err.println("In EnvObsForStateResponse the response was not RL-Viz compatible");
			theResponse=null;
		}

		return theResponse;

	}

	/**
	 * @return the theRequestStates
	 */
	public Vector<Observation> getTheRequestStates() {
		return theRequestStates;
	}

	@Override
	public boolean canHandleAutomatically(Object theReceiver) {
		return (theReceiver instanceof getEnvObsForStateInterface);
	}

	@Override
	public String handleAutomatically(EnvironmentInterface theEnvironment) {
		Vector<Observation> theObservations= new Vector<Observation>();
		getEnvObsForStateInterface castedEnv=(getEnvObsForStateInterface)theEnvironment;
		
		for(int i=0;i<theRequestStates.size();i++){
			Observation thisObs=castedEnv.getObservationForState(theRequestStates.get(i));
			theObservations.add(thisObs);
		}

		EnvObsForStateResponse theResponse = new EnvObsForStateResponse(theObservations);
		return theResponse.makeStringResponse();

	}

}

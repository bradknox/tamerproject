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


import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlglue.codec.EnvironmentInterface;

import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.interfaces.ReceivesRunTimeParameterHolderInterface;

public class EnvReceiveRunTimeParametersRequest extends EnvironmentMessages{
ParameterHolder theParams=null;


public EnvReceiveRunTimeParametersRequest(GenericMessage theMessageObject){
		super(theMessageObject);
		
		theParams=new ParameterHolder(super.getPayload());
	}

	public static EnvReceiveRunTimeParametersResponse Execute(ParameterHolder theParams){
		String serializedParameters=theParams.stringSerialize();
		
		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kEnv.id(),
				MessageUser.kBenchmark.id(),
				EnvMessageType.kEnvReceiveRunTimeParameters.id(),
				MessageValueType.kNone.id(),
				serializedParameters);

		String responseMessage=RLGlue.RL_env_message(theRequest);

		EnvReceiveRunTimeParametersResponse theResponse;
		try {
			theResponse = new EnvReceiveRunTimeParametersResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			return new EnvReceiveRunTimeParametersResponse(false);			
		}
		return theResponse;
	}

	@Override
	public String handleAutomatically(EnvironmentInterface theEnvironment) {
		ReceivesRunTimeParameterHolderInterface castedEnv = (ReceivesRunTimeParameterHolderInterface)theEnvironment;
		EnvReceiveRunTimeParametersResponse theResponse=new EnvReceiveRunTimeParametersResponse(castedEnv.receiveRunTimeParameters(theParams));
		return theResponse.makeStringResponse();
	}

	@Override
	public boolean canHandleAutomatically(Object theEnvironment) {
		return (theEnvironment instanceof ReceivesRunTimeParameterHolderInterface);
	}
	
	
}

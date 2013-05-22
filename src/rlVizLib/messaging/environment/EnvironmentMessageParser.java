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

import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.GenericMessageParser;
import rlVizLib.messaging.NotAnRLVizMessageException;

public class EnvironmentMessageParser extends GenericMessageParser{
	public static EnvironmentMessages parseMessage(String theMessage) throws NotAnRLVizMessageException{
		GenericMessage theGenericMessage=new GenericMessage(theMessage);

		int cmdId=theGenericMessage.getTheMessageType();

		if(cmdId==EnvMessageType.kEnvQueryVarRanges.id()) 				return new EnvRangeRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvQueryObservationsForState.id()) 	return new EnvObsForStateRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvQuerySupportedVersion.id()) 	return new EnvVersionSupportedRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvReceiveRunTimeParameters.id()) 	return new EnvReceiveRunTimeParametersRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvQueryVisualizerName.id()) 	return new EnvVisualizerNameRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvQueryEpisodeSummary.id()) 	return new EpisodeSummaryRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvCustom.id())		return new EnvCustomRequest(theGenericMessage);
		if(cmdId==EnvMessageType.kEnvGetGraphic.id())		return new EnvGraphicRequest(theGenericMessage);


		System.out.println("EnvironmentMessageParser - unknown query type: "+theMessage);
		Thread.dumpStack();
		System.exit(1);
		return null;
	}
}

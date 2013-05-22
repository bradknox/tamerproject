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

  
package rlVizLib.messaging.environmentShell;

import org.rlcommunity.rlglue.codec.RLGlue;

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;

public class EnvShellRefreshRequest extends EnvironmentShellMessages{

	public EnvShellRefreshRequest(GenericMessage theMessageObject) {
		super(theMessageObject);
	}
	
	

	public static EnvShellRefreshResponse Execute(){
		String theRequest=AbstractMessage.makeMessage(
				MessageUser.kEnvShell.id(),
				MessageUser.kBenchmark.id(),
				EnvShellMessageType.kEnvShellRefresh.id(),
				MessageValueType.kNone.id(),
				"NULL");


		String responseMessage=RLGlue.RL_env_message(theRequest);

                EnvShellRefreshResponse theResponse;
		try {
			theResponse = new EnvShellRefreshResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			System.err.println("In EnvShellRefreshRequest: response was not an RLViz Message");
			return null;
		}
                
		return theResponse;


	}
	
}


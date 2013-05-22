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

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;



public class AgentShellLoadResponse extends AbstractResponse{
//	Constructor when the Shell is responding to the load request
	boolean theResult;

	public AgentShellLoadResponse(boolean theResult){
		this.theResult=theResult;
	}
        
        public boolean getTheResult(){
            return theResult;
        }

//	Constructor when the benchmark is interpreting the returned response
	public AgentShellLoadResponse(String theMessage) throws NotAnRLVizMessageException {
		GenericMessage theGenericResponse=new GenericMessage(theMessage);

		String thePayLoadString=theGenericResponse.getPayload();

		StringTokenizer obsTokenizer = new StringTokenizer(thePayLoadString, ":");
		String loadResult=obsTokenizer.nextToken();
		String loadMessage=obsTokenizer.nextToken();
                
                theResult = true;
		if(!loadResult.equals("SUCCESS")){
                    theResult = false;
			System.err.println("Didn't load remote agentironment for reason: "+loadMessage);
		}
	}




	@Override
	public String makeStringResponse() {
		String thePayLoadString="";

		if(theResult)
			thePayLoadString+="SUCCESS:No Message";
		else
			thePayLoadString+="FAILURE:No Message";

		String theResponse=AbstractMessage.makeMessage(
				MessageUser.kBenchmark.id(),
				MessageUser.kAgentShell.id(),
				AgentShellMessageType.kAgentShellResponse.id(),
				MessageValueType.kStringList.id(),
				thePayLoadString);

		return theResponse;
		}


};
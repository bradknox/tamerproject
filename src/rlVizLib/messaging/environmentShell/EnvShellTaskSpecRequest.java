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

import java.util.StringTokenizer;

import org.rlcommunity.rlglue.codec.RLGlue;

import rlVizLib.general.ParameterHolder;
import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;

/**
 * This message takes a parameter holder to the environment and asks the environment
 * for a task spec for this parameter holder.  Environment may also provide a
 * list of errors if there is a problem with this parameterization.
 * 
 * We want to make sure that if the environment does not support this part of the 
 * protocol, there is no penalization.
 * @author btanner
 */
public class EnvShellTaskSpecRequest extends EnvironmentShellMessages{
	private String envName;
	private ParameterHolder theParams;

	public EnvShellTaskSpecRequest(GenericMessage theMessageObject) {
		super(theMessageObject);

		StringTokenizer st=new StringTokenizer(super.getPayload(),":");
		this.envName=st.nextToken();
		theParams=new ParameterHolder(st.nextToken());
	}



	public ParameterHolder getTheParams() {
		return theParams;
	}


	//This is intended for debugging but works well to be just called to save code duplication
	public static String getRequestMessage(String envName, ParameterHolder theParams){

		String paramString="NULL";
		if(theParams!=null)paramString=theParams.stringSerialize();

		String payLoadString=envName+":"+paramString;

		return AbstractMessage.makeMessage(
				MessageUser.kEnvShell.id(),
				MessageUser.kBenchmark.id(),
				EnvShellMessageType.kEnvShellTaskspec.id(),
				MessageValueType.kString.id(),
				payLoadString);

	}

        public static EnvShellTaskSpecResponse Execute(String envName, ParameterHolder theParams){
		String theRequestString=getRequestMessage(envName,theParams);

		String responseMessage=RLGlue.RL_env_message(theRequestString);

		EnvShellTaskSpecResponse theResponse;
		try {
			theResponse = new EnvShellTaskSpecResponse(responseMessage);
		} catch (NotAnRLVizMessageException e) {
			System.err.println("In EnvShellLoadRequest: response was not an RLViz Message");
			return null;
                }       return theResponse;


	}

	public String getEnvName() {
		return envName;
	}
	
	public ParameterHolder getParameterHolder(){
		return theParams;
	}

}

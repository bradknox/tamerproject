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

import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.GenericMessageParser;
import rlVizLib.messaging.NotAnRLVizMessageException;

public class EnvironmentShellMessageParser extends GenericMessageParser{

	public static EnvironmentShellMessages parseMessage(String theMessage) throws NotAnRLVizMessageException{
		GenericMessage theGenericMessage=new GenericMessage(theMessage);
		return EnvironmentShellMessageParser.makeMessage(theGenericMessage);
	}

	public static EnvironmentShellMessages makeMessage(GenericMessage theGenericMessage) {
		int cmdId=theGenericMessage.getTheMessageType();
		if(cmdId==EnvShellMessageType.kEnvShellListQuery.id()) 				return new EnvShellListRequest(theGenericMessage);
		if(cmdId==EnvShellMessageType.kEnvShellLoad.id()) 				return new EnvShellLoadRequest(theGenericMessage);
		if(cmdId==EnvShellMessageType.kEnvShellUnLoad.id()) 				return new EnvShellUnLoadRequest(theGenericMessage);
                if(cmdId==EnvShellMessageType.kEnvShellRefresh.id()) 				return new EnvShellRefreshRequest(theGenericMessage);
                if(cmdId==EnvShellMessageType.kEnvShellTaskspec.id()) 				return new EnvShellTaskSpecRequest(theGenericMessage);

		System.out.println("EnvironmentShellMessageParser - unknown query type: "+cmdId);
		Thread.dumpStack();
		System.exit(1);
		return null;
	}
}

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

  
package rlVizLib.messaging;

import java.util.StringTokenizer;

public class GenericMessageParser {

		public static MessageUser parseUser(String userChunk){
			StringTokenizer tok=new StringTokenizer(userChunk,"=");
			tok.nextToken();
			String theUserString=tok.nextToken();
			
			int theIntValue=Integer.parseInt(theUserString);
			
			if(theIntValue==MessageUser.kAgent.id())
				return MessageUser.kAgent;
			if(theIntValue==MessageUser.kEnv.id())
				return MessageUser.kEnv;
			if(theIntValue==MessageUser.kAgentShell.id())
				return MessageUser.kAgentShell;
			if(theIntValue==MessageUser.kBenchmark.id())
				return MessageUser.kBenchmark;
			if(theIntValue==MessageUser.kEnvShell.id())
				return MessageUser.kEnvShell;

			Thread.dumpStack();
			return null;
			
		}

		public static int parseInt(String typeString) {
			StringTokenizer typeTokenizer=new StringTokenizer(typeString,"=");
			typeTokenizer.nextToken();
			String theCMD=typeTokenizer.nextToken();
			int theCMDInt=Integer.parseInt(theCMD);
			return theCMDInt;
		}

		public static MessageValueType parseValueType(String typeString) {
			StringTokenizer typeTokenizer=new StringTokenizer(typeString,"=");
			typeTokenizer.nextToken();
			String theValueTypeString=typeTokenizer.nextToken();
			int theValueType=Integer.parseInt(theValueTypeString);

			if(theValueType==MessageValueType.kStringList.id())
				return MessageValueType.kStringList;
			if(theValueType==MessageValueType.kString.id())
				return MessageValueType.kString;
			if(theValueType==MessageValueType.kBoolean.id())
				return MessageValueType.kBoolean;
			if(theValueType==MessageValueType.kNone.id())
				return MessageValueType.kNone;

			System.out.println("Unknown Value type: "+theValueType);
			Thread.dumpStack();
			System.exit(1);
			return null;
		}
                
                /**
                 * @deprecate
                 * @param payloadString
                 * @return
                 */
		public static String parsePayLoad(String payloadString) {
                    return parsePayload(payloadString);
                }
                
                public static String parsePayload(String payloadString) {
                        int firstEqualPos=payloadString.indexOf("=");
                        String payload=payloadString.substring(firstEqualPos+1);


			return payload;
		}
}

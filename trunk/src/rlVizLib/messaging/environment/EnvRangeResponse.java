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

import rlVizLib.messaging.AbstractMessage;
import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;



public class EnvRangeResponse extends AbstractResponse{
	private Vector<Double> mins=null;
	private Vector<Double> maxs=null;
	
	public EnvRangeResponse(Vector<Double> mins, Vector<Double> maxs){
		this.mins=mins;
		this.maxs=maxs;
	}

	public EnvRangeResponse(String responseMessage) throws NotAnRLVizMessageException {

		GenericMessage theGenericResponse = new GenericMessage(responseMessage);

		String thePayLoadString=theGenericResponse.getPayload();

		StringTokenizer obsTokenizer = new StringTokenizer(thePayLoadString, ":");

		String numValuesToken=obsTokenizer.nextToken();
		int numValues=Integer.parseInt(numValuesToken);


		mins=new Vector<Double>();
		maxs=new Vector<Double>();

		for(int i=0;i<numValues;i++){
			mins.add(Double.parseDouble(obsTokenizer.nextToken()));
			maxs.add(Double.parseDouble(obsTokenizer.nextToken()));
		}

	}

	public String toString() {
		String theResponse="EnvRangeResponse: " + mins.size()+" variables, they are:";
		for(int i=0;i<mins.size();i++){
			theResponse+=" ("+mins.get(i)+","+maxs.get(i)+") ";
		}
		// TODO Auto-generated method stub
		return theResponse;
	}

	public Vector<Double> getMins() {
		return mins;
	}

	public Vector<Double> getMaxs() {
		return maxs;
	}

	@Override
	public String makeStringResponse() {

		String thePayLoadString=mins.size()+":";
		
		for(int i=0;i<mins.size();i++){
			thePayLoadString+=mins.get(i)+":"+maxs.get(i)+":";
		}
		
		String theResponse=AbstractMessage.makeMessage(
				MessageUser.kBenchmark.id(),
				MessageUser.kEnv.id(),
				EnvMessageType.kEnvResponse.id(),
				MessageValueType.kStringList.id(),
				thePayLoadString);
		
		return theResponse;
	
		
	}
};
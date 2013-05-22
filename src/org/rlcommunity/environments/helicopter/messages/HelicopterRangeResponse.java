/* Helicopter Domain Visualizer Resources for RL - Competition 
* Copyright (C) 2007, Brian Tanner
* 
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
* 
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA. */
package org.rlcommunity.environments.helicopter.messages;

import java.util.StringTokenizer;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;

public class HelicopterRangeResponse extends AbstractResponse{
	double [] mins = new double[13];
	double [] maxs = new double[13];

	public HelicopterRangeResponse(double[] mins, double[] maxs){
		this.mins = mins;
		this.maxs = maxs;
	}
	
	public HelicopterRangeResponse(String responseMessage)throws NotAnRLVizMessageException{
		
		GenericMessage theGenericResponse;

        	theGenericResponse = new GenericMessage(responseMessage);
			
		String thePayLoadString=theGenericResponse.getPayLoad();
		
		StringTokenizer stateTokenizer = new StringTokenizer(thePayLoadString, ":");
		//this.something = Integer.parseInt(stateTokenizer.nextToken());
		for(int i=0; i< mins.length; i++){
			mins[i] = Double.parseDouble(stateTokenizer.nextToken());	
		}
		for(int i=0; i< maxs.length; i++){
			maxs[i] = Double.parseDouble(stateTokenizer.nextToken());	
		}
	}


	@Override
	public String makeStringResponse() {
		StringBuffer theResponseBuffer= new StringBuffer();
		theResponseBuffer.append("TO=");
		theResponseBuffer.append(MessageUser.kBenchmark.id());
		theResponseBuffer.append(" FROM=");
		theResponseBuffer.append(MessageUser.kEnv.id());
		theResponseBuffer.append(" CMD=");
		theResponseBuffer.append(EnvMessageType.kEnvResponse.id());
		theResponseBuffer.append(" VALTYPE=");
		theResponseBuffer.append(MessageValueType.kStringList.id());
		theResponseBuffer.append(" VALS=");

		//theResponseBuffer.append(this.something);
		//theResponseBuffer.append(":");
		for(int i=0; i< mins.length; i++){
			theResponseBuffer.append(mins[i]);
			theResponseBuffer.append(":");
		}
		for(int i=0; i< maxs.length; i++){
			theResponseBuffer.append(maxs[i]);
			theResponseBuffer.append(":");
		}	

		return theResponseBuffer.toString();
	}

	public double [] getMins(){
		return mins;
	}
	public double [] getMaxs(){
		return maxs;
	}
	public double getMinAt(int i){
		if((i>=0)&&(i<mins.length))
		return mins[i];
		return Double.NEGATIVE_INFINITY;
	}
	
	public double getMaxAt(int i){
		if((i>=0)&&(i<maxs.length))
		return maxs[i];
		return Double.POSITIVE_INFINITY;
	}
	
}

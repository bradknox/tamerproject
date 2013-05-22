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


package org.rlcommunity.rlviz.app.gluestepper;

import org.rlcommunity.rlviz.app.RLGlueLogic;

public class GlueStepper{
	int timeStepDelay=100;
	boolean running=false;

	RLGlueLogic theGlueLogic=null;
	GlueRunner theGlueRunner=null;

	public GlueStepper(RLGlueLogic theGlueLogic){
		this.theGlueLogic=theGlueLogic;
	}
	public void setNewStepDelay(int stepDelay) {
		this.timeStepDelay=stepDelay;
		if(running)start();
	}

	public void start() {
		if(running)
			stop();
		running=true;

		//If time is the minimum we want to do something different
//		if(timeStepDelay==1)
//			theGlueRunner=new NoDelayGlueRunner(theGlueLogic);
//		else
			theGlueRunner=new FixedIntervalGlueRunner(theGlueLogic,timeStepDelay);

		theGlueRunner.start();
	}

	public void stop() {
		if(theGlueRunner!=null)theGlueRunner.stop();
		theGlueRunner=null;

		running=false;

	}
}
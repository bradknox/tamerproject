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

public class NoDelayGlueRunner implements GlueRunner,Runnable {
	RLGlueLogic theGlueLogic=null;
	Thread theThread=null;
	
	volatile boolean shouldDie=false;
	
	public NoDelayGlueRunner(RLGlueLogic theGlueLogic){
		this.theGlueLogic=theGlueLogic;	
	}

	public void start() {
		theThread=new Thread(this);
		theThread.start();

	}

	public void stop() {
		shouldDie=true;
		try {
			theThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while(!shouldDie)theGlueLogic.step();
	}

}

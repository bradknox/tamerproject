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

import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

import org.rlcommunity.rlviz.app.RLGlueLogic;

public class FixedIntervalGlueRunner implements GlueRunner {

    Timer currentTimer = null;
    int timeStepDelay = 100;
    boolean running = false;
    RLGlueLogic theGlueLogic = null;

    public FixedIntervalGlueRunner(RLGlueLogic theGlueLogic, int timeStepDelay) {
        this.theGlueLogic = theGlueLogic;
        this.timeStepDelay = timeStepDelay;
    }

    public void start() {
        currentTimer = new Timer();

        currentTimer.schedule(new TimerTask() {

            public void run() {
                theGlueLogic.step();
            }
		}, new Date(), timeStepDelay);

    }

    public void stop() {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }
    }
}

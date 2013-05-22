/*
 * Copyright 2008 Brian Tanner
 * http://bt-recordbook.googlecode.com/
 * brian@tannerpages.com
 * http://brian.tannerpages.com
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.rlcommunity.rlviz.app;

import org.rlcommunity.rlglue.codec.LocalGlue;
import org.rlcommunity.rlglue.codec.RLGlue;
import org.rlcommunity.rlviz.agentshell.AgentShell;
import org.rlcommunity.rlviz.environmentshell.EnvironmentShell;
import org.rlcommunity.rlviz.settings.RLVizSettings;

import rlVizLib.general.ParameterHolder;

/**
 *
 * @author Brian Tanner
 */
public class AppSetup {

    static void setup(String[] args) {
         RLVizSettings.initializeSettings(args);
        RLVizSettings.addNewParameters(getSettings());

        if (RLVizSettings.getBooleanSetting("local-glue")) {
//            assert RLVizSettings.getBooleanSetting("list-environments") && RLVizSettings.getBooleanSetting("list-agents") : "If using local glue, must specify list-environments and list-agents.";
            RLVizSettings.overrideBooleanParameter("list-agents", true);
            RLVizSettings.overrideBooleanParameter("list-environments", true);
            RLVizSettings.addNewParameters(EnvironmentShell.getSettings());
            RLVizSettings.addNewParameters(AgentShell.getSettings());
            EnvironmentShell E = new EnvironmentShell();
            AgentShell A = new AgentShell();
            RLGlue.setGlue(new LocalGlue(E, A));
        } else {
            //If we're local we can trust the shells to set where to find the visualizers.  Otherwise, we need to do it.
            if (RLVizSettings.isStringParamSet("agent-environment-jar-path")) {
                RLVizSettings.overrideStringSetting("environment-jar-path", RLVizSettings.getStringSetting("agent-environment-jar-path"));
                RLVizSettings.overrideStringSetting("agent-jar-path", RLVizSettings.getStringSetting("agent-environment-jar-path"));
            }

        }
   }
    private static ParameterHolder getSettings() {
        ParameterHolder AppSettings = new ParameterHolder();
        AppSettings.addBooleanParam("list-agents", false);
        AppSettings.addBooleanParam("list-environments", false);
        AppSettings.addBooleanParam("local-glue", false);
        AppSettings.addBooleanParam("agent-viz", false);
        AppSettings.addBooleanParam("env-viz", false);
        AppSettings.addStringParam("agent-environment-jar-path");
        AppSettings.addStringParam("agent-jar-path");
        AppSettings.addStringParam("environment-jar-path");
        AppSettings.addIntegerParam("default-speed",50);
        AppSettings.addBooleanParam("speed-frozen",false);
        AppSettings.addBooleanParam("autoload",false);
        AppSettings.addBooleanParam("unload-disabled",false);
        return AppSettings;
    }

}

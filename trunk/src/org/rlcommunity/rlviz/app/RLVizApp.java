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
package org.rlcommunity.rlviz.app;

import org.rlcommunity.rlviz.app.frames.RLVizFrame;
import java.io.IOException;

import javax.swing.UIManager;


import org.rlcommunity.rlviz.settings.RLVizSettings;

/**
 * @author btanner
 */
public class RLVizApp {


    public static void main(String[] args) throws IOException {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
        }
        
        AppSetup.setup(args);

        new RLVizFrame(RLVizSettings.getBooleanSetting("env-viz"), RLVizSettings.getBooleanSetting("agent-viz"));
    }
}

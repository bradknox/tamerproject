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


package org.rlcommunity.rlviz.agentshell;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.rlcommunity.rlviz.settings.RLVizSettings;

/**
 *
 * @author mradkie
 */
public class AgentShellPreferences {

    private static AgentShellPreferences ourInstance = new AgentShellPreferences();
    private Vector<URI> agentURIList = new Vector<URI>();
    private String jniLoaderLibDir = null;
    
    public static AgentShellPreferences getInstance() {
        return ourInstance;
    }

    /**
     * Set's a default path to the same place as where this jar be livin'
     */
    private AgentShellPreferences() {
        //By default assume this the jniLoader is in the same directory as the AgentShell Jar
        try {
            URL thisJarUrl = this.getClass().getProtectionDomain().getCodeSource().getLocation();
           jniLoaderLibDir = new File(thisJarUrl.toURI()).getParent();
        } catch (URISyntaxException ex) {
            Logger.getLogger(AgentShellPreferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addToList(URI theURI){
        this.agentURIList.add(theURI);
    }
    public Vector<URI> getList(){
        if(this.agentURIList.isEmpty()){
//                agentLocationList.add(new File(jniLoaderLibDir).toURI());
//                agentLocationList.add(new File("../../rl-library/system/dist/").toURI());
                  String agentPath=RLVizSettings.getStringSetting("agent-jar-path");
                  agentURIList.add(new File(agentPath).toURI());
        }
        return this.agentURIList;
    }
    public String getJNILoaderLibDir(){
        return this.jniLoaderLibDir;
    }
    public void setJNILoaderLibDir(String path){
        this.jniLoaderLibDir = path;
    }
}

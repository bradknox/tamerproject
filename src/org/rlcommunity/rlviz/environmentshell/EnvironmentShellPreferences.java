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


package org.rlcommunity.rlviz.environmentshell;

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
public class EnvironmentShellPreferences {

    private static EnvironmentShellPreferences ourInstance = new EnvironmentShellPreferences();
    private Vector<URI> envURIList = new Vector<URI>();
    private String jniLoaderLibDir = null;
    
    public static EnvironmentShellPreferences getInstance() {
        return ourInstance;
    }

    /**
     * Set's a default path to the same place as where this jar be livin'
     */
    private EnvironmentShellPreferences() {
        try {
            URL thisJarUrl = this.getClass().getProtectionDomain().getCodeSource().getLocation();
            jniLoaderLibDir = new File(thisJarUrl.toURI()).getParent();
        } catch (URISyntaxException ex) {
            Logger.getLogger(EnvironmentShellPreferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addToList(URI theURI){
        this.envURIList.add(theURI);
    }
    public Vector<URI> getList(){
        if(this.envURIList.isEmpty()){
//                envUriList.add(new File(jniLoaderLibDir).toURI());
  //              envUriList.add(new File("/Users/mradkie/competition/rlcomplibrary/libraries/envJars/").toURI());
//                envUriList.add(new File("../../rlcomplibrary/libraries/envJars/").toURI());
  //              envUriList.add(new File("../../rl-library/system/dist/").toURI());
                  String envPath=RLVizSettings.getStringSetting("environment-jar-path");
                  envURIList.add(new File(envPath).toURI());
        }
        return this.envURIList;
    }
    public String getJNILoaderLibDir(){
        return this.jniLoaderLibDir;
    }
    public void setJNILoaderLibDir(String path){
        this.jniLoaderLibDir = path;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rlcommunity.rlviz.environmentshell;

import java.net.URI;
import org.rlcommunity.rlviz.dynamicloading.SharedLibraryContentFilter;
import org.rlcommunity.rlviz.settings.RLVizSettings;

/**
 *
 * @author btanner
 */
public class JNIEnvironmentSharedLibraryContentFilter implements SharedLibraryContentFilter {
    public native int JNIvalidEnv(String path, boolean verbose);

    public boolean accept(URI theLibraryURI) {
        boolean printVerbose=false;
        printVerbose=RLVizSettings.getBooleanSetting("envshell-verbose-loading");
        int errorCode = JNIvalidEnv(theLibraryURI.getPath(), printVerbose);
        if (errorCode == 0) {
            return true;
        }

        return false;
    }
}

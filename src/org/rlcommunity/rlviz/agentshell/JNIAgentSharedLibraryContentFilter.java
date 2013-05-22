/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rlcommunity.rlviz.agentshell;

import java.net.URI;
import org.rlcommunity.rlviz.dynamicloading.SharedLibraryContentFilter;

/**
 *
 * @author btanner
 */
public class JNIAgentSharedLibraryContentFilter implements SharedLibraryContentFilter {

    private boolean debug = false;

    public native int JNIvalidAgent(String path, boolean verbose);

    public boolean accept(URI theLibraryURI) {
        int errorCode = JNIvalidAgent(theLibraryURI.getPath(), debug);
        if (errorCode == 0) {
            return true;
        }

        return false;
    }
}

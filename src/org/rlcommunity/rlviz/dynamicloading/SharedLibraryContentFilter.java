/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.rlviz.dynamicloading;

import java.net.URI;

/**
 *
 * @author btanner
 */
public interface SharedLibraryContentFilter {
    public boolean accept(URI theLibraryURI);
}

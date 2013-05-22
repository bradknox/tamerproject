/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.utexas.cs.tamerProject.environments.loopmaze;

import java.util.Vector;

/**
 *
 * @author btanner
 */
public class ZoneGen {

    public static Vector<Zone> makeZones() {
        Vector<Zone> theZones = new Vector<Zone>();
        theZones.add(new Zone(.1, .75, .45, .75, .1));
        theZones.add(new Zone(.45, .4, .45, .8, .1));
        return theZones;
    }
}

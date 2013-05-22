/*
 * (c) 2009 Marc G. Bellemare.
 */

package org.rlcommunity.environments.continuousgridworld.observationmapping;

import org.rlcommunity.rlglue.codec.types.Observation;

/** A mapping encapsulating another mapping, which first scales the observations
 *   to be in [0,1] before passing them to the encapsulated mapping. The result
 *   is in the same scale as what was provided, e.g. scaling is only applied so
 *   that the data falls within the [0,1] range for the encapsulated mapping.
 *
 * @author Marc G. Bellemare (mg17 at cs ualberta ca)
 */
public class ScaledMapping implements ObservationMapping {
    protected final double[] aMins;
    protected final double[] aMaxes;

    protected ObservationMapping aMapping;

    public ScaledMapping(ObservationMapping pMap, double[] pMins, double[] pMaxes) {
        aMins = pMins;
        aMaxes = pMaxes;
        aMapping = pMap;
    }

    public Observation map(Observation pOriginal) {
        // Scale the observation
        Observation obs = new Observation(pOriginal);

        // Scale the observation vector to be in [0,1]^d
        for (int i = 0; i < obs.doubleArray.length; i++) {
            double v = pOriginal.doubleArray[i];
            obs.doubleArray[i] = (v - aMins[i]) / (aMaxes[i] - aMins[i]);
        }

        // Call the underlying map on the scaled observation
        Observation result = aMapping.map(obs);

        // Now un-scale it
        for (int i = 0; i < obs.doubleArray.length; i++) {
            double v = result.doubleArray[i];
            obs.doubleArray[i] = v * (aMaxes[i] - aMins[i]) + aMins[i];
        }

        return obs;
    }
}

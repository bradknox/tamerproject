/*
 * (c) 2009 Marc G. Bellemare.
 */

package org.rlcommunity.environments.continuousgridworld.observationmapping;

import org.rlcommunity.rlglue.codec.types.Observation;

/**
 *
 * @author Marc G. Bellemare (mg17 at cs ualberta ca)
 */
public interface ObservationMapping {

    public Observation map(Observation pOriginal);
}

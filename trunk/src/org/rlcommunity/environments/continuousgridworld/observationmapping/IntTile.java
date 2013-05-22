/*
 * (c) 2009 Marc G. Bellemare.
 */

package org.rlcommunity.environments.continuousgridworld.observationmapping;


/**
 * Defines a tile (a portion of the observation space) to be used with
 *   TiledObservationMapping.
 *
 * @author Marc G. Bellemare (mg17 at cs ualberta ca)
 */
public class IntTile {
    /** The set of coordinates this tile corresponds to */
    private int[] aCoordinates;
    /** The slope of the tile, in each dimension */
    private boolean[] aSlopes;

    public IntTile(int[] pCoordinates, boolean[] pSlopes) {
        aCoordinates = pCoordinates;
        aSlopes = pSlopes;
    }

    /** Maps a point in [0,1]^d, where d is the number of dimensions, to the
     *   point corresponding to it in this tile.
     *
     * @param pPoint A d-dimensional point in [0,c]^d
     * @return The mapped point, in [0,c]^d where c is the number of cells
     *   per dimension
     */
    public double[] map(double[] pPoint) {
        double[] result = new double[pPoint.length];

        for (int d = 0; d < result.length; d++) {
            int low = aCoordinates[d];
            int high = aCoordinates[d]+1;

            double alpha;

            // if slope is true, then the tile spans from c[i] to c[i]+1, where
            //  c[i] is our coordinate
            if (aSlopes[d]) {
                alpha = pPoint[d];
            }
            // Otherwise, the tile spans from c[i] + 1 to c[i]
            else {
                alpha = 1.0 - pPoint[d];
            }

            assert(alpha >= 0 && alpha <= 1.0);

            // Interpolate to find the new coordinate
            result[d] = high * alpha + low * (1.0 - alpha);
        }

        return result;
    }
}

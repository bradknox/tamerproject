/*
 * (c) 2009 Marc G. Bellemare.
 */

package org.rlcommunity.environments.continuousgridworld.observationmapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;
import org.rlcommunity.rlglue.codec.types.Observation;

/**
 *
 * @author Marc G. Bellemare (mg17 at cs ualberta ca)
 */
public class TiledObservationMapping implements ObservationMapping {
    private boolean debugPrint = false;

    protected int aNumCells;
    protected int aNumDims;

    /** The mapping itself, from a vector of tile coordinates to a new vector
     *    of signed coordinates (see below for the exact meaning)
     */
    protected HashMap<Vector<Integer>,IntTile> aMapping;
    
    /** Creates a new discontinuous tiling with the given number of cells in
     *   each dimension. Cells are assigned randomly based on the Random
     *   object.
     *
     * @param pNumCells The number of cells per dimension.
     * @param pRandom A random number generator.
     */
    public TiledObservationMapping(int pNumDims, int pNumCells, Random pRandom) {
        aNumDims = pNumDims;
        aNumCells = pNumCells;

        aMapping = new HashMap<Vector<Integer>,IntTile>();
        generateMapping(pRandom);
    }

    /** Generates a mapping from the given Random generator. Assumes that
     *   aNumDims and aNumCells have been set.
     *
     * @param pRandom
     */
    private void generateMapping(Random pRandom) {
        // Create a new vector which we will fill with coordinates
        int[] input = new int[aNumDims];
        int[] output = new int[aNumDims];

        recurseGenerate(input, output, 0, pRandom);
    }

    /** A simple recursion for generating mappings. It does not generate the
     *   nastiest of mappings.
     *
     * @param pInput
     * @param pOutput
     * @param pDim The dimension along which to partition
     * @param pRandom
     */
    private void recurseGenerate(int[] pInput, int[] pOutput, int pDim,
            Random pRandom) {
        // Generate a permutation of 1..c, where c is the number of cells
        int[] pi = permute(aNumCells, pRandom);

        for (int i = 0; i < aNumCells; i++) {
            pInput[pDim] = i;
            pOutput[pDim] = pi[i];

            if (pDim + 1 < aNumDims)
                recurseGenerate(pInput, pOutput, pDim + 1, pRandom);
            else {
                // add this mapping
                Vector<Integer> key = new Vector<Integer>(aNumDims);
                int[] value = new int[aNumDims];
                boolean[] slopes = new boolean[aNumDims];
                
                for (int j = 0; j < aNumDims; j++) {
                    key.add(j, pInput[j]);
                    value[j] = pOutput[j];
                    // Also randomly generate a slope for this coordinate
                    slopes[j] = pRandom.nextBoolean();
                }
                
                aMapping.put(key, new IntTile(value, slopes));
            }
        }
    }

    private static int[] privatePermutation;

    /* Generates a uniformly distributed permutation over n elements
     *  For efficiency the returned array should NOT be stored (it will be
     *  reused).

     * @param n The number of elements
     * @param rand A random number generator
     */
    private static int[] permute(int n, Random rand) {
        if (privatePermutation == null || privatePermutation.length != n) {
            privatePermutation = new int[n];
        }

        // Initialize to the first permutation
        for (int i = 0; i < n; i++)
            privatePermutation[i] = i;

        for (int i = 0; i < n; i++) {
            // Swap element 'i' with element 'pos'
            int pos = rand.nextInt(n-i) + i;

            int tmp = privatePermutation[i];
            privatePermutation[i] = privatePermutation[pos];
            privatePermutation[pos] = tmp;
        }

        return privatePermutation;
    }

    
    /** Maps a given observation (with values in range [0,1]) to a 'tiled'
     *    observation (where things are re-mapped in a discontinuous, piecewise
     *    linear fashion.
     *
     * @param pOriginal The original (continuous) observation.
     * @return The remapped observation.
     */
    public Observation map(Observation pOriginal) {
        if (debugPrint) System.out.println ("1");

        // No integers allowed here
        assert (pOriginal.intArray.length == 0);

        if (debugPrint) System.out.println ("2");

        // Step 1: retrieve the IntTile corresponding to our point
        double[] inputPoint = pOriginal.doubleArray;
        int numDims = inputPoint.length;

        if (debugPrint) System.out.println ("3");

        // Create a vector of integer coordinates to retrieve the IntTile
        Vector<Integer> intPoint = new Vector<Integer>(numDims);
        // The alphas will be the remainder of the integer coordinates
        double[] alphas = new double[numDims];

        if (debugPrint) System.out.println ("4");
        for (int i = 0; i < numDims; i++) {
            // Scale the coordinate up to fall in [0,numCells]
            double scaled = inputPoint[i] * aNumCells;
            
            // Truncate the original point
            int trunc = (int)(scaled);
            // Keep the remainder
            alphas[i] = scaled - trunc;
            
            if (debugPrint) System.out.println ("5."+i);
            intPoint.add(i, new Integer(trunc));
            // While we're at it, compute the coordinates of the point within
            //  the tile (s.t. the coordinates are in [0,1]^d)
        }

        if (debugPrint) System.out.println ("From: "+Arrays.toString(inputPoint));
        if (debugPrint) System.out.println ("My vector is: "+intPoint);
        IntTile theTile = aMapping.get(intPoint);

        if (debugPrint) System.out.println ("6: "+theTile);

        // Step 2: map the point using the alpha vector
        double[] newPoint = theTile.map(alphas);
        // newPoint is now a vector in [0,c]^d, scale it back

        for (int i = 0; i < newPoint.length; i++) {
            newPoint[i] /= aNumCells;
        }

        if (debugPrint) System.out.println ("EOF");
        Observation mappedObservation = new Observation(0, newPoint.length);
        mappedObservation.doubleArray = newPoint;

        return mappedObservation;
    }
}

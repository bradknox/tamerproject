//Code stolen from here: http://warp.cpsc.ucalgary.ca/Software/SimKit/Documentation/

package rlVizLib.utilities.random;

import java.lang.Math;

/************************************************************
 * The Exponential class is used to generate random 
 * variables from an exponential distribution.
 * <br>
 * The Exponential distribution is often used to model
 * inter-arrival times in a simulation.
 * <br><br>
 * This is a continuous distribution.
 * <br><br>
 * <b>Tests Performed</b><br>
 * 1000 samples were generated and the means and variances
 * were examined.  Subjectively, they seemed correct.
 * A goodness of fit test was performed with 100 samples
 * and 10 intervals.  It succeeded about 19/20 times.
 * 
 * @version 1.96
 * @author Juraj Pivovarov
 ************************************************************/

public class Exponential extends RandomNumber {
    private double fMean;
    
    /************************************************************
     * The Exponential constructor initializes the exponential
     * distribution by setting the distribution's mean.
     * @param mean The mean of the exponential distribution.
     * The mean alone characterizes the exponential distribution.
     ************************************************************/

    public Exponential(double mean) {
	fMean = mean;
    }
    
    // Random number sampling functions

    /************************************************************
     * Generate a random variable, a double, from the exponential 
     * distribution.
     * @return The double representing a random draw from the
     * exponential distribution.
     ************************************************************/

    public double sampleDouble() {
	return (-fMean * Math.log(1-sample01()));
    }
    
    /************************************************************
     * The sampleInt function should not be called for
     * this continuous distribution.
     ************************************************************/

    public int sampleInt() {
	throw new RuntimeException();
    } 
}

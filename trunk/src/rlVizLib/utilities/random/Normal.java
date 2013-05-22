//Code stolen from here: http://warp.cpsc.ucalgary.ca/Software/SimKit/Documentation/

package rlVizLib.utilities.random;
import java.lang.Math;

/************************************************************
 * Normal is used to generate random variables from
 * the normal distribution.  A specific normal distribution
 * is characterized by a mean and a variance.
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

public class Normal extends RandomNumber {
    double fMean;
    double fVariance;
    double fStdDev;
    
    private boolean flipFlop = false;
    private double u1;
    private double u2;
    
    /************************************************************
     * Normal constructor.  Initialize the parameters of the
     * normal distribution.
     * <br>
     * @param mean The mean of the distribution
     * @param variance The variance of the distribution
     ************************************************************/
    
    public Normal(double mean, double variance) {
	fMean = mean;
	fVariance = variance;
	
	// Calculate the StdDev
	fStdDev = Math.sqrt( variance );
	
	flipFlop = false;
    }
    
    // Read only access to member data
    public double getMean() { 
	return fMean;  
    }

    public double getVariance() {
	return fVariance; 
    }

    public double getStdDev() {
	return fStdDev; 
    }
    
    private double norm01() {
	// Save the states of flipFlop, u1, u2.
	double TWOPI = 6.2831854;
	
	flipFlop = !flipFlop;
	if (flipFlop){ 
	    u1 = sample01();
	    u2 = sample01();
	    return ( Math.sqrt(-2.0*Math.log(u1))*
		    Math.sin( TWOPI*u2) );
	}
	else {
	    return ( Math.sqrt(-2.0*Math.log(u1))*
		    Math.cos( TWOPI*u2));
	}
    }
    
    // Random number sampling functions

    /************************************************************
     * The sampleDouble function returns a random variable
     * that is chosen from a normal distribution with parameters
     * as set in the constructor.
     * @see Normal#Normal 
     ************************************************************/

    public double sampleDouble() {
	return norm01()*fStdDev + fMean;
    }
    
    /************************************************************
     * The sampleInt function should not be called for
     * this continuous distribution.
     ************************************************************/

    public int sampleInt() {
	throw new RuntimeException();
    } 
}

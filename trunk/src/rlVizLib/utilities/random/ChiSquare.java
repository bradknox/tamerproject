//Code stolen from here: http://warp.cpsc.ucalgary.ca/Software/SimKit/Documentation/

package rlVizLib.utilities.random;

import java.lang.Math;

/************************************************************
 * ChiSquare is used to generate random variables from
 * the chi-squared distribution.  A specific chi-squared
 * distribution is characterized by a single parameter,
 * the degrees of freedom.
 * <br><br>
 * This is a continuous distribution.
 * <br><br>
 * <b>Tests Performed</b><br>
 * 1000 samples were generated and the means and variances
 * were examined.  Subjectively, they seemed correct.
 * Goodness of fit tests where not performed.
 * 
 * @version 1.96
 * @author Juraj Pivovarov
 ************************************************************/

public class ChiSquare extends RandomNumber {
    
    private int fDegsFreedom;
    
    private int fDegsDiv2;
    private boolean fDegsOdd;
    
    private static Exponential fExp = new Exponential(1.0);
    private static Normal fNorm = new Normal(0,1);
    
    /************************************************************
     * ChiSquare constructor.  Initialize the degrees of freedom
     * for the distribution.
     * <br>
     * @param degsFreedom The degrees of freedom of the distribution.
     ************************************************************/
    
    public ChiSquare(int degsFreedom) {
	fDegsFreedom = degsFreedom;
	fDegsDiv2 = degsFreedom / 2;
	fDegsOdd =  degsFreedom % 2 == 1;
    }
    
    // Random number sampling functions

    /************************************************************
     * The sampleDouble function returns a random variable
     * that is chosen from a chi-squared distribution with 
     * degrees of freedom as set in the constructor.
     *
     * <br>
     * Algorithm is found in Knuth, "The Art of Comp. Programming" 
     * Volume 2.
     *
     * @see ChiSquare#ChiSquare 
     ************************************************************/

    public double sampleDouble() {
	double chi = 0.0;
	for(int i=0;i<fDegsDiv2;i++)
	    chi += fExp.sampleDouble();
	chi *= 2.0;
	
	if( fDegsOdd ) {
	    double z = fNorm.sampleDouble();
	    chi += z*z;
	}
	return chi;
    }
    
    /************************************************************
     * The sampleInt function should not be called for
     * this continuous distribution.
     ************************************************************/

    public int sampleInt() {
	throw new RuntimeException();
    } 
}
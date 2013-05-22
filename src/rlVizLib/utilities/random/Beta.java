//Code stolen from here: http://warp.cpsc.ucalgary.ca/Software/SimKit/Documentation/

package rlVizLib.utilities.random;

import java.lang.Math;

/************************************************************
 * Beta is used to generate random variables from
 * the Beta distribution.  A specific beta distribution
 * is characterized by two parameters, both representing
 * degrees of freedom.
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

public class Beta extends RandomNumber {
    
    private int fDegsFreedom1;
    private int fDegsFreedom2;
    
    private static ChiSquare fChi1;
    private static ChiSquare fChi2;
    
    /************************************************************
     * Beta constructor.  Initialize the parameters of the
     * beta distribution.
     * <br>
     * @param degsFreedom1 The first degrees of freedom parameter
     * @param degsFreedom2 The second degrees of freedom parameter
     ************************************************************/
    
    public Beta(int degsFreedom1, int degsFreedom2) {
	fDegsFreedom1 = degsFreedom1;
	fDegsFreedom2 = degsFreedom2;
	fChi1 = new ChiSquare(degsFreedom1);
	fChi2 = new ChiSquare(degsFreedom2);
    }
    
    // Random number sampling functions
    
    /************************************************************
     * The sampleDouble function returns a random variable
     * that is chosen from a beta distribution with parameters
     * as set in the constructor.
     * <br>
     * Algorithm is found in Knuth, "The Art of Comp. Programming" 
     * Volume 2.
     * @see Beta#Beta 
     ************************************************************/

    public double sampleDouble() {
	double chi1 = fChi1.sampleDouble();
	double chi2 = fChi2.sampleDouble();
	return chi1 / (chi1+chi2);
    }
    
    /************************************************************
     * The sampleInt function should not be called for
     * this continuous distribution.
     ************************************************************/

    public int sampleInt() {
	throw new RuntimeException();
    } 
}

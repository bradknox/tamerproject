//Code stolen from here: http://warp.cpsc.ucalgary.ca/Software/SimKit/Documentation/
package rlVizLib.utilities.random;

/************************************************************
 * The RandomNumber class implements a random number
 * generator.  It behaves as a uniform distribution in [0,1)
 * that gnerates doubles.  It provides no support for 
 * directly generating random integers.
 * <br>
 * It is intended to be used a parent class for other random
 * numbers.  
 * <br><br> <b>Extending this class ...</b><br>
 * When creating a subclass, be sure to use the
 * sample01() function instead of the nextDouble() function.
 * Both discrete and continuous distributions exist.  The
 * sampleInt() function is intended to be used with discrete
 * distributions and the sampleDouble() function is intended
 * to be used with continuous distributions.  To make the
 * class easier to use, it will not be considered an error
 * to call sampleDouble() for a discrete distribution. The integer
 * returned from sampleInt() can easily be cast to a double.
 * But,
 * in the case of calling sampleInt() for a continous 
 * distribution, a RuntimeException will be thrown.  So when
 * in doubt, use the sampleDouble() function.
 * <br>
 * Thus, users should pay attention to whether or not a 
 * distribution they are using is continuous or discrete.
 * The RandomNumber class is treated as a continuous
 * distribution - Uniform [0,1).
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

public class RandomNumber {
    private static int tseed = 1073;            //common random seeds
    private static int cseed = 12345;
    private static int random_mixer = 5;     // can be set as input para ?
    private static boolean rand_initialized = false;

    // These values are from a chi squared table, the first entry for
    // 1 degree of freedom, the last for 30 degrees of freedom.  Each
    // is for the tail area alpha = 0.05.  That means that a chi squared
    // random variable with n degrees of freedom has a probability of
    // 5% of being greater than chiSqTable[n].

    private static double chiSqTable[] = { 3.84, 5.99, 7.81, 9.49, 11.1, 
					   12.6, 14.1, 15.5, 16.9, 18.3, 
					   19.7, 21.0, 22.4, 23.7, 25.0, 
					   26.3, 27.6, 28.9, 30.1, 31.4, 
					   32.7, 33.9, 35.2, 36.4, 37.7, 
					   39.9, 40.1, 41.3, 42.6, 43.8 };
    
    private int Tseed;
    private int Cseed;
    
    // constructor
    public RandomNumber() {
	int i;
	if (! rand_initialized ) {
	    Random();
	    for (i = 0; i < 10*random_mixer; i++) 
		blender();
	    rand_initialized = true;
	}
	
	for ( i = 0; i < random_mixer; i++) 
	    Random();
	Tseed = tseed;
	Cseed = cseed;
    }

    private static void blender() {
	tseed ^= ((tseed >>15) & 0377777);
	tseed ^= (tseed <<17);
	cseed *= 69069;
    }

    
    /************************************************************
     * Reset the random_mixer to a value greater than 5, default 
     * value is 5 
     * 
     * @param mixer Should be an integer >= 5, it denotes the 
     * number of times mix the random number seeds.
     ************************************************************/

    public static void resetRandomMixer(int mixer) {
        if (mixer > 5 )  random_mixer = mixer;
    }
    
    public static int randomMixer() {
	return random_mixer;
    }
    
    
    private static void Random() {
	int teven;
	
	do {
	    cseed += 190113578;
	    tseed += 1027;
	    
	    teven = ((tseed &1)== 0) ? 1:0;
	    tseed |= 1;
	    cseed |= 1;
	    
	    cseed = tseed + (cseed^tseed) * 314159;
	    tseed = cseed + (cseed ^tseed) * 1492365;
	    
	    tseed ^= teven;
	}  while ( tseed == 0);
	
	/**
	 * For good measure, run off a couple - it just mixes more.
	 */		
	blender();
	blender();
    }
    
    // Random number sampling functions

    // We avoid making them abstract because we still want to have
    // instances of RandomNumbers.

    /************************************************************
     * Draw a random sample.  This function should be overridden
     * by all derived classes.  
     * The RandomNumber class returns a uniform double in the
     * interval [0,1).
     ************************************************************/

    public double sampleDouble () {
	return sample01();
    }
    
    /************************************************************
     * Draw a random sample.  This function should be overridden
     * by all derived classes.  
     * The RandomNumber class throws a RuntimeException when
     * this function is invoked, but this function should be
     * used in derived classes that implement discrete 
     * distributions. 
     ************************************************************/

    public int sampleInt() {
	throw new RuntimeException();
    }
    
    /************************************************************
     * This function returns a uniform double in the interval 
     * [0,1).  
     * <br>
     * Call this function from derived classes rather
     * than using super.sampleDouble().  Even though both 
     * functions return the same type of result, calling
     * sampleDouble() from derived classes' implmentations
     * of sampleDouble() may result in some nasty infinite 
     * recursion.
     ************************************************************/
    
    protected double sample01() {
	double x;
	Tseed ^= ((Tseed >> 15) & 0377777);
	Tseed ^= (Tseed << 17);
	Cseed *= 69069;
	
	x = ((double) (Tseed ^Cseed))/4294967296.;
	if ( x < 0.) x+= 1.;
	return (x);
    }

    /************************************************************
     * Sample some numbers from a distribution and record how
     * many numbers fall into each bucket (interval).  The count
     * for each bucket is stored in the observed array which is 
     * passed as an argument.  
     *
     * @param sampleSize The number of random draws to make
     *
     * @param seperators The number of seperators.  If there
     * are n-1 of these, then there should be n buckets.
     *
     * @param observed This is actually a return value.  Each
     * element is a counter for one of the buckets.
     *
     * @exception RuntimeException Thrown if the length of the
     * observed array is not one greater than the length of 
     * the seperators array.
     ************************************************************/

    private void countObserved(int sampleSize, double seperators[],
			       int observed[]) {
	// Generate the random draws.
        if( seperators.length+1 != observed.length )
	    throw new RuntimeException();

        int numBuckets = observed.length;
	for(int i=0;i<sampleSize;i++) {
	    double d = sampleDouble();	    // Make a random draw
	    
	    // Now drop it in the correct bucket.
	    int j=0;
	    for(j = 0;j<numBuckets-1;j++) {
		if( d <= seperators[j]+.0001  ) {
		    observed[j]++;
		    break;
		}
	    }
	    if( j == numBuckets-1 ) observed[j]++;
	}
    }

    /************************************************************
     * Given some expected and observed frequencies, compute
     * the corresponding chi squared statistic.  The chi-squared
     * statistic is defined as the sum from 1 to n of 
     * ( (o-e)^2 / e )
     * where o and e range over the observed and expected
     * frequencies, where there are n of each.
     *
     * @param o The observed frequencies.  Type: int[10].
     * This parameter will be cleared before use.
     *
     * @param e The expected frequencies.  These should total
     * the number of samples that will be drawn.
     *
     * @return The chi square statisitic
     *
     * @exception RuntimeException Thrown if the length of the
     * observed and expected frequencies arrays do not match.
     ************************************************************/

    private double calcChiSquare(int o[], double e[]) {
	if( o.length != e.length )
	    throw new RuntimeException();

	double x = 0.0;
	for(int i=0;i<o.length;i++) {
	    x += (o[i]-e[i])*(o[i]-e[i]) / e[i];
	}
	return x;
    }
    
    /************************************************************
     * Run a chi squared <em>Goodness of Fit Test</em>.  This 
     * involves subdividing the range of a random variable into
     * intervals (also known as buckets) and then taking a sample
     * from the distribution and recording how many samples fall
     * into each bucket.  To use this test, one must also
     * know how many balls are expected to fall in each bucket.
     * This statistic measures the deviation of the observed 
     * values from the expected values.  At this point, the 
     * chi square statistic is compared against a tabulated
     * value and the test either succeeds or fails.
     *
     * <br><br>
     * In stats jargon, the null hypothesis for this test is
     * that the given random number generator generates numbers
     * according to some known distribution.
     * (Ex I think my generator generates numbers are normal
     * with mean 3.5 and variance 28.8.)
     * The alternative hypothesis is that the null hypothesis
     * is false. <br>
     * This test is conducted at the level of significance 0.05.
     * <br><br>
     * This test will succeed if the generator creates numbers
     * that appear to match those in the expected array and will
     * fail otherwise.  There is only a 5% the test will fail if
     * the random sample does indeed come from the distribution 
     * given by the expected frequencies. 
     * 
     * @param sampleSize The number of samples to draw.
     * 
     * @param seperators The upper bounds of the intervals.  The
     * last upper bound is always +infinity, (and thus not specified).
     * The bounds are included in the intervals.  Ex: seperators[] = 
     * {3,4,5} would indicate four intervals: (-inf,3], (3,4], 
     * (4,5], and (5,+inf).
     *
     * @param expected How many observations are expected to fall in
     * each of the given intervals. 
     * 
     * @exception RuntimeException Thrown if the length of the
     * expected array is not one bigger than the length of the
     * seperators array.
     ************************************************************/

    public boolean goodnessOfFitTest(int sampleSize, double seperators[],
				     double expected[]) {
	return goodnessOfFitTest(sampleSize,seperators,expected,0);
    }

    /************************************************************
     * Let the user specify how many parameters had to be 
     * estimated.
     * @see goodnessOfFitTest(int,double,double).
     ************************************************************/

    public boolean goodnessOfFitTest(int sampleSize, double seperators[],
				     double expected[], int estimatedParams) {

	int observed[] = new int[seperators.length+1];
	for(int i=0;i<observed.length;i++)
	    observed[i] = 0;

	countObserved(sampleSize, seperators, observed);
	double x = calcChiSquare(observed, expected);

	// degsFreedom = # of intervals - 1 - # of estimated parameters
	int degsFreedom = seperators.length - estimatedParams;
	if( degsFreedom < 1 || degsFreedom > 30 ) 
	    throw new RuntimeException();

	// Now lookup the appropriate chi squared statistic.
	return( x <= chiSqTable[degsFreedom-1] );
    }
}

package edu.utexas.cs.tamerProject.envModels.transModels;

import java.util.Arrays;
import java.util.Random;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

import edu.utexas.cs.tamerProject.envModels.EnvTransModel;
import edu.utexas.cs.tamerProject.utils.encapsulation.ObsAndTerm;

public class CartPoleTransModel extends EnvTransModel {
	private final static double GRAVITY = 9.8;
	private final static double MASSCART = 1.0;
	private final static double MASSPOLE = 0.1;
	private final static double TOTAL_MASS = (MASSPOLE + MASSCART);
	private final static double LENGTH = 0.5;	  /* actually half the pole's length */
	
	private final static double POLEMASS_LENGTH = (MASSPOLE * LENGTH);
	private final static double FORCE_MAG = 10.0;
	private final static double TAU = 0.02;	  /* seconds between state updates */
	
	private final static double FOURTHIRDS = 4.0d / 3.0d;
	final static double DEFAULTLEFTCARTBOUND = -2.4;
	final static double DEFAULTRIGHTCARTBOUND = 2.4;
	final static double DEFAULTLEFTANGLEBOUND = -Math.toRadians(12.0d);
	final static double DEFAULTRIGHTANGLEBOUND = Math.toRadians(12.0d);
    double leftAngleBound = DEFAULTLEFTANGLEBOUND;
    double rightAngleBound = DEFAULTRIGHTANGLEBOUND;
    double leftCartBound = DEFAULTLEFTCARTBOUND;
    double rightCartBound = DEFAULTRIGHTCARTBOUND;

    private Random theRandom;
    private long randomSeed = 0L;
    boolean randomStartStates = true;
    double transitionNoise = 1.0d;
    
    
    public CartPoleTransModel() {//boolean randomStartStates, double transitionNoise, long randomSeed, boolean useDiscountFactor) {
        if(randomSeed == 0){
            theRandom = new Random();
        }
        else{
            theRandom = new Random(randomSeed);
        }
        //Throw away the first few bits because they depend heavily on the seed.
        theRandom.nextDouble();
        theRandom.nextDouble();
    }
	
	
    
    public ObsAndTerm getStartObs(){
        double[] obsDoubleArray = new double[4];
        obsDoubleArray[0] = 0.0f; // x (cart position)
        obsDoubleArray[1] = 0.0f; // x_dot (cart velocity)
        obsDoubleArray[2] = 0.0f; // theta (pole angle)
        obsDoubleArray[3] = 0.0f; // theta_dot (pole angular velocity)

        if(randomStartStates){
            //Going to have the random start states be near to equilibrium                                                                            
        	obsDoubleArray[0] = theRandom.nextDouble()-.5d; //x
        	obsDoubleArray[1] = theRandom.nextDouble()-.5d; //x_dot
        	obsDoubleArray[2] = (theRandom.nextDouble()-.5d)/8.0d; //theta
        	obsDoubleArray[3] = (theRandom.nextDouble()-.5d)/8.0d; //theta_dot
        }

        Observation obs = new Observation();
        obs.doubleArray = obsDoubleArray;
        
        return new ObsAndTerm(obs, false);
    }

    
    
	public ObsAndTerm sampleNextObsNoForceCont(Observation obs, Action act) {
		//System.out.println("obs: " + Arrays.toString(obs.doubleArray));
		int actNum = act.intArray[0];
		//System.out.println("act: " + actNum);

	    double x = obs.doubleArray[0];			/* cart position, meters */
	    double x_dot = obs.doubleArray[1];			/* cart velocity */
	    double theta = obs.doubleArray[2];			/* pole angle, radians */
	    double theta_dot = obs.doubleArray[3];		/* pole angular velocity */
	    
	    /*
	     * Check for terminal state
	     */
        if (x <= leftCartBound || x >= rightCartBound || theta <= leftAngleBound || theta >= rightAngleBound)
        	return new ObsAndTerm(null, true);
		
		
//        if(theRandom.nextDouble()<=(1.0d-gammaOrExitProb) && !useDiscountFactor){
//            natureSaysFail=true;
//        }
        double xacc;
        double thetaacc;
        double force;
        double costheta;
        double sintheta;
        double temp;

        if (actNum > 0) {
            force = FORCE_MAG;
        } else {
            force = -FORCE_MAG;
        }

        //Noise of 1.0 means possibly full opposite action
        double thisNoise = 2.0d * transitionNoise * FORCE_MAG * (theRandom.nextDouble() - .5d);

        force += thisNoise;

        costheta = Math.cos(theta);
        sintheta = Math.sin(theta);

        temp = (force + POLEMASS_LENGTH * theta_dot * theta_dot * sintheta) / TOTAL_MASS;

        thetaacc = (GRAVITY * sintheta - costheta * temp) / (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta / TOTAL_MASS));

        xacc = temp - POLEMASS_LENGTH * thetaacc * costheta / TOTAL_MASS;

        /*** Update the four state variables, using Euler's method. ***/
        x += TAU * x_dot;
        x_dot += TAU * xacc;
        theta += TAU * theta_dot;
        theta_dot += TAU * thetaacc;

        /**These probably never happen because the pole would crash **/
        while (theta >= Math.PI) {
            theta -= 2.0d * Math.PI;
        }
        while (theta < -Math.PI) {
            theta += 2.0d * Math.PI;
        }
        
        if (x < leftCartBound)
        	x = leftCartBound;
        else if (x > rightCartBound)
        	x = rightCartBound;
        if (theta < leftAngleBound)
        	theta = leftAngleBound;
        else if (theta > rightAngleBound)
        	theta = rightAngleBound;
        
        Observation nextObs = new Observation();
        nextObs.doubleArray = new double[4];
	    nextObs.doubleArray[0] = x;			/* cart position, meters */
	    nextObs.doubleArray[1] = x_dot;			/* cart velocity */
	    nextObs.doubleArray[2] = theta;			/* pole angle, radians */
	    nextObs.doubleArray[3] = theta_dot;     /* pole angular velocity */
        
	    System.out.println("nextObs: " + Arrays.toString(nextObs.doubleArray));
	    
	    return new ObsAndTerm(nextObs, false);
    }
	
    public boolean inFailure(Observation obs) {
	    double x = obs.doubleArray[0];			/* cart position, meters */
	    double theta = obs.doubleArray[2];			/* pole angle, radians */
        if (x <= leftCartBound || x >= rightCartBound || theta <= leftAngleBound || theta >= rightAngleBound) {
            return true;
        } /* to signal failure */
        return false;
    }
}

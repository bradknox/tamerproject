/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rlVizLib.utilities.random;

import java.util.Random;



/**
 *
 * @author btanner
 */
public class myBeta {
    private int fDegsFreedom1;
    private int fDegsFreedom2;
    private myChi fChi1;
    private myChi fChi2;
    
    private Random randGenerator;

    public myBeta(int degsFreedom1, int degsFreedom2, Random randGenerator) {
	fDegsFreedom1 = degsFreedom1;
	fDegsFreedom2 = degsFreedom2;
	fChi1 = new myChi(degsFreedom1,randGenerator);
	fChi2 = new myChi(degsFreedom2,randGenerator);
        this.randGenerator=randGenerator;
    }

    public double sampleDouble() {
	double chi1 = fChi1.sampleDouble();
	double chi2 = fChi2.sampleDouble();
	return chi1 / (chi1+chi2);
    }


}

class myChi{
    private int fDegsFreedom;
    private int fDegsDiv2;
    private boolean fDegsOdd;
    
    private Random randGenerator;
    private myExponential fExp;

    public myChi(int degsFreedom, Random randGenerator){
	fDegsFreedom = degsFreedom;
	fDegsDiv2 = degsFreedom / 2;
	fDegsOdd =  degsFreedom % 2 == 1;
        this.randGenerator=randGenerator;
        fExp = new myExponential(1.0,randGenerator);
    }

    double sampleDouble() {
      	double chi = 0.0;
	for(int i=0;i<fDegsDiv2;i++)
	    chi += fExp.sampleDouble();
	chi *= 2.0;
	
	if( fDegsOdd ) {
	    double z = randGenerator.nextGaussian();
	    chi += z*z;
	}
	return chi;

    }
}
 class myExponential {
    private double fMean;
    private Random randGenerator;
    


    public myExponential(double mean, Random randGenerator) {
	fMean = mean;
        this.randGenerator=randGenerator;
    }
    
    public double sampleDouble() {
	return (-fMean * Math.log(1-randGenerator.nextDouble()));
    }
}
    

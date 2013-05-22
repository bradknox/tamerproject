package edu.utexas.cs.tamerProject.utils;

/**
 * This class allows a double to be passed by reference, so that changing it
 * in one place (a certain class) changes it everywhere else.
 * 
 */
public class MutableDouble {

	private double value;
	
	public MutableDouble(double value) {
		this.value = value;
	}

	public double getValue() {
		return this.value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public String toString(){
		return "" + value;
	}
}

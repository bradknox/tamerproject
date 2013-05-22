package edu.utexas.cs.tamerProject.utils;

/**
 * TODO Change the name of this so that it doesn't conflict with the Timer class in the Java API.
 * 
 * @author bradknox
 *
 */
public class Stopwatch {
	double startTime = Double.NaN;
	
	public static double getWallTimeInSec(){ return System.currentTimeMillis() / 1000.0; } //((new Date()).getTime() / 1000.0);
	
	/**
	 * According to some online resources, System.nanoTime() gives the most accurate time 
	 * available for the given system, but does not guaranteed accuracy above 15ms. It also
	 * should only be used for elapsed time, since it is relative to an arbitrary point 
	 * i.e., it's not wall-clock time).
	 * 
	 * @return
	 */
	public static double getComparableTimeInSec(){ return System.nanoTime() / 1000000000.0; }
	public void startTimer(){ this.startTime = Stopwatch.getComparableTimeInSec(); }
	public double getTimeElapsed(){ return Stopwatch.getTimeElapsedSince(this.startTime); }
	public static double getTimeElapsedSince(double startTime){ return Stopwatch.getComparableTimeInSec() - startTime; }
}

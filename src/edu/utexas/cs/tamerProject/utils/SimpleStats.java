package edu.utexas.cs.tamerProject.utils;

public class SimpleStats {

	public static double getMean(double[] nums) {
		double sum = 0;
		for (int i = 0; i < nums.length; i++) {
			sum += nums[i];
		}
		return sum / nums.length;
	}
	
	public static double getStDev(double[] nums) {
		double sumOfSqDev = 0;
		double mean = SimpleStats.getMean(nums);
		for (int i = 0; i < nums.length; i++) {
			sumOfSqDev += Math.pow(nums[i] - mean, 2);
		}
		return Math.sqrt(sumOfSqDev / (nums.length - 1));
	}
	
	public static double getStErr(double[] nums) {
		return SimpleStats.getStDev(nums) / Math.sqrt(nums.length);
	}
	
	
	public static void main(String[] args) {
		double[] testNums = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		double externallyCalcedStErr = 0.912870929175;
		System.out.println("Mean: " + SimpleStats.getMean(testNums));
		System.out.println("Standard deviation: " + SimpleStats.getStDev(testNums));
		
		if (Math.abs(SimpleStats.getStErr(testNums) - externallyCalcedStErr) < 0.00001)
			System.out.println("Standard error is correct: " + SimpleStats.getStErr(testNums));
		else
			System.out.println("Standard error test failed with value " + SimpleStats.getStErr(testNums));
		
	}
}

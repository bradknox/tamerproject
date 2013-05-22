package edu.utexas.cs.tamerProject.agents.tamer;


public class HRew{
	public double time;
	public double val;
	
	public HRew() {;}
	public HRew(double val, double time) {
		this.val = val;
		this.time = time;
	}
	
	public String toString(){
		return "{" + this.val + " @ " + String.format("%f", this.time) + "}";
	}
}
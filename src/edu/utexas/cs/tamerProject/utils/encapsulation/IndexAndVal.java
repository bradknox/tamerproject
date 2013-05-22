package edu.utexas.cs.tamerProject.utils.encapsulation;

import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

public class IndexAndVal {
	private double value = 0;
	private int index = -1;
	
	public IndexAndVal(int index, double value){
		this.index = index;
		this.value = value;
		//System.err.println("New ObsAndAct" + Arrays.toString(Thread.currentThread().getStackTrace()));	
	}
	
	public void setIndex(int index){this.index = index;}
	public int getIndex(){return this.index;}
	
	public boolean indexNotSet(){
		return this.index == -1;
	}
	
	public void setVal(double val) {this.value = val;}
	public double getVal() {return this.value;}
	
	public String toString(){
		return this.index + ", " + this.value;
	}
}

package edu.utexas.cs.tamerProject.utils.encapsulation;

import java.util.Arrays;

import org.rlcommunity.rlglue.codec.types.Action;
import org.rlcommunity.rlglue.codec.types.Observation;

public class ObsAndAct implements Comparable<ObsAndAct> {
	private Observation o = null;
	private Action a = null;
	
	public ObsAndAct(){
		//System.err.println("New ObsAndAct" + Arrays.toString(Thread.currentThread().getStackTrace()));	
	}
	
	public ObsAndAct(Observation o, Action a){
		this.o = o;
		this.a = a;
		//System.err.println("New ObsAndAct" + Arrays.toString(Thread.currentThread().getStackTrace()));	
	}
	
	public void setObs(Observation o){
		//System.err.println("Observation changed" + Arrays.toString(Thread.currentThread().getStackTrace()));	
		//System.err.println("Old obs: " + (obsIsNull() ? "null" : this.o.doubleArray[0]));
		this.o = o;
		//System.err.println("New obs: " + (obsIsNull() ? "null" : this.o.doubleArray[0]));
	}
	public Observation getObs(){return this.o;}
	
	public void setAct(Action a){this.a = a;}
	public Action getAct() {return this.a;}
	
	public boolean obsIsNull(){
		return this.o == null;
	}
	public boolean actIsNull(){
		return this.a == null;
	}
	
	

	public boolean actEquals(Action otherA) {
		if (this.a == otherA) {
			return true;
		}		
		if (this.a == null) {
			if (otherA != null) {
				return false;
			}
		} else if (!Arrays.equals(a.intArray, otherA.intArray) ||
				!Arrays.equals(a.doubleArray, otherA.doubleArray) ||
				!Arrays.equals(a.charArray, otherA.charArray)) {
			return false;
		}
		return true;
	}
	
	public boolean obsEquals(Observation otherO) {
		if (this.o == otherO) {
			return true;
		}		
		if (this.o == null) {
			if (otherO != null) {
				return false;
			}
		} else if (!Arrays.equals(o.intArray, otherO.intArray) ||
				!Arrays.equals(o.doubleArray, otherO.doubleArray) ||
				!Arrays.equals(o.charArray, otherO.charArray)) {
			return false;
		}
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ObsAndAct other = (ObsAndAct) obj;
		if (a == null) {
			if (other.a != null) {
				return false;
			}
		} else if (!Arrays.equals(a.intArray, other.a.intArray) ||
				!Arrays.equals(a.doubleArray, other.a.doubleArray) ||
				!Arrays.equals(a.charArray, other.a.charArray)) {
			return false;
		}
		
		if (o == null) {
			if (other.o != null) {
				return false;
			}
		} else if (!Arrays.equals(o.intArray, other.o.intArray) ||
				!Arrays.equals(o.doubleArray, other.o.doubleArray) ||
				!Arrays.equals(o.charArray, other.o.charArray)) {
			return false;
		}
		return true;
	}

	public int hashCode() {
		String str = "";
		if (a.intArray != null)
			str += Arrays.toString(a.intArray);
		if (a.doubleArray != null)
			str += Arrays.toString(a.doubleArray);
		if (a.charArray != null)
			str += Arrays.toString(a.charArray);
		if (o.intArray != null)
			str += Arrays.toString(o.intArray);
		if (o.doubleArray != null)
			str += Arrays.toString(o.doubleArray);
		if (o.charArray != null)
			str += Arrays.toString(o.charArray);
		return str.hashCode();	
	}
	
	public String toString(){
		return o + ",\n" + a;
	}

	public int compareTo(ObsAndAct other) {
		
		for (int i = 0; i < this.a.intArray.length; i++) {
			if (this.a.intArray[i] < other.a.intArray[i])
				return -1;
			else if (this.a.intArray[i] > other.a.intArray[i])
				return 1;
		}
		for (int i = 0; i < this.a.doubleArray.length; i++) {
			if (this.a.doubleArray[i] < other.a.doubleArray[i])
				return -1;
			else if (this.a.doubleArray[i] > other.a.doubleArray[i])
				return 1;
		}
		for (int i = 0; i < this.a.charArray.length; i++) {
			if (this.a.charArray[i] < other.a.charArray[i])
				return -1;
			else if (this.a.charArray[i] > other.a.charArray[i])
				return 1;
		}

		
		for (int i = 0; i < this.o.intArray.length; i++) {
			if (this.o.intArray[i] < other.o.intArray[i])
				return -1;
			else if (this.o.intArray[i] > other.o.intArray[i])
				return 1;
		}
		for (int i = 0; i < this.o.doubleArray.length; i++) {
			if (this.o.doubleArray[i] < other.o.doubleArray[i])
				return -1;
			else if (this.o.doubleArray[i] > other.o.doubleArray[i])
				return 1;
		}
		for (int i = 0; i < this.o.charArray.length; i++) {
			if (this.o.charArray[i] < other.o.charArray[i])
				return -1;
			else if (this.o.charArray[i] > other.o.charArray[i])
				return 1;
		}
		
		return 0;
	}
	
}

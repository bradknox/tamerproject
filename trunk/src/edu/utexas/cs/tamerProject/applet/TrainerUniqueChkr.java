package edu.utexas.cs.tamerProject.applet;

public class TrainerUniqueChkr {
	
	public boolean checkUnique(String hitID){
		return (hitID.replaceAll("[^0-9]", "").length() > 0);
	}

}

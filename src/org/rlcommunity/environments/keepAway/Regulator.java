package org.rlcommunity.environments.keepAway;

public class Regulator {
int actionFrequency;

int timeSinceLastAction;

	public Regulator(int actionFrequency){
		this.actionFrequency=actionFrequency;
		timeSinceLastAction=actionFrequency;
	}
	
	public void tick(){
		timeSinceLastAction++;
	}
	
	public boolean ready(){
		return timeSinceLastAction>=actionFrequency;
	}
	
	public void execute(){
		timeSinceLastAction=0;
	}
	
	
	
	
}

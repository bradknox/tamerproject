package org.rlcommunity.environments.keepAway;

import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

public class Driver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SoccerPitch thePitch=new SoccerPitch(5,5);
		
		for(int i=0;i<100;i++){
			System.out.println(thePitch);
			thePitch.Update();
			if(i%5==0){
				thePitch.theBall.Kick(new Vector2D(1.0,0.0), 5.0);
			}
			
		}

	}

}

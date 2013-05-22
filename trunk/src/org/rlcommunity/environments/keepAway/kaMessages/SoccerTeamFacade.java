package org.rlcommunity.environments.keepAway.kaMessages;

import java.util.StringTokenizer;
import java.util.Vector;

public class SoccerTeamFacade implements SoccerTeamFacadeInterface {
//Make a fake enough, printable soccer team facade from the string
	
	Vector<PlayerFacade> thePlayers=null;
	public SoccerTeamFacade(String teamAString) {
		thePlayers=new Vector<PlayerFacade>();
		StringTokenizer strTok=new StringTokenizer(teamAString,"_");
		
		int numPlayers=Integer.parseInt(strTok.nextToken());
		for(int i=0;i<numPlayers;i++)
			thePlayers.add(new PlayerFacade(strTok));
	}
	public PlayerFacade getPlayer(int which) {
		return thePlayers.get(which);
	}
	public int getPlayerCount() {
		return thePlayers.size();
	}
	public String stringSerialize() {
		String responseString=getPlayerCount()+"_";
		for(int i=0;i<getPlayerCount();i++)
			responseString+=getPlayer(i).stringSerialize()+"_";
		return responseString;
	}

}

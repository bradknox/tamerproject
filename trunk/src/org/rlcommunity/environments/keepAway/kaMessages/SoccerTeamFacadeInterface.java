package org.rlcommunity.environments.keepAway.kaMessages;

public interface SoccerTeamFacadeInterface {

	public int getPlayerCount();
	public PlayerFacadeInterface getPlayer(int which);
	public String stringSerialize();
}

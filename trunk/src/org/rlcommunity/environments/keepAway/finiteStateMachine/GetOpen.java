package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.players.PlayerInterface;

public class GetOpen extends State<PlayerInterface> {
private static final GetOpen instance=new GetOpen();

public static GetOpen Instance(){return instance;}

	@Override
	public void enter(PlayerInterface theEntity) {

	}

	@Override
	public void execute(PlayerInterface theEntity) {
		//Draw a line segment between our player with the ball and his nearest attacker
		

	}

	@Override
	public void exit(PlayerInterface theEntity) {
		// TODO Auto-generated method stub

	}

}

package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.players.PlayerInterface;

public class ReturnToHomeSpot extends State<PlayerInterface> {
	boolean debugThis=false;
	static ReturnToHomeSpot instance=null;
	static{
		instance=new ReturnToHomeSpot();
	}
	
	public static ReturnToHomeSpot Instance()
	{
		return instance;
	}



	@Override
	public void enter(PlayerInterface player) {
		  player.Steering().SetTarget(player.homeRegion().Center());
		  player.Steering().ArriveOn();
		  
		 if(debugThis) System.out.println("enter called for player: "+player.ID());
	}

	@Override
	public void execute(PlayerInterface player) {
		if(debugThis)  System.out.println("execute called for player: "+player.ID());

		if(player.AtTarget()){
		    player.GetFSM().changeState(Wait.Instance());
		    if(debugThis)System.out.println("Player: "+player.ID()+" set to "+player.GetFSM().getCurrentState().getClass());
		}
		if(player.isClosestTeamMemberToBall()){
			player.GetFSM().changeState(ChaseBall.Instance());
			if(debugThis)System.out.println("Player: "+player.ID()+" set to "+player.GetFSM().getCurrentState().getClass());
		}
}
	
	
	@Override
	public void exit(PlayerInterface player) {
		  player.Steering().ArriveOff();
	}






}

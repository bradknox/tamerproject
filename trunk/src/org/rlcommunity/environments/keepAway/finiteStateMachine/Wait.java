package org.rlcommunity.environments.keepAway.finiteStateMachine;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

public class Wait extends State<PlayerInterface> {
	boolean debugThis=false;
	static Wait instance=null;
	static{
		instance=new Wait();
	}
	
	@Override
	public void enter(PlayerInterface player) {
		
	}

	@Override
	public void execute(PlayerInterface player) {

		  if (player.isClosestTeamMemberToBall()){
			     player.GetFSM().changeState(ChaseBall.Instance());
			     if(debugThis)System.out.println("Player: "+player.ID()+" set to "+player.GetFSM().getCurrentState().getClass());
			     return;
		  }
		  //if the player has been jostled out of position, get back in position  
		  if (!player.AtTarget())
		  {
		    player.Steering().ArriveOn();
		    return;
		  } else
		  {
		    player.Steering().ArriveOff();
		    player.SetVelocity(new Vector2D(0,0));
		    //the player should keep his eyes on the ball!
		    player.TrackBall();
//		    player.Steering().WanderOn();
		  }
		  

	}

	@Override
	public void exit(PlayerInterface player) {
	    player.Steering().ArriveOff();
	    player.Steering().WanderOff();
	}

	public static Wait Instance()
	{
		return instance;
	}

	
}

package org.rlcommunity.environments.keepAway.finiteStateMachine;

import org.rlcommunity.environments.keepAway.messages.ReceivePassTelegram;
import org.rlcommunity.environments.keepAway.players.PlayerInterface;
import org.rlcommunity.environments.keepAway.Prm;
import org.rlcommunity.environments.keepAway.generalGameCode.Utilities;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

public class KickBall extends State<PlayerInterface> {
	boolean debugThis=false;
	
	static KickBall instance=null;
	static{
		instance=new KickBall();
	}
	
	public static KickBall Instance(){return instance;}
	
	@Override
	public
	void enter(PlayerInterface player) {
		  //let the team know this player is controlling
		   player.Team().setControllingPlayer(player);
		   
		   //the player can only make so many kick attempts per second.
		   if (!player.isReadyForNextKick()) 
		   {
		     player.GetFSM().changeState(ChaseBall.Instance());
		     if(debugThis)System.out.println("Player: "+player.ID()+" set to "+player.GetFSM().getCurrentState().getClass());
		   }


	}

//	@Override
	public void execute(PlayerInterface player) {
		  //calculate the dot product of the vector pointing to the ball
		  //and the player's heading
		  Vector2D ToBall = player.Ball().Pos().subtractToCopy(player.Pos());
		  double   dot    = player.Heading().Dot(ToBall.normalizeToCopy()); 

		  //the dot product is used to adjust the shooting force. The more
		  //directly the ball is ahead, the more forceful the kick
		  double power = Prm.MaxShootingForce * dot;
		  
		//See if I can stop the ball
		if(player.Ball().Velocity().LengthSq()>Prm.holdingBallSpeed){
			System.out.println("Trying to stop the ball!");
			Vector2D randomSmallVector=new Vector2D(Utilities.RandomClamped(),Utilities.RandomClamped());
//The illusion of stopping the ball
			if(player.isReadyForNextKick()){
				player.Ball().setPos(randomSmallVector.add(player.Pos()));
				Vector2D BallTarget=player.Ball().Pos().addToCopy(player.Ball().Velocity());
				player.Kick(BallTarget,Prm.StoppingForce);

			}
		}
		


//		 Vector2D  BallTarget=new Vector2D(Math.random()*100.0d, Math.random()*50.0d);
		  //add some noise to the kick. We don't want players who are 
		   //too accurate! The amount of noise can be adjusted by altering
		   //Prm.PlayerKickingAccuracy
//		   BallTarget = AddNoiseToKick(player.Ball().Pos(), BallTarget);

		   //this is the direction the ball will be kicked in
//		   Vector2D KickDirection = BallTarget.subtractToCopy(player.Ball().Pos());
		PlayerInterface closestOpponent=player.Team().getClosestOpponent(player);
		double closestOpponentDistanceSquared=Double.MAX_VALUE;
		if(closestOpponent!=null){
			closestOpponentDistanceSquared=closestOpponent.Pos().distanceToSquared(player.Pos());
		}
		//Only do passing 25% of the time
		if(Math.random()<.5&&closestOpponentDistanceSquared>36.0d){
			player.GetFSM().changeState(DribbleBall.Instance());
			return;
		}
		  //Try to setup a pass
		  PlayerInterface receiver=player.Team().getSomeoneElse(player);
		  
		  if(receiver!=null){
			  Vector2D BallTarget=receiver.Pos().subtractToCopy(player.Ball().Pos());
	//		  BallTarget=AddNoiseToKick(player.Ball().Pos(), BallTarget);
				if(player.isReadyForNextKick()){
					receiver.receiveMessage(new ReceivePassTelegram());
					player.Kick(BallTarget,Prm.MaxShootingForce);
				}
		  }else{
		    Vector2D KickDirection=new Vector2D(Math.random()-.5d,Math.random()-.5d);
			if(player.isReadyForNextKick())player.Kick(KickDirection, power);
		  }
		   //change state   
		   player.GetFSM().changeState(ChaseBall.Instance());
		   if(debugThis)System.out.println("Player: "+player.ID()+" set to "+player.GetFSM().getCurrentState().getClass());

	}
	@Override
	public
	void exit(PlayerInterface player) {
	}

	
	//----------------------------- AddNoiseToKick --------------------------------
	//
	//  this can be used to vary the accuracy of a player's kick. Just call it 
	//  prior to kicking the ball using the ball's position and the ball target as
	//  parameters.
	//-----------------------------------------------------------------------------
	Vector2D AddNoiseToKick(Vector2D BallPos, Vector2D BallTarget)
	{

	  double displacement = (Math.PI - Math.PI*Prm.PlayerKickingAccuracy) * Utilities.RandomClamped();

	  Vector2D toTarget = BallTarget.subtractToCopy(BallPos);
	  toTarget.RotateAroundOrigin(displacement);

	  return toTarget.add(BallPos);
	}


}

package org.rlcommunity.environments.keepAway.players;

import org.rlcommunity.environments.keepAway.messages.Telegram;
import org.rlcommunity.environments.keepAway.kaMessages.PlayerFacadeInterface;
import org.rlcommunity.environments.keepAway.finiteStateMachine.StateMachine;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;
import org.rlcommunity.environments.keepAway.Region;
import org.rlcommunity.environments.keepAway.SoccerBall;
import org.rlcommunity.environments.keepAway.SoccerTeam;

public interface PlayerInterface extends PlayerFacadeInterface{

	public abstract void Update();

	// SteeringBehaviors* Steering(){return m_pSteering;}
	// Region* HomeRegion();
	// void SetHomeRegion(int NewRegion){m_iHomeRegion = NewRegion;}
	public abstract SoccerTeam Team();

	public abstract SoccerBall Ball();

	public abstract Vector2D Heading();

	public abstract SteeringBehaviors Steering();

	public abstract boolean BallWithinKickingRange();

	public abstract boolean isClosestTeamMemberToBall();

	public abstract StateMachine<PlayerInterface> GetFSM();

	public abstract boolean isReadyForNextKick();

	public abstract void setDistanceToBallSquared(double dist);

	public abstract boolean AtTarget();
	
	public abstract int ID();
	
	public abstract Vector2D Pos();
	
	public abstract Region homeRegion();
	
	public abstract void SetVelocity(Vector2D newVelocity);
        
        public abstract void resetFSM();

	//----------------------------- TrackBall --------------------------------
	//
	//  sets the player's heading to point at the ball
	//------------------------------------------------------------------------
	public abstract void TrackBall();

	public abstract String stringSerialize();

	public abstract void setPos(Vector2D center);

	public abstract void receiveMessage(Telegram theMessage);

	public abstract void Kick(Vector2D ballTarget, double d);

	public abstract double BRadius();
}
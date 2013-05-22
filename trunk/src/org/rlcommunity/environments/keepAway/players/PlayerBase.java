package org.rlcommunity.environments.keepAway.players;

import org.rlcommunity.environments.keepAway.generalGameCode.MovingEntity;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

import java.util.Vector;

import org.rlcommunity.environments.keepAway.messages.Telegram;

import org.rlcommunity.environments.keepAway.kaMessages.PlayerFacadeInterface;
import org.rlcommunity.environments.keepAway.Prm;
import org.rlcommunity.environments.keepAway.Region;
import org.rlcommunity.environments.keepAway.Regulator;
import org.rlcommunity.environments.keepAway.SoccerBall;
import org.rlcommunity.environments.keepAway.SoccerPitch;
import org.rlcommunity.environments.keepAway.SoccerTeam;

public abstract class PlayerBase extends MovingEntity implements PlayerFacadeInterface, PlayerInterface{

	public enum player_role {
		attacker, defender
	};

	// this player's role in the team
	protected player_role m_PlayerRole;

	// a pointer to this player's team
	protected SoccerTeam m_pTeam;

	// the steering behaviors
	 SteeringBehaviors m_pSteering;

	// the distance to the ball (in squared-space). This value is queried
	// a lot so it's calculated once each time-step and stored here.
	double distanceToBallSquared;

	// the vertex buffer
	Vector<Vector2D> m_vecPlayerVB;
	// the buffer for the transformed vertices
	Vector<Vector2D> m_vecPlayerVBTrans;

//	StateMachine<PlayerInterface> theFSM=null;
	
	Region myHomeRegion=null;

	private Regulator kickRegulator=null;
	
	/* (non-Javadoc)
	 * @see players.PlayerInterface#Update()
	 */
	public void Update(){
		kickRegulator.tick();
	}
	
		public String stringSerialize() {
			StringBuffer B=new StringBuffer();
			B.append(Pos().stringSerialize());
			B.append("_");
			B.append(Heading().stringSerialize());
			B.append("_");
			B.append(Side().stringSerialize());
			B.append("_");
			B.append(Scale().stringSerialize());
			B.append("_");
			B.append(m_pSteering.m_vSteeringForce.stringSerialize());
			B.append("_");
		
			B.append(m_vecPlayerVB.size());
			B.append("_");

			for (Vector2D thisVector : m_vecPlayerVB) {
				B.append(thisVector.stringSerialize());
				B.append("_");
			}
			return B.toString();
		}	
		
	player_role Role() {
		return m_PlayerRole;
	}

	double distanceToBallSquared() {
		return distanceToBallSquared;
	}


	// SteeringBehaviors* Steering(){return m_pSteering;}
	// Region* HomeRegion();
	// void SetHomeRegion(int NewRegion){m_iHomeRegion = NewRegion;}
	/* (non-Javadoc)
	 * @see players.PlayerInterface#Team()
	 */
	public SoccerTeam Team() {
		return m_pTeam;
	}

	PlayerBase(SoccerTeam home_team, Vector2D  heading, Vector2D velocity, double mass, double max_force, double max_speed, double max_turn_rate,
			double scale, player_role role,Region myHomeRegion){
		super(/* Have to set start position */new Vector2D(),scale,velocity,max_speed,heading,mass,new Vector2D(scale,scale),max_turn_rate,max_force);
		System.out.println("Created a new player base, id: "+ID());
				m_pTeam=home_team;
		m_PlayerRole=role;
		m_vecPlayerVB=new Vector<Vector2D>();
		m_vecPlayerVBTrans=new Vector<Vector2D>();
		kickRegulator=new Regulator(Prm.kickFrequency);

		this.myHomeRegion=myHomeRegion;
//		setup the vertex buffers and calculate the bounding radius
		int NumPlayerVerts = 4;

		Vector2D player[] = {new Vector2D(-3, 8),
				new Vector2D(3,10),
				new Vector2D(3,-10),
				new Vector2D(-3,-8)};

		for (int vtx=0; vtx<NumPlayerVerts; ++vtx)
		{
			m_vecPlayerVB.add(player[vtx]);

//			set the bounding radius to the length of the
//			greatest extent
			if (Math.abs(player[vtx].x) > m_dBoundingRadius)
			{
			//	m_dBoundingRadius = Math.abs(player[vtx].x);
			}

			if (Math.abs(player[vtx].y) > m_dBoundingRadius)
			{
			//	m_dBoundingRadius = Math.abs(player[vtx].y);
			}
		}

//		set up the steering behavior class
		m_pSteering = new SteeringBehaviors(this,m_pTeam.Pitch(),Ball());  


//		theFSM=new StateMachine<PlayerInterface>(this);
//		theFSM.setGlobalState(new GlobalState());
//		theFSM.changeState(ChaseBall.Instance());
	}
	
	/* (non-Javadoc)
	 * @see players.PlayerInterface#Ball()
	 */
	public SoccerBall Ball(){
		return Pitch().theBall;
	}
	
	private final SoccerPitch Pitch(){
		return m_pTeam.Pitch();
	}
	

	

	/* (non-Javadoc)
	 * @see players.PlayerInterface#Heading()
	 */
	public Vector2D Heading() {
		return m_vHeading;
	}

	/* (non-Javadoc)
	 * @see players.PlayerInterface#Steering()
	 */
	public SteeringBehaviors Steering() {
		return m_pSteering;
	}

	/* (non-Javadoc)
	 * @see players.PlayerInterface#BallWithinKickingRange()
	 */
	public boolean BallWithinKickingRange() {
		return (Ball().Pos().distanceToSquared(Pos())<Prm.PlayerKickingDistanceSq);
//		  return (Vec2DDistanceSq(Ball()->Pos(), Pos()) < Prm.PlayerKickingDistanceSq);
	}

	/* (non-Javadoc)
	 * @see players.PlayerInterface#isClosestTeamMemberToBall()
	 */
	public boolean isClosestTeamMemberToBall() {
		  return Team().PlayerClosestToBall() == this;
	}


	/* (non-Javadoc)
	 * @see players.PlayerInterface#isReadyForNextKick()
	 */
	public boolean isReadyForNextKick() {
		return kickRegulator.ready();
	}

	/* (non-Javadoc)
	 * @see players.PlayerInterface#setDistanceToBallSquared(double)
	 */
	public void setDistanceToBallSquared(double dist) {
		this.distanceToBallSquared=dist;
	}

	public Region homeRegion() {
		return myHomeRegion;
	}

	/* (non-Javadoc)
	 * @see players.PlayerInterface#AtTarget()
	 */
	public boolean AtTarget() {
	//	 return (Vec2DDistanceSq(Pos(), Steering()->Target()) < Prm.PlayerInTargetRangeSq);
		return Pos().distanceToSquared(Steering().Target())<Prm.PlayerInTargetRangeSq;
	}	
	
	//----------------------------- TrackBall --------------------------------
	//
	//  sets the player's heading to point at the ball
	//------------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see players.PlayerInterface#TrackBall()
	 */
	public void TrackBall()
	{
	  RotateHeadingToFacePosition(Ball().Pos());  
	}
	public Vector2D SteeringForce() {
		return m_pSteering.m_vSteeringForce;
	}

	public void receiveMessage(Telegram theMessage){
		GetFSM().receiveMessage(this,theMessage);
	}
	public void Kick(Vector2D direction, double power){
			kickRegulator.execute();
			Ball().Kick(direction, power);
	}
	

}

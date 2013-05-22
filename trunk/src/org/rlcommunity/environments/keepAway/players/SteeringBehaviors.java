package org.rlcommunity.environments.keepAway.players;

import java.text.DecimalFormat;
import java.util.Vector;

import org.rlcommunity.environments.keepAway.generalGameCode.MovingEntity;
import org.rlcommunity.environments.keepAway.generalGameCode.Utilities;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;
import org.rlcommunity.environments.keepAway.Prm;
import org.rlcommunity.environments.keepAway.SoccerBall;
import org.rlcommunity.environments.keepAway.SoccerPitch;

public class SteeringBehaviors {

    int bt_none = 0x0000;
    int bt_seek = 0x0001;
    int bt_arrive = 0x0002;
    int bt_separation = 0x0004;
    int bt_pursuit = 0x0008;
    int bt_interpose = 0x0016;
    int bt_wander = 0x0032;
    int dc_slow = 3;
    int dc_normal = 2;
    int dc_fast = 1;
    double m_dWanderRadius;
    double m_dWanderJitter;
    double m_dWanderDistance;
    PlayerBase m_pPlayer;

    //the steering force created by the combined effect of all
    //the selected behaviors
    Vector2D m_vSteeringForce;

    //the current target (usually the ball or predicted ball position)
    Vector2D m_vTarget;
    //the distance the player tries to interpose from the target
    double m_dInterposeDist;

    //multipliers. 
    double m_dMultSeparation;

    //how far it can 'see'
    double m_dViewDistance;
    //create a vector to a target position on the wander circle
    Vector2D m_vWanderTarget = null;
    //binary flags to indicate whether or not a behavior should be active
    int m_iFlags;

    enum behavior_type {

        none, seek, arrive, separation, pursuit, interpose
    }

    
    
     ;

    //used by group behaviors to tag neighbours
      

        
        
        
    

    
    

    boolean m_bTagged ; //Arrive makes use of these to determine how quickly a vehicle
            //should decelerate to its target
             
             
    
     

      
         
    

      
         
    

        
          
           enum Deceleration {
		slow, normal, fast
	};

	//this function tests if a specific bit of m_iFlags is set
	boolean On(int bt) {
		return (m_iFlags & bt) == bt;
	}

	//a vertex buffer to contain the feelers rqd for dribbling
	Vector<Vector2D> m_Antenna;

	Vector2D Force() {
		return m_vSteeringForce;
	}

	Vector2D Target() {
		return m_vTarget;
	}

	public void SetTarget(Vector2D t) {
		m_vTarget = t;
		if (t == null) {
            Thread.dumpStack();
            System.exit(1);
        }
    }

    double InterposeDistance() {
        return m_dInterposeDist;
    }

    void SetInterposeDistance(double d) {
        m_dInterposeDist = d;
    }

    final boolean Tagged() {
        return m_bTagged;
    }

    final void Tag() {
        m_bTagged = true;
    }

    final void UnTag() {
        m_bTagged = false;
    }

    public void SeekOn() {
        m_iFlags |= bt_seek;
    }

    public void WanderOn() {
        m_iFlags |= bt_wander;
    }

    public void ArriveOn() {
        m_iFlags |= bt_arrive;
    }

    public void PursuitOn() {
        m_iFlags |= bt_pursuit;
    }

    public void SeparationOn() {
        m_iFlags |= bt_separation;
    }

    public void InterposeOn(double d) {
        m_iFlags |= bt_interpose;
        m_dInterposeDist = d;
    }

    public void SeekOff() {
        if (On(bt_seek)) {
            m_iFlags ^= bt_seek;
        }
    }

    public void WanderOff() {
        if (On(bt_wander)) {
            m_iFlags ^= bt_wander;
        }
    }

    public void ArriveOff() {
        if (On(bt_arrive)) {
            m_iFlags ^= bt_arrive;
        }
    }

    public void PursuitOff() {
        if (On(bt_pursuit)) {
            m_iFlags ^= bt_pursuit;
        }
    }

    public void SeparationOff() {
        if (On(bt_separation)) {
            m_iFlags ^= bt_separation;
        }
    }

    public void InterposeOff() {
        if (On(bt_interpose)) {
            m_iFlags ^= bt_interpose;
        }
    }

    public boolean SeekIsOn() {
        return On(bt_seek);
    }

    public boolean ArriveIsOn() {
        return On(bt_arrive);
    }

    public boolean WanderIsOn() {
        return On(bt_wander);
    }

    public boolean PursuitIsOn() {
        return On(bt_pursuit);
    }

    public boolean SeparationIsOn() {
        return On(bt_separation);
    }

    public boolean InterposeIsOn() {
        return On(bt_interpose);
    }

    public SteeringBehaviors(PlayerBase agent, SoccerPitch world,
                              SoccerBall ball) {
        m_pPlayer = agent;
        m_iFlags = 0;
        m_dMultSeparation = (Prm.SeparationCoefficient);
        m_bTagged = false;
        m_dViewDistance = Prm.ViewDistance;
        m_Antenna = new Vector<Vector2D>(5);
        m_dWanderJitter = .1d;
        m_dWanderRadius = 1.0d;
        m_dWanderDistance = 5.0d;

        m_vSteeringForce = new Vector2D();

        //stuff for the wander behavior
        double theta = Math.random() * Math.PI * 2.0d;

        //create a vector to a target position on the wander circle
        m_vWanderTarget = new Vector2D(m_dWanderRadius * Math.cos(theta), m_dWanderRadius * Math.sin(theta));

    }

    //---------------------- Calculate ---------------------------------------
    //
    //  calculates the overall steering force based on the currently active
    //  steering behaviors. 
    //------------------------------------------------------------------------
    public Vector2D Calculate() {
        //reset the force
        m_vSteeringForce.Zero();

        //this will hold the value of each individual steering force
        m_vSteeringForce = SumForces();

        //make sure the force doesn't exceed the vehicles maximum allowable
        m_vSteeringForce.Truncate(m_pPlayer.MaxForce());
        return m_vSteeringForce;
    }

    //-------------------------- SumForces -----------------------------------
    //
    //  this method calls each active steering behavior and acumulates their
    //  forces until the max steering force magnitude is reached at which
    //  time the function returns the steering force accumulated to that 
    //  point
    //------------------------------------------------------------------------
    Vector2D SumForces() {
        boolean debugThis = false;

        Vector2D force = new Vector2D();
        if (debugThis) {
            System.out.print(" Player: " + m_pPlayer.ID() + " ");
        }
        //the soccer players must always tag their neighbors
        FindNeighbours();

        if (On(bt_separation)) {
            if (debugThis) {
                System.out.print("Separation is on");
            }
            force.add(Separation().multiplyToCopy(m_dMultSeparation));

            if (!AccumulateForce(m_vSteeringForce, force)) {
                return m_vSteeringForce;
            }
        }

        boolean addRandom = true;
        if (addRandom) {
            force.add(new Vector2D(Math.random() / 10.0d,
                    Math.random() / 10.0d));
        }
        if (On(bt_wander)) {
            if (debugThis) {
                System.out.print("wander is on");
            }

            Vector2D wanderForce = Wander();

            force.add(wanderForce);

            if (!AccumulateForce(m_vSteeringForce, force)) {
                return m_vSteeringForce;
            }
        }

        if (On(bt_seek)) {
            if (debugThis) {
                System.out.print("seek is on");
            }

            Vector2D seekForce = Seek(m_vTarget);

            force.add(seekForce);

            if (!AccumulateForce(m_vSteeringForce, force)) {
                return m_vSteeringForce;
            }
        }

        if (On(bt_arrive)) {
            if (debugThis) {
                System.out.print("bt_arrive is on");
            }

            force.add(Arrive(m_vTarget, dc_fast));

            if (!AccumulateForce(m_vSteeringForce, force)) {
                return m_vSteeringForce;
            }
        }

        if (On(bt_pursuit)) {
            if (debugThis) {
                System.out.print("bt_pursuit is on");
            }
            force.add(Pursuit());

            if (!AccumulateForce(m_vSteeringForce, force)) {
                return m_vSteeringForce;
            }
        }

        if (On(bt_interpose)) {
            if (debugThis) {
                System.out.print("bt_interpose is on");
            }
            force.add(Interpose(m_vTarget, m_dInterposeDist));

            if (!AccumulateForce(m_vSteeringForce, force)) {
                return m_vSteeringForce;
            }
        }
        if (debugThis) {
            System.out.println(" Cumulative steering force: " + m_vSteeringForce);
        }
        return m_vSteeringForce;
    }

    //-------------------------- FindNeighbours ------------------------------
    //
    //  tags any vehicles within a predefined radius
    //------------------------------------------------------------------------
    void FindNeighbours() {
        Vector<PlayerInterface> allPlayers = new Vector<PlayerInterface>(m_pPlayer.Team().getThePlayers());
        //Maybe this should be over the opponents too?

        for (PlayerInterface thisPlayer : allPlayers) {
            thisPlayer.Steering().UnTag();

            //work in distance squared to avoid sqrts
            Vector2D to = thisPlayer.Pos().subtractToCopy(m_pPlayer.Pos());

            if (to.LengthSq() < (m_dViewDistance * m_dViewDistance)) {
                thisPlayer.Steering().Tag();
            }
        }//next
    }

    //---------------------------- Separation --------------------------------
    //
    // this calculates a force repelling from the other neighbors
    //------------------------------------------------------------------------
    //Many of these data structures can be precalculated and cached
    Vector2D Separation() {
        //iterate through all the neighbors and calculate the vector from the
        Vector2D SteeringForce = new Vector2D();

        Vector<PlayerInterface> allPlayers = new Vector<PlayerInterface>(m_pPlayer.Team().getThePlayers());
        //Maybe this should be over the opponents too?

        for (PlayerInterface thisPlayer : allPlayers) {
            //		     //make sure this agent isn't included in the calculations and that
            //		     //the agent is close enough
            if (thisPlayer != m_pPlayer && thisPlayer.Steering().Tagged()) {
                Vector2D ToAgent = m_pPlayer.Pos().subtractToCopy(thisPlayer.Pos());

                //		       //scale the force inversely proportional to the agents distance  
                //		       //from its neighbor.
                //		       SteeringForce += Vec2DNormalize(ToAgent)/ToAgent.Length();
                SteeringForce.add(ToAgent.divide(ToAgent.Length()).normalize());
            }

        }
        return SteeringForce;
    }

    public Vector2D Wander() {
        //first, add a small random vector to the target's position
        m_vWanderTarget.add(new Vector2D(Utilities.RandomClamped() * m_dWanderJitter, Utilities.RandomClamped() * m_dWanderJitter));

        //reproject this new vector back on to a unit circle
        m_vWanderTarget.normalize();

        //increase the length of the vector to the same as the radius
        //of the wander circle
        m_vWanderTarget.multiply(m_dWanderRadius);

        //move the target into a position WanderDist in front of the agent
        Vector2D target = m_vWanderTarget.addToCopy(new Vector2D(m_dWanderDistance, 0));

        //project the target into world space
        Vector2D Target = Utilities.PointToWorldSpace(target, m_pPlayer.Heading(), m_pPlayer.Side(), m_pPlayer.Pos());

        //and steer towards it
        return Target.subtract(m_pPlayer.Pos());
    }

    //--------------------- AccumulateForce ----------------------------------
    //
    //  This function calculates how much of its max steering force the 
    //  vehicle has left to apply and then applies that amount of the
    //  force to add.
    //------------------------------------------------------------------------
    boolean AccumulateForce(Vector2D sf, Vector2D ForceToAdd) {
        final boolean debugThis = false;

        DecimalFormat df = new DecimalFormat("#.###");

        if (Double.isNaN(sf.x) || Double.isNaN(sf.y)) {
            System.out.println("Nan at the start");
            Thread.dumpStack();
            System.exit(1);
        }

        if (debugThis) {
            System.out.println("Accumulate Force:\n---------------");
        }
        //first calculate how much steering force we have left to use
        double MagnitudeSoFar = sf.Length();
        if (debugThis) {
            System.out.println("\tMagnitude so far: " + df.format(MagnitudeSoFar));
        }

        double magnitudeRemaining = m_pPlayer.MaxForce() - MagnitudeSoFar;
        if (debugThis) {
            System.out.println("\tmagnitudeRemaining: " + df.format(magnitudeRemaining));
        }

        //return false if there is no more force left to use
        if (magnitudeRemaining <= 0.0) {
            return false;
        }

        if (debugThis) {
            System.out.println("\tForceToAdd.length: " + df.format(ForceToAdd.Length()));
        }
        //calculate the magnitude of the force we want to add
        double MagnitudeToAdd = ForceToAdd.Length();
        if (debugThis) {
            System.out.println("\tMagnitudeToAdd: " + df.format(MagnitudeToAdd));
        }

        //now calculate how much of the force we can really add  
        if (MagnitudeToAdd > magnitudeRemaining) {
            MagnitudeToAdd = magnitudeRemaining;
        }

        //	   System.out.println("\tWhat is added?: "+(ForceToAdd.normalizeToCopy().multiply(MagnitudeToAdd))+" which is: "+ForceToAdd+" normalized to copy = "+ForceToAdd.normalizeToCopy()+" * "+MagnitudeToAdd);
        //add it to the steering force
        if (debugThis) {
            System.out.println("\tAdding Force: " + ForceToAdd.normalizeToCopy().multiply(MagnitudeToAdd) + " to " + sf);
        }

        sf.add(ForceToAdd.normalizeToCopy().multiply(MagnitudeToAdd));

        if (Double.isNaN(sf.x) || Double.isNaN(sf.y)) {
            System.out.println("Nan!  Force to add awas: " + ForceToAdd);
            Thread.dumpStack();
            System.exit(1);
        }
        return true;
    }

    //------------------------------- Seek -----------------------------------
    //
    //  Given a target, this behavior returns a steering force which will
    //  allign the agent with the target and move the agent in the desired
    //  direction
    //------------------------------------------------------------------------
    Vector2D Seek(Vector2D target) {

        //	  Vector2D DesiredVelocity = Vec2DNormalize(target - m_pPlayer->Pos())
        //	                            * m_pPlayer->MaxSpeed();
        Vector2D DesiredVelocity = target.subtractToCopy(m_pPlayer.Pos()).normalize().multiply(m_pPlayer.MaxSpeed());
        return (DesiredVelocity.subtract(m_pPlayer.Velocity()));
    }

    //--------------------------- Arrive -------------------------------------
    //
    //  This behavior is similar to seek but it attempts to arrive at the
    //  target with a zero velocity
    //------------------------------------------------------------------------
    Vector2D Arrive(Vector2D target, int deceleration) {
        Vector2D ToTarget = target.subtractToCopy(m_pPlayer.Pos());

        //calculate the distance to the target
        double dist = ToTarget.Length();

        if (dist > 0) {
            //because Deceleration is enumerated as an int, this value is required
            //to provide fine tweaking of the deceleration..
            double DecelerationTweaker = 0.3;

            //calculate the speed required to reach the target given the desired
            //deceleration
            double speed = dist / ((double) deceleration * DecelerationTweaker);

            //make sure the velocity does not exceed the max
            speed = Math.min(speed, m_pPlayer.MaxSpeed());

            //from here proceed just like Seek except we don't need to normalize 
            //the ToTarget vector because we have already gone to the trouble
            //of calculating its length: dist. 
            Vector2D DesiredVelocity = ToTarget.multiplyToCopy(speed).divide(
                    dist);

            return (DesiredVelocity.subtract(m_pPlayer.Velocity()));
        }

        return new Vector2D(0, 0);
    }

    //----------------------- Pursuit ---------------------------------
    //
    //  this behavior creates a force that steers the agent towards the 
    //  ball
    //------------------------------------------------------------------------
    Vector2D Pursuit() {
        SoccerBall ball = m_pPlayer.Ball();
        Vector2D ToBall = ball.Pos().subtractToCopy(m_pPlayer.Pos());

        //the lookahead time is proportional to the distance between the ball
        //and the pursuer; 
        double LookAheadTime = 0.0;

        if (ball.Speed() != 0.0) {
            LookAheadTime = ToBall.Length() / ball.Speed();
        }

        //calculate where the ball will be at this time in the future
        m_vTarget = ball.FuturePosition(LookAheadTime);

        //now seek to the predicted future position of the ball
        return Arrive(m_vTarget, dc_fast);
    }

    //--------------------------- Interpose ----------------------------------
    //
    //  Given an opponent and an object position this method returns a 
    //  force that attempts to position the agent between them
    //------------------------------------------------------------------------
    Vector2D Interpose(Vector2D target, double DistFromTarget) {
        SoccerBall ball = m_pPlayer.Ball();

        //	  return Arrive(target + Vec2DNormalize(ball->Pos() - target) * DistFromTarget, normal);
        //this coudl be way wrong
        return Arrive(target.addToCopy(ball.Pos().subtractToCopy(target).normalize().multiply(DistFromTarget)), dc_normal);
    }

    //------------------------- ForwardComponent -----------------------------
    //
    //  calculates the forward component of the steering force
    //------------------------------------------------------------------------
    double ForwardComponent() {
        return m_pPlayer.Heading().Dot(m_vSteeringForce);
    }

    //--------------------------- SideComponent ------------------------------
    //
    //  //  calculates the side component of the steering force
    //------------------------------------------------------------------------
    double SideComponent() {
        //		System.out.println("Calculating side componenet: "+m_pPlayer.Side()+".dot("+m_vSteeringForce+") * "+m_pPlayer.MaxTurnRate());
        //		  return m_pPlayer->Side().Dot(m_vSteeringForce) * m_pPlayer->MaxTurnRate();

        double sideComponent = m_pPlayer.Side().Dot(m_vSteeringForce) * m_pPlayer.MaxTurnRate();
        //		  System.out.println("Side component :"+sideComponent+" of steering force:"+m_vSteeringForce+" comes from taking player side: "+m_pPlayer.Side()+" dot "+m_vSteeringForce+" * max turn rate");
        return sideComponent;
    }
}

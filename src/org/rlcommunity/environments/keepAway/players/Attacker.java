package org.rlcommunity.environments.keepAway.players;

import java.text.DecimalFormat;
import java.util.Vector;

import org.rlcommunity.environments.keepAway.finiteStateMachine.ChaseBall;
import org.rlcommunity.environments.keepAway.finiteStateMachine.GlobalState;
import org.rlcommunity.environments.keepAway.finiteStateMachine.StateMachine;
import org.rlcommunity.environments.keepAway.generalGameCode.Utilities;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;
import org.rlcommunity.environments.keepAway.Prm;
import org.rlcommunity.environments.keepAway.Region;
import org.rlcommunity.environments.keepAway.SoccerTeam;

public class Attacker extends PlayerBase {

    //an instance of the state machine class
    StateMachine<PlayerInterface> theFSM = null;
    //limits the number of kicks a player may take per second
    //  Regulator*                  m_pKickLimiter;
    public Attacker(SoccerTeam home_team,
                     Vector2D heading,
                     Vector2D velocity,
                     double mass,
                     double max_force,
                     double max_speed,
                     double max_turn_rate,
                     double scale,
                     player_role role, Region myHomeRegion) {
        super(home_team, heading, velocity, mass, max_force, max_speed, max_turn_rate, scale, role, myHomeRegion);

        //set up the state machine
        theFSM = new StateMachine<PlayerInterface>(this);
        theFSM.setGlobalState(new GlobalState());
        theFSM.changeState(ChaseBall.Instance());
    }

    /* (non-Javadoc)
     * @see players.PlayerInterface#GetFSM()
     */
    public StateMachine<PlayerInterface> GetFSM() {
        return theFSM;
    }
    
    public void resetFSM(){
        theFSM.changeState(ChaseBall.Instance());
    }

//------------------------------ Update ----------------------------------
//
//
//------------------------------------------------------------------------
    public void Update() {
        super.Update();
        boolean useNew = false;

        if (useNew) {
            UpdateNew();
        } else {
            updateOld();
        }
    }

    public void UpdateNew() {
        GetFSM().update();
        //calculate the combined steering force
        m_pSteering.Calculate();

//	if (m_pSteering.Force().isZero())
//	{
//		double BrakingRate = 0.8; 
//		m_vVelocity = m_vVelocity.multiply(BrakingRate);                                     
//	}

        Vector2D acceleration = m_pSteering.m_vSteeringForce.divideToCopy(m_dMass);
        m_vVelocity.add(acceleration);
        m_vVelocity.Truncate(Prm.playerMaxSpeed);

        System.out.println("Position: " + m_vPosition + " + " + m_vVelocity + " = " + m_vPosition.addToCopy(m_vVelocity));
        m_vPosition.add(m_vVelocity);

        if (m_vVelocity.LengthSq() > .0000001) {
            m_vHeading = m_vVelocity.normalizeToCopy();
            m_vSide = m_vHeading.Perp();
        }


        System.out.println("New heading: " + m_vHeading + " new side: " + m_vSide + " new position: " + m_vPosition);
    }

    public void updateOld() {

        boolean debugThis = false;

//	if(ID()==1)debugThis=true;
        GetFSM().update();

        DecimalFormat df = new DecimalFormat("#.##");


//run the logic for the current state
//m_pStateMachine->Update();

//calculate the combined steering force
        m_pSteering.Calculate();

        if (debugThis) {
            System.out.print("\tSteer Force: " + m_pSteering.m_vSteeringForce);
        }

//if no steering force is produced decelerate the player by applying a
//braking force
        if (m_pSteering.Force().isZero()) {
            double BrakingRate = 0.8;
            m_vVelocity = m_vVelocity.multiply(BrakingRate);
        }

//the steering force's side component is a force that rotates the 
//player about its axis. We must limit the rotation so that a player
//can only turn by PlayerMaxTurnRate rads per update.

//Try to calcualte this another way:
        double TurningForce = m_pSteering.SideComponent();

//double steeringHeadingDot=m_pSteering.m_vSteeringForce.Dot(m_vHeading);
//double angleRatio=steeringHeadingDot/(m_pSteering.m_vSteeringForce.Length()+m_vHeading.Length());
//double TurningForce=Math.acos(angleRatio);
//System.out.println("I think you should turn:  "+TurningForce+" radians");


        if (debugThis) {
            System.out.print("\tTurning force " + df.format(TurningForce));
        }

        TurningForce = Utilities.Clamp(TurningForce, -Prm.PlayerMaxTurnRate, Prm.PlayerMaxTurnRate);
        if (debugThis) {
            System.out.print("\t clamped to: " + df.format(TurningForce));
        }

//rotate the heading vector
        m_vHeading.RotateAroundOrigin(TurningForce);
        if (debugThis) {
            System.out.print("\tHeading: " + m_vHeading);
        }

//make sure the velocity vector points in the same direction as
//the heading vector
        m_vVelocity = m_vHeading.multiplyToCopy(m_vVelocity.Length());

        if (debugThis) {
            System.out.print("\tm_vVelocity" + m_vVelocity);
        }

//and recreate m_vSide
        m_vSide = m_vHeading.Perp();

        if (debugThis) {
            System.out.print("\tm_vSide" + m_vSide);
        }

//now to calculate the acceleration due to the force exerted by
//the forward component of the steering force in the direction
//of the player's heading
//Vector2D accel = m_vHeading * m_pSteering->ForwardComponent() / m_dMass;

        if (debugThis) {
            System.out.print("\tForwardComponent" + m_pSteering.ForwardComponent());
        }
//Vector2D accel = m_vHeading * m_pSteering->ForwardComponent() / m_dMass;
        Vector2D accel = m_vHeading.multiplyToCopy(m_pSteering.ForwardComponent()).divideToCopy(m_dMass);
//if(m_pSteering.ForwardComponent()<0){
//	System.out.println("Forward component is: "+m_pSteering.ForwardComponent());
//	System.exit(1);
//}
        if (debugThis) {
            System.out.print("\taccel" + accel);
        }

        m_vVelocity.add(accel);
        if (debugThis) {
            System.out.println("\tm_vVelocity" + m_vVelocity);
        }

//make sure player does not exceed maximum velocity
        m_vVelocity.Truncate(m_dMaxSpeed);
        if (debugThis) {
            System.out.println("\tm_vVelocity truncated" + m_vVelocity);
        }

//update the position
        m_vPosition.add(m_vVelocity);
        if (debugThis) {
            System.out.println("\new Pos" + m_vPosition);
        }


//enforce a non-penetration constraint if desired
        if (Prm.bNonPenetrationConstraint) {
            Utilities.EnforceNonPenetrationContraint(this, Team().getAllPlayersOnBothTeams());
        }
    }

//-------------------- HandleMessage -------------------------------------
//
//routes any messages appropriately
//------------------------------------------------------------------------
    boolean HandleMessage(String msg) {
//return m_pStateMachine->HandleMessage(msg);
        return false;
    }

    public Vector<Vector2D> getVertices() {
        return m_vecPlayerVB;
    }
}

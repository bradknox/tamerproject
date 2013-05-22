package org.rlcommunity.environments.keepAway;

import java.util.Vector;

import org.rlcommunity.environments.keepAway.generalGameCode.MovingEntity;
import org.rlcommunity.environments.keepAway.generalGameCode.Utilities;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

public class SoccerBall extends MovingEntity{

	  //keeps a record of the ball's position at the last update
		private  Vector2D                  m_vOldPos;

	  //a local reference to the Walls that make up the pitch boundary
	  Vector<Wall2D> m_PitchBoundary;                                      
	  Region playingArea=null;

	  

	    //tests to see if the ball has collided with a ball and reflects 
	  //the ball's velocity accordingly
//		public void TestCollisionWithWalls( Vector<Wall2D> walls);

	  SoccerBall(Vector2D pos, double BallSize, double mass, Vector<Wall2D> PitchBoundary, Region playingArea){
	  
	      //set up the base class
	      super(pos, BallSize, new Vector2D(0,0), -1.0,                //max speed - unused
	                  new Vector2D(0,1),
	                  mass,
	                  new Vector2D(1.0,1.0),  //scale     - unused
	                  0,                   //turn rate - unused
	                  0);                  //max force - unused
	     this.m_PitchBoundary=PitchBoundary;
	     this.playingArea=playingArea;
	  }

	  //this is used by players and goalkeepers to 'trap' a ball -- to stop
	  //it dead. That player is then assumed to be in possession of the ball
	  //and m_pOwner is adjusted accordingly
	  void      Trap(){m_vVelocity.Zero();}  

	  Vector2D  OldPos(){return m_vOldPos;}
	  

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

	  

	//-------------------------- Kick ----------------------------------------
//	                                                                        
	//  applys a force to the ball in the direction of heading. Truncates
	//  the new velocity to make sure it doesn't exceed the max allowable.
	//------------------------------------------------------------------------
	public void Kick(Vector2D direction, double force)
	{  
	  //ensure direction is normalized
	  direction.normalize();
	  
	  //calculate the acceleration
	  Vector2D acceleration = (direction.multiplyToCopy(force)).divide(m_dMass);

	  //update the velocity
	  m_vVelocity = acceleration;
	}

	//----------------------------- Update -----------------------------------
	//
	//  updates the ball physics, tests for any collisions and adjusts
	//  the ball's velocity accordingly
	//------------------------------------------------------------------------
	void Update()
	{
	  //keep a record of the old position so the goal::scored method
	  //can utilize it for goal testing
	  m_vOldPos = m_vPosition.copy();

	      //Test for collisions
//	  TestCollisionWithWalls(m_PitchBoundary);

	  //Simulate Prm.Friction. Make sure the speed is positive 
	  //first though
	  if (m_vVelocity.LengthSq() > Prm.Friction * Prm.Friction)
	  {
//	    m_vVelocity += Vec2DNormalize(m_vVelocity) * Prm.Friction;
		  Vector2D componentToAdd=m_vVelocity.normalizeToCopy().multiply(Prm.Friction);
		  m_vVelocity.add(componentToAdd);

		  m_vPosition.add(m_vVelocity);



	    //update heading
	    m_vHeading = m_vVelocity.normalizeToCopy();
	  }
	  if(!playingArea.contains(m_vPosition)){
		  m_vPosition=m_vOldPos.copy();
		  m_vVelocity.Zero();
	  }
	}

	//---------------------- TimeToCoverDistance -----------------------------
	//
	//  Given a force and a distance to cover given by two vectors, this
	//  method calculates how long it will take the ball to travel between
	//  the two points
	//------------------------------------------------------------------------
	double TimeToCoverDistance(Vector2D A,
	                                      Vector2D B,
	                                      double force)
	{
	  //this will be the velocity of the ball in the next time step *if*
	  //the player was to make the pass. 
	  double speed = force / m_dMass;

	  //calculate the velocity at B using the equation
	  //
	  //  v^2 = u^2 + 2as
	  //

	  //first calculate s (the distance between the two positions)
	  double DistanceToCover =  A.distanceTo(B);

	  double term = speed*speed + 2.0*DistanceToCover*Prm.Friction;

	  //if  (u^2 + 2as) is negative it means the ball cannot reach point B.
	  if (term <= 0.0) return -1.0;

	  double v = Math.sqrt(term);

	  //it IS possible for the ball to reach B and we know its speed when it
	  //gets there, so now it's easy to calculate the time using the equation
	  //
	  //    t = v-u
	  //        ---
	  //         a
	  //
	  return (v-speed)/Prm.Friction;
	}

	//--------------------- FuturePosition -----------------------------------
	//
	//  given a time this method returns the ball position at that time in the
	//  future
	//------------------------------------------------------------------------
	public Vector2D FuturePosition(double time)
	{
	  //using the equation s = ut + 1/2at^2, where s = distance, a = friction
	  //u=start velocity

	  //calculate the ut term, which is a vector
	  Vector2D ut = m_vVelocity.multiplyToCopy(time);

	  //calculate the 1/2at^2 term, which is scalar
	  double half_a_t_squared = 0.5 * Prm.Friction * time * time;

	  //turn the scalar quantity into a vector by multiplying the value with
	  //the normalized velocity vector (because that gives the direction)
	  Vector2D ScalarToVector = m_vVelocity.normalizeToCopy().multiply(half_a_t_squared);

	  //the predicted position is the balls position plus these two terms
	  return Pos().addToCopy(ut).add(ScalarToVector);
	}


	//----------------------------- Render -----------------------------------
	//
	//  Renders the ball
	//------------------------------------------------------------------------
	void Render()
	{
//	  gdi->BlackBrush();

	//  gdi->Circle(m_vPosition, m_dBoundingRadius);

	}


	//----------------------- TestCollisionWithWalls -------------------------
	//
	void TestCollisionWithWalls(Vector<Wall2D> walls)
	{  
	  //test ball against each wall, find out which is closest
	  int idxClosest = -1;

	  Vector2D VelNormal = m_vVelocity.normalizeToCopy();

	  Vector2D IntersectionPoint, CollisionPoint;

	  double DistToIntersection = Double.MAX_VALUE;

	  //iterate through each wall and calculate if the ball intersects.
	  //If it does then store the index into the closest intersecting wall
	  
	  //This code doesn't quite work yet
	  for ( int w=0; w<walls.size(); ++w)
	  {
	    //assuming a collision if the ball continued on its current heading 
	    //calculate the point on the ball that would hit the wall. This is 
	    //simply the wall's normal(inversed) multiplied by the ball's radius
	    //and added to the balls center (its position)
//	    Vector2D ThisCollisionPoint = Pos() - (walls[w].Normal() * BRadius());
	    Vector2D ThisCollisionPoint = Pos().subtractToCopy(walls.get(w).Normal().multiply(BRadius()));
	    
	    System.out.println("Collides at: "+ThisCollisionPoint);
	    //calculate exactly where the collision point will hit the plane    
	    if (Utilities.WhereIsPoint(ThisCollisionPoint,
	                     walls.get(w).From(),
	                     walls.get(w).Normal()) == Utilities.plane_backside)
	    {
	    	System.out.println("backside");
	      double DistToWall = Utilities.DistanceToRayPlaneIntersection(ThisCollisionPoint,
	                                                         walls.get(w).Normal(),
	                                                         walls.get(w).From(),
	                                                         walls.get(w).Normal());

	      IntersectionPoint = ThisCollisionPoint.addToCopy(walls.get(w).Normal().multiply(DistToWall));
	      
	    }

	    else
	    {
	    	System.out.println("second case");

	      double DistToWall = Utilities.DistanceToRayPlaneIntersection(ThisCollisionPoint,
	                                                         VelNormal,
	                                                         walls.get(w).From(),
	                                                         walls.get(w).Normal());

	      IntersectionPoint = ThisCollisionPoint.addToCopy(VelNormal.multiplyToCopy(DistToWall));
	    }
	    
	    //check to make sure the intersection point is actually on the line
	    //segment
	    boolean OnLineSegment = false;

	    if (Utilities.LineIntersection2D(walls.get(w).From(), 
	                           walls.get(w).To(),
	                           ThisCollisionPoint.subtractToCopy(walls.get(w).Normal().multiply(20.0)),
	                           ThisCollisionPoint.addToCopy(walls.get(w).Normal().multiply(20.0))))
	    {
	    	System.out.println("On line segment true");

	      OnLineSegment = true;                                               
	    }

	  
	                                                                          //Note, there is no test for collision with the end of a line segment
	    
	    //now check to see if the collision point is within range of the
	    //velocity vector. [work in distance squared to avoid sqrt] and if it
	    //is the closest hit found so far. 
	    //If it is that means the ball will collide with the wall sometime
	    //between this time step and the next one.
	    double distSq = ThisCollisionPoint.distanceToSquared(IntersectionPoint);

	    if ((distSq <= m_vVelocity.LengthSq()) && (distSq < DistToIntersection) && OnLineSegment)            
	    {       
	    	System.out.println("close enough to hit next step");

	      DistToIntersection = distSq;
	      idxClosest = w;
	      CollisionPoint = IntersectionPoint;
	    }     else
	    	System.out.println("not close enough to hit next step");

	  }//next wall

	    
	  //to prevent having to calculate the exact time of collision we
	  //can just check if the velocity is opposite to the wall normal
	  //before reflecting it. This prevents the case where there is overshoot
	  //and the ball gets reflected back over the line before it has completely
	  //reentered the playing area.
	  if(idxClosest>=0){
		  System.out.println("We're going to collide...");
		  System.out.println("Result of the equation is: "+VelNormal.Dot(walls.get(idxClosest).Normal()));
	  }
	  if ( (idxClosest >= 0 ))/* && VelNormal.Dot(walls.get(idxClosest).Normal()) < 0)*/
	  {
		  System.out.println("Relfecting the ball");
	    m_vVelocity.Reflect(walls.get(idxClosest).Normal());   
	  }else{
		  System.out.println("Decided not to reflect");
	  }
	}

	//----------------------- PlaceAtLocation -------------------------------------
	//
	//  positions the ball at the desired location and sets the ball's velocity to
	//  zero
	//-----------------------------------------------------------------------------
	void PlaceAtPosition(Vector2D NewPos)
	{
	  m_vPosition = NewPos.copy();

	  m_vOldPos = m_vPosition;
	  
	  m_vVelocity.Zero();
	}
	
	public String toString(){
		return "Ball Position: "+m_vPosition.toString()+" Ball Velocity: "+m_vVelocity.toString();
	}
}

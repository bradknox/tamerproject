package org.rlcommunity.environments.keepAway.generalGameCode;

import java.util.Collection;
import java.util.Vector;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;

import org.rlcommunity.environments.keepAway.players.PlayerInterface;

public class Utilities {
	public static double RandomClamped() {
		double r = Math.random() - Math.random();
		return r;
	}

	public static final int plane_backside = 1;
	public static final int plane_front = 2;
	public static final int on_plane = 3;

	public static final int WhereIsPoint(Vector2D point, Vector2D PointOnPlane,
			Vector2D PlaneNormal) {
		Vector2D dir = PointOnPlane.subtractToCopy(point);
		double d = dir.Dot(PlaneNormal);

		if (d < -0.000001)
			return plane_front;
		else if (d > 0.000001)
			return plane_backside;

		return on_plane;
	}

	public static final double DistanceToRayPlaneIntersection(
			Vector2D RayOrigin, Vector2D RayHeading, Vector2D PlanePoint,
			Vector2D PlaneNormal) {
		double d = -PlaneNormal.Dot(PlanePoint);
		double numer = PlaneNormal.Dot(RayOrigin) + d;
		double denom = PlaneNormal.Dot(RayHeading);

		// normal is parallel to vector
		if ((denom < 0.000001) && (denom > -0.000001))
			return (-1.0);

		return -(numer / denom);
	}

	//--------------------LineIntersection2D-------------------------
	//
	//	Given 2 lines in 2D space AB, CD this returns true if an
	//	intersection occurs.
	//
	//-----------------------------------------------------------------
	public static final boolean LineIntersection2D(Vector2D A, Vector2D B, Vector2D C,
			Vector2D D) {
		double rTop = (A.y - C.y) * (D.x - C.x) - (A.x - C.x) * (D.y - C.y);
		double sTop = (A.y - C.y) * (B.x - A.x) - (A.x - C.x) * (B.y - A.y);

		double Bot = (B.x - A.x) * (D.y - C.y) - (B.y - A.y) * (D.x - C.x);

		if (Bot == 0) //parallel
			return false;

		double invBot = 1.0 / Bot;
		double r = rTop * invBot;
		double s = sTop * invBot;

		if ((r > 0) && (r < 1) && (s > 0) && (s < 1))
			//lines intersect
			return true;

		//lines do not intersect
		return false;
	}

	public static double Clamp(double value, double min, double max){
		if(value<min)return min;
		if(value>max)return max;
		return value;
	}
	
	//--------------------------- WorldTransform -----------------------------
	//
	//  given a std::vector of 2D vectors, a position, orientation and scale,
	//  this function transforms the 2D vectors into the object's world space
	//------------------------------------------------------------------------
	public static Vector<Vector2D> WorldTransform(Vector<Vector2D> points,
	                                             Vector2D   pos,
	                                             Vector2D   forward,
	                                             Vector2D   side,
	                                             Vector2D   scale)
	{
		//copy the original vertices into the buffer about to be transformed
	    Vector<Vector2D> TranVector2Ds = new Vector<Vector2D>();
	    
	    for (Vector2D thisVector : points) {
	    	TranVector2Ds.add(thisVector.copy());
			
		}

	    //create a transformation matrix, it will be the identity be default
		C2DMatrix matTransform=new C2DMatrix();
		matTransform.Identity();

		//scale
	    if ( (scale.x != 1.0) || (scale.y != 1.0) )
	        matTransform.Scale(scale.x, scale.y);

		//rotate
		matTransform.Rotate(forward, side);

		//and translate
		matTransform.Translate(pos.x, pos.y);

	    //now transform the object's vertices
	    for (Vector2D thisVector : TranVector2Ds) {
	    	matTransform.TransformVector2Ds(thisVector);			
		}

	    return TranVector2Ds;
	}
	
	//--------------------- PointToWorldSpace --------------------------------
	//
	//  Transforms a point from the agent's local space into world space
	//------------------------------------------------------------------------
	public static Vector2D PointToWorldSpace( Vector2D point,Vector2D AgentHeading,Vector2D AgentSide,Vector2D AgentPosition)
	{
		//make a copy of the point
	    Vector2D TransPoint = point.copy();

		C2DMatrix matTransform=new C2DMatrix();
		matTransform.Identity();
		
		//rotate
		matTransform.Rotate(AgentHeading, AgentSide);

		//and translate
		matTransform.Translate(AgentPosition.x, AgentPosition.y);

	    //now transform the vertices
	    matTransform.TransformVector2Ds(TransPoint);

	    return TransPoint;
	}

	public static Vector2D randomClampedVector() {
		return new Vector2D(RandomClamped(),RandomClamped());
	}
	
	//------------------- EnforceNonPenetrationContraint ---------------------
	//
	//  Given a pointer to an entity and a std container of pointers to nearby
	//  entities, this function checks to see if there is an overlap between
	//  entities. If there is, then the entities are moved away from each
	//  other
	//------------------------------------------------------------------------
	
	public static void EnforceNonPenetrationContraint(PlayerInterface P, Collection<PlayerInterface> others)
	{

	    //iterate through all entities checking for any overlap of bounding
	    //radii
		for (PlayerInterface thisPlayer : others) {
	        //make sure we don't check against this entity
			if(thisPlayer!=P){
				
			
	        //calculate the distance between the positions of the entities
	        Vector2D ToEntity = thisPlayer.Pos().subtractToCopy(P.Pos());

	        double DistFromEachOther = ToEntity.Length();

	        //if this distance is smaller than the sum of their radii then this
	        //entity must be moved away in the direction parallel to the
	        //ToEntity vector
	        double AmountOfOverLap = thisPlayer.BRadius() + P.BRadius() - DistFromEachOther;

	        if (AmountOfOverLap >= 0){
		        System.out.println("Amount of overlap = "+thisPlayer.BRadius() +" + " +P.BRadius() +" - "+ DistFromEachOther+" = "+AmountOfOverLap);
		        System.out.println("Setting to position: "+thisPlayer.Pos().addToCopy(ToEntity.divideToCopy(DistFromEachOther *AmountOfOverLap)));
	            //move the entity a distance away equivalent to the amount of overlap.
		        System.out.println("ToEntity is:"+ToEntity);
		        System.out.println("going to divide that by "+(DistFromEachOther*AmountOfOverLap));
	        	thisPlayer.setPos(thisPlayer.Pos().addToCopy(ToEntity.divideToCopy(DistFromEachOther *AmountOfOverLap)));
	        }
			}
		}
	  

	}




}

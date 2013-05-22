/*
 * Vector2D.java
 * 
 * Created on Oct 16, 2007, 9:54:07 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rlcommunity.environments.keepAway.generalGameCode;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

/**
 *
 * @author btanner
 */
public class Vector2D {
public double x=0.0d;
public double y=0.0d;

public Vector2D(double x, double y){
    this.x=x;
    this.y=y;
}

public Vector2D(){
    this(0.0d,0.0d);
}

public Vector2D(StringTokenizer strTok){
	this(Double.parseDouble(strTok.nextToken()),Double.parseDouble(strTok.nextToken()));
}
public Vector2D(String ballPositionString) {
	this(new StringTokenizer(ballPositionString,"_"));
}

public final Vector2D Perp(){
    return( new Vector2D( -1.0 *y, x ) );
}

public final double  LengthSq() {
	return (x*x + y*y);
	}

public final double Length() {
	return Math.sqrt(x*x + y*y);
	}

public final Vector2D subtract(Vector2D pos) {
	x-=pos.x;
	y-=pos.y;
	return this;
}
public final Vector2D subtractToCopy(Vector2D pos) {
	return new Vector2D(x-pos.x,y-pos.y);
}


public final Vector2D normalize() {
	double length=Length();
	if(length>.00001d){
		x /= length;
		y /= length;
	}
	return this;
	}
public final Vector2D normalizeToCopy() {
	double length=Length();
	if(length>.00001d){
			return new Vector2D(x/length,y/length);
	}
	return new Vector2D(x,y);
	}

public double Dot(Vector2D target) {
	return (x*target.x+y*target.y);
}

public double Sign(Vector2D v2) {
	if (y*v2.x > x*v2.y)
  {
    return -1.0d;
  }
  else
  {
    return 1.0d;
  }
}

public final void Zero() {
	x=0.0d;
	y=0.0d;
}

public void RotateAroundOrigin(double displacement) {
	C2DMatrix mat= new C2DMatrix();
	mat.Identity();
	
	mat.Rotate(displacement);
	mat.TransformVector2Ds(this);
}

public Vector2D add(Vector2D pos) {
	x+=pos.x;
	y+=pos.y;
	return this;
}
public Vector2D addToCopy(Vector2D pos) {
	return new Vector2D(x+pos.x,y+pos.y);
}

public Vector2D multiplyToCopy(double force) {
	return new Vector2D(x*force,y*force);
}
public Vector2D multiply(double force) {
	x*=force;
	y*=force;
	return this;
}

public Vector2D divide(double mass) {
	 x/=mass;
	 y/=mass;
	 return this;
}
public Vector2D divideToCopy(double mass) {
	return new Vector2D(x/mass,y/mass);
}

public double distanceTo(Vector2D b) {
	double ySeparation = b.y - y;
    double xSeparation = b.x - x;

    return Math.sqrt(ySeparation*ySeparation + xSeparation*xSeparation);
}

public double distanceToSquared(Vector2D v2) {
	 double ySeparation = v2.y - y;
	    double xSeparation = v2.x -x;

	    return ySeparation*ySeparation + xSeparation*xSeparation;}



//--------------------------- Reflect ------------------------------------
//
//  given a normalized vector this method reflects the vector it
//  is operating upon. (like the path of a ball bouncing off a wall)
//------------------------------------------------------------------------
public Vector2D ReflectToCopy(Vector2D norm)
{
return	this.addToCopy(norm.ReverseToCopy().multiply(this.Dot(norm)*2.0));
//    *this += 2.0 * this->Dot(norm) * norm.Reverse();
}
public Vector2D Reflect(Vector2D norm)
{
add(norm.ReverseToCopy().multiply(this.Dot(norm)*2.0));
return this;
//    *this += 2.0 * this->Dot(norm) * norm.Reverse();
}

//----------------------- Reverse ----------------------------------------
//
//  returns the vector that is the reverse of this vector
//------------------------------------------------------------------------
private Vector2D ReverseToCopy() {
	return new Vector2D(-x,-y);
}

public Vector2D copy() {
	return new Vector2D(x,y);
}

public String toString(){
       DecimalFormat df = new DecimalFormat("#.##");
	return "("+df.format(x)+","+df.format(y)+")";
}

public String stringSerialize() {
	return (x+"_"+y);
}

public Vector2D Truncate(double max){
	 if (Length() > max)
	  {
	    normalize();
	    multiply(max);
	  }
	 return this;
}

public boolean isZero() {
	return (x*x + y*y) < .000001;}
}
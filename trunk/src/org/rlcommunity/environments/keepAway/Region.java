package org.rlcommunity.environments.keepAway;

import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

public class Region{
	//not sure if this is right
double minX;
double minY;
double width;
double height;

public Region(double a,double b,double c,double d){
	minX=a;
	minY=b;
	width=c;
	height=d;
}

public double Top(){
	return minY+height;
}

public double Bottom(){
	return minY;
}

public double Left(){
	return minX;
}

public double Right(){
	return minX+width;
}

public boolean contains(Vector2D position) {
	if(position.x>Right()||position.x<Left()||position.y>Top()||position.y<Bottom())return false;
	return true;
}

public Vector2D Center() {
	return new Vector2D(minX+width/2,minY+height/2);
}
}


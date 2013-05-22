package org.rlcommunity.environments.keepAway.kaMessages;

import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

import java.util.StringTokenizer;
import java.util.Vector;

public class PlayerFacade implements PlayerFacadeInterface {
Vector2D Position=null;
Vector2D Heading=null;
Vector2D Side=null;
Vector2D Scale=null;
Vector2D SteeringForce=null;

Vector<Vector2D> vertices=null;

	public PlayerFacade(StringTokenizer strTok) {
		vertices=new Vector<Vector2D>();
		
		Position=new Vector2D(strTok);
		Heading=new Vector2D(strTok);
		Side=new Vector2D(strTok);
		Scale=new Vector2D(strTok);
		SteeringForce=new Vector2D(strTok);

		int numVertices=Integer.parseInt(strTok.nextToken());
		for(int i=0;i<numVertices;i++){
			vertices.add(new Vector2D(strTok));
		}
	}
	public Vector2D Heading() {
		return Heading;
	}
	public Vector2D Pos() {
		// TODO Auto-generated method stub
		return Position;
	}
	public String stringSerialize() {
		StringBuffer B=new StringBuffer();
		B.append(Position.stringSerialize());
		B.append("_");
		B.append(Heading.stringSerialize());
		B.append("_");
		B.append(Side.stringSerialize());
		B.append("_");
		B.append(Scale.stringSerialize());
		B.append("_");
		B.append(SteeringForce.stringSerialize());
		B.append("_");
		B.append(vertices.size());
		B.append("_");
		for (Vector2D thisVector : vertices) {
			B.append(thisVector.stringSerialize());
			B.append("_");
		}
		return B.toString();
	}
	public Vector<Vector2D> getVertices() {
		return vertices;
	}
	public Vector2D Side() {
		return Side;
	}
	public Vector2D Scale() {
		return Scale;
	}
	
	public String toString(){
		String result="";
		result+="\tPos: "+Pos()+"\n";
		result+="\tHeading: "+Heading()+"\n";
		result+="\tSide: "+Side()+"\n";
		result+="\tScale: "+Scale()+"\n";
		return result;
	}
	public Vector2D SteeringForce() {
	return SteeringForce;
	}

}

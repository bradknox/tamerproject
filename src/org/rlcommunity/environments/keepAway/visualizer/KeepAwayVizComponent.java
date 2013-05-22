package org.rlcommunity.environments.keepAway.visualizer;

import org.rlcommunity.environments.keepAway.generalGameCode.Utilities;
import org.rlcommunity.environments.keepAway.generalGameCode.Vector2D;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import org.rlcommunity.environments.keepAway.SoccerPitch;
import org.rlcommunity.environments.keepAway.Wall2D;

import org.rlcommunity.environments.keepAway.kaMessages.KAStateRequest;
import org.rlcommunity.environments.keepAway.kaMessages.KAStateResponse;
import org.rlcommunity.environments.keepAway.kaMessages.PlayerFacadeInterface;
import org.rlcommunity.environments.keepAway.kaMessages.SoccerTeamFacadeInterface;

import rlVizLib.visualization.VizComponent;
import org.rlcommunity.rlglue.codec.types.Observation;

public class KeepAwayVizComponent implements VizComponent{
	KeepAwayVisualizer myVisualizer=null;
	
	int lastUpdateStep=-1;
	
	public KeepAwayVizComponent(KeepAwayVisualizer keepAwayVisualizer) {
		myVisualizer=keepAwayVisualizer;
		
			
		
			
	}

	public void render(Graphics2D g) {
		final boolean debugThis=false;
		
		double ballDiam=1.0d;
		double halfBD=ballDiam/2.0d;
		
		double widthDivider=.001d;
		double heightDivider=.001d;
		
		double Width=1.0d/widthDivider;
		double Height=1.0d/heightDivider;
		
		double fieldWidth=100.0d;
		double fieldHeight=50.0d;
		
		double wM=Width/fieldWidth;
		double hM=Width/fieldWidth;
		//Make it 100 x 100
	    AffineTransform saveAT = g.getTransform();
   	    g.scale(widthDivider, heightDivider);

		//Draw the ball
   	    //Should only do this when new info available
		KAStateResponse R=KAStateRequest.Execute();
		
	if(R==null)return;



		Vector<Wall2D> theWalls=R.getTheWalls();
		for (Wall2D thisWall : theWalls) {
			g.drawLine((int)(thisWall.From().x*wM),(int)(thisWall.From().y*hM),(int)(thisWall.To().x*wM),(int)(thisWall.To().y*hM));
//			System.out.println("Drawing a line from: "+(int)thisWall.From().x+","+(int)thisWall.From().y+" to "+(int)thisWall.To().x+","+(int)thisWall.To().y);
		}
		
		SoccerTeamFacadeInterface theKeepers=R.getKeepers();
		g.setColor(Color.blue);
		drawTeam(g,theKeepers,wM,hM);
		
		SoccerTeamFacadeInterface theTakers=R.getTakers();
		g.setColor(Color.red);
		drawTeam(g,theTakers,wM,hM);
		
		Vector2D BallPosition=R.getBallPosition();
		

		g.setColor(Color.white);
		g.fillOval((int)((BallPosition.x-halfBD)*wM),(int)((BallPosition.y-halfBD)*hM), (int)(ballDiam*wM), (int)(ballDiam*hM));

	    g.setTransform(saveAT);

	}
	
	public void drawTeam(Graphics2D g,SoccerTeamFacadeInterface theTeam,double wM,double hM){
		boolean debugThis=false;
		int playerCount=theTeam.getPlayerCount();
		
		for(int i=0;i<playerCount;i++){
				PlayerFacadeInterface thisPlayer=theTeam.getPlayer(i);
				Vector<Vector2D> vertices=thisPlayer.getVertices();
				
				Vector<Vector2D> transformedVertices=Utilities.WorldTransform(vertices, thisPlayer.Pos(), thisPlayer.Heading(), thisPlayer.Side(), thisPlayer.Scale());
				if(debugThis)	System.out.println("Player Details: \n"+thisPlayer);
				int numPoints=transformedVertices.size();
				int xPoints[]=new int[numPoints];
				int yPoints[]=new int[numPoints];
				for(int j=0;j<numPoints;j++){
					xPoints[j]=(int)(wM*transformedVertices.get(j).x);
					yPoints[j]=(int)(hM*transformedVertices.get(j).y);
					}
				g.fillPolygon(xPoints, yPoints, numPoints);
				
				Vector2D steering=thisPlayer.SteeringForce();
				Vector2D position=thisPlayer.Pos();
				Vector2D endPosOfSteering=position.addToCopy(steering.multiplyToCopy(2.0));
				g.drawLine((int)(wM*position.x), (int)(hM*position.y), (int)(wM*endPosOfSteering.x),(int) (hM*endPosOfSteering.y));
				g.fillOval((int)(wM*position.x), (int)(hM*position.y),5,5);
			}
	}

	public boolean update() {
		int currentTimeStep=myVisualizer.theGlueState.getTotalSteps();
		if(currentTimeStep>lastUpdateStep){
			lastUpdateStep=currentTimeStep;
			return true;
		}
		return false;
	}

}

package org.rlcommunity.environments.continuousgridworld.messages;


import java.awt.geom.Rectangle2D;
import java.util.StringTokenizer;
import java.util.Vector;

import rlVizLib.messaging.AbstractResponse;
import rlVizLib.messaging.GenericMessage;
import rlVizLib.messaging.MessageUser;
import rlVizLib.messaging.MessageValueType;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvMessageType;


public class CGWMapResponse extends AbstractResponse{
	Vector<Rectangle2D> resetRegions;
	Vector<Rectangle2D> rewardRegions;
	Vector<Double> theRewards;
	Vector<Rectangle2D> barrierRegions;
	Vector<Double> thePenalties;
	Rectangle2D theWorldRect;
	
	public CGWMapResponse(Rectangle2D theWorldRect, Vector<Rectangle2D> resetRegions,	Vector<Rectangle2D> rewardRegions,	Vector<Double> theRewards,	Vector<Rectangle2D> barrierRegions,
	Vector<Double> thePenalties) {
		
		this.resetRegions=resetRegions;
		this.rewardRegions=rewardRegions;
		this.theRewards=theRewards;
		this.barrierRegions=barrierRegions;
		this.thePenalties=thePenalties;
		this.theWorldRect=theWorldRect;
		
	}

	public CGWMapResponse(String responseMessage) throws NotAnRLVizMessageException {

		GenericMessage theGenericResponse = new GenericMessage(responseMessage);

		String thePayLoadString=theGenericResponse.getPayLoad();

		StringTokenizer stateTokenizer = new StringTokenizer(thePayLoadString, ":");
		
		String worldRectString=stateTokenizer.nextToken();
		String resetString=stateTokenizer.nextToken();
		String rewardRegionString=stateTokenizer.nextToken();
		String rewardString=stateTokenizer.nextToken();
		String barrierRegionString=stateTokenizer.nextToken();
		String thePenaltyString=stateTokenizer.nextToken();
		
		//First do the reset regions
		theWorldRect=makeRectangleFromString(worldRectString);
		resetRegions=makeRectangleVectorFromString(resetString);
		rewardRegions=makeRectangleVectorFromString(rewardRegionString);
		barrierRegions=makeRectangleVectorFromString(barrierRegionString);

		theRewards=makeDoubleVectorFromString(rewardString);
		thePenalties=makeDoubleVectorFromString(thePenaltyString);

	}

	private Rectangle2D makeRectangleFromString(String theString) {
		StringTokenizer rectTokenizer=new StringTokenizer(theString,"_");
		double x=Double.parseDouble(rectTokenizer.nextToken());
		double y=Double.parseDouble(rectTokenizer.nextToken());
		double width=Double.parseDouble(rectTokenizer.nextToken());
		double height=Double.parseDouble(rectTokenizer.nextToken());
		Rectangle2D thisRect=new Rectangle2D.Double(x,y,width,height);
		return thisRect;
	}

	private Vector<Double> makeDoubleVectorFromString(String theString) {
		Vector<Double> theDoubles=new Vector<Double>();
		//Bail if the string is null, which means there was no vector
		if(theString.equalsIgnoreCase("null"))return theDoubles;

		StringTokenizer doubleTokenizer=new StringTokenizer(theString,"_");
		
		while(doubleTokenizer.hasMoreTokens()){
			Double theValue=Double.parseDouble(doubleTokenizer.nextToken());
			theDoubles.add(theValue);
		}
		return theDoubles;
	}

	private Vector<Rectangle2D> makeRectangleVectorFromString(String theString) {
		Vector<Rectangle2D> theRects=new Vector<Rectangle2D>();

		//Bail if the string is null, which means there was no vector
		if(theString.equalsIgnoreCase("null"))return theRects;

		StringTokenizer rectTokenizer=new StringTokenizer(theString,"_");
		
		while(rectTokenizer.hasMoreTokens()){
			double x=Double.parseDouble(rectTokenizer.nextToken());
			double y=Double.parseDouble(rectTokenizer.nextToken());
			double width=Double.parseDouble(rectTokenizer.nextToken());
			double height=Double.parseDouble(rectTokenizer.nextToken());
			Rectangle2D thisRect=new Rectangle2D.Double(x,y,width,height);
			theRects.add(thisRect);
		}
		return theRects;
		
	}

	@Override
	public String toString() {
		String theResponse="CGWMapResponse: not implemented ";
		return theResponse;
	}


	@Override
	public String makeStringResponse() {
		StringBuffer theResponseBuffer= new StringBuffer();
		theResponseBuffer.append("TO=");
		theResponseBuffer.append(MessageUser.kBenchmark.id());
		theResponseBuffer.append(" FROM=");
		theResponseBuffer.append(MessageUser.kEnv.id());
		theResponseBuffer.append(" CMD=");
		theResponseBuffer.append(EnvMessageType.kEnvResponse.id());
		theResponseBuffer.append(" VALTYPE=");
		theResponseBuffer.append(MessageValueType.kStringList.id());
		theResponseBuffer.append(" VALS=");

		appendStringOfRectangle(theResponseBuffer, theWorldRect);
		theResponseBuffer.append(":");
		appendStringOfRectangleVector(theResponseBuffer,resetRegions);
		theResponseBuffer.append(":");
		appendStringOfRectangleVector(theResponseBuffer,rewardRegions);
		theResponseBuffer.append(":");
		appendStringOfDoubleVector(theResponseBuffer,theRewards);
		theResponseBuffer.append(":");
		appendStringOfRectangleVector(theResponseBuffer,barrierRegions);
		theResponseBuffer.append(":");
		appendStringOfDoubleVector(theResponseBuffer,thePenalties);
		theResponseBuffer.append(":");
	

		return theResponseBuffer.toString();
	}

	private void appendStringOfDoubleVector(StringBuffer theResponseBuffer,	Vector<Double> theDoubleVector) {
		if(theDoubleVector.size()==0)theResponseBuffer.append("null");
		for (Double thisDouble : theDoubleVector) {
			theResponseBuffer.append(thisDouble).append("_");
		}
	}


	private void appendStringOfRectangleVector(StringBuffer theResponseBuffer,Vector<Rectangle2D> theRectVector) {
		if(theRectVector.size()==0)theResponseBuffer.append("null");
		for (Rectangle2D thisRect : theRectVector) {
			appendStringOfRectangle(theResponseBuffer,thisRect);
		}
	}
	private void appendStringOfRectangle(StringBuffer theResponseBuffer,Rectangle2D thisRect) {
		theResponseBuffer.append(thisRect.getX());
		theResponseBuffer.append("_");
		theResponseBuffer.append(thisRect.getY());
		theResponseBuffer.append("_");
		theResponseBuffer.append(thisRect.getWidth());
		theResponseBuffer.append("_");
		theResponseBuffer.append(thisRect.getHeight());
		theResponseBuffer.append("_");
}

	public Vector<Rectangle2D> getResetRegions() {
		return resetRegions;
	}

	public Vector<Rectangle2D> getRewardRegions() {
		return rewardRegions;
	}

	public Vector<Double> getTheRewards() {
		return theRewards;
	}

	public Vector<Rectangle2D> getBarrierRegions() {
		return barrierRegions;
	}

	public Vector<Double> getThePenalties() {
		return thePenalties;
	}

	public Rectangle2D getTheWorldRect() {
		return theWorldRect;
	}


};